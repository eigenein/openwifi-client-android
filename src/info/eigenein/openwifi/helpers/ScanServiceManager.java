package info.eigenein.openwifi.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import info.eigenein.openwifi.activities.SettingsActivity;
import info.eigenein.openwifi.helpers.location.DefaultLocationListener;
import info.eigenein.openwifi.helpers.location.LocationUpdatesManager;

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
        // Obtain the scan period.
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String periodString = preferences.getString(SettingsActivity.SCAN_PERIOD_KEY, "60");
        long period = 1000L * Long.parseLong(periodString);
        // Schedule the scan.
        PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager(context).setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period,
                period,
                scanPendingIntent);
        // Start location tracking.
        LocationUpdatesManager.requestUpdates(context, DefaultLocationListener.getInstance());
    }

    /**
     * Restarts the scan service if it is already started.
     */
    public static void restartIfStarted(Context context) {
        if (isStarted(context)) {
            Log.i(LOG_TAG, "restartIfStarted");
            restart(context);
        }
    }

    /**
     * Stops the scan service.
     */
    public static void stop(Context context) {
        // Stop location tracking.
        LocationUpdatesManager.removeUpdates(context, DefaultLocationListener.getInstance());
        // Stop pending intent.
        PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
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
    public static boolean isStarted(Context context) {
        return PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    /**
     * Gets the alarm manager instance.
     */
    private static AlarmManager getAlarmManager(Context context) {
        return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }
}
