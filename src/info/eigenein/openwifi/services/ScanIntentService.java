package info.eigenein.openwifi.services;

import android.app.*;
import android.content.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.*;
import android.support.v4.app.*;
import android.util.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.location.*;

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
        final long period = info.eigenein.openwifi.helpers.Settings.with(context).scanPeriod();
        final PendingIntent scanPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                scanServiceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        getAlarmManager(context).setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period,
                info.eigenein.openwifi.helpers.Settings.with(context).scanPeriod(),
                scanPendingIntent);
        // Start location tracking.
        LocationUpdatesManager.requestUpdates(context, DefaultLocationListener.getInstance());
    }

    /**
     * Restarts the scan service if it is already started.
     */
    public static void restartIfStarted(final Context context) {
        if (isStarted(context)) {
            Log.i(SERVICE_NAME, "restartIfStarted");
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

    @Override
    protected void onHandleIntent(final Intent intent) {
        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Log.i(SERVICE_NAME, "WiFi is not enabled.");
            notifyWiFiIsNotEnabled();
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

    /**
     * Shows notification that Wi-Fi is disabled and allows the user
     * to enable Wi-Fi through WiFi settings intent.
     */
    private void notifyWiFiIsNotEnabled() {
        final NotificationManager notificationManager = (NotificationManager)getSystemService(
                NOTIFICATION_SERVICE);
        PendingIntent wifiSettingsPendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(Settings.ACTION_WIFI_SETTINGS),
                0
        );

        final String wifi_is_disabled_title = getString(R.string.wifi_is_disabled_title);
        final String wifi_is_disabled_text = getString(R.string.wifi_is_disabled_text);
        final Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setTicker(wifi_is_disabled_title +
                                System.getProperty("line.separator") +
                                wifi_is_disabled_text)
                .setContentText(wifi_is_disabled_text)
                .setContentIntent(wifiSettingsPendingIntent)
                .setContentTitle(wifi_is_disabled_title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .build();

        notificationManager.notify(0, notification);
    }
}
