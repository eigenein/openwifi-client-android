package info.eigenein.openwifi.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.MapLayerHelper;
import info.eigenein.openwifi.helpers.ScanServiceManager;
import ru.yandex.yandexmapkit.MapController;
import ru.yandex.yandexmapkit.MapView;
import ru.yandex.yandexmapkit.map.MapLayer;

import java.util.List;

/**
 * Main application activity with the map.
 */
public class MainActivity extends Activity {
    private final static float DEFAULT_ZOOM = 17.0f;

    private final static String MAP_LAYER_REQUEST_NAME_KEY = "map_layer_request_name";

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

        // Setup map layer.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String layerRequestName = preferences.getString(MAP_LAYER_REQUEST_NAME_KEY, null);
        if (layerRequestName != null) {
            mapController.setCurrentMapLayer(
                    mapController.getMapLayerByLayerRequestName(layerRequestName));
        }
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
        // Setup map.
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        mapView.getMapController().setHDMode(preferences.getBoolean(
                SettingsActivity.IS_HD_MODE_ENABLED_KEY, false));
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
}
