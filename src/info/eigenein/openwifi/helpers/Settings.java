package info.eigenein.openwifi.helpers;

import android.content.*;
import android.preference.*;

/**
 * Wraps preference manager for convenience.
 */
public class Settings {
    private static final String LOG_TAG = Settings.class.getCanonicalName();

    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    public static final String STATISTICS_KEY = "show_statistics";

    private final SharedPreferences preferences;

    public static Settings with(final Context context) {
        return new Settings(context);
    }

    private Settings(final Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isNetworkProviderEnabled() {
        return preferences.getBoolean(IS_NETWORK_PROVIDER_ENABLED_KEY, false);
    }

    public long scanPeriod() {
        final String periodString = preferences.getString(SCAN_PERIOD_KEY, "60");
        return 1000L * Long.parseLong(periodString);
    }
}
