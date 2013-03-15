package info.eigenein.openwifi.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import info.eigenein.openwifi.services.ScanIntentService;

/**
 * Receives scan intents and runs the scan service.
 */
public class ScanIntentBroadcastReceiver extends BroadcastReceiver {
    private static final String LOG_TAG = ScanIntentBroadcastReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG, "onReceive");

        Intent scanServiceIntent = new Intent(context, ScanIntentService.class);
        context.startService(scanServiceIntent);
    }
}
