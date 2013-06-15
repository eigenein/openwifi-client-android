package info.eigenein.openwifi.helpers.internal;

import android.content.*;
import android.preference.*;
import android.util.*;
import info.eigenein.openwifi.enums.*;
import org.acra.*;

/**
 * Wraps preference manager for convenience.
 */
public class Settings {
    private static final String LOG_TAG = Settings.class.getCanonicalName();

    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    public static final String STATISTICS_KEY = "show_statistics";

    public static final String MAX_SCAN_RESULTS_FOR_BSSID_KEY = "max_scan_results_for_bssid";

    public static final String CLIENT_ID_KEY = "client_id";

    public static final String LAST_SYNC_ID_KEY = "last_sync_id";

    public static final String SYNC_STATUS_KEY = "syncStatus";

    public static final String SYNC_NOW_KEY = "sync_now";

    public static final String LAST_SYNC_TIME = "last_sync_date";

    public static final String LOG_IN_KEY = "log_in";

    public static final String IS_HELP_SHOWN_KEY = "is_help_shown";

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

    public int maxScanResultsForBssidCount() {
        return Integer.parseInt(preferences.getString(MAX_SCAN_RESULTS_FOR_BSSID_KEY, "4"));
    }

    public long scanPeriod() {
        final String periodString = preferences.getString(SCAN_PERIOD_KEY, "60");
        return 1000L * Long.parseLong(periodString);
    }

    public String clientId() {
        String clientId = preferences.getString(CLIENT_ID_KEY, null);
        if (clientId == null) {
            clientId = String.format(
                    "%s-%s-%s",
                    RandomHelper.randomNumeric(4),
                    RandomHelper.randomNumeric(4),
                    RandomHelper.randomNumeric(4));
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

    public SyncIntentServiceStatus syncStatus() {
        return SyncIntentServiceStatus.valueOf(
                preferences.getString(SYNC_STATUS_KEY, SyncIntentServiceStatus.NOT_SYNCING.toString()));
    }

    public boolean isHelpShown() {
        return preferences.getBoolean(IS_HELP_SHOWN_KEY, false);
    }

    /**
     * Starts editing the settings.
     */
    public SettingsEditor edit() {
        return new SettingsEditor(preferences.edit());
    }

    public class SettingsEditor {
        private final SharedPreferences.Editor editor;

        private SettingsEditor(final SharedPreferences.Editor editor) {
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
        public SettingsEditor lastSyncId(final String syncId) {
            editor.putString(LAST_SYNC_ID_KEY, syncId);
            ACRA.getErrorReporter().putCustomData("lastSyncId", syncId);
            return this;
        }

        /**
         * Sets the last synchronization time.
         */
        public SettingsEditor lastSyncTime(final long syncTime) {
            editor.putLong(LAST_SYNC_TIME, syncTime);
            return this;
        }

        public SettingsEditor syncStatus(final SyncIntentServiceStatus status) {
            editor.putString(SYNC_STATUS_KEY, status.toString());
            return this;
        }

        public SettingsEditor helpShown(final boolean helpShown) {
            editor.putBoolean(IS_HELP_SHOWN_KEY, helpShown);
            return this;
        }
    }
}
