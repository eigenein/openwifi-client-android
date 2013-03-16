package info.eigenein.openwifi.persistency.entities;

import android.net.wifi.ScanResult;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import info.eigenein.openwifi.helpers.ScanResultTracker;

/**
 * Represents a scan result that is stored in the application database.
 */
@DatabaseTable(tableName = "scan_results")
public class StoredScanResult {
    public static final String BSSID = "bssid";

    public static final String SSID = "ssid";

    public static final String LOCATION_TIMESTAMP = "location_timestamp";

    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(
            columnName = BSSID,
            canBeNull = false,
            width = 17,
            uniqueIndex = true,
            uniqueIndexName = "scan_results_bssid_location_timestamp_idx")
    private String bssid;

    @DatabaseField(columnName = SSID, canBeNull = false, index = true)
    private String ssid;

    @DatabaseField(columnName = "capabilities", canBeNull = true)
    private String capabilities;

    @DatabaseField(columnName = "level", canBeNull = true)
    private Integer level;

    @DatabaseField(
            columnName = LOCATION_TIMESTAMP,
            uniqueIndex = true,
            uniqueIndexName = "scan_results_bssid_location_timestamp_idx",
            foreign = true,
            foreignColumnName = "timestamp")
    private StoredLocation location;

    @DatabaseField(columnName = "synced", canBeNull = false)
    private boolean synced;

    public StoredScanResult() {
        // ORMLite needs a no-arg constructor.
    }

    public StoredScanResult(ScanResult scanResult, StoredLocation location) {
        this.bssid = scanResult.BSSID;
        this.ssid = scanResult.SSID;
        this.capabilities = scanResult.capabilities;
        this.level = scanResult.level;
        this.location = location;
    }

    public String getBssid() {
        return bssid;
    }

    public String getSsid() {
        return ssid;
    }

    public StoredLocation getLocation() {
        return location;
    }
}
