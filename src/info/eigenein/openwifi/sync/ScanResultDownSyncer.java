package info.eigenein.openwifi.sync;

import android.content.Context;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
import info.eigenein.openwifi.persistency.MyScanResult;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScanResultDownSyncer extends ScanResultSyncer {
    private static final String LOG_TAG = ScanResultDownSyncer.class.getCanonicalName();

    private static final String URL = "http://openwifi.info/api/scan-results/%s/%s/";

    private final Settings settings;

    public ScanResultDownSyncer(Settings settings) {
        this.settings = settings;
    }

    @Override
    public TaggedRequest getNextRequest(Context context) {
        HttpGet request = new HttpGet(String.format(URL, settings.lastSyncId(), PAGE_SIZE));
        return new TaggedRequest(request, null);
    }

    @Override
    public boolean processResponse(Context context, TaggedRequest request, HttpResponse response) {
        JSONArray scanResultList = null;
        try {
            scanResultList = new JSONArray(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse the response.", e);
        }
        for (int i = 0; i < scanResultList.length(); i++) {
            String syncId = null;
            // Initialize entities.
            MyScanResult scanResult = null;
            try {
                final JSONObject scanResultObject = scanResultList.getJSONObject(i);
                syncId = scanResultObject.getString("_id");
                scanResult = MyScanResult.fromJsonObject(scanResultObject);
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse the response item.", e);
            }
            scanResult.setOwn(false);
            scanResult.setSynced(true);
            // Store the entity.
            ScanResultTracker.add(context, scanResult);
            settings.edit().lastSyncId(syncId).commit();
            syncedEntitiesCount += 1;
        }
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
