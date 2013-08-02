package info.eigenein.openwifi.activities;

import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.support.v4.content.*;
import android.view.*;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.enums.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.services.*;

import java.text.*;
import java.util.*;

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getCanonicalName();

    private final Authenticator.AuthenticatedHandler authenticatedHandler = new Authenticator.AuthenticatedHandler() {
        @SuppressWarnings("deprecation")
        @Override
        public void onAuthenticated(
                final AuthenticationStatus status,
                final String authToken,
                final String accountName) {
            assert(authToken == null || accountName != null);

            final Preference logInPreference = findPreference(Settings.LOG_IN_KEY);

            EasyTracker.getInstance().setContext(SettingsActivity.this);
            if (status == AuthenticationStatus.AUTHENTICATED) {
                logInPreference.setTitle(R.string.preference_sign_in_again);
                logInPreference.setSummary(accountName);
                EasyTracker.getTracker().sendEvent(LOG_TAG, "onAuthenticated", "success", 1L);
            } else if (status == AuthenticationStatus.ERROR) {
                logInPreference.setSummary(R.string.preference_sign_in_error);
            } else {
                logInPreference.setTitle(R.string.preference_sign_in);
                logInPreference.setSummary(R.string.preference_sign_in_summary);
                EasyTracker.getTracker().sendEvent(LOG_TAG, "onAuthenticated", "null", 0L);
            }
        }
    };


    @SuppressWarnings("deprecation")
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildHelper.isHoneyComb()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.preferences);

        // Statistics option.
        final Preference statisticsPreference = findPreference(Settings.STATISTICS_KEY);
        statisticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsActivity.this, StatisticsActivity.class));
                return true;
            }
        });

        // Sync now option.
        final Preference syncNowPreference = findPreference(Settings.SYNC_NOW_KEY);
        syncNowPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // Start the service.
                SyncIntentService.start(SettingsActivity.this, false);
                // Done.
                return true;
            }
        });
        updateSyncNowPreference();

        // The log in option.
        final Preference logInPreference = findPreference(Settings.LOG_IN_KEY);
        logInPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(final Preference preference) {
                // Authenticate.
                Authenticator.authenticate(SettingsActivity.this, true, false, true, true, true, authenticatedHandler);
                return true;
            }
        });

        // Subscribe to the sync service status updates.
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(final Context context, final Intent intent) {
                        // Update the preference when the sync service status has changed.
                        final SyncIntentServiceStatus status = (SyncIntentServiceStatus)intent.getSerializableExtra(
                                SyncIntentService.STATUS_EXTRA_KEY);
                        updateSyncNowPreference(status);
                    }
                },
                new IntentFilter(SyncIntentService.SERVICE_NAME));
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    /**
     * http://stackoverflow.com/questions/16374820/action-bar-home-button-not-functional-with-nested-preferencescreen
     */
    public boolean onPreferenceTreeClick(
            final PreferenceScreen preferenceScreen,
            final Preference preference)
    {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        // Enable home button for child preference screens.
        if (BuildHelper.isHoneyComb()) {
            if (preference != null && preference instanceof PreferenceScreen) {
                final Dialog dialog = ((PreferenceScreen)preference).getDialog();
                if (dialog != null) {
                    dialog.getActionBar().setDisplayHomeAsUpEnabled(true);
                }
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onResume() {
        super.onResume();

        // Update UI.
        updatePeriodPreference();
        updateMaxScanResultsForBssidPreference();
        updateSyncNowPreference();

        // Update the authentication state.
        Authenticator.authenticate(this, false, false, false, false, false, authenticatedHandler);

        // Listen to changes.
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onSharedPreferenceChanged(
            final SharedPreferences sharedPreferences,
            final String key) {
        if (key.equals(Settings.SCAN_PERIOD_KEY)) {
            // Update UI.
            updatePeriodPreference();
            // Restart the service so that the new period is used.
            ScanIntentService.restartIfStarted(this);
        } else if (key.equals(Settings.IS_NETWORK_PROVIDER_ENABLED_KEY)) {
            // Restart the service so that the new provider set is used.
            ScanIntentService.restartIfStarted(this);
        } else if (key.equals(Settings.MAX_SCAN_RESULTS_FOR_BSSID_KEY)) {
            // Update UI.
            updateMaxScanResultsForBssidPreference();
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("deprecation")
    private void updatePeriodPreference() {
        final ListPreference periodPreference =
                (ListPreference)getPreferenceScreen().findPreference(Settings.SCAN_PERIOD_KEY);
        periodPreference.setSummary(periodPreference.getEntry());
    }

    @SuppressWarnings("deprecation")
    private void updateMaxScanResultsForBssidPreference() {
        final ListPreference maxScanResultsForBssidPreference =
                (ListPreference)getPreferenceScreen().findPreference(Settings.MAX_SCAN_RESULTS_FOR_BSSID_KEY);
        maxScanResultsForBssidPreference.setSummary(maxScanResultsForBssidPreference.getEntry());
    }

    private void updateSyncNowPreference() {
        updateSyncNowPreference(Settings.with(this).syncStatus());
    }

    @SuppressWarnings("deprecation")
    private void updateSyncNowPreference(final SyncIntentServiceStatus status) {
        final Preference syncNowPreference = findPreference(Settings.SYNC_NOW_KEY);
        final Settings settings = Settings.with(this);

        switch (status) {
            case NOT_SYNCING:
                final long lastSyncTime = settings.lastSyncTime();
                if (lastSyncTime != 0) {
                    // Synced at the lastSyncTime.
                    syncNowPreference.setSummary(String.format(
                            getString(R.string.preference_sync_now_synced_at_summary),
                            DateFormat.getDateTimeInstance().format(new Date(lastSyncTime))));
                } else {
                    // Never synced.
                    syncNowPreference.setSummary(getString(R.string.preference_sync_now_never_synced_summary));
                }
                break;
            case AUTHENTICATING:
                syncNowPreference.setSummary(getString(R.string.preference_sync_now_authenticating));
                break;
            case SYNCING:
                syncNowPreference.setSummary(getString(R.string.preference_sync_now_syncing_summary));
                break;
        }
    }
}
