package info.eigenein.openwifi.services;

import android.app.IntentService;
import android.content.*;
import android.net.wifi.*;
import android.support.v4.content.*;
import android.util.Log;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.enums.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.sync.ScanResultDownSyncer;
import info.eigenein.openwifi.sync.ScanResultUpSyncer;
import info.eigenein.openwifi.sync.Syncer;
import info.eigenein.openwifi.sync.TaggedRequest;
import org.apache.http.*;
import org.apache.http.client.HttpClient;

import java.io.IOException;

/**
 * Synchronizes the local database with the server database.
 */
public class SyncIntentService extends IntentService {
    public static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();

    /**
     * Used to notify receivers with the service state.
     */
    public static final String STATUS_EXTRA_KEY = "status";
    /**
     * Used to check whether the device is still connected to the specified wireless network.
     */
    public static final String SSID_EXTRA_KEY = "ssid";
    /**
     * Whether the service is run without user interaction.
     */
    public static final String SILENT_EXTRA_KEY = "silent";
    /**
     * Authentication token.
     */
    public static final String AUTH_TOKEN_EXTRA_KEY = "auth_token";

    /**
     * Minimal sync period.
     */
    public static final long SYNC_PERIOD_MILLIS = 60L * 60L * 1000L;

    /**
     * Starts the service.
     */
    public static void start(final Context context, final boolean silent) {
        start(context, new Intent(context, SyncIntentService.class), silent);
    }

    /**
     * Starts the service with SSID check.
     */
    public static void start(final Context context, final String ssid, final boolean silent) {
        final Intent intent = new Intent(context, SyncIntentService.class);
        intent.putExtra(SSID_EXTRA_KEY, ssid);
        start(context, intent, silent);
    }

    private static void start(final Context context, final Intent intent, final boolean silent) {
        // Put extras.
        intent.putExtra(SILENT_EXTRA_KEY, silent);
        // Start the service.
        context.startService(intent);
    }

    public SyncIntentService() {
        super(SERVICE_NAME);
    }

    protected void onHandleIntent(final Intent intent) {
        Log.i(SERVICE_NAME + ".onHandleIntent", "Service is running.");
        EasyTracker.getInstance().setContext(this);

        // Check if we've already authenticated.
        if (intent.hasExtra(AUTH_TOKEN_EXTRA_KEY)) {
            // Check current network SSID if specified.
            final String ssid = intent.getStringExtra(SSID_EXTRA_KEY);
            if (ssid != null && !checkSsid(ssid)) {
                // We're not connected or connected to the network other than requested.
                return;
            }
            syncAll(intent.getStringExtra(AUTH_TOKEN_EXTRA_KEY));
        } else {
            // We're not authenticated.
            final boolean silent = intent.getBooleanExtra(SILENT_EXTRA_KEY, true);
            // Notify the receivers.
            setStatus(SyncIntentServiceStatus.AUTHENTICATING);
            // Authenticate.
            Log.i(SERVICE_NAME + ".onHandleIntent", "Authenticating ...");
            Authenticator.authenticate(this, true, silent, false, !silent, !silent, new Authenticator.AuthenticatedHandler() {
                @Override
                public void onAuthenticated(
                        final AuthenticationStatus status,
                        final String authToken,
                        final String accountName) {
                    if (status == AuthenticationStatus.AUTHENTICATED) {
                        Log.d(SERVICE_NAME + ".onHandleIntent", "Authenticated.");
                        // Add the authentication token to the intent.
                        intent.putExtra(AUTH_TOKEN_EXTRA_KEY, authToken);
                    } else {
                        Log.w(SERVICE_NAME + ".onHandleIntent", "No authentication token.");
                        // Add the empty authentication token.
                        intent.putExtra(AUTH_TOKEN_EXTRA_KEY, (String)null);
                    }
                    // Notify the user.
                    if (!silent) {
                        Toast.makeText(SyncIntentService.this, R.string.toast_sync_now_started, Toast.LENGTH_LONG).show();
                    }
                    // Start the service again.
                    startService(intent);
                }
            });
        }

        Log.i(SERVICE_NAME + ".onHandleIntent", "Everything is finished.");
    }


    /**
     * Runs the syncing service with the specified authentication token.
     */
    private void syncAll(final String authToken) {
        final Settings settings = Settings.with(this);

        // Notify the receivers that we're syncing.
        setStatus(SyncIntentServiceStatus.SYNCING);
        NotificationHelper.notifySyncingStarted(this);
        // These will be used as the additional headers.
        final String clientId = settings.clientId();
        assert(clientId != null);
        // Prepare the HTTP client.
        final HttpClient client = new SyncHttpClient(this);
        // Start syncing.
        try {
            // Download the scan results.
            final boolean downSyncSucceeded = sync(
                    client, new ScanResultDownSyncer(settings), clientId, null);
            // Upload our scan results.
            final boolean upSyncSucceeded;
            if (authToken != null) {
                upSyncSucceeded = sync(client, new ScanResultUpSyncer(), clientId, authToken);
            } else {
                // We're not authenticated. Do not perform the up syncing.
                upSyncSucceeded = true;
            }
            // Update last sync time.
            if (downSyncSucceeded && upSyncSucceeded) {
                settings.edit().lastSyncTime(System.currentTimeMillis()).commit();
            }
        } finally {
            // Ensure immediate deallocation of all system resources.
            client.getConnectionManager().shutdown();
            // Notify the receiver that we've finished.
            NotificationHelper.notifySyncingFinished(this);
            setStatus(SyncIntentServiceStatus.NOT_SYNCING);
        }
    }

