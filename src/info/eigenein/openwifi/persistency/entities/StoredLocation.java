package info.eigenein.openwifi.persistency.entities;

import android.location.Location;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a location that is stored in the application database.
 */
@DatabaseTable(tableName = "locations")
public class StoredLocation {
    @DatabaseField(columnName = "accuracy", canBeNull = false)
    private float accuracy;

    @DatabaseField(columnName = "latitude", canBeNull = false)
    private double latitude;

    @DatabaseField(columnName = "longitude", canBeNull = false)
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

    public long getTimestamp() {
        return timestamp;
    }
}
