package info.eigenein.openwifi.activities;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.preference.*;
import android.view.*;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.services.*;

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = SettingsActivity.class.getCanonicalName();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
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
}
