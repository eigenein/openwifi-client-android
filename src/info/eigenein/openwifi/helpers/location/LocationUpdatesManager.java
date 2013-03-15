package info.eigenein.openwifi.helpers.location;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import info.eigenein.openwifi.activities.SettingsActivity;

/**
 * Manages location update requests.
 */
public class LocationUpdatesManager {
    /**
     * Starts location tracking.
     */
    public static void requestUpdates(Context context, LocationListener listener) {
        final LocationManager locationManager = getLocationManager(context);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, listener);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(SettingsActivity.IS_NETWORK_PROVIDER_ENABLED_KEY, false)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
    }

    /**
     * Stops location tracking.
     */
    public static void removeUpdates(Context context, LocationListener listener) {
        getLocationManager(context).removeUpdates(listener);
    }

    private static LocationManager getLocationManager(Context context) {
        return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }
}
