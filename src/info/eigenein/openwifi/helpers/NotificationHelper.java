package info.eigenein.openwifi.helpers;

import android.app.*;
import android.content.*;
import android.provider.Settings;
import android.support.v4.app.*;
import info.eigenein.openwifi.*;

public class NotificationHelper {

    private static class NotificationId {
        public static final int WI_FI_IS_NOT_ENABLED = 0;
        public static final int SYNCING = 1;
    }

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Shows notification that Wi-Fi is disabled and allows the user
     * to enable Wi-Fi through the Wi-Fi settings intent.
     */
    public static void notifyWiFiIsNotEnabled(final Context context) {
        final PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, new Intent(Settings.ACTION_WIFI_SETTINGS), 0);

        final String title = context.getString(R.string.notification_title_wifi_is_disabled);
        final String text = context.getString(R.string.notification_text_wifi_is_disabled);

        final Notification notification = new NotificationCompat.Builder(context)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setContentText(text)
                .setContentTitle(title)
                .setDefaults(Notification.DEFAULT_ALL)
                .setOnlyAlertOnce(true)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setTicker(title + LINE_SEPARATOR + text)
                .build();

        getNotificationManager(context).notify(NotificationId.WI_FI_IS_NOT_ENABLED, notification);
    }

    /**
     * Gets the notification manager.
     */
    private static NotificationManager getNotificationManager(final Context context) {
        return (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
