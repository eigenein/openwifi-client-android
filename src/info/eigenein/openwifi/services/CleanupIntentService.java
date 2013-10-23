package info.eigenein.openwifi.services;

import android.app.*;
import android.content.*;
import android.net.wifi.*;
import android.text.*;
import android.util.*;
import info.eigenein.openwifi.persistence.*;

import java.util.*;

/**
 * Used to delete old scan results.
 */
public class CleanupIntentService extends IntentService {
    private static final String SERVICE_NAME = CleanupIntentService.class.getCanonicalName();

    private static final String BSSIDS_EXTRA_KEY = "bssids";

    private static final int MAX_SCAN_RESULTS_FOR_BSSID_COUNT = 3;

    public static void queueMyScanResults(final Context context, final Collection<MyScanResult> scanResults) {
        // Get the BSSIDs.
        final HashSet<String> bssids = new HashSet<String>();
        for (final MyScanResult result : scanResults) {
            bssids.add(result.getBssid());
        }
        // Start the service.
        queue(context, bssids);
    }

    public static void queueNativeScanResults(final Context context, final Collection<ScanResult> scanResults) {
        // Get the BSSIDs.
        final HashSet<String> bssids = new HashSet<String>();
        for (final ScanResult result : scanResults) {
            bssids.add(result.BSSID);
        }
        // Start the service.
        queue(context, bssids);
    }

    private static void queue(final Context context, final HashSet<String> bssids) {
        if (bssids.isEmpty()) {
            Log.d(SERVICE_NAME + ".queue", "Queueing nothing.");
            return;
        }

        Log.d(SERVICE_NAME + ".queue", String.format("Queueing %s BSSIDs.", bssids.size()));

        final Intent intent = new Intent(context, CleanupIntentService.class);
        intent.putExtra(BSSIDS_EXTRA_KEY, bssids);
        context.startService(intent);
    }

    public CleanupIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final HashSet<String> bssids = (HashSet<String>)intent.getSerializableExtra(BSSIDS_EXTRA_KEY);
        Log.d(SERVICE_NAME + ".onHandleIntent", TextUtils.join(", ", bssids));
        // Initialize the DAO.
        final MyScanResult.Dao dao = CacheOpenHelper.getInstance(this).getMyScanResultDao();
        // Iterate over the BSSIDs.
        for (final String bssid : bssids) {
            // Get the results for this BSSID.
            final Iterator<MyScanResult> resultIterator = dao.queryNewestByBssid(bssid).iterator();
            // List of results to delete.
            final List<Long> ids = new ArrayList<Long>();
            for (int i = 0; resultIterator.hasNext(); i++) {
                // Move to the next result.
                final MyScanResult scanResult = resultIterator.next();
                // If maximum count is exceeded.
                if (i >= MAX_SCAN_RESULTS_FOR_BSSID_COUNT) {
                    // Add to the delete list.
                    ids.add(scanResult.getId());
                }
            }
            // Delete the results.
            if (!ids.isEmpty()) {
                Log.i(SERVICE_NAME + ".onHandleIntent", String.format(
                        "Deleting %s results for \"%s\".", ids.size(), bssid));
                dao.delete(ids);
            } else {
                Log.d(SERVICE_NAME + ".onHandleIntent", String.format(
                        "Nothing to delete for \"%s\".", bssid
                ));
            }
        }
    }
}
