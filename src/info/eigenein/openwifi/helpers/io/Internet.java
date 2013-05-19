package info.eigenein.openwifi.helpers.io;

import android.util.Log;

import java.io.IOException;
import java.net.*;

public class Internet {
    private static final String LOG_TAG = Internet.class.getCanonicalName();

    private static final int CONNECT_TIMEOUT = 1000;

    private static final String[] urls = new String[] {
            // google.by
            "http://173.194.40.83",
            // vk.com
            "http://87.240.143.244",
            // tut.by
            "http://178.124.133.66",
    };

    public static boolean check() {
        for (final String url : urls) {
            Log.d(LOG_TAG, "Trying " + url);
            try {
                final HttpURLConnection connection = (HttpURLConnection)new URL(url).openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.connect();
                Log.i(LOG_TAG, "Successfully connected.");
                return true;
            } catch (MalformedURLException e) {
                Log.w(LOG_TAG, "Malformed URL error.");
            } catch (IOException e) {
                Log.w(LOG_TAG, "I/O error.");
            }
        }
        Log.w(LOG_TAG, "Failed to connect.");
        return false;
    }
}
