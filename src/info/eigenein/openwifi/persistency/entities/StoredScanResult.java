package info.eigenein.openwifi.persistency.entities;

import android.net.wifi.ScanResult;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Represents a scan result that is stored in the application database.
 */
@DatabaseTable(tableName = "scan_results")
public class StoredScanResult {
    @DatabaseField(columnName = "id", generatedId = true)
    private int id;

    @DatabaseField(
            columnName = "bssid",
            canBeNull = false,
            width = 17,
            uniqueIndex = true,
            uniqueIndexName = "scan_results_bssid_location_timestamp_idx")
    private String bssid;

    @DatabaseField(columnName = "ssid", canBeNull = false, index = true)
    private String ssid;

    @DatabaseField(columnName = "capabilities", canBeNull = true)
    private String capabilities;

    @DatabaseField(columnName = "level", canBeNull = true)
    private Integer level;

    @DatabaseField(columnName = "timestamp", canBeNull = false, index = true)
    private long timestamp;

    @DatabaseField(
            columnName = "location_timestamp",
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

    public StoredScanResult(ScanResult scanResult, StoredLocation location, long timestamp) {
        this.bssid = scanResult.BSSID;
        this.ssid = scanResult.SSID;
        this.capabilities = scanResult.capabilities;
        this.level = scanResult.level;
        this.timestamp = timestamp;
        this.location = location;
    }
}
