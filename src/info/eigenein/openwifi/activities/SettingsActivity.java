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
import com.google.analytics.tracking.android.EasyTracker;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.Settings;
import info.eigenein.openwifi.helpers.scan.ScanServiceManager;
import info.eigenein.openwifi.helpers.io.FileUtils;
import info.eigenein.openwifi.persistency.DatabaseHelper;
import info.eigenein.openwifi.services.SyncIntentService;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

public class SettingsActivity extends PreferenceActivity
                              implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String LOG_TAG = SettingsActivity.class.getCanonicalName();

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        addPreferencesFromResource(R.xml.preferences);

        // Share database option.
        final Preference shareDatabasePreference = findPreference(Settings.SHARE_DATABASE_KEY);
        shareDatabasePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                return ShareDatabase();
            }
        });

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
                Toast.makeText(SettingsActivity.this, R.string.sync_now_started, Toast.LENGTH_LONG).show();
                startService(new Intent(SettingsActivity.this, SyncIntentService.class));
                updateSyncNowPreference(true);
                return true;
            }
        });
        updateSyncNowPreference(false);
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this);
    }

    /**
     * Shares the application database.
     */
    private boolean ShareDatabase() {
        // Copy the database into the internal cache.
        final File sourceFile = new File("/data/data/" + getPackageName() + "/databases/" + DatabaseHelper.DATABASE_NAME);
        if (!sourceFile.exists()) {
            Toast.makeText(this, "No database file.", Toast.LENGTH_SHORT).show();
            return true;
        }
        final File cacheDir = getCacheDir();
        final File outputFile = new File(cacheDir, DatabaseHelper.DATABASE_NAME);
        Log.i(LOG_TAG, "copying database");
        try {
            FileUtils.copy(sourceFile, outputFile);
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return true;
        }
        // Share it.
        Log.i(LOG_TAG, "sharing database");
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/x-sqlite3");
        final Uri uri = Uri.parse("content://info.eigenein.openwifi/" + DatabaseHelper.DATABASE_NAME);
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
        updateSyncNowPreference(false);

        // Listen to changes.
        getPreferenceManager().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Settings.SCAN_PERIOD_KEY)) {
            // Update UI.
            updatePeriodPreference();
            // Restart the service so that the new period is used.
            ScanServiceManager.restartIfStarted(this);
        } else if (key.equals(Settings.IS_NETWORK_PROVIDER_ENABLED_KEY)) {
            // Restart the service so that the new provider set is used.
            ScanServiceManager.restartIfStarted(this);
        } else if (key.equals(Settings.MAX_SCAN_RESULTS_FOR_BSSID_KEY)) {
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
                (ListPreference)getPreferenceScreen().findPreference(Settings.SCAN_PERIOD_KEY);
        periodPreference.setSummary(periodPreference.getEntry());
    }

    @SuppressWarnings("deprecation")
    private void updateMaxScanResultsForBssidPreference() {
        ListPreference maxScanResultsForBssidPreference =
                (ListPreference)getPreferenceScreen().findPreference(Settings.MAX_SCAN_RESULTS_FOR_BSSID_KEY);
        maxScanResultsForBssidPreference.setSummary(maxScanResultsForBssidPreference.getEntry());
    }

    @SuppressWarnings("deprecation")
    private void updateSyncNowPreference(boolean forceSyncingNow) {
        final Preference syncNowPreference = findPreference(Settings.SYNC_NOW_KEY);
        final Settings settings = Settings.with(this);

        if (forceSyncingNow || settings.isSyncingNow()) {
            // Syncing right now.
            syncNowPreference.setSummary(getString(R.string.sync_now_syncing_summary));
        } else {
            final long lastSyncTime = settings.lastSyncTime();
            if (lastSyncTime != 0) {
                // Synced at the lastSyncTime.
                syncNowPreference.setSummary(String.format(
                        getString(R.string.sync_now_synced_at_summary),
                        DateFormat.getDateTimeInstance().format(new Date(lastSyncTime))));
            } else {
                // Never synced.
                syncNowPreference.setSummary(getString(R.string.sync_now_never_synced_summary));
            }
        }
    }
}
