package info.eigenein.openwifi.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.view.MenuItem;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.ScanServiceManager;

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String IS_HD_MODE_ENABLED_KEY = "is_hd_mode_enabled";

    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

        updatePeriodPreference();
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(SCAN_PERIOD_KEY)) {
            // Update UI.
            updatePeriodPreference();
            // Restart the service so that the new period is used.
            ScanServiceManager.restartIfStarted(this);
        } else if (key.equals(IS_NETWORK_PROVIDER_ENABLED_KEY)) {
            // Restart the service so that the new provider set is used.
            ScanServiceManager.restartIfStarted(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        ListPreference periodPreference =
                (ListPreference)getPreferenceScreen().findPreference(SCAN_PERIOD_KEY);
        periodPreference.setSummary(periodPreference.getEntry());
    }
}
