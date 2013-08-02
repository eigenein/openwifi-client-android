package info.eigenein.openwifi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import info.eigenein.openwifi.helpers.parsers.ScanResultCapabilities;
import info.eigenein.openwifi.helpers.location.CurrentLocationTracker;
import info.eigenein.openwifi.persistence.*;
import info.eigenein.openwifi.services.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Receives a WiFi access point scan results.
 */
public class ScanResultsReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ScanResultsReceiver.class.getCanonicalName();

    /**
     * Maximum allowed accuracy for the location.
     */
    private static final int MAX_ACCURACY = 50;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Check current location.
        final Location location = CurrentLocationTracker.getInstance().getLocation(context);
        if (location == null) {
            Log.w(LOG_TAG, "getLocation returned null.");
            return;
        }
        if (location.getAccuracy() > MAX_ACCURACY) {
            Log.w(LOG_TAG, "extra large accuracy " + location.getAccuracy());
            return;
        }

        // Obtain scan results.
        final WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        final List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults == null) {
            Log.w(LOG_TAG, "No scan results.");
            return;
        }

        // Reset alarm in case the scan results were not requested.
        ScanIntentService.restartIfStarted(context);

        Log.i(LOG_TAG, "An access point scan has completed.");
        // Filter out open access points.
        final List<ScanResult> openScanResults = new ArrayList<ScanResult>();
        for (ScanResult scanResult : scanResults) {
            final ScanResultCapabilities capabilities = ScanResultCapabilities.fromString(
                    scanResult.capabilities);
            if (!capabilities.isSecured()) {
                openScanResults.add(scanResult);
                Log.d(LOG_TAG, scanResult.toString());
            }
        }

        if (openScanResults.size() != 0) {
            // Add all these scan results.
            final MyScanResultDao dao = CacheOpenHelper.getInstance(context).getMyScanResultDao();
            dao.insert(location, openScanResults, false, true);
            // Start the cleanup service.
            CleanupIntentService.queueNativeScanResults(context, openScanResults);
        } else {
            Log.d(LOG_TAG, "No open access points here.");
        }
        Log.i(LOG_TAG, "Done processing scan results.");
    }
}
