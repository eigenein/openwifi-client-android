package info.eigenein.openwifi.helpers.scan;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.helpers.location.*;

/**
 * Manages the background WiFi scan service.
 */
public class ScanServiceManager {
    private static final String LOG_TAG = ScanServiceManager.class.getCanonicalName();

    private static final Intent scanServiceIntent =
            new Intent("info.eigenein.intents.SCAN_INTENT");

    /**
     * Starts or updates the scan service.
     */
    public static void restart(Context context) {
        // Stop the scan service.
        stop(context);
        // Schedule the scan.
        final long period = Settings.with(context).scanPeriod();
        final PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager(context).setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period,
                Settings.with(context).scanPeriod(),
                scanPendingIntent);
        // Start location tracking.
        LocationUpdatesManager.requestUpdates(context, DefaultLocationListener.getInstance());
    }

    /**
     * Restarts the scan service if it is already started.
     */
    public static void restartIfStarted(final Context context) {
        if (isStarted(context)) {
            Log.i(LOG_TAG, "restartIfStarted");
            restart(context);
        }
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

    /**
     * Gets the alarm manager instance.
     */
    private static AlarmManager getAlarmManager(final Context context) {
        return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }
}
