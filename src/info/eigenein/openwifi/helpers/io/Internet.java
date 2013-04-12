package info.eigenein.openwifi.helpers.io;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Internet {
    private static final String LOG_TAG = Internet.class.getCanonicalName();

    private static final int IS_REACHABLE_TIMEOUT = 1000;

    public static boolean check() {
        try {
            InetAddress.getByName("google.com").isReachable(IS_REACHABLE_TIMEOUT);
            Log.i(LOG_TAG, "Success.");
            return true;
        } catch (UnknownHostException e) {
            Log.w(LOG_TAG, "Unknown host error.");
            return false;
        } catch (IOException e) {
            Log.w(LOG_TAG, "I/O error.");
            return false;
        }
    }
}
