package info.eigenein.openwifi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import info.eigenein.openwifi.helpers.ScanResultTracker;
import info.eigenein.openwifi.helpers.parsers.ScanResultCapabilities;
import info.eigenein.openwifi.helpers.ScanServiceManager;
import info.eigenein.openwifi.helpers.location.LocationTracker;

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
    private static final int MAX_ACCURACY = 70;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check current location.
        Location location = LocationTracker.getInstance().getLocation(context);
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
        List<ScanResult> scanResults = wifiManager.getScanResults();
        if (scanResults == null) {
            Log.w(LOG_TAG, "No scan results.");
            return;
        }

        // Reset alarm in case the scan results were not requested.
        ScanServiceManager.restartIfStarted(context);

        Log.i(LOG_TAG, "An access point scan has completed.");
        // Filter out open access points.
        List<ScanResult> openScanResults = new ArrayList<ScanResult>();
        for (ScanResult scanResult : scanResults) {
            ScanResultCapabilities capabilities = ScanResultCapabilities.fromString(
                    scanResult.capabilities);
            if (!capabilities.isSecured()) {
                openScanResults.add(scanResult);
                Log.d(LOG_TAG, scanResult.toString());
            }
        }

        if (openScanResults.size() != 0) {
            // Finally, add all these scan results.
            ScanResultTracker.add(context, location, openScanResults);
        } else {
            Log.d(LOG_TAG, "No open access points here.");
        }
        Log.i(LOG_TAG, "Done processing scan results.");
    }

    public void cleanup() {

    }
}
