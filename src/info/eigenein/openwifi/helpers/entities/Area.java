package info.eigenein.openwifi.helpers.entities;

/**
 * Represents an area.
 */
public class Area {
    private final double latitude;

    private final double longitude;

    private final float accuracy;

    public Area(double latitude, double longitude, float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    @Override
    public String toString() {
        return String.format(
                "%s[lat=%s, lon=%s, acc=%s]",
                Area.class.getSimpleName(),
                latitude,
                longitude,
                accuracy
        );
    }
}
