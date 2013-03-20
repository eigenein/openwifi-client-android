package info.eigenein.openwifi.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import info.eigenein.openwifi.R;

/**
 * Background service that runs a WiFi access point scan.
 */
public class ScanIntentService extends IntentService {
    public static final String SERVICE_NAME = ScanIntentService.class.getCanonicalName();

    private static final String LOG_TAG = ScanIntentService.class.getCanonicalName();

    public ScanIntentService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Log.i(LOG_TAG, "WiFi is not enabled.");
            notifyWiFiIsNotEnabled();
            return;
        }

        if (!wifiManager.startScan()) {
            Log.w(LOG_TAG, "wifiManager.startScan() returned false.");
        } else {
            Log.i(LOG_TAG, "The scan was initiated.");
        }
    }

    /**
     * Shows notification that WiFi is disabled and allows the user
     * to enable WiFi through WiFi settings intent.
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

        String wifi_is_disabled_title = getString(R.string.wifi_is_disabled_title);
        String wifi_is_disabled_text = getString(R.string.wifi_is_disabled_text);
        Notification notification = new NotificationCompat.Builder(this)
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
