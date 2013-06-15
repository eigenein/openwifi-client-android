package info.eigenein.openwifi.helpers.location;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import info.eigenein.openwifi.helpers.internal.Settings;

/**
 * Manages location update requests.
 */
public class LocationUpdatesManager {
    /**
     * Starts location tracking.
     */
    public static void requestUpdates(final Context context, final LocationListener listener) {
        final LocationManager locationManager = getLocationManager(context);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, listener);

        if (Settings.with(context).isNetworkProviderEnabled()) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
    }

    /**
     * Stops location tracking.
     */
    public static void removeUpdates(final Context context, final LocationListener listener) {
        getLocationManager(context).removeUpdates(listener);
    }

    private static LocationManager getLocationManager(final Context context) {
        return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }
}
