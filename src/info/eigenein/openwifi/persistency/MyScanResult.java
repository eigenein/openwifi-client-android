package info.eigenein.openwifi.persistency;

import android.location.Location;
import android.net.wifi.ScanResult;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a scan result record in the database.
 */
@DatabaseTable(tableName = "my_scan_results")
public class MyScanResult {
    public static final String ID = "id";
    public static final String ACCURACY = "accuracy";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String TIMESTAMP = "timestamp";
    public static final String SYNCED = "synced";
    public static final String OWN = "own";
    public static final String BSSID = "bssid";
    public static final String SSID = "ssid";

    public static final int BSSID_LENGTH = 17;
    public static final int MAX_SSID_LENGTH = 32;

    @DatabaseField(
            columnName = ID,
            canBeNull = false,
            generatedId = true,
            index = true)
    private long id;

    @DatabaseField(
            columnName = ACCURACY,
            canBeNull = false
    )
    private float accuracy;

    @DatabaseField(
            columnName = LATITUDE,
            canBeNull = false,
            index = true
    )
    private double latitude;

    @DatabaseField(
            columnName = LONGITUDE,
            canBeNull = false,
            index = true
    )
    private double longitude;

    @DatabaseField(
            columnName = TIMESTAMP,
            canBeNull = false,
            index = true,
            indexName = "my_scan_results_bssid_timestamp_idx"
    )
    private long timestamp;

    @DatabaseField(
            columnName = SYNCED,
            canBeNull = false
    )
    private boolean synced;

    @DatabaseField(
            columnName = OWN,
            canBeNull = false
    )
    private boolean own;

    @DatabaseField(
            columnName = BSSID,
            canBeNull = false,
            width = BSSID_LENGTH,
            index = true,
            indexName = "my_scan_results_bssid_timestamp_idx"
    )
    private String bssid;

    @DatabaseField(
            columnName = SSID,
            canBeNull = false,
            width = MAX_SSID_LENGTH
    )
    private String ssid;

    public static MyScanResult fromJsonObject(final JSONObject object) {
        final MyScanResult scanResult = new MyScanResult();
        try {
            scanResult.timestamp = object.getLong("ts");
            scanResult.ssid = object.getString("ssid");
            scanResult.accuracy = (float)object.getDouble("acc");
            scanResult.bssid = object.getString("bssid");
            final JSONObject locationObject = object.getJSONObject("loc");
            scanResult.latitude = locationObject.getDouble("lat");
            scanResult.longitude = locationObject.getDouble("lon");
        } catch (JSONException e) {
            throw new RuntimeException("Error while converting from JSON object.", e);
        }
        return scanResult;
    }

    /**
     * Parameterless constructor for ORMLite.
     */
    public MyScanResult() {
        // Do nothing.
    }

    public MyScanResult(ScanResult scanResult, Location location) {
        this.accuracy = location.getAccuracy();
        this.bssid = scanResult.BSSID;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.ssid = scanResult.SSID;
        this.timestamp = location.getTime();
    }

    public String getSsid() {
        return ssid;
    }

    public String getBssid() {
        return bssid;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public boolean isOwn() {
        return own;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    public void setOwn(boolean own) {
        this.own = own;
    }

    public JSONObject toJsonObject() {
        try {
            // Build the object.
            final JSONObject object = new JSONObject();
            object.put("acc", accuracy);
            object.put("ssid", ssid);
            object.put("bssid", bssid);
            object.put("ts", timestamp);
            // Build the location object.
            final JSONObject locationObject = new JSONObject();
            locationObject.put("lat", latitude);
            locationObject.put("lon", longitude);
            object.put("loc", locationObject);
            // Done.
            return object;
        } catch (JSONException e) {
            throw new RuntimeException("Error while building JSON object.", e);
        }
    }

    @Override
    public String toString() {
        return String.format(
                "%s[ssid=%s, bssid=%s, synced=%s, own=%s]",
                MyScanResult.class.getSimpleName(),
                bssid,
                ssid,
                synced,
                own);
    }
}
