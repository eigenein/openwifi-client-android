package info.eigenein.openwifi.helpers;

import java.util.*;

public class RandomHelper {
    private final static Random RANDOM = new Random();

    private final static char[] NUMERIC_CHARS = "0123456789".toCharArray();

    public static String randomNumeric(final int length) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            builder.append(NUMERIC_CHARS[RANDOM.nextInt(NUMERIC_CHARS.length)]);
        }
        return builder.toString();
    }
}
