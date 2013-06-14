package info.eigenein.openwifi.helpers.internal;

import android.os.*;

public class BuildHelper {
    public static boolean isHoneyComb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean isFroyo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }
}
