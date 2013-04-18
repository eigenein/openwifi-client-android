package info.eigenein.openwifi.helpers.location;

import android.location.Location;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.persistency.entities.StoredLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes locations into an area.
 */
public class LocationProcessor {
    private static final float MIN_AREA_ACCURACY = 10.0f;

    private final List<StoredLocation> locations = new ArrayList<StoredLocation>();

    public void add(StoredLocation location) {
        locations.add(location);
    }

    public Area getArea() {
        if (locations.size() == 1) {
            // Return the location as the area.
            final StoredLocation location = locations.get(0);
            return new Area(
                    location.getLatitude(),
                    location.getLongitude(),
                    Math.max(location.getAccuracy(), MIN_AREA_ACCURACY));
        }

        // Find weighted mean of locations.
        double latitudeSum = 0.0, longitudeSum = 0.0;
        for (StoredLocation location : locations) {
            latitudeSum += location.getLatitude();
            longitudeSum += location.getLongitude();
        }
        double latitude = latitudeSum / locations.size();
        double longitude = longitudeSum / locations.size();

        // Find accuracy.
        float accuracy = 0.0f;
        float[] distance = new float[1];
        for (StoredLocation location : locations) {
            Location.distanceBetween(
                    latitude,
                    longitude,
                    location.getLatitude(),
                    location.getLongitude(),
                    distance
            );
            if (distance[0] > accuracy) {
                accuracy = distance[0];
            }
        }

        // Return the area object with computed values.
        return new Area(
                latitude,
                longitude,
                Math.max(accuracy, MIN_AREA_ACCURACY));
    }
}
