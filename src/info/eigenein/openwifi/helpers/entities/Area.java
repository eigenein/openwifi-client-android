package info.eigenein.openwifi.helpers.entities;

import com.google.android.gms.maps.model.*;

/**
 * Represents an area.
 */
public class Area {
    private final double latitude;

    private final double longitude;

    private final float accuracy;

    public Area(final double latitude, final double longitude, final float accuracy) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracy = accuracy;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public LatLng getLatLng() {
        return new LatLng(latitude, longitude);
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
