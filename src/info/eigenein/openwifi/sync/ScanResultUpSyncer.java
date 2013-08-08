package info.eigenein.openwifi.sync;

import android.content.*;
import android.util.*;
import info.eigenein.openwifi.persistence.*;
import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.entity.*;
import org.apache.http.protocol.*;
import org.json.*;

import java.io.*;
import java.util.*;

public class ScanResultUpSyncer extends ScanResultSyncer {
    private static final String LOG_TAG = ScanResultUpSyncer.class.getCanonicalName();

    private static final String URL = "https://openwifi.info/api/scan-results/";

    public TaggedRequest getNextRequest(final Context context) {
        // Prepare the page.
        Log.d(LOG_TAG + ".getNextRequest", "Querying for the page ...");
        final MyScanResult.Dao dao = CacheOpenHelper.getInstance(context).getMyScanResultDao();
        final List<MyScanResult> scanResults = dao.queryUnsynced(PAGE_SIZE);
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
    public boolean processResponse(
            final Context context,
            final TaggedRequest request,
            final HttpResponse response) {
        Log.d(LOG_TAG + ".processResponse", "Marking the results as synced ...");
        final List<MyScanResult> scanResults = (List<MyScanResult>)request.getTag();
        final MyScanResult.Dao dao = CacheOpenHelper.getInstance(context).getMyScanResultDao();
        dao.setSynced(scanResults);
        syncedEntitiesCount += scanResults.size();
        return scanResults.size() != 0;
    }

    @Override
    public String toString() {
        return ScanResultUpSyncer.class.getSimpleName();
    }
}
