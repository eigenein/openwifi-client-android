package info.eigenein.openwifi.helpers.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Tracks the best known location.
 */
public class LocationTracker {
    private static final String LOG_TAG = LocationTracker.class.getCanonicalName();

    /**
     * Time delta to treat a location outdated.
     */
    private static final long OUTDATED_TIME_DELTA = 1000 * 15 * 2;

    private static final LocationTracker instance = new LocationTracker();

    /**
     * Tracked location.
     */
    private static Location location = null;

    /**
     * Gets the location tracker.
     */
    public static LocationTracker getInstance() {
        return instance;
    }

    /**
     * Gets the best tracked location.
     */
    public Location getLocation(Context context) {
        final LocationManager locationManager = getLocationManager(context);

        // Find the best location from the current location and last known locations.
        Location bestLocation = getBestLocation(
                location,
                getBestLocation(
                        locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER),
                        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                )
        );

        // Check if the best location is not outdated.
        Log.d(LOG_TAG, "bestLocation " + formatLocation(bestLocation));
        if (System.currentTimeMillis() - bestLocation.getTime() < OUTDATED_TIME_DELTA) {
            Log.d(LOG_TAG, "getLocation not null");
            return bestLocation;
        }

        Log.d(LOG_TAG, "getLocation null");
        return null;
    }

    /**
     * Notifies the tracker that there is location update.
     */
    public void notifyLocationChanged(Location location) {
        Log.d(LOG_TAG, "notifyLocationChanged " + formatLocation(location));

        // Choose the best location.
        LocationTracker.location = getBestLocation(
                LocationTracker.location, location);

        Log.d(LOG_TAG, "LocationTracker.location " + formatLocation(LocationTracker.location));
    }

    private static LocationManager getLocationManager(Context context) {
        return (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }

    /**
     * See https://developer.android.com/guide/topics/location/strategies.html
     */
    private static Location getBestLocation(Location location2, Location location1) {
        // A new location is always better than no location.
        if (location1 == null) {
            return location2;
        } else if (location2 == null) {
            return location1;
        }

        // Check whether the new location fix is newer or older.
        long timeDelta = location2.getTime() - location1.getTime();
        boolean isSignificantlyNewer = timeDelta > OUTDATED_TIME_DELTA;
        boolean isSignificantlyOlder = timeDelta < -OUTDATED_TIME_DELTA;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return location2;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return location1;
        }

        // Check whether the new location fix is more or less accurate.
        float accuracyDelta = location2.getAccuracy() - location1.getAccuracy();
        boolean isLessAccurate = accuracyDelta > 0.0f;
        boolean isMoreAccurate = accuracyDelta < 0.0f;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200.0f;

        // Check if the old and new location are from the same provider.
        boolean isFromSameProvider = isSameProvider(
                location2.getProvider(),
                location1.getProvider());

        // Determine location quality using a combination of timeliness and accuracy.
        if (isMoreAccurate) {
            return location2;
        } else if (isNewer && !isLessAccurate) {
            return location2;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return location2;
        }
        return location1;
    }

    /**
     * Checks whether two providers are the same.
     */
    private static boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    private static String formatLocation(Location location) {
        if (location == null) {
            return "null";
        }
        return String.format(
                "Location[time=%s, provider=%s, accuracy=%s, lat=%s, lot=%s]",
                location.getTime(),
                location.getProvider(),
                location.getAccuracy(),
                location.getLatitude(),
                location.getLongitude());
    }
}
