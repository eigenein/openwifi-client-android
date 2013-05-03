package info.eigenein.openwifi.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;
import info.eigenein.openwifi.sync.ScanResultDownSyncer;
import info.eigenein.openwifi.sync.ScanResultUpSyncer;
import info.eigenein.openwifi.sync.Syncer;
import info.eigenein.openwifi.sync.TaggedRequest;
import org.apache.http.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Synchronizes the local database with the server database.
 */
public class SyncIntentService extends IntentService {
    private static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();

    public SyncIntentService() {
        super(SERVICE_NAME);
    }

    protected void onHandleIntent(Intent intent) {
        Log.i(SERVICE_NAME, "Service is running.");

        final Settings settings = Settings.with(this);

        final String clientId = settings.clientId();
        // Download the scan results.
        sync(new ScanResultDownSyncer(settings), clientId);
        // Upload our scan results.
        sync(new ScanResultUpSyncer(), clientId);

        Log.i(SERVICE_NAME, "Everything is finished.");
    }

    private void sync(final Syncer syncer, final String clientId) {
        Log.i(SERVICE_NAME, "Starting syncronization with " + syncer);
        // Prepare the HTTP client.
        final DefaultHttpClient client = prepareHttpClient();
        // Performance counters.
        final long syncStartTime = System.currentTimeMillis();
        // Syncronization loop.
        while (true) {
            // Get next request.
            final TaggedRequest taggedRequest = syncer.getNextRequest(this);
            if (taggedRequest == null) {
                break;
            }
            // Initialize the request with the common parameters.
            initializeRequest(taggedRequest.getRequest(), clientId);
            // Execute the request.
            Log.d(SERVICE_NAME, "Executing the request: " + taggedRequest.getRequest().getURI());
            final long requestStartTime = System.currentTimeMillis();
            HttpResponse response = null;
            try {
                response = client.execute(taggedRequest.getRequest());
            } catch (IOException e) {
                Log.w(SERVICE_NAME, e.getMessage());
                break;
            }
            final long requestEndTime = System.currentTimeMillis();
            // Check the status code.
            final StatusLine statusLine = response.getStatusLine();
            Log.d(SERVICE_NAME, String.format("Request is finished in %sms: %s",
                    requestEndTime - requestStartTime,
                    statusLine));
            // Process the response.
            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                Log.w(SERVICE_NAME, "Syncing is broken.");
                break;
            }
            Log.d(SERVICE_NAME, "Processing the response ...");
            final long processResponseStartTime = System.currentTimeMillis();
            boolean hasNext = syncer.processResponse(this, taggedRequest, response);
            final long processResponseEndTime = System.currentTimeMillis();
            Log.d(SERVICE_NAME, String.format(
                    "Response is processed in %sms.",
                    processResponseEndTime - processResponseStartTime));
            if (!hasNext) {
                Log.i(SERVICE_NAME, "Sync is finished.");
                break;
            }
        }
        // The loop is finished.
        final long syncTime = System.currentTimeMillis() - syncStartTime;
        final int syncedEntitiesCount = syncer.getSyncedEntitiesCount();
        Log.i(SERVICE_NAME, String.format("Synced %s entities in %sms (%sms per entity)",
                syncedEntitiesCount,
                syncTime,
                syncedEntitiesCount != 0 ? syncTime / syncedEntitiesCount : 0));
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
        request.setHeader("Content-Type", "application/json");
        request.setHeader("Connection", "Keep-Alive");
    }
}
