package info.eigenein.openwifi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import info.eigenein.openwifi.helpers.io.Internet;
import info.eigenein.openwifi.services.SyncIntentService;

/**
 * Monitors network connectivity.
 */
public class ConnectivityChangeBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG =
            ConnectivityChangeBroadcastReceiver.class.getCanonicalName();

    public void onReceive(Context context, Intent intent) {
        // Obtain the current state.
        final NetworkInfo.State state =
                ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();

        final WifiInfo info = getInfo(context);
        if (state == NetworkInfo.State.CONNECTED) {
            // Check if the Internet is actually available.
            new CheckConnectivityAsyncTask(context).execute();
        } else if (info != null) {
            onConnecting(info);
        }
    }

    /**
     * Called when the device is connecting to the Internet.
     */
    private void onConnecting(WifiInfo info) {
        Log.i(LOG_TAG, "onConnecting: " + info.getSSID());
    }

    /**
     * Called when the device is successfully connected to the Internet.
     */
    private void onSucceeded(Context context, WifiInfo info) {
        Log.i(LOG_TAG, "onSucceeded: " + info.getSSID());
        // Starting the synchronization service.
        Log.d(LOG_TAG, "Starting " + SyncIntentService.class.getSimpleName());
        Intent syncServiceIntent = new Intent(context, SyncIntentService.class);
        context.startService(syncServiceIntent);
    }

    /**
     * Called when the device failed to connect to the Internet.
     */
    private void onFailed(WifiInfo info) {
        Log.w(LOG_TAG, "onFailed: " + info.getSSID());
    }

    private WifiInfo getInfo(Context context) {
        return ((WifiManager)context.getSystemService(Context.WIFI_SERVICE))
                .getConnectionInfo();
    }

    private class CheckConnectivityAsyncTask extends AsyncTask<Void, Void, Void> {
        private final Context context;

        public CheckConnectivityAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (Internet.check()) {
                onSucceeded(context, getInfo(context));
            } else {
                onFailed(getInfo(context));
            }
            return null;
        }
    }
}
