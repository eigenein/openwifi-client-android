package info.eigenein.openwifi.persistence;

import android.database.*;
import android.database.sqlite.*;
import android.location.*;
import android.net.wifi.*;
import android.util.*;
import info.eigenein.openwifi.helpers.location.*;

import java.util.*;

public class MyScanResultDao extends BaseDao {
    private static final String LOG_TAG = MyScanResultDao.class.getCanonicalName();

    private static final int PAGE_SIZE = 4096;

    public MyScanResultDao(final SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        database.execSQL(
                "CREATE TABLE `my_scan_results` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "`accuracy` FLOAT NOT NULL, " +
                "`latitude` INTEGER NOT NULL, " +
                "`longitude` INTEGER NOT NULL, " +
                "`timestamp` BIGINT NOT NULL, " +
                "`synced` SMALLINT NOT NULL, " +
                "`own` SMALLINT NOT NULL, " +
                "`bssid` VARCHAR NOT NULL, " +
                "`ssid` VARCHAR NOT NULL);"
        );
    }

    public Collection<MyScanResult> queryByLocation(
                final CancellationToken cancellationToken,
                final double minLatitude,
                final double minLongitude,
                final double maxLatitude,
                final double maxLongitude) {
        Log.d(LOG_TAG + ".queryByLocation", String.format(
                "[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s]",
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude));
        final long startTimeMillis = System.currentTimeMillis();
        // Initialize the collection.
        final Collection<MyScanResult> results = new ArrayList<MyScanResult>();
        // Paging.
        int offset = 0;
        while (true) {
            // Test the cancellation token.
            if (cancellationToken.isCancelled()) {
                Log.d(LOG_TAG + ".queryByLocation", "Cancelled.");
                return null;
            }
            // Run query.
            Log.d(LOG_TAG + ".queryByLocation", String.format("Reading page at %s ...", offset));
            final Cursor cursor = database.rawQuery(
                    "SELECT id, accuracy, latitude, longitude, timestamp, synced, own, bssid, ssid " +
                            "FROM my_scan_results " +
                            "WHERE (latitude BETWEEN ? AND ?) AND (longitude BETWEEN ? AND ?) " +
                            "LIMIT ? OFFSET ?;",
                    new String[] {
                            Integer.toString(L.toE6(minLatitude)),
                            Integer.toString(L.toE6(maxLatitude)),
                            Integer.toString(L.toE6(minLongitude)),
                            Integer.toString(L.toE6(maxLongitude)),
                            Integer.toString(PAGE_SIZE),
                            Integer.toString(offset)
                    }
            );
            if (cursor.getCount() == 0) {
                break;
            }
            // Read results.
            Log.d(LOG_TAG + ".queryByLocation", String.format("Read page: %s rows.", cursor.getCount()));
            try {
                while (cursor.moveToNext()) {
                    results.add(read(cursor));
                }
            } finally {
                cursor.close();
            }
            // Move.
            offset += PAGE_SIZE;
        }
        Log.d(LOG_TAG + ".queryByLocation", String.format(
                "Got %d results in %sms.",
                results.size(),
                System.currentTimeMillis() - startTimeMillis
        ));
        // Return the results.
        return results;
    }

    public List<MyScanResult> queryUnsynced(final int limit) {
        return new ArrayList<MyScanResult>();
    }

    public List<MyScanResult> queryByBssid(final String bssid, final int limit) {
        return new ArrayList<MyScanResult>();
    }

    public void insert(final Location location, final Collection<ScanResult> results, final boolean own) {
        // TODO.
    }

    public void insert(final Collection<MyScanResult> results) {
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
        // Perform inserting.
        try {
            for (final MyScanResult result : results) {
                insertHelper.prepareForInsert();
                // Put the values.
                insertHelper.bind(accuracyIndex, result.getAccuracy());
                insertHelper.bind(latitudeIndex, result.getLatitudeE6());
                insertHelper.bind(longitudeIndex, result.getLongitudeE6());
                insertHelper.bind(timestampIndex, result.getTimestamp());
                insertHelper.bind(syncedIndex, result.isSynced());
                insertHelper.bind(ownIndex, result.isOwn());
                insertHelper.bind(bssidIndex, result.getBssid());
                insertHelper.bind(ssidIndex, result.getSsid());
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

    public void setSynced(final Collection<MyScanResult> results) {
        // TODO.
    }

    public long getCount() {
        return 0L;
    }

    public long getUniqueBssidCount() {
        return 0L;
    }

    public long getUniqueSsidCount() {
        return 0L;
    }

    public void deleteOlderThan(final String bssid, final long timestamp) {
        // TODO.
    }

    private static MyScanResult read(final Cursor cursor) {
        final MyScanResult result = new MyScanResult();
        result.setId(cursor.getLong(0));
        result.setAccuracy(cursor.getFloat(1));
        result.setLatitudeE6(cursor.getInt(2));
        result.setLongitudeE6(cursor.getInt(3));
        result.setTimestamp(cursor.getLong(4));
        result.setSynced(cursor.getInt(5) != 0);
        result.setOwn(cursor.getInt(6) != 0);
        result.setBssid(cursor.getString(7));
        result.setSsid(cursor.getString(8));
        return result;
    }
}
