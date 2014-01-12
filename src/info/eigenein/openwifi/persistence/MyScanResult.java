package info.eigenein.openwifi.persistence;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.location.*;
import android.net.wifi.*;
import android.text.*;
import android.util.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.services.*;
import info.eigenein.openwifi.tasks.*;
import org.json.*;

import java.util.*;

/**
 * Represents a scan result.
 */
public final class MyScanResult {

    private long id;
    private int accuracy;
    private int latitudeE6;
    private int longitudeE6;
    private long timestamp;
    private String bssid;
    private String ssid;
    private long quadtreeIndex;

    public static MyScanResult fromJsonObject(final JSONObject object) {
        final MyScanResult scanResult = new MyScanResult();
        try {
            scanResult.timestamp = object.getLong("ts");
            scanResult.ssid = object.getString("ssid");
            scanResult.accuracy = (int)object.getDouble("acc");
            scanResult.bssid = object.getString("bssid");
            final JSONObject locationObject = object.getJSONObject("loc");
            scanResult.setLatitude(locationObject.getDouble("lat"));
            scanResult.setLongitude(locationObject.getDouble("lon"));
        } catch (JSONException e) {
            throw new RuntimeException("Error while converting from JSON object.", e);
        }
        return scanResult;
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
        return L.fromE6(latitudeE6);
    }

    public int getLatitudeE6() {
        return latitudeE6;
    }

    public double getLongitude() {
        return L.fromE6(longitudeE6);
    }

