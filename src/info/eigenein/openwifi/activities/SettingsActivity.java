package info.eigenein.openwifi.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.ScanServiceManager;
import info.eigenein.openwifi.helpers.io.FileUtils;
import info.eigenein.openwifi.persistency.DatabaseHelper;

import java.io.File;
import java.io.IOException;

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = SettingsActivity.class.getCanonicalName();

    public static final String SCAN_PERIOD_KEY = "scan_period";

    public static final String IS_NETWORK_PROVIDER_ENABLED_KEY = "is_network_provider_enabled";

    public static final String SHARE_DATABASE_KEY = "share_database";

    public static final String STATISTICS_KEY = "show_statistics";

    public static final String MAX_SCAN_RESULTS_FOR_BSSID_KEY = "max_scan_results_for_bssid";

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.preferences);

        // Share database option.
        Preference shareDatabasePreference = findPreference(SHARE_DATABASE_KEY);
        shareDatabasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return ShareDatabase();
            }
        });

        // Statistics option.
        Preference statisticsPreference = findPreference(STATISTICS_KEY);
        statisticsPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(SettingsActivity.this, StatisticsActivity.class));
                return true;
            }
        });
    }

    /**
     * Shares the application database.
     */
    private boolean ShareDatabase() {
        // Copy the database into the internal cache.
        File sourceFile = new File("/data/data/" + getPackageName() + "/databases/" + DatabaseHelper.DATABASE_NAME);
        if (!sourceFile.exists()) {
            Toast.makeText(this, "No database file.", Toast.LENGTH_SHORT).show();
            return true;
        }
        File cacheDir = getCacheDir();
        File outputFile = new File(cacheDir, DatabaseHelper.DATABASE_NAME);
        Log.i(LOG_TAG, "copying database");
        try {
            FileUtils.copy(sourceFile, outputFile);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return true;
        }
        // Share it.
        Log.i(LOG_TAG, "sharing database");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/x-sqlite3");
        Uri uri = Uri.parse("content://info.eigenein.openwifi/" + DatabaseHelper.DATABASE_NAME);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_database)));
        return true;
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

        // Listen to changes.
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
        } else if (key.equals(MAX_SCAN_RESULTS_FOR_BSSID_KEY)) {
            // Update UI.
            updateMaxScanResultsForBssidPreference();
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

    @SuppressWarnings("deprecation")
    private void updateMaxScanResultsForBssidPreference() {
        ListPreference maxScanResultsForBssidPreference =
                (ListPreference)getPreferenceScreen().findPreference(MAX_SCAN_RESULTS_FOR_BSSID_KEY);
        maxScanResultsForBssidPreference.setSummary(maxScanResultsForBssidPreference.getEntry());
    }
}
