package info.eigenein.openwifi.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.j256.ormlite.dao.GenericRawResults;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.entities.ClusterList;
import info.eigenein.openwifi.helpers.ui.MapLayerHelper;
import info.eigenein.openwifi.helpers.ScanResultTracker;
import info.eigenein.openwifi.helpers.ScanServiceManager;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.OverlayManager;
import ru.yandex.yandexmapkit.map.MapEvent;
import ru.yandex.yandexmapkit.map.MapLayer;
import ru.yandex.yandexmapkit.map.OnMapListener;
import ru.yandex.yandexmapkit.overlay.Overlay;
import ru.yandex.yandexmapkit.utils.GeoPoint;
import ru.yandex.yandexmapkit.utils.ScreenPoint;

import java.sql.SQLException;
import java.util.List;

/**
 * Main application activity with the map.
 */
public class MainActivity extends Activity implements OnMapListener {
    private final static String LOG_TAG = MainActivity.class.getCanonicalName();

    private final static float DEFAULT_ZOOM = 17.0f;

    private final static String MAP_LAYER_REQUEST_NAME_KEY = "map_layer_request_name";

    private Overlay scanResultsOverlay = null;

    private RefreshScanResultsAsyncTask refreshScanResultsAsyncTask = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        // Setup action bar.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayShowTitleEnabled(false);
        }

        // Setup map view.
        final MapView mapView = (MapView)findViewById(R.id.mapView);
        mapView.showFindMeButton(true);
        mapView.showJamsButton(false);
        mapView.showScaleView(true);
        mapView.showZoomButtons(true);

        // Setup map.
        final MapController mapController = mapView.getMapController();
        mapController.setZoomCurrent(DEFAULT_ZOOM);
        mapController.addMapListener(this);

        // Initialize overlays.
        final OverlayManager overlayManager = mapController.getOverlayManager();
        // Create scan results overlay.
        scanResultsOverlay = new Overlay(mapController);
        overlayManager.addOverlay(scanResultsOverlay);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean isServiceStarted = ScanServiceManager.isStarted(this);
        menu.findItem(R.id.start_scan_menuitem).setVisible(!isServiceStarted);
        menu.findItem(R.id.pause_scan_menuitem).setVisible(isServiceStarted);

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        final MapView mapView = (MapView)findViewById(R.id.mapView);
        final MapController mapController = mapView.getMapController();
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Setup map.
        mapController.setHDMode(preferences.getBoolean(SettingsActivity.IS_HD_MODE_ENABLED_KEY, false));

        // Setup map layer.
        String layerRequestName = preferences.getString(MAP_LAYER_REQUEST_NAME_KEY, null);
        if (layerRequestName != null) {
            mapController.setCurrentMapLayer(mapController.getMapLayerByLayerRequestName(layerRequestName));
        }

        // Setup overlays.
        startRefreshScanResultsOverlay();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menuitem:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.start_scan_menuitem:
                ScanServiceManager.restart(this);
                Toast.makeText(this, R.string.scan_started, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
            case R.id.map_layer_menuitem:
                showChooseMapLayerDialog();
                return true;
            case R.id.pause_scan_menuitem:
                ScanServiceManager.stop(this);
                Toast.makeText(this, R.string.scan_paused, Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
            case R.id.statistics_menuitem:
                startActivity(new Intent(this, StatisticsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onMapActionEvent(MapEvent mapEvent) {
        switch (mapEvent.getMsg()) {
            case MapEvent.MSG_SCALE_END:
            case MapEvent.MSG_ZOOM_END:
            case MapEvent.MSG_SCROLL_END:
                startRefreshScanResultsOverlay();
                break;
        }
    }

    private void showChooseMapLayerDialog() {
        // Obtain map layers.
        final MapView mapView = (MapView)findViewById(R.id.mapView);
        final MapController mapController = mapView.getMapController();
        final List mapLayers = mapController.getListMapLayer();
        // Show dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.map_layer).setItems(
                MapLayerHelper.getMapLayerNames(this, mapLayers),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        MapLayer mapLayer = (MapLayer)mapLayers.get(which);
                        // Choose the layer.
                        mapController.setCurrentMapLayer(mapLayer);
                        // Remember the choice.
                        final SharedPreferences.Editor editor =
                                PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                                .edit();
                        editor.putString(MAP_LAYER_REQUEST_NAME_KEY, mapLayer.requestName);
                        editor.commit();
                    }
        }).create().show();
    }

    /**
     * Refreshes the scan results on the map.
     */
    private void startRefreshScanResultsOverlay() {
        final MapView mapView = (MapView)findViewById(R.id.mapView);
        final MapController mapController = mapView.getMapController();

        // Get map bounds.
        ScreenPoint leftTop = new ScreenPoint(0.0f, 0.0f);
        GeoPoint leftTopGeoPoint = mapController.getGeoPoint(leftTop);
        ScreenPoint bottomRight = new ScreenPoint(mapView.getWidth(), mapView.getHeight());
        GeoPoint bottomRightGeoPoint = mapController.getGeoPoint(bottomRight);

        // Retrieve scan results.
        GenericRawResults<StoredScanResult> scanResults = ScanResultTracker.getScanResults(
                this,
                bottomRightGeoPoint.getLat(),
                leftTopGeoPoint.getLon(),
                leftTopGeoPoint.getLat(),
                bottomRightGeoPoint.getLon()
        );
        // And run the task to process them.
        if (refreshScanResultsAsyncTask != null) {
            // Cancel old task.
            refreshScanResultsAsyncTask.cancel(true);
            refreshScanResultsAsyncTask = null;
        }
        // Count of cells that should fit the screen dimension.
        final double gridCells = 8.0;
        refreshScanResultsAsyncTask = new RefreshScanResultsAsyncTask(Math.min(
                (leftTopGeoPoint.getLat() - bottomRightGeoPoint.getLat()) / gridCells,
                (bottomRightGeoPoint.getLon() - leftTopGeoPoint.getLon()) / gridCells
        ));
        refreshScanResultsAsyncTask.execute(scanResults);
    }

    /**
     * Used to aggregate the scan results from the application database.
     */
    public class RefreshScanResultsAsyncTask extends AsyncTask<GenericRawResults<StoredScanResult>, Void, ClusterList> {
        private double gridSize;

        public RefreshScanResultsAsyncTask(double gridSize) {
            this.gridSize = gridSize;
        }

        @Override
        protected ClusterList doInBackground(
                GenericRawResults<StoredScanResult>... params) {
            return doInBackgroundOne(params[0]);
        }

        @Override
        protected void onPostExecute(ClusterList aggregatedScanResult) {
            scanResultsOverlay.clearOverlayItems();

            // TODO: add new items.
        }

        private ClusterList doInBackgroundOne(GenericRawResults<StoredScanResult> genericRawResults) {
            try {
                return null;
            } finally {
                try {
                    genericRawResults.close();
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Could not close genericRawResults.", e);
                }
            }
        }
    }
}
