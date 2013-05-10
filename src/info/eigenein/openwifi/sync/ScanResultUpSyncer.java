package info.eigenein.openwifi.sync;

import android.content.Context;
import android.util.Log;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
import info.eigenein.openwifi.persistency.MyScanResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;

import java.io.UnsupportedEncodingException;
import java.util.List;

public class ScanResultUpSyncer extends ScanResultSyncer {
    private static final String LOG_TAG = ScanResultUpSyncer.class.getCanonicalName();

    private static final String URL = "http://openwifi.info/api/scan-results/";

    public TaggedRequest getNextRequest(Context context) {
        // Prepare the page.
        Log.d(LOG_TAG + ".getNextRequest", "Querying for the page ...");
        final List<MyScanResult> scanResults =
                ScanResultTracker.getUnsyncedScanResults(context, PAGE_SIZE);
        Log.d(LOG_TAG + ".getNextRequest", "scanResults: " + scanResults.size());
        if (scanResults.isEmpty()) {
            // Finished.
            return null;
        }
        // Prepare the request.
        final HttpPost request = new HttpPost(URL);
        try {
            final JSONArray array = new JSONArray();
            for (MyScanResult scanResult : scanResults) {
                array.put(scanResult.toJsonObject());
            }
            final String jsonString = array.toString();
            request.setEntity(new StringEntity(jsonString, HTTP.UTF_8));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not create the entity.", e);
        }
        return new TaggedRequest(request, scanResults);
    }

    @Override
    public boolean processResponse(Context context, TaggedRequest request, HttpResponse response) {
        Log.d(LOG_TAG + ".processResponse", "Marking the results as synced ...");
        final List<MyScanResult> scanResults = (List<MyScanResult>)request.getTag();
        ScanResultTracker.markAsSynced(context, scanResults);
        syncedEntitiesCount += scanResults.size();
        return scanResults.size() != 0;
    }

    @Override
    public String toString() {
        return ScanResultUpSyncer.class.getSimpleName();
    }
}