    public int getLongitudeE6() {
        return longitudeE6;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public long getQuadtreeIndex() {
        return quadtreeIndex;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public void setAccuracy(final int accuracy) {
        this.accuracy = accuracy;
    }

    public void setLatitude(final double latitude) {
        this.latitudeE6 = L.toE6(latitude);
    }

    public void setLatitudeE6(final int latitude) {
        this.latitudeE6 = latitude;
    }

    public void setLongitude(final double longitude) {
        this.longitudeE6 = L.toE6(longitude);
    }

    public void setLongitudeE6(final int longitude) {
        this.longitudeE6 = longitude;
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

    /**
     * Represents the {@link MyScanResult} DAO.
     */
    public static class Dao extends BaseDao {

        private static final String LOG_TAG = Dao.class.getCanonicalName();

        public Dao(final SQLiteDatabase database) {
            super(database);
        }

        @Override
        public void onCreate(final SQLiteDatabase database) {
            database.execSQL(
                    "CREATE TABLE `my_scan_results` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "`accuracy` INTEGER NOT NULL, " +
                            "`latitude` INTEGER NOT NULL, " +
                            "`longitude` INTEGER NOT NULL, " +
                            "`timestamp` BIGINT NOT NULL, " +
                            "`synced` SMALLINT NOT NULL, " +
                            "`own` SMALLINT NOT NULL, " +
                            "`bssid` VARCHAR NOT NULL, " +
                            "`ssid` VARCHAR NOT NULL, " +
                            "`quadtree_index` BIGINT NOT NULL);"
            );
            // Used to query by location.
            database.execSQL(
                    "CREATE INDEX `idx_my_scan_results_quadtree_index` " +
                            "ON `my_scan_results` (`quadtree_index`);");
            // Used to query unsynced results while syncing.
            database.execSQL(
                    "CREATE INDEX `idx_my_scan_results_synced` " +
                            "ON `my_scan_results` (`synced`);");
            // Used to query by BSSID when cleaning up the database.
            database.execSQL(
                    "CREATE INDEX `idx_my_scan_results_bssid` " +
                            "ON `my_scan_results` (`bssid`);");
        }

        @Override
        public void onUpgrade(
                final SQLiteDatabase database,
                final Context context,
                final int oldVersion,
                final int newVersion) {
            Log.i(LOG_TAG + ".onUpgrade", String.format("From v%s to v%s.", oldVersion, newVersion));

            if (newVersion == CacheOpenHelper.DatabaseVersion.QUADTREES) {
                // Drop the table.
                Log.d(LOG_TAG + ".onUpgrade", "Dropping the table ...");
                database.execSQL("DROP TABLE my_scan_results;");
                // Create the table.
                Log.d(LOG_TAG + ".onUpgrade", "Creating the table ...");
                onCreate(database);
                // Reset the last sync ID.
                Settings.with(context).edit().lastSyncId(Settings.DEFAULT_LAST_SYNC_ID).commit();
                // Start sync.
                SyncIntentService.start(context, true);
            }
        }

        /**
         * Queries the cluster within the specified quad. This method
         * returns the position and the size only (not scan results).
         */
        public RefreshMapAsyncTask.Network.Cluster queryClusterByQuadtreeIndex(
                final QuadtreeIndexer.Query.IndexRange indexRange) {
            final Cursor cursor = database.rawQuery(
                    "SELECT COUNT(DISTINCT ssid), AVG(latitude), AVG(longitude) " +
                            "FROM my_scan_results " +
                            "WHERE quadtree_index BETWEEN ? AND ?;",
                    new String[] {
                            Long.toString(indexRange.getLeftIndex()),
                            Long.toString(indexRange.getRightIndex()) });
            try {
                cursor.moveToFirst();
                return new RefreshMapAsyncTask.Network.Cluster(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getInt(2),
                        null);
            } finally {
                cursor.close();
            }
        }

        /**
         * Queries the scan results within the specified quad.
         */
        public List<MyScanResult> queryScanResultsByQuadtreeIndex(
                QuadtreeIndexer.Query.IndexRange indexRange) {
            final List<MyScanResult> results = new ArrayList<MyScanResult>();
            final Cursor cursor = database.rawQuery(
                    "SELECT  id, accuracy, latitude, longitude, timestamp, bssid, ssid, quadtree_index " +
                            "FROM my_scan_results " +
                            "WHERE quadtree_index BETWEEN ? AND ?;",
                    new String[] {
                            Long.toString(indexRange.getLeftIndex()),
                            Long.toString(indexRange.getRightIndex())
                    });
            read(cursor, results);
            return results;
        }

        /**
         * Gets the unsynced results.
         */
        public List<MyScanResult> queryUnsynced(final int limit) {
            // Initialize the collection.
            final List<MyScanResult> results = new ArrayList<MyScanResult>();
            // Run the query.
            final Cursor cursor = database.rawQuery(
                    "SELECT id, accuracy, latitude, longitude, timestamp, bssid, ssid, quadtree_index " +
                            "FROM my_scan_results " +
                            "WHERE NOT synced " +
                            "LIMIT ?;",
                    new String[] { Integer.toString(L.toE6(limit)) }
            );
            // Read results.
            read(cursor, results);
            // Return the results.
            return results;
        }

        /**
         * Gets the scan results by the specified BSSID from the newest to the oldest.
         */
        public List<MyScanResult> queryNewestByBssid(final String bssid) {
            Log.d(LOG_TAG + ".queryNewestByBssid", bssid);

            final List<MyScanResult> results = new ArrayList<MyScanResult>();
            final Cursor cursor = database.rawQuery(
                    "SELECT id, accuracy, latitude, longitude, timestamp, bssid, ssid, quadtree_index " +
                            "FROM my_scan_results " +
                            "WHERE bssid = ? " +
                            "ORDER BY timestamp DESC;",
                    new String[] { bssid }
            );
            read(cursor, results);
            return results;
        }

        /**
         * Inserts the results.
         */
        public void insert(
                final Location location,
                final Collection<ScanResult> results,
                final boolean synced,
                final boolean own) {
            if (results.isEmpty()) {
                return;
            }
            for (final ScanResult scanResult : results) {
                // Avoid duplicates.
                final long duplicatesCount = DatabaseUtils.longForQuery(
                        database,
                        "SELECT COUNT(*) FROM my_scan_results WHERE bssid = ? AND timestamp = ? AND own;",
                        new String[] { scanResult.BSSID, Long.toString(location.getTime()) });
                if (duplicatesCount == 0L) {
                    final ContentValues values = new ContentValues();
                    values.put("accuracy", (int)location.getAccuracy());
                    final int latitudeE6 = L.toE6(location.getLatitude());
                    values.put("latitude", latitudeE6);
                    final int longitudeE6 = L.toE6(location.getLongitude());
                    values.put("longitude", longitudeE6);
                    values.put("timestamp", location.getTime());
                    values.put("synced", synced);
                    values.put("own", own);
                    values.put("bssid", scanResult.BSSID);
                    values.put("ssid", scanResult.SSID);
                    values.put("quadtree_index", QuadtreeIndexer.getIndex(latitudeE6, longitudeE6));
                    final long rowId = database.insert("my_scan_results", null, values);
                    Log.d(LOG_TAG + ".insert(location, results)", String.format(
                            "Inserted #%s (%s).", rowId, scanResult.SSID));
                } else {
                    Log.d(LOG_TAG + ".insert(location, results)", String.format(
                            "Skipped %s (%s)", scanResult.BSSID, scanResult.SSID));
                }
            }
        }

        /**
         * Inserts the results.
         */
        @SuppressWarnings("deprecation")
        public void insert(
                final Collection<MyScanResult> results,
                final boolean synced,
                final boolean own) {
            // Check the parameters.
            if (results.isEmpty()) {
                return;
            }
            final long startTimeMillis = System.currentTimeMillis();
            // Initialize the helper.
            final DatabaseUtils.InsertHelper insertHelper =
                    new DatabaseUtils.InsertHelper(database, "my_scan_results");
            final int accuracyIndex = insertHelper.getColumnIndex("accuracy");
            final int latitudeIndex = insertHelper.getColumnIndex("latitude");
            final int longitudeIndex = insertHelper.getColumnIndex("longitude");
            final int timestampIndex = insertHelper.getColumnIndex("timestamp");
            final int syncedIndex = insertHelper.getColumnIndex("synced");
            final int ownIndex = insertHelper.getColumnIndex("own");
            final int bssidIndex = insertHelper.getColumnIndex("bssid");
            final int ssidIndex = insertHelper.getColumnIndex("ssid");
            final int quadtreeIndexIndex = insertHelper.getColumnIndex("quadtree_index");
            // Perform inserting.
            try {
                for (final MyScanResult result : results) {
                    insertHelper.prepareForInsert();
                    // Put the values.
                    insertHelper.bind(accuracyIndex, result.getAccuracy());
                    insertHelper.bind(latitudeIndex, result.getLatitudeE6());
                    insertHelper.bind(longitudeIndex, result.getLongitudeE6());
                    insertHelper.bind(timestampIndex, result.getTimestamp());
                    insertHelper.bind(syncedIndex, synced);
                    insertHelper.bind(ownIndex, own);
                    insertHelper.bind(bssidIndex, result.getBssid());
                    insertHelper.bind(ssidIndex, result.getSsid());
                    insertHelper.bind(quadtreeIndexIndex, result.getQuadtreeIndex());
                    // Insert the result.
                    insertHelper.execute();
                }
            } finally {
                insertHelper.close();
            }
            // Done.
            Log.d(LOG_TAG + ".insert(results)", String.format(
                    "Inserted %d results in %sms.",
                    results.size(),
                    System.currentTimeMillis() - startTimeMillis
            ));
        }

        /**
         * Sets the synced flag on the specified results.
         */
        public void setSynced(final Collection<MyScanResult> results) {
            final StringBuilder idsStringBuilder = new StringBuilder();
            for (final MyScanResult result : results) {
                if (idsStringBuilder.length() != 0) {
                    idsStringBuilder.append(", ");
                }
                idsStringBuilder.append(result.getId());
            }
            if (idsStringBuilder.length() != 0) {
                final String ids = idsStringBuilder.toString();
                Log.d(LOG_TAG + ".setSynced", ids);
                database.execSQL(String.format("UPDATE my_scan_results SET synced = 1 WHERE id IN (%s);", ids));
            }
        }

        /**
         * Gets the total scan result count.
         */
        public long getCount() {
            return DatabaseUtils.queryNumEntries(database, "my_scan_results");
        }

        public long getUniqueBssidCount() {
            return getUniqueCount("bssid");
        }

        public long getUniqueSsidCount() {
            return getUniqueCount("ssid");
        }

        /**
         * Deletes the scan results with specified IDs.
         */
        public void delete(final Collection<Long> ids) {
            Log.d(LOG_TAG + ".delete", String.format("Delete %s results.", ids.size()));
            if (!ids.isEmpty()) {
                final long startTimeMillis = System.currentTimeMillis();
                database.execSQL(String.format(
                        "DELETE FROM my_scan_results WHERE id in (%s);",
                        TextUtils.join(", ", ids)));
                Log.d(LOG_TAG + ".delete", String.format("Done in %sms.", System.currentTimeMillis() - startTimeMillis));
            }
        }

        private long getUniqueCount(final String columnName) {
            return DatabaseUtils.longForQuery(
                    database,
                    String.format("SELECT COUNT(DISTINCT %s) FROM my_scan_results;", columnName),
                    null);
        }

        /**
         * Reads the results from the cursor.
         */
        private static void read(final Cursor cursor, final Collection<MyScanResult> results) {
            Log.d(LOG_TAG + ".read", String.format("Reading %s rows ...", cursor.getCount()));
            try {
                while (cursor.moveToNext()) {
                    results.add(read(cursor));
                }
            } finally {
                cursor.close();
            }
        }

        /**
         * Reads the result from the cursor.
         */
        private static MyScanResult read(final Cursor cursor) {
            final MyScanResult result = new MyScanResult();
            result.setId(cursor.getLong(0));
            result.setAccuracy(cursor.getInt(1));
            result.setLatitudeE6(cursor.getInt(2));
            result.setLongitudeE6(cursor.getInt(3));
            result.setTimestamp(cursor.getLong(4));
            result.setBssid(cursor.getString(5));
            result.setSsid(cursor.getString(6));
            result.setQuadtreeIndex(cursor.getLong(7));
            return result;
        }
    }
}
