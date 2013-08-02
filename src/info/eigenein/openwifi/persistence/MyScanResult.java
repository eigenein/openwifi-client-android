package info.eigenein.openwifi.persistence;

import android.location.*;
import android.net.wifi.*;
import info.eigenein.openwifi.helpers.location.*;
import org.json.*;

/**
 * Represents a scan result.
 */
public final class MyScanResult {

    private long id;

    private float accuracy;

    /**
     * Latitude * 10e6.
     */
    private int latitude;

    /**
     * Longitude * 10e6.
     */
    private int longitude;

    private long timestamp;

    private String bssid;

    private String ssid;

    private long quadtreeIndex;

    public static MyScanResult fromJsonObject(final JSONObject object) {
        final MyScanResult scanResult = new MyScanResult();
        try {
            scanResult.timestamp = object.getLong("ts");
            scanResult.ssid = object.getString("ssid");
            scanResult.accuracy = (float)object.getDouble("acc");
            scanResult.bssid = object.getString("bssid");
            final JSONObject locationObject = object.getJSONObject("loc");
            scanResult.setLatitude(locationObject.getDouble("lat"));
            scanResult.setLongitude(locationObject.getDouble("lon"));
        } catch (JSONException e) {
            throw new RuntimeException("Error while converting from JSON object.", e);
        }
        return scanResult;
    }

    public MyScanResult() {
        // Do nothing.
    }

    public MyScanResult(final ScanResult scanResult, final Location location) {
        this.accuracy = location.getAccuracy();
        this.bssid = scanResult.BSSID;
        this.ssid = scanResult.SSID;
        this.timestamp = location.getTime();
        // These need 10e6 fix.
        this.setLatitude(location.getLatitude());
        this.setLongitude(location.getLongitude());
    }

    public long getId() {
        return id;
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
        return L.fromE6(latitude);
    }

    public int getLatitudeE6() {
        return latitude;
    }

    public double getLongitude() {
        return L.fromE6(longitude);
    }

    public int getLongitudeE6() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public long getQuadtreeIndex() {
        return quadtreeIndex;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setAccuracy(final float accuracy) {
        this.accuracy = accuracy;
    }

    public void setLatitude(final double latitude) {
        this.latitude = L.toE6(latitude);
    }

    public void setLatitudeE6(final int latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(final double longitude) {
        this.longitude = L.toE6(longitude);
    }

    public void setLongitudeE6(final int longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setSsid(final String ssid) {
        this.ssid = ssid;
    }

    public void setBssid(final String bssid) {
        this.bssid = bssid;
    }

    public void setQuadtreeIndex(final long index) {
        this.quadtreeIndex = index;
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
            // These need 10e6 fix.
            locationObject.put("lat", getLatitude());
            locationObject.put("lon", getLongitude());
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
                "%s[ssid=%s, bssid=%s]",
                MyScanResult.class.getSimpleName(),
                bssid,
                ssid);
    }
}
