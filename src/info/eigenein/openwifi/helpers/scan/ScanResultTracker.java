package info.eigenein.openwifi.helpers.scan;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.persistency.DatabaseHelper;
import info.eigenein.openwifi.persistency.entities.StoredLocation;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;

import java.sql.SQLException;
import java.util.List;

/**
 * Tracks an access point scan results in the application database.
 */
public class ScanResultTracker {
    private static final String LOG_TAG = ScanResultTracker.class.getCanonicalName();

    /**
     * Adds the scan results to the database.
     */
    public static void add(Context context, Location location, List<ScanResult> scanResults) {
        Log.d(LOG_TAG, "Storing scan results in the database.");

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);

            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
            Dao<StoredLocation, Long> locationDao = getLocationDao(databaseHelper);

            StoredLocation storedLocation = createLocation(locationDao, location);
            for (ScanResult scanResult : scanResults) {
                createScanResult(scanResultDao, scanResult, storedLocation);
                purgeOldScanResults(
                        scanResultDao,
                        scanResult.BSSID,
                        Settings.with(context).maxScanResultsForBssidCount());
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while storing scan results.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
                Log.d(LOG_TAG, "Done.");
            }
        }
    }

    /**
     * Gets the unique BSSID count (that is an access point count).
     */
    public static long getUniqueBssidCount(Context context) {
        return getScanResultDistinctCount(context, StoredScanResult.BSSID);
    }

    /**
     * Gets the unique SSID count (that is a network count).
     */
    public static long getUniqueSsidCount(Context context) {
        return getScanResultDistinctCount(context, StoredScanResult.SSID);
    }

    /**
     * Gets the scan result count.
     */
    public static long getScanResultCount(Context context) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
            return scanResultDao.countOf();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while querying scan result count count.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
        }
    }

    /**
     * Gets the stored scan results in the specified area.
     */
    public static List<StoredScanResult> getScanResults(
            Context context,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude) {
        Log.d(LOG_TAG, String.format("getScanResults %s %s %s %s",
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude));

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);

            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);

            final String query =
                    "select sr1.bssid, sr1.ssid, loc.accuracy, loc.latitude, loc.longitude \n" +
                    "from scan_results sr1\n" +
                    "join locations loc\n" +
                    "on loc.id = sr1.location_id\n" +
                    "where loc.latitude >= ? and loc.latitude <= ?\n" +
                    "and loc.longitude >= ? and loc.longitude <= ?\n" +
                    "order by sr1.bssid, sr1.location_timestamp desc;";
            return scanResultDao.queryRaw(
                    query,
                    GetScanResultsRawRowMapper.getInstanceWithoutId(),
                    Double.toString(minLatitude),
                    Double.toString(maxLatitude),
                    Double.toString(minLongitude),
                    Double.toString(maxLongitude))
                    .getResults();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while querying scan results.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
        }
    }

    /**
     * Gets the unsynchronized scan result list.
     */
    public static List<StoredScanResult> getUnsyncedScanResults(
            Context context,
            int limit) {
        Log.d(LOG_TAG, "Getting unsynced scan results ...");

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);

            final String query =
                    "select sr1.bssid, sr1.ssid, loc.accuracy, loc.latitude, loc.longitude, loc.timestamp, sr1.id\n" +
                    "from scan_results sr1\n" +
                    "join locations loc\n" +
                    "on loc.id = sr1.location_id\n" +
                    "where not sr1.synced\n" +
                    "order by sr1.location_timestamp\n" +
                    "limit ?;";
            return scanResultDao.queryRaw(
                    query,
                    // Because we add timestamp and id here.
                    GetScanResultsRawRowMapper.getInstanceWithId(),
                    Integer.toString(limit))
                    .getResults();
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while querying scan results.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(LOG_TAG, "Done.");
        }
    }

    /**
     * Marks the results as synced.
     */
    public static void markAsSynced(Context context, Iterable<StoredScanResult> scanResults) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
            final String query = "update scan_results set synced = 1 where id = ?;";
            for (StoredScanResult scanResult: scanResults) {
                scanResultDao.queryRaw(query, Integer.toString(scanResult.getId()));
            }
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while updating the scan results.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(LOG_TAG, "Done.");
        }
    }

    public static void add(Context context, StoredScanResult scanResult) {
        Log.d(LOG_TAG, "Adding the result: " + scanResult);
        final long startTime = System.currentTimeMillis();

        DatabaseHelper databaseHelper = null;
        try {
            // Initialize DAOs.
            databaseHelper = getDatabaseHelper(context);
            final Dao<StoredLocation, Long> locationDao = getLocationDao(databaseHelper);
            createLocation(locationDao, scanResult.getLocation());
            final Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
            // Store the scan result.
            createScanResult(scanResultDao, scanResult);
            // Remove old scan results.
            purgeOldScanResults(
                    scanResultDao,
                    scanResult.getBssid(),
                    Settings.with(context).maxScanResultsForBssidCount());
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while adding the result.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(LOG_TAG, String.format("Done in %sms.", System.currentTimeMillis() - startTime));
        }
    }

    /**
     * Creates a database helper.
     */
    private static DatabaseHelper getDatabaseHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    /**
     * Gets a stored scan result DAO.
     */
    private static Dao<StoredScanResult, Integer> getScanResultDao(DatabaseHelper databaseHelper)
            throws SQLException {
        return databaseHelper.getDao(StoredScanResult.class);
    }

    /**
     * Gets a location DAO.
     */
    private static Dao<StoredLocation, Long> getLocationDao(DatabaseHelper databaseHelper)
            throws SQLException {
        return databaseHelper.getDao(StoredLocation.class);
    }

    /**
     * Creates the scan result if it does not exist.
     */
    private static void createScanResult(
            final Dao<StoredScanResult, Integer> scanResultDao,
            final StoredScanResult scanResult)
            throws SQLException {
        final StoredLocation storedLocation = scanResult.getLocation();
        StoredScanResult cachedScanResult = null;
        // Avoid own results duplication.
        if (storedLocation.isOwn()) {
            Where<StoredScanResult, Integer> where = scanResultDao.queryBuilder().where();
            cachedScanResult = where.and(
                    where.eq(StoredScanResult.BSSID, scanResult.BSSID),
                    where.eq(StoredScanResult.LOCATION_TIMESTAMP, storedLocation.getTimestamp()))
                    .queryForFirst();
        }
        if (cachedScanResult == null) {
            scanResultDao.create(scanResult);
            Log.d(LOG_TAG, "Created the scan result: " + scanResult);
        } else {
            Log.d(LOG_TAG, "Not creating the scan result - already exists: " + cachedScanResult);
        }
    }

    /**
     * Creates the scan result if it does not exist.
     */
    private static void createScanResult(
            final Dao<StoredScanResult, Integer> scanResultDao,
            final ScanResult scanResult,
            final StoredLocation storedLocation)
            throws SQLException {
        createScanResult(scanResultDao, new StoredScanResult(scanResult, storedLocation));
    }

    /**
     * Creates the location if it does not exist.
     */
    private static StoredLocation createLocation(
            final Dao<StoredLocation, Long> locationDao,
            final StoredLocation location) throws SQLException {
        StoredLocation cachedLocation = null;
        // Avoid own location duplication.
        if (location.isOwn()) {
            Where<StoredLocation, Long> where = locationDao.queryBuilder().where();
            where.eq("timestamp", location.getTimestamp());
            cachedLocation = where.queryForFirst();
        }
        if (cachedLocation == null) {
            locationDao.create(location);
            Log.d(LOG_TAG, "Created the location: " + location);
            return location;
        } else {
            Log.d(LOG_TAG, "Not creating the location - already exists: " + cachedLocation);
            return cachedLocation;
        }
    }

    /**
     * Creates the location if it does not exist.
     */
    private static StoredLocation createLocation(
            Dao<StoredLocation, Long> locationDao,
            Location location) throws SQLException {
        return createLocation(locationDao, new StoredLocation(location));
    }

    /**
     * Gets the count of unique scan results by the specified column.
     */
    private static long getScanResultDistinctCount(Context context, String columnName) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            Dao<StoredScanResult, Integer> scanResultDao = getScanResultDao(databaseHelper);
            return Long.parseLong(scanResultDao.queryRaw(
                    "select count(distinct " +columnName + ") from scan_results;")
                    .getFirstResult()[0]);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Error while querying unique count.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
        }
    }

    /**
     * Deletes old scan results for the specified BSSID.
     */
    private static void purgeOldScanResults(
            Dao<StoredScanResult, Integer> dao,
            String bssid,
            int maxScanResultsForBssidCount)
            throws SQLException {
        // Check parameters.
        if (bssid == null) {
            throw new RuntimeException("The bssid parameter is null.");
        }

        Log.d(LOG_TAG + ".purgeOldScanResults", bssid);
        @SuppressWarnings("deprecation")
        List<StoredScanResult> scanResults = dao.queryBuilder()
                .orderBy(StoredScanResult.LOCATION_TIMESTAMP, false)
                .limit(maxScanResultsForBssidCount)
                .where().eq(StoredScanResult.BSSID, bssid)
                .query();

        if (scanResults.size() != maxScanResultsForBssidCount) {
            Log.d(LOG_TAG + ".purgeOldScanResults", scanResults.size() + " scan results for " + bssid);
            return;
        }

        // Obtain the timestamp of the oldest result.
        long lastLocationTimestamp = scanResults.get(maxScanResultsForBssidCount - 1)
                .getLocation()
                .getTimestamp();
        Log.d(LOG_TAG + ".purgeOldScanResults", "lastLocationTimestamp " + lastLocationTimestamp);
        // Delete all results that are even older.
        DeleteBuilder<StoredScanResult, Integer> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where()
                .eq(StoredScanResult.BSSID, bssid)
                .and()
                .lt(StoredScanResult.LOCATION_TIMESTAMP, lastLocationTimestamp);
        int deletedRowsCount = dao.delete(deleteBuilder.prepare());

        Log.d(LOG_TAG + ".purgeOldScanResults", "deleted " + deletedRowsCount + " row(s)");
    }

    private static class GetScanResultsRawRowMapper implements RawRowMapper<StoredScanResult> {
        private static final GetScanResultsRawRowMapper instanceWithoutId =
                new GetScanResultsRawRowMapper(false);
        private static final GetScanResultsRawRowMapper instanceWithId =
                new GetScanResultsRawRowMapper(true);

        public static GetScanResultsRawRowMapper getInstanceWithoutId() {
            return instanceWithoutId;
        }
        public static GetScanResultsRawRowMapper getInstanceWithId() {
            return instanceWithId;
        }

        private final boolean withId;

        public GetScanResultsRawRowMapper(boolean withId) {
            this.withId = withId;
        }

        @Override
        public StoredScanResult mapRow(String[] columnNames, String[] resultColumns)
                throws SQLException {
            StoredLocation storedLocation = new StoredLocation();
            StoredScanResult storedScanResult = new StoredScanResult();

            storedScanResult.setLocation(storedLocation);

            storedScanResult.setBssid(resultColumns[0]);
            storedScanResult.setSsid(resultColumns[1]);
            storedLocation.setAccuracy(Float.parseFloat(resultColumns[2]));
            storedLocation.setLatitude(Double.parseDouble(resultColumns[3]));
            storedLocation.setLongitude(Double.parseDouble(resultColumns[4]));
            if (withId) {
                storedLocation.setTimestamp(Long.parseLong(resultColumns[5]));
                storedScanResult.setId(Integer.parseInt(resultColumns[6]));
            }

            return storedScanResult;
        }
    }
}
