package info.eigenein.openwifi.helpers.ui;

import android.content.Context;
import android.os.Vibrator;

public class VibratorHelper {
    private static long VIBRATE_MILLISECONDS = 25L;

    public static void vibrate(Context context) {
        final Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATE_MILLISECONDS);
    }
}
