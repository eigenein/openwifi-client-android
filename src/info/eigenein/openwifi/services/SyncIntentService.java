package info.eigenein.openwifi.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Synchronizes the local database with the server database.
 */
public class SyncIntentService extends IntentService {
    private static final String SERVICE_NAME = SyncIntentService.class.getCanonicalName();

    private static final String getUrl = "http://openwifi.info/api/scan-results/%s/%s/";

    private static final String postUrl = "http://openwifi.info/api/scan-results/";

    /**
     * Maximum allowed number of scan results to be requested at once.
     */
    private static final int CHUNK_SIZE = 64;

    public SyncIntentService() {
        super(SERVICE_NAME);
    }

    protected void onHandleIntent(Intent intent) {
        Log.i(SERVICE_NAME, "Service is running.");

        final Settings settings = Settings.with(this);

        // Download the results and update last ID.
        final String lastSyncId = downloadScanResults(settings.lastSyncId());
        settings.edit().lastSyncId(lastSyncId).commit();
        // Upload our results.
        final String clientId = settings.clientId();
        uploadScanResults(clientId);

        Log.i(SERVICE_NAME, "Everything is finished.");
    }

    private String downloadScanResults(String lastSyncId) {
        Log.i(SERVICE_NAME, "Starting to download scan results ...");
        Log.d(SERVICE_NAME, "lastSyncId: " + lastSyncId);
        // TODO: implement downloading.
        Log.i(SERVICE_NAME, "Finished. lastSyncId: " + lastSyncId);
        return lastSyncId;
    }

    private void uploadScanResults(String clientId) {
        Log.i(SERVICE_NAME, "Starting to upload scan results ...");
        List<StoredScanResult> scanResults = ScanResultTracker.getUnsyncedScanResults(this);
        Log.d(SERVICE_NAME, "Unsynced scan results found: " + scanResults.size());
        final HttpClient client = new DefaultHttpClient(new BasicHttpParams());
        int syncedResultCount = 0;
        for (StoredScanResult scanResult : scanResults) {
            // Prepare the request.
            HttpPost request = new HttpPost(postUrl);
            request.setHeader("X-Client-ID", clientId);
            request.setHeader("Accept", "application/json");
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Connection", "Keep-Alive");
            try {
                request.setEntity(new StringEntity(scanResult.toJson()));
            } catch (UnsupportedEncodingException e) {
                Log.e(SERVICE_NAME, "Could not create the entity.", e);
                continue;
            }
            // Execute the request.
            HttpResponse response = null;
            try {
                response = client.execute(request);
            } catch (IOException e) {
                Log.w(SERVICE_NAME, e.getMessage());
                break;
            }
            // Check the status code.
            final StatusLine statusLine = response.getStatusLine();
            Log.d(SERVICE_NAME, "Status line: " + statusLine);
            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ScanResultTracker.markAsSynced(this, scanResult);
                syncedResultCount += 1;
            }
        }
        Log.i(SERVICE_NAME, "Finished. syncedResultCount: " + syncedResultCount);
    }
}
