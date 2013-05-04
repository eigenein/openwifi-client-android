package info.eigenein.openwifi.helpers.scan;

import android.content.Context;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.persistency.DatabaseHelper;
import info.eigenein.openwifi.persistency.MyScanResult;

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
    public static void add(
            Context context,
            Location location,
            List<ScanResult> scanResults,
            boolean own) {
        Log.d(LOG_TAG + ".add", "Storing scan results in the database.");

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);

            for (ScanResult scanResult : scanResults) {
                createScanResult(scanResultDao, scanResult, location, own);
                purgeOldScanResults(
                        scanResultDao,
                        scanResult.BSSID,
                        Settings.with(context).maxScanResultsForBssidCount());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while storing scan results.", e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
                Log.d(LOG_TAG + ".add", "Done.");
            }
        }
    }

    /**
     * Gets the unique BSSID count (that is an access point count).
     */
    public static long getUniqueBssidCount(Context context) {
        return getScanResultDistinctCount(context, MyScanResult.BSSID);
    }

    /**
     * Gets the unique SSID count (that is a network count).
     */
    public static long getUniqueSsidCount(Context context) {
        return getScanResultDistinctCount(context, MyScanResult.SSID);
    }

    /**
     * Gets the scan result count.
     */
    public static long getScanResultCount(Context context) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);
            return scanResultDao.countOf();
        } catch (SQLException e) {
            Log.e(LOG_TAG + ".getScanResultCount", "Error while querying scan result count count.", e);
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
    public static List<MyScanResult> getScanResults(
            Context context,
            double minLatitude,
            double minLongitude,
            double maxLatitude,
            double maxLongitude) {
        Log.d(LOG_TAG + ".getScanResults", String.format("getScanResults %s %s %s %s",
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude));

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);

            final Where<MyScanResult, Long> where = scanResultDao.queryBuilder().where();
            where.gt(MyScanResult.LATITUDE, minLatitude).and()
                    .lt(MyScanResult.LATITUDE, maxLatitude).and()
                    .gt(MyScanResult.LONGITUDE, minLongitude).and()
                    .lt(MyScanResult.LONGITUDE, maxLongitude);

            return where.query();
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
    public static List<MyScanResult> getUnsyncedScanResults(
            Context context,
            int limit) {
        Log.d(LOG_TAG + ".getUnsyncedScanResults", "Getting unsynced scan results ...");

        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);

            final QueryBuilder<MyScanResult, Long> queryBuilder = scanResultDao.queryBuilder();
            final Where<MyScanResult, Long> where = queryBuilder.where();
            where.not().eq(MyScanResult.SYNCED, true);
            queryBuilder.limit(limit);

            return queryBuilder.query();
        } catch (SQLException e) {
            Log.e(LOG_TAG + ".getUnsyncedScanResults", "Error while querying scan results.", e);
            throw new RuntimeException(e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(LOG_TAG + ".getUnsyncedScanResults", "Done.");
        }
    }

    /**
     * Marks the results as synced.
     */
    public static void markAsSynced(Context context, Iterable<MyScanResult> scanResults) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);
            for (MyScanResult scanResult : scanResults) {
                scanResult.setSynced(true);
                scanResultDao.update(scanResult);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating the scan results.", e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(LOG_TAG + ".markAsSynced", "Done.");
        }
    }

    public static void add(Context context, List<MyScanResult> scanResults) {
        final long startTime = System.currentTimeMillis();

        DatabaseHelper databaseHelper = null;
        try {
            // Initialize DAOs.
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);
            for (MyScanResult scanResult : scanResults) {
                Log.d(LOG_TAG + ".add", "Adding the result: " + scanResult);
                // Store the scan result.
                createScanResult(scanResultDao, scanResult);
                // Remove old scan results.
                purgeOldScanResults(
                        scanResultDao,
                        scanResult.getBssid(),
                        Settings.with(context).maxScanResultsForBssidCount());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding the result.", e);
        } finally {
            if (databaseHelper != null) {
                OpenHelperManager.releaseHelper();
                //noinspection UnusedAssignment
                databaseHelper = null;
            }
            Log.d(
                    LOG_TAG + ".add",
                    String.format("Done in %sms.", System.currentTimeMillis() - startTime));
        }
    }

    /**
     * Creates a database helper.
     */
    private static DatabaseHelper getDatabaseHelper(Context context) {
        return OpenHelperManager.getHelper(context, DatabaseHelper.class);
    }

    /**
     * Gets a scan result DAO.
     */
    private static Dao<MyScanResult, Long> getScanResultDao(DatabaseHelper databaseHelper)
            throws SQLException {
        return databaseHelper.getDao(MyScanResult.class);
    }

    /**
     * Creates the scan result if it does not exist.
     */
    private static void createScanResult(
            final Dao<MyScanResult, Long> scanResultDao,
            final MyScanResult scanResult)
            throws SQLException {
        MyScanResult cachedScanResult = null;
        // Avoid own results duplication.
        if (scanResult.isOwn()) {
            Where<MyScanResult, Long> where = scanResultDao.queryBuilder().where();
            cachedScanResult = where.and(
                    where.eq(MyScanResult.BSSID, scanResult.BSSID),
                    where.eq(MyScanResult.TIMESTAMP, scanResult.getTimestamp()))
                    .queryForFirst();
        }
        if (cachedScanResult == null) {
            scanResultDao.create(scanResult);
            Log.d(LOG_TAG + ".createScanResult", "Created the scan result: " + scanResult);
        } else {
            Log.d(LOG_TAG + ".createScanResult", "Not creating the scan result - already exists: " + cachedScanResult);
        }
    }

    /**
     * Creates the scan result if it does not exist.
     */
    private static void createScanResult(
            final Dao<MyScanResult, Long> scanResultDao,
            final ScanResult scanResult,
            final Location location,
            final boolean own)
            throws SQLException {
        final MyScanResult myScanResult = new MyScanResult(scanResult, location);
        myScanResult.setOwn(own);
        createScanResult(scanResultDao, myScanResult);
    }

    /**
     * Gets the count of unique scan results by the specified column.
     */
    private static long getScanResultDistinctCount(Context context, String columnName) {
        DatabaseHelper databaseHelper = null;
        try {
            databaseHelper = getDatabaseHelper(context);
            final Dao<MyScanResult, Long> scanResultDao = getScanResultDao(databaseHelper);
            return Long.parseLong(scanResultDao.queryRaw(
                    "select count(distinct " +columnName + ") from my_scan_results;")
                    .getFirstResult()[0]);
        } catch (SQLException e) {
            throw new RuntimeException("Error while querying unique count.", e);
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
            Dao<MyScanResult, Long> dao,
            String bssid,
            int maxScanResultsForBssidCount)
            throws SQLException {
        // Check parameters.
        if (bssid == null) {
            throw new RuntimeException("The bssid parameter is null.");
        }

        Log.d(LOG_TAG + ".purgeOldScanResults", bssid);
        @SuppressWarnings("deprecation")
        final List<MyScanResult> scanResults = dao.queryBuilder()
                .orderBy(MyScanResult.TIMESTAMP, false)
                .limit(maxScanResultsForBssidCount)
                .where().eq(MyScanResult.BSSID, bssid)
                .query();

        if (scanResults.size() != maxScanResultsForBssidCount) {
            Log.d(LOG_TAG + ".purgeOldScanResults", scanResults.size() + " scan results for " + bssid);
            return;
        }

        // Obtain the timestamp of the oldest result.
        final long lastLocationTimestamp = scanResults.get(maxScanResultsForBssidCount - 1).getTimestamp();
        Log.d(LOG_TAG + ".purgeOldScanResults", "lastLocationTimestamp " + lastLocationTimestamp);
        // Delete all results that are even older.
        final DeleteBuilder<MyScanResult, Long> deleteBuilder = dao.deleteBuilder();
        deleteBuilder.where()
                .eq(MyScanResult.BSSID, bssid)
                .and()
                .lt(MyScanResult.TIMESTAMP, lastLocationTimestamp);
        final int deletedRowsCount = dao.delete(deleteBuilder.prepare());

        Log.d(LOG_TAG + ".purgeOldScanResults", "deleted " + deletedRowsCount + " row(s)");
    }
}
