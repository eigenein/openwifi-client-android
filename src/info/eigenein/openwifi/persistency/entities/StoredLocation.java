package info.eigenein.openwifi.persistency.entities;

import android.location.Location;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a location that is stored in the application database.
 */
@DatabaseTable(tableName = "locations")
public class StoredLocation {
    @DatabaseField(columnName = "accuracy", canBeNull = false, index = true)
    private float accuracy;

    @DatabaseField(columnName = "latitude", canBeNull = false, index = true)
    private double latitude;

    @DatabaseField(columnName = "longitude", canBeNull = false, index = true)
    private double longitude;

    @DatabaseField(columnName = "provider", canBeNull = true)
    private String provider;

    @DatabaseField(columnName = "timestamp", uniqueIndex = true, id = true)
    private long timestamp;

    public StoredLocation() {
        // ORMLite needs a no-arg constructor.
    }

    public StoredLocation(Location location) {
        this.accuracy = location.getAccuracy();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.provider = location.getProvider();
        this.timestamp = location.getTime();
    }

    public float getAccuracy() {
        return accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format(
                "StoredLocation[latitude=%s, longitude=%s]",
                latitude,
                longitude);
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
