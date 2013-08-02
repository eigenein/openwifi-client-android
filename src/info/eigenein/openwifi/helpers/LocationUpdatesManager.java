package info.eigenein.openwifi.helpers;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;
import com.google.analytics.tracking.android.*;

/**
 * Manages location update requests.
 */
public class LocationUpdatesManager {

    private static final String LOG_TAG = LocationUpdatesManager.class.getCanonicalName();

    /**
     * Starts location tracking.
     */
    public static void requestUpdates(final Context context, final LocationListener listener) {
        final LocationManager locationManager = getLocationManager(context);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        } catch (IllegalArgumentException ex) {
            EasyTracker.getInstance().setContext(context);
            EasyTracker.getTracker().sendEvent(LOG_TAG, "requestUpdates", "IllegalArgumentException", 0L);
        }

        if (Settings.with(context).isNetworkProviderEnabled()) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        }
    }

    /**
     * Stops location tracking.
     */
    public static void removeUpdates(final Context context, final LocationListener listener) {
        final LocationManager locationManager = getLocationManager(context);
        // Remove the listener.
        locationManager.removeUpdates(listener);
    }

    private static LocationManager getLocationManager(final Context context) {
        return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }
}