    /**
     * Checks that the device is connected to the wireless network with the specified SSID.
     */
    private boolean checkSsid(final String expectedSsid) {
        final WifiInfo wifiInfo = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getSSID() == null || !wifiInfo.getSSID().equals(expectedSsid)) {
            Log.w(SERVICE_NAME + ".checkSsid", String.format(
                    "SSID has been changed or Wi-Fi is not available. Expected: %s, actual: %s.",
                    expectedSsid,
                    wifiInfo != null ? wifiInfo.getSSID() : "(null)"));
            EasyTracker.getTracker().sendEvent(
                    SERVICE_NAME,
                    "checkSsid",
                    wifiInfo != null ? "SSID mismatch" : "wifiInfo is null",
                    0L);
            return false;
        } else {
            return true;
        }
    }

    /**
     * Sends the service status to the local broadcast receivers.
     */
    private void setStatus(final SyncIntentServiceStatus status) {
        // Update settings.
        Settings.with(this).edit().syncStatus(status).commit();
        // Send the notification.
        final Intent intent = new Intent(SERVICE_NAME);
        intent.putExtra(STATUS_EXTRA_KEY, status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    /**
     * Performs syncing with the specified syncer.
     */
    private boolean sync(
            final HttpClient client,
            final Syncer syncer,
            final String clientId,
            final String authToken) {
        Log.i(SERVICE_NAME + ".sync", "Starting syncing with " + syncer);
        boolean isSucceeded = true;
        // Prepare the event tracker.
        EasyTracker.getInstance().setContext(this);
        final Tracker tracker = EasyTracker.getTracker();
        // Performance counters.
        final long syncStartTime = System.currentTimeMillis();
        // Synchronization loop.
        while (true) {
            // Get next request.
            final TaggedRequest taggedRequest = syncer.getNextRequest(this);
            if (taggedRequest == null) {
                Log.d(SERVICE_NAME, "getNextRequest returned null.");
                break;
            }
            // Initialize the request with the common parameters.
            initializeRequest(taggedRequest.getRequest(), clientId, authToken);
            // Execute the request.
            Log.d(SERVICE_NAME + ".sync", "Executing the request: " + taggedRequest.getRequest().getURI());
            final long requestStartTime = System.currentTimeMillis();
            HttpResponse response;
            try {
                response = client.execute(taggedRequest.getRequest());
            } catch (IOException e) {
                Log.w(SERVICE_NAME + ".sync", "Syncing is broken: " + e.getMessage());
                tracker.sendEvent(
                        SERVICE_NAME,
                        "client.execute",
                        String.format("%s/%s", syncer.getClass().getSimpleName(), e.getClass().getSimpleName()),
                        syncer.getSyncedEntitiesCount());
                isSucceeded = false;
                break;
            }
            final long requestEndTime = System.currentTimeMillis();
            // Log headers.
            for (Header header : response.getAllHeaders()) {
                Log.d(SERVICE_NAME + ".sync", String.format("%s: %s", header.getName(), header.getValue()));
            }
            // Check the status code.
            final StatusLine statusLine = response.getStatusLine();
            Log.d(SERVICE_NAME + ".sync", String.format("Request is finished in %sms: %s",
                    requestEndTime - requestStartTime,
                    statusLine));
            // Process the response.
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                Log.w(SERVICE_NAME + ".sync", "Syncing is broken:" + statusLine);
                tracker.sendEvent(
                        SERVICE_NAME,
                        "client.execute",
                        String.format("%s/%d", syncer.getClass().getSimpleName(), statusLine.getStatusCode()),
                        syncer.getSyncedEntitiesCount());
                isSucceeded = false;
                break;
            }
            Log.d(SERVICE_NAME + ".sync", "Processing the response ...");
            final long processResponseStartTime = System.currentTimeMillis();
            final boolean hasNext = syncer.processResponse(this, taggedRequest, response);
            final long processResponseTime = System.currentTimeMillis() - processResponseStartTime;
            Log.d(SERVICE_NAME + ".sync", String.format("Response is processed in %sms.", processResponseTime));
            tracker.sendTiming(SERVICE_NAME, processResponseTime, "syncer.processResponse", syncer.toString());
            if (!hasNext) {
                Log.i(SERVICE_NAME + ".sync", "Sync is finished.");
                tracker.sendEvent(SERVICE_NAME, "sync", syncer.getClass().getSimpleName(), syncer.getSyncedEntitiesCount());
                break;
            }
        }
        // The sync loop is finished. Collect sync statistics.
        final long syncTime = System.currentTimeMillis() - syncStartTime;
        final long syncedEntitiesCount = syncer.getSyncedEntitiesCount();
        final long entitySyncTime = syncedEntitiesCount != 0 ? syncTime / syncedEntitiesCount : 0;
        Log.i(SERVICE_NAME + ".sync", String.format("Synced %s entities in %sms (%sms per entity)",
                syncedEntitiesCount,
                syncTime,
                entitySyncTime));
        // Send sync time.
        tracker.sendTiming(SERVICE_NAME, syncTime, "sync", syncer.toString());
        // Return whether we succeeded.
        return isSucceeded;
    }

    /**
     * Initializes the request.
     */
    private static void initializeRequest(
            final HttpRequest request,
            final String clientId,
            final String authToken) {
        request.setHeader("Accept", "application/json");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Connection", "Keep-Alive");
        request.setHeader("X-Client-ID", clientId);
        // Add the authentication token if exists.
        if (authToken != null) {
            request.setHeader("X-Auth-Token", authToken);
        }
    }
}
