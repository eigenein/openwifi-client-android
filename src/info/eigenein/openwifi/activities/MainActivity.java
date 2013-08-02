package info.eigenein.openwifi.activities;

import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.preference.*;
import android.support.v4.app.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.analytics.tracking.android.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.services.*;
import info.eigenein.openwifi.tasks.*;

import java.util.*;

/**
 * Main application activity with the map.
 */
public class MainActivity extends FragmentActivity {
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private final HashMap<String, RefreshMapAsyncTask.Network.Cluster> markerToClusterMapping =
            new HashMap<String, RefreshMapAsyncTask.Network.Cluster>();

    private GoogleMap map;

    private RefreshMapAsyncTask refreshScanResultsAsyncTask = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup default values for the settings.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Setup view.
        setContentView(R.layout.main);

        // Check for Google Play Services and Google Maps.
        if (!GooglePlayServicesHelper.check(this) || !GoogleMapsHelper.check(this)) {
            Log.e(LOG_TAG + ".onCreate", "Not initializing the map.");
            return;
        }

        // Initialize the map.
        map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_map)).getMap();
        // Set up the map.
        map.setMyLocationEnabled(true);
        // Setup the map UI settings.
        final UiSettings mapSettings = map.getUiSettings();
        mapSettings.setAllGesturesEnabled(true);
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setMyLocationButtonEnabled(true);
        mapSettings.setCompassEnabled(false);
        // Set up the map handlers.
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            /**
             * Zoom that the map sets when the my location button is pressed.
             */
            private static final float FIRST_FIX_MAP_ZOOM = 15.0f;

            /**
             * A flag used to set up the camera for the first time.
             */
            private boolean firstFixReceived = false;

            @Override
            public void onMyLocationChange(final Location location) {
                // Track the location.
                CurrentLocationTracker.getInstance().notifyLocationChanged(location);
                // Animate to the current location for the first time.
                if (!firstFixReceived) {
                    firstFixReceived = true;
                    // Animate camera.
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, FIRST_FIX_MAP_ZOOM));
                }
            }
        });
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            public void onCameraChange(final CameraPosition cameraPosition) {
                Log.d(LOG_TAG + ".onCameraChange", cameraPosition.toString());

                // Update overlays.
                startRefreshingScanResultsOnMap(cameraPosition);
            }
        });
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                // Open the info window for the marker.
                marker.showInfoWindow();

                // Event was handled by our code. Do not launch default behaviour.
                return true;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(final Marker marker) {
                final RefreshMapAsyncTask.Network.Cluster cluster =
                        markerToClusterMapping.get(marker.getId());
                if (cluster != null) {
                    VibratorHelper.vibrate(MainActivity.this);

                    // Start network set activity with the selected networks.
                    final Bundle networkSetActivityBundle = new Bundle();
                    networkSetActivityBundle.putSerializable(
                            NetworkSetActivity.NETWORK_SET_KEY,
                            null /*TODO: cluster.getNetworks()*/);
                    final Intent networkSetActivityIntent = new Intent(
                            MainActivity.this,
                            NetworkSetActivity.class);
                    networkSetActivityIntent.putExtras(networkSetActivityBundle);
                    startActivity(networkSetActivityIntent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean isServiceStarted = ScanIntentService.isStarted(this);
        menu.findItem(R.id.menuitem_start_scan).setVisible(!isServiceStarted);
        menu.findItem(R.id.menuitem_pause_scan).setVisible(isServiceStarted);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Show the help for the very first time.
        final Settings settings = Settings.with(this);
        if (!settings.isHelpShown()) {
            settings.edit().helpShown(true).commit();
            startActivity(new Intent(this, HelpActivity.class));
        }

        // Update overlays.
        if (map != null) {
            startRefreshingScanResultsOnMap(map.getCameraPosition());
        }

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        invalidateOptionsMenu();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Cancel the scan results refresh task if any.
        cancelRefreshScanResultsAsyncTask();

        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuitem_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menuitem_start_scan:
                ScanIntentService.restart(this);
                Toast.makeText(this, R.string.toast_scan_started, Toast.LENGTH_LONG).show();
                invalidateOptionsMenu();
                return true;
            case R.id.menuitem_pause_scan:
                ScanIntentService.stop(this);
                Toast.makeText(this, R.string.toats_scan_paused, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
            case R.id.menuitem_map_view:
                final CharSequence[] items = getResources().getTextArray(R.array.map_views);
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dialog_title_map_view))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, final int item) {
                                switch (item) {
                                    case 0:
                                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        startRefreshingScanResultsOnMap(map.getCameraPosition());
                                        break;
                                    case 1:
                                        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                        startRefreshingScanResultsOnMap(map.getCameraPosition());
                                        break;
                                }
                            }
                        })
                        .show();
                return true;
            case R.id.menuitem_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;
            case R.id.menuitem_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void invalidateOptionsMenu() {
        if (BuildHelper.isHoneyComb()) {
            // Added in API level 11
            super.invalidateOptionsMenu();
        }
    }

    /**
     * Updates the refreshing scan results progress bar visibility.
     */
    public void updateRefreshingScanResultsProgressBar(boolean visible) {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_refreshing_scan_results);
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Refreshes the scan results on the map.
     */
    private void startRefreshingScanResultsOnMap(final CameraPosition cameraPosition) {
        Log.d(LOG_TAG + ".startRefreshingScanResultsOnMap", String.format(
                "[zoom=%s]", cameraPosition.zoom));
        updateRefreshingScanResultsProgressBar(true);

        // Check if the task is already running.
        cancelRefreshScanResultsAsyncTask();

        // Get the visible region.
        final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        // Check the region.
        if (Math.min(
                Math.abs(bounds.northeast.latitude - bounds.southwest.latitude),
                Math.abs(bounds.northeast.longitude - bounds.southwest.longitude)) < 10e-6) {
            return;
        }

        // Run task to retrieve the scan results and process them into a cluster list.
        refreshScanResultsAsyncTask = new RefreshMapAsyncTask(
                this,
                map,
                markerToClusterMapping
        );
        refreshScanResultsAsyncTask.execute(new RefreshMapAsyncTask.Params[] {
                new RefreshMapAsyncTask.Params(
                        Math.round(cameraPosition.zoom),
                        L.toE6(bounds.southwest.latitude),
                        L.toE6(bounds.southwest.longitude),
                        L.toE6(bounds.northeast.latitude),
                        L.toE6(bounds.northeast.longitude))
        });
    }

    private synchronized void cancelRefreshScanResultsAsyncTask() {
        if (refreshScanResultsAsyncTask != null) {
            // Cancel old task.
            refreshScanResultsAsyncTask.cancel(true);
            refreshScanResultsAsyncTask = null;
        }
    }
}
