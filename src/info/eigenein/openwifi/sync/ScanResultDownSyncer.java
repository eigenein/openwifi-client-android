package info.eigenein.openwifi.sync;

import android.content.Context;
import android.util.Log;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;
import info.eigenein.openwifi.persistency.entities.StoredLocation;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;
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
            final StoredLocation location = new StoredLocation();
            final StoredScanResult scanResult = new StoredScanResult();
            String syncId = null;
            // Initialize entities.
            try {
                final JSONObject scanResultObject = scanResultList.getJSONObject(i);
                Log.d(LOG_TAG, "scanResultObject: " + scanResultObject);
                syncId = scanResultObject.getString("_id");
                location.setTimestamp(scanResultObject.getLong("ts"));
                location.setAccuracy((float)scanResultObject.getDouble("acc"));
                final JSONObject locationObject = scanResultObject.getJSONObject("loc");
                location.setLatitude(locationObject.getDouble("lat"));
                location.setLongitude(locationObject.getDouble("lon"));
                location.setOwn(false);
                scanResult.setLocation(location);
                scanResult.setBssid(scanResultObject.getString("bssid"));
                scanResult.setSsid(scanResultObject.getString("ssid"));
                scanResult.setSynced(true);
                scanResult.setLocationTimestamp(location.getTimestamp());
                // Store the entities.
            } catch (JSONException e) {
                throw new RuntimeException("Could not parse the response item.", e);
            }
            // Store the entities.
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
