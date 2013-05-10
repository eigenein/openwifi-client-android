package info.eigenein.openwifi.helpers.formatters;

/**
 * Formats the count.
 */
public class CountFormatter {
    public static int format(
            final int count,
            // "1 сеть"
            final int resourceId1,
            // "2 сети"
            final int resourceId2,
            // "5 сетей"
            final int resourceId3) {
        final int lastDigit = count % 10;

        if (count >= 10 && count <= 20) {
            return resourceId3;
        } else if (lastDigit == 1) {
            return resourceId1;
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return resourceId2;
        } else {
            return resourceId3;
        }
    }
}
