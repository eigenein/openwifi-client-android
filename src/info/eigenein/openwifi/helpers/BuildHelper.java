package info.eigenein.openwifi.helpers;

import android.os.*;

public class BuildHelper {
    public static boolean isHoneyComb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }
}
