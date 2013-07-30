package info.eigenein.openwifi.services;

import android.app.*;
import android.content.*;
import android.net.wifi.*;
import android.os.*;
import android.util.Log;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.helpers.location.*;
import info.eigenein.openwifi.helpers.ui.*;

/**
 * Background service that runs a WiFi access point scan.
 */
public class ScanIntentService extends IntentService {

    private static final String SERVICE_NAME = ScanIntentService.class.getCanonicalName();

    private static final Intent scanServiceIntent = new Intent("info.eigenein.intents.SCAN_INTENT");

    public ScanIntentService() {
        super(SERVICE_NAME);
    }

    /**
     * Starts or updates the scan service.
     */
    public static void restart(Context context) {
        // Stop the scan service.
        stop(context);
        // Schedule the scan.
        final long period = info.eigenein.openwifi.helpers.internal.Settings.with(context).scanPeriod();
        final PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager(context).setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period,
                info.eigenein.openwifi.helpers.internal.Settings.with(context).scanPeriod(),
                scanPendingIntent);
        // Start location tracking.
        LocationUpdatesManager.requestUpdates(context, DefaultLocationListener.getInstance());
        // Google Analytics.
        EasyTracker.getInstance().setContext(context);
        EasyTracker.getTracker().sendEvent(SERVICE_NAME, "service", "restart", 0L);
    }

    /**
     * Restarts the scan service if it is already started.
     */
    public static void restartIfStarted(final Context context) {
        if (isStarted(context)) {
            Log.i(SERVICE_NAME, "restartIfStarted");
            restart(context);
        }
        // Google Analytics.
        EasyTracker.getInstance().setContext(context);
        EasyTracker.getTracker().sendEvent(SERVICE_NAME, "service", "restartIfStarted", 0L);
    }

    /**
     * Stops the scan service.
     */
    public static void stop(final Context context) {
        // Stop location tracking.
        LocationUpdatesManager.removeUpdates(context, DefaultLocationListener.getInstance());
        // Stop pending intent.
        final PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        if (scanPendingIntent != null) {
            getAlarmManager(context).cancel(scanPendingIntent);
            scanPendingIntent.cancel();
        }
        // Google Analytics.
        EasyTracker.getInstance().setContext(context);
        EasyTracker.getTracker().sendEvent(SERVICE_NAME, "service", "stop", 0L);
    }

    /**
     * Gets whether the scan service is started.
     */
    public static boolean isStarted(final Context context) {
        return PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Log.i(SERVICE_NAME, "WiFi is not enabled.");
            NotificationHelper.notifyWiFiIsNotEnabled(this);
            return;
        }

        if (!wifiManager.startScan()) {
            Log.w(SERVICE_NAME, "wifiManager.startScan() returned false.");
        } else {
            Log.i(SERVICE_NAME, "The scan was initiated.");
        }
    }

    /**
     * Gets the alarm manager instance.
     */
    private static AlarmManager getAlarmManager(final Context context) {
        return (AlarmManager)context.getSystemService(ALARM_SERVICE);
    }
}
