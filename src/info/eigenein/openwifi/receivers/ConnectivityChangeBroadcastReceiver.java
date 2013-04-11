package info.eigenein.openwifi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Monitors network connectivity.
 */
public class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ConnectivityChangeBroadcastReceiver.class.getCanonicalName();

    public void onReceive(Context context, Intent intent) {
        boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
        String reason = intent.getStringExtra(ConnectivityManager.EXTRA_REASON);
        NetworkInfo[] networkInfos =
                ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getAllNetworkInfo();

        Log.d(LOG_TAG, "onReceive");
        Log.d(LOG_TAG, "noConnectivity: " + noConnectivity);
        Log.d(LOG_TAG, "reason: " + reason);
        for (NetworkInfo networkInfo : networkInfos) {
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Log.d(LOG_TAG, "networkInfo: " + networkInfo);
            }
        }
    }
}
