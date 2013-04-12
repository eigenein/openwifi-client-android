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
    private final List<StoredLocation> locations = new ArrayList<StoredLocation>();

    public void add(StoredLocation location) {
        locations.add(location);
    }

    public Area getArea() {
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
            float distanceWithAccuracy = distance[0] + location.getAccuracy();
            if (distanceWithAccuracy > accuracy) {
                accuracy = distanceWithAccuracy;
            }
        }

        // Return the area object with computed values.
        return new Area(latitude, longitude, accuracy);
    }
}
