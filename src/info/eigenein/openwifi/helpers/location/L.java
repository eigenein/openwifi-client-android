package info.eigenein.openwifi.helpers.location;

import com.google.android.maps.*;

/**
 * Converts location from degrees into MapView format and vice versa.
 */
public class L {
    private final static double TO_E6_FIX = 1.0e6;

    public static double fromE6(final int value) {
        return value / TO_E6_FIX;
    }

    public static int toE6(final double value) {
        return (int)(value * TO_E6_FIX);
    }

    public static GeoPoint toGeoPoint(final double latitude, final double longitude) {
        return new GeoPoint(toE6(latitude), toE6(longitude));
    }
}
