package info.eigenein.openwifi.sync;

import android.content.Context;
import info.eigenein.openwifi.helpers.internal.Settings;
import info.eigenein.openwifi.persistence.*;
import info.eigenein.openwifi.services.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ScanResultDownSyncer extends ScanResultSyncer {
    private static final String URL = "https://openwifi.info/api/scan-results/%s/%s/";

    private final Settings settings;

    public ScanResultDownSyncer(final Settings settings) {
        this.settings = settings;
    }

    @Override
    public TaggedRequest getNextRequest(final Context context) {
        HttpGet request = new HttpGet(String.format(URL, settings.lastSyncId(), PAGE_SIZE));
        return new TaggedRequest(request, null);
    }

    @Override
    public boolean processResponse(
            final Context context,
            final TaggedRequest request,
            final HttpResponse response) {
        // Parse JSON.
        JSONArray scanResultList;
        try {
            scanResultList = new JSONArray(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse the response.", e);
        }
        final List<MyScanResult> scanResults = new ArrayList<MyScanResult>();
        // Process the source objects.
        String lastSyncId = null;
        for (int i = 0; i < scanResultList.length(); i++) {
            String syncId;
            // Initialize entities.
            final MyScanResult scanResult;
            try {
                final JSONObject scanResultObject = scanResultList.getJSONObject(i);
                syncId = scanResultObject.getString("_id");
                scanResult = MyScanResult.fromJsonObject(scanResultObject);
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse the response item.", e);
            }
            scanResult.setOwn(false);
            scanResult.setSynced(true);
            // Add to the list.
            scanResults.add(scanResult);
            // Update lastSyncId.
            if (syncId != null) {
                lastSyncId = syncId;
            }
        }
        // Store the entities.
        final MyScanResultDao dao = CacheOpenHelper.getInstance(context).getMyScanResultDao();
        dao.insert(scanResults);
        // Start the cleanup service.
        CleanupIntentService.queueMyScanResults(context, scanResults);
        // Update lastSyncId setting.
        if (lastSyncId != null) {
            settings.edit().lastSyncId(lastSyncId).commit();
        }
        // Update performance counters.
        syncedEntitiesCount += scanResults.size();
        // Finish sync if there is any result.
        return scanResultList.length() != 0;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[lastSyncId=%s]",
                ScanResultDownSyncer.class.getSimpleName(),
                settings.lastSyncId()
        );
    }
}
