package info.eigenein.openwifi.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import org.acra.ACRA;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * Wraps preference manager for convenience.
 */
public class Settings {
    private static final String LOG_TAG = Settings.class.getCanonicalName();

    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    public static final String SHARE_DATABASE_KEY = "share_database";

    public static final String STATISTICS_KEY = "show_statistics";

    public static final String MAX_SCAN_RESULTS_FOR_BSSID_KEY = "max_scan_results_for_bssid";

    public static final String CLIENT_ID_KEY = "client_id";

    public static final String LAST_SYNC_ID_KEY = "last_sync_id";

    public static final String SYNC_NOW_KEY = "sync_now";

    public static final String LAST_SYNC_TIME = "last_sync_date";

    public static final String SYNCING_NOW_KEY = "syncing_now";

    private final SharedPreferences preferences;

    public static Settings with(Context context) {
        return new Settings(context);
    }

    private Settings(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isNetworkProviderEnabled() {
        return preferences.getBoolean(IS_NETWORK_PROVIDER_ENABLED_KEY, false);
    }

    public int maxScanResultsForBssidCount() {
        return Integer.parseInt(preferences.getString(MAX_SCAN_RESULTS_FOR_BSSID_KEY, "4"));
    }

    public long scanPeriod() {
        String periodString = preferences.getString(SCAN_PERIOD_KEY, "60");
        return 1000L * Long.parseLong(periodString);
    }

    public String clientId() {
        String clientId = preferences.getString(CLIENT_ID_KEY, null);
        if (clientId == null) {
            clientId = String.format(
                    "%s-%s-%s",
                    RandomStringUtils.randomNumeric(4),
                    RandomStringUtils.randomNumeric(4),
                    RandomStringUtils.randomNumeric(4));
            Log.i(LOG_TAG, "clientId: " + clientId);
            preferences.edit().putString(CLIENT_ID_KEY, clientId).commit();
        }
        ACRA.getErrorReporter().putCustomData("clientId", clientId);
        return clientId;
    }

    /**
     * Gets the ID of the last synchronized scan result.
     */
    public String lastSyncId() {
        String syncId = preferences.getString(LAST_SYNC_ID_KEY, null);
        if (syncId != null) {
            ACRA.getErrorReporter().putCustomData("lastSyncId", syncId);
            return syncId;
        } else {
            // Return the minimal object ID.
            return "000000000000000000000000";
        }
    }

    /**
     * Gets the last synchronization time.
     */
    public long lastSyncTime() {
        return preferences.getLong(LAST_SYNC_TIME, 0);
    }

    /**
     * Gets whether syncing is performed right now.
     */
    public boolean isSyncingNow() {
        return preferences.getBoolean(SYNCING_NOW_KEY, false);
    }

    /**
     * Starts editing the settings.
     */
    public SettingsEditor edit() {
        return new SettingsEditor(preferences.edit());
    }

    public class SettingsEditor {
        private final SharedPreferences.Editor editor;

        private SettingsEditor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }

        /**
         * Commits the settings changes.
         */
        public void commit() {
            editor.commit();
        }

        /**
         * Sets the ID of the last synchronized scan result.
         */
        public SettingsEditor lastSyncId(String syncId) {
            editor.putString(LAST_SYNC_ID_KEY, syncId);
            ACRA.getErrorReporter().putCustomData("lastSyncId", syncId);
            return this;
        }

        /**
         * Sets the last synchronization time.
         */
        public SettingsEditor lastSyncTime(long syncTime) {
            editor.putLong(LAST_SYNC_TIME, syncTime);
            return this;
        }

        public SettingsEditor syncingNow(boolean syncingNow) {
            editor.putBoolean(SYNCING_NOW_KEY, syncingNow);
            return this;
        }
    }
}
