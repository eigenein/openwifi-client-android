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

import java.util.concurrent.TimeUnit;

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

        if (state == NetworkInfo.State.CONNECTED) {
            // Check if the Internet is actually available.
            new CheckConnectivityAsyncTask(context).execute();
        } else if (state == NetworkInfo.State.CONNECTING) {
            onConnecting(getInfo(context));
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
    private void onSucceeded(WifiInfo info) {
        Log.i(LOG_TAG, "onSucceeded: " + info.getSSID());
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
                onSucceeded(getInfo(context));
            } else {
                onFailed(getInfo(context));
            }
            return null;
        }
    }
}
