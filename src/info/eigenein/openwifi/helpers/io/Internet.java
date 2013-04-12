package info.eigenein.openwifi.helpers.io;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Internet {
    private static final String LOG_TAG = Internet.class.getCanonicalName();

    public static boolean check() {
        try {
            InetAddress.getByName("google.com").isReachable(1000);
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
