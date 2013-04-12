package info.eigenein.openwifi.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import info.eigenein.openwifi.activities.SettingsActivity;

/**
 * Wraps preference manager for convenience.
 */
public class Settings {
    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    public static final String SHARE_DATABASE_KEY = "share_database";

    public static final String STATISTICS_KEY = "show_statistics";

    public static final String MAX_SCAN_RESULTS_FOR_BSSID_KEY = "max_scan_results_for_bssid";

    private final SharedPreferences preferences;

    public static Settings with(Context context) {
        return new Settings(PreferenceManager.getDefaultSharedPreferences(context));
    }

    private Settings(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    public boolean isNetworkProviderEnabled() {
        return preferences.getBoolean(IS_NETWORK_PROVIDER_ENABLED_KEY, false);
    }

    public int maxScanResultsForBssidCount() {
        return Integer.parseInt(preferences.getString(MAX_SCAN_RESULTS_FOR_BSSID_KEY, null));
    }

    public long scanPeriod() {
        String periodString = preferences.getString(SCAN_PERIOD_KEY, "60");
        return 1000L * Long.parseLong(periodString);
    }
}
