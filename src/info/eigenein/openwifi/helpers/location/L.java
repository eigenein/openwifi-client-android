package info.eigenein.openwifi.helpers.location;

/**
 * Converts location from degrees into MapView format and vice versa.
 */
public class L {
    private final static double TO_E6_FIX = 1.0e6;

    public static double toDegrees(int value) {
        return value / TO_E6_FIX;
    }
}
