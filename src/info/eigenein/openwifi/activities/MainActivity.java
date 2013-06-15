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
import com.google.common.collect.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.entities.*;
import info.eigenein.openwifi.helpers.internal.*;
import info.eigenein.openwifi.helpers.location.*;
import info.eigenein.openwifi.helpers.scan.*;
import info.eigenein.openwifi.persistency.*;
import info.eigenein.openwifi.services.*;

import java.util.*;

/**
 * Main application activity with the map.
 */
public class MainActivity extends FragmentActivity {
    private static final String LOG_TAG = MainActivity.class.getCanonicalName();

    private GoogleMap map;

    private RefreshScanResultsAsyncTask refreshScanResultsAsyncTask = null;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup default values for the settings.
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // Setup view.
        setContentView(R.layout.main);

        // Check for Google Play Services.
        GooglePlayServicesHelper.check(this);
        // Check for Google Maps.
        GoogleMapsHelper.check(this);

        // Initialize the map.
        map = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
        // Setup the map.
        map.setMyLocationEnabled(true);
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
                if (!firstFixReceived) {
                    firstFixReceived = true;
                    // Animate camera.
                    final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, FIRST_FIX_MAP_ZOOM));
                }
            }
        });
        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            private final String LOG_TAG = this.getClass().getCanonicalName();

            public void onCameraChange(final CameraPosition cameraPosition) {
                Log.d(LOG_TAG + ".onCameraChange", cameraPosition.toString());

                // Update overlays.
                startRefreshingScanResultsOnMap();
            }
        });
        // Setup the map UI settings.
        final UiSettings mapSettings = map.getUiSettings();
        mapSettings.setAllGesturesEnabled(true);
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setMyLocationButtonEnabled(true);
        mapSettings.setCompassEnabled(false);
        // Setup overlays.
        // TODO.
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
        startRefreshingScanResultsOnMap();

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
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0:
                                        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        // TODO: startRefreshingScanResultsOnMap();
                                        break;
                                    case 1:
                                        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                        // TODO: startRefreshingScanResultsOnMap();
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
    private void updateRefreshingScanResultsProgressBar(boolean visible) {
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar_refreshing_scan_results);
        progressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    /**
     * Refreshes the scan results on the map.
     */
    private void startRefreshingScanResultsOnMap() {
        Log.d(LOG_TAG, "startRefreshingScanResultsOnMap");
        updateRefreshingScanResultsProgressBar(true);

        // Check if the task is already running.
        cancelRefreshScanResultsAsyncTask();

        // Check map bounds.
        /* if (mapView.getLatitudeSpan() == 0 || mapView.getLongitudeSpan() == 0) {
            Log.w(LOG_TAG, "Zero mapView span.");
            return;
        } */
        // Get map bounds.
        /* final Projection mapViewProjection = mapView.getProjection();
        GeoPoint nwGeoPoint = mapViewProjection.fromPixels(0, 0);
        GeoPoint seGeoPoint = mapViewProjection.fromPixels(mapView.getWidth(), mapView.getHeight());
        // Run task to retrieve the scan results and process them into a cluster list.
        refreshScanResultsAsyncTask = new RefreshScanResultsAsyncTask(
                L.fromE6(seGeoPoint.getLatitudeE6()),
                L.fromE6(nwGeoPoint.getLongitudeE6()),
                L.fromE6(nwGeoPoint.getLatitudeE6()),
                L.fromE6(seGeoPoint.getLongitudeE6()),
                0.0005 * Math.pow(2.0, 20.0 - mapView.getZoomLevel())
        );
        refreshScanResultsAsyncTask.execute(); */
    }

    private synchronized void cancelRefreshScanResultsAsyncTask() {
        if (refreshScanResultsAsyncTask != null) {
            // Cancel old task.
            refreshScanResultsAsyncTask.cancel(true);
            refreshScanResultsAsyncTask = null;
        }
    }

    /**
     * Used to aggregate the scan results from the application database.
     */
    public class RefreshScanResultsAsyncTask extends AsyncTask<Void, Void, ClusterList> {
        private final String LOG_TAG = RefreshScanResultsAsyncTask.class.getCanonicalName();

        /**
         * Defines a "border" for selecting scan results within the specified area.
         * Without this border a cluster "jumps" when one of its scan results
         * goes off the visible area.
         */
        private static final double BORDER_WIDTH = 0.002;

        private final double minLatitude;

        private final double minLongitude;

        private final double maxLatitude;

        private final double maxLongitude;

        private final double gridSize;

        /**
         * Groups scan results into the grid by their location.
         */
        private final Table<Integer, Integer, List<MyScanResult>> cellToScanResultCache = HashBasedTable.create();

        public RefreshScanResultsAsyncTask(
                final double minLatitude,
                final double minLongitude,
                final double maxLatitude,
                final double maxLongitude,
                final double gridSize) {
            Log.d(LOG_TAG, String.format(
                    "RefreshScanResultsAsyncTask[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s, gridSize=%s]",
                    minLatitude,
                    minLongitude,
                    maxLatitude,
                    maxLongitude,
                    gridSize));
            this.minLatitude = minLatitude;
            this.minLongitude = minLongitude;
            this.maxLatitude = maxLatitude;
            this.maxLongitude = maxLongitude;
            this.gridSize = gridSize;
        }

        @Override
        protected ClusterList doInBackground(Void... params) {
            // Retrieve scan results.
            final long getScanResultsStartTime = System.currentTimeMillis();
            final List<MyScanResult> scanResults = ScanResultTracker.getScanResults(
                    MainActivity.this,
                    minLatitude - BORDER_WIDTH,
                    minLongitude - BORDER_WIDTH,
                    maxLatitude + BORDER_WIDTH,
                    maxLongitude + BORDER_WIDTH
            );
            Log.d(LOG_TAG + ".doInBackground", String.format(
                    "fetched %d results in %sms.",
                    scanResults.size(),
                    System.currentTimeMillis() - getScanResultsStartTime
            ));
            // Process them if we're still not cancelled.
            if (isCancelled()) {
                return null;
            }
            for (final MyScanResult scanResult : scanResults) {
                // Check if we're cancelled.
                if (isCancelled()) {
                    return null;
                }
                addScanResult(scanResult);
            }
            return buildClusterList();
        }

        @Override
        protected synchronized void onPostExecute(final ClusterList clusterList) {
            Log.d(LOG_TAG + ".onPostExecute", clusterList.toString());

            // Clear old overlays.
            // TODO: clusterListOverlay.clearClusterOverlays();
            // Add the overlays for the clusters.
            /* TODO: for (final Cluster cluster : clusterList) {
                ClusterOverlay clusterOverlay = new ClusterOverlay(
                        MainActivity.this,
                        cluster
                );
                clusterListOverlay.addClusterOverlay(clusterOverlay);
            }

            mapView.invalidate();
            updateRefreshingScanResultsProgressBar(false); */
        }

        @Override
        protected void onCancelled(final ClusterList result) {
            Log.d(LOG_TAG + ".onCancelled", "cancelled");
        }

        private void addScanResult(final MyScanResult scanResult) {
            final int key1 = (int)Math.floor(scanResult.getLatitude() / gridSize);
            final int key2 = (int)Math.floor(scanResult.getLongitude() / gridSize);

            List<MyScanResult> subCache = cellToScanResultCache.get(key1, key2);
            if (subCache == null) {
                subCache = new ArrayList<MyScanResult>();
                cellToScanResultCache.put(key1, key2, subCache);
            }

            subCache.add(scanResult);
        }

        private ClusterList buildClusterList() {
            final ClusterList clusterList = new ClusterList();

            // Iterate through grid cells.
            for (final List<MyScanResult> cellResults : cellToScanResultCache.values()) {
                // Check if we're cancelled.
                if (isCancelled()) {
                    return null;
                }

                final Multimap<String, String> ssidToBssidCache = HashMultimap.create();

                LocationProcessor locationProcessor = new LocationProcessor();
                for (final MyScanResult scanResult : cellResults) {
                    // Check if we're cancelled.
                    if (isCancelled()) {
                        return null;
                    }
                    // Union the scan results by SSID.
                    ssidToBssidCache.put(scanResult.getSsid(), scanResult.getBssid());
                    // Track the location.
                    locationProcessor.add(scanResult);
                }

                // Initialize a cluster.
                final Area area = locationProcessor.getArea();
                final Cluster cluster = new Cluster(area);
                // And fill it with networks.
                for (final Map.Entry<String, Collection<String>> entry : ssidToBssidCache.asMap().entrySet()) {
                    // Check if we're cancelled.
                    if (isCancelled()) {
                        return null;
                    }
                    cluster.add(new Network(entry.getKey(), entry.getValue()));
                }
                // Finally, add the cluster to the cluster list.
                clusterList.add(cluster);
                Log.d(LOG_TAG, "clusterList.add " + cluster);
            }

            return clusterList;
        }
    }
}
