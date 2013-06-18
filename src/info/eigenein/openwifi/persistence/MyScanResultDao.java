package info.eigenein.openwifi.persistence;

import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.location.*;
import android.net.wifi.*;
import android.util.*;
import info.eigenein.openwifi.helpers.location.*;

import java.util.*;

public class MyScanResultDao extends BaseDao {
    private static final String LOG_TAG = MyScanResultDao.class.getCanonicalName();

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
        // Run query.
        final Cursor cursor = database.rawQuery(
                "SELECT id, accuracy, latitude, longitude, timestamp, synced, own, bssid, ssid " +
                "FROM my_scan_results " +
                "WHERE (latitude BETWEEN ? AND ?) AND (longitude BETWEEN ? AND ?);",
                new String[] {
                        Integer.toString(L.toE6(minLatitude)),
                        Integer.toString(L.toE6(maxLatitude)),
                        Integer.toString(L.toE6(minLongitude)),
                        Integer.toString(L.toE6(maxLongitude))
                }
        );
        Log.d(LOG_TAG + ".queryByLocation", String.format("cursor.getCount() = %s", cursor.getCount()));
        final Collection<MyScanResult> results;
        try {
            // Initialize the collection.
            results = new ArrayList<MyScanResult>();
            // Read results.
            while (cursor.moveToNext()) {
                results.add(read(cursor));
            }
        } finally {
            cursor.close();
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
        // Insert the results.
        final long startTimeMillis = System.currentTimeMillis();
        for (final MyScanResult result : results) {
            final ContentValues values = new ContentValues();
            // Put the values.
            values.put("accuracy", result.getAccuracy());
            values.put("latitude", result.getLatitudeE6());
            values.put("longitude", result.getLongitudeE6());
            values.put("timestamp", result.getTimestamp());
            values.put("synced", result.isSynced());
            values.put("own", result.isOwn());
            values.put("bssid", result.getBssid());
            values.put("ssid", result.getSsid());
            // Insert the result.
            database.insert("my_scan_results", null, values);
        }
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
