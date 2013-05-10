package info.eigenein.openwifi.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.sync.ScanResultDownSyncer;
import info.eigenein.openwifi.sync.ScanResultUpSyncer;
import info.eigenein.openwifi.sync.Syncer;
import info.eigenein.openwifi.sync.TaggedRequest;
import org.apache.http.*;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.util.Date;

/**
 * Synchronizes the local database with the server database.
 */
public class SyncIntentService extends IntentService {
    private static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();

    /**
     * Minimal sync period.
     */
    public static final long SYNC_PERIOD_MILLIS = 60L * 60L * 1000L;

    public SyncIntentService() {
        super(SERVICE_NAME);
    }

    protected void onHandleIntent(final Intent intent) {
        Log.i(SERVICE_NAME + ".onHandleIntent", "Service is running.");

        final Settings settings = Settings.with(this);

        // Set the "syncing now" flag.
        settings.edit().syncingNow(true).commit();

        final String clientId = settings.clientId();
        try {
            // Download the scan results.
            sync(new ScanResultDownSyncer(settings), clientId);
            // Upload our scan results.
            sync(new ScanResultUpSyncer(), clientId);
            // Update last sync time.
            settings.edit().lastSyncTime(System.currentTimeMillis()).commit();
        } finally {
            // Reset the "syncing now" flag.
            settings.edit().syncingNow(false).commit();
        }

        Log.i(SERVICE_NAME + ".onHandleIntent", "Everything is finished.");
    }

    private void sync(final Syncer syncer, final String clientId) {
        Log.i(SERVICE_NAME + ".sync", "Starting syncronization with " + syncer);
        // Prepare the HTTP client.
        final DefaultHttpClient client = prepareHttpClient();
        // Performance counters.
        final long syncStartTime = System.currentTimeMillis();
        // Synchronization loop.
        while (true) {
            // Get next request.
            final TaggedRequest taggedRequest = syncer.getNextRequest(this);
            if (taggedRequest == null) {
                break;
            }
            // Initialize the request with the common parameters.
            initializeRequest(taggedRequest.getRequest(), clientId);
            // Execute the request.
            Log.d(SERVICE_NAME + ".sync", "Executing the request: " + taggedRequest.getRequest().getURI());
            final long requestStartTime = System.currentTimeMillis();
            HttpResponse response;
            try {
                response = client.execute(taggedRequest.getRequest());
            } catch (IOException e) {
                Log.w(SERVICE_NAME + ".sync", e.getMessage());
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
                Log.w(SERVICE_NAME + ".sync", "Syncing is broken.");
                break;
            }
            Log.d(SERVICE_NAME + ".sync", "Processing the response ...");
            final long processResponseStartTime = System.currentTimeMillis();
            boolean hasNext = syncer.processResponse(this, taggedRequest, response);
            final long processResponseEndTime = System.currentTimeMillis();
            Log.d(SERVICE_NAME + ".sync", String.format(
                    "Response is processed in %sms.",
                    processResponseEndTime - processResponseStartTime));
            if (!hasNext) {
                Log.i(SERVICE_NAME + ".sync", "Sync is finished.");
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
        // Send sync statistics.
        final Tracker tracker = EasyTracker.getTracker();
        tracker.sendEvent(SERVICE_NAME, "sync", syncer.toString(), syncedEntitiesCount);
        tracker.sendTiming(SERVICE_NAME, syncTime, "sync", syncer.toString());
    }

    /**
     * Initializes the HTTP client.
     */
    private static DefaultHttpClient prepareHttpClient() {
        final HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        final DefaultHttpClient client = new DefaultHttpClient(params);
        client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        return client;
    }

    /**
     * Initializes the request.
     */
    private static void initializeRequest(HttpRequest request, String clientId) {
        request.setHeader("X-Client-ID", clientId);
        request.setHeader("Accept", "application/json");
        // TODO: request.setHeader("Accept-Encoding", "gzip");
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Connection", "Keep-Alive");
    }
}
