package info.eigenein.openwifi.helpers.location;

import android.location.Location;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.persistency.MyScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes locations into an area.
 */
public class LocationProcessor {
    private static final float MIN_AREA_ACCURACY = 30.0f;

    private final List<MyScanResult> scanResults = new ArrayList<MyScanResult>();

    public void add(final MyScanResult scanResult) {
        scanResults.add(scanResult);
    }

    public Area getArea() {
        if (scanResults.size() == 1) {
            // Return the location as the area.
            final MyScanResult scanResult = scanResults.get(0);
            return new Area(
                    scanResult.getLatitude(),
                    scanResult.getLongitude(),
                    Math.max(scanResult.getAccuracy(), MIN_AREA_ACCURACY));
        }

        // Find weighted mean of locations.
        double latitudeSum = 0.0, longitudeSum = 0.0;
        for (MyScanResult scanResult : scanResults) {
            latitudeSum += scanResult.getLatitude();
            longitudeSum += scanResult.getLongitude();
        }
        final double latitude = latitudeSum / scanResults.size();
        final double longitude = longitudeSum / scanResults.size();

        // Find accuracy.
        float accuracy = 0.0f;
        final float[] distance = new float[1];
        for (MyScanResult scanResult : scanResults) {
            Location.distanceBetween(
                    latitude,
                    longitude,
                    scanResult.getLatitude(),
                    scanResult.getLongitude(),
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
