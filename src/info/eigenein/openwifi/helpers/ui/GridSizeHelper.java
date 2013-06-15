package info.eigenein.openwifi.helpers.ui;

import java.util.*;

public class GridSizeHelper {
    private static final long MIN_ZOOM = 3L;

    private static final long MAX_ZOOM = 21L;

    private static final HashMap<Long, Double> LONGITUDE_STEP =
            new HashMap<Long, Double>();

    static {
        LONGITUDE_STEP.put(21L, 0.0001);
        LONGITUDE_STEP.put(20L, 0.0002);
        LONGITUDE_STEP.put(19L, 0.0004);
        LONGITUDE_STEP.put(18L, 0.0008);
        LONGITUDE_STEP.put(17L, 0.0016);
        LONGITUDE_STEP.put(16L, 0.0032);
        LONGITUDE_STEP.put(15L, 0.0064);
        LONGITUDE_STEP.put(14L, 0.0128);
        LONGITUDE_STEP.put(13L, 0.0256);
        LONGITUDE_STEP.put(12L, 0.0512);
        LONGITUDE_STEP.put(11L, 0.1024);
        LONGITUDE_STEP.put(10L, 0.2048);
        LONGITUDE_STEP.put(9L, 0.4096);
        LONGITUDE_STEP.put(8L, 0.8192);
        LONGITUDE_STEP.put(7L, 1.6536);
        LONGITUDE_STEP.put(6L, 3.2768);
        LONGITUDE_STEP.put(5L, 6.5536);
        LONGITUDE_STEP.put(4L, 13.1072);
        LONGITUDE_STEP.put(3L, 26.2144);
    }

    public static GridSize get(final double zoom) {
        final long roundedZoom = Math.round(zoom);

        final double latitudeStep;

        if (roundedZoom < MIN_ZOOM) {
            latitudeStep = LONGITUDE_STEP.get(MIN_ZOOM);
        } else if (roundedZoom > MAX_ZOOM) {
            latitudeStep = LONGITUDE_STEP.get(MAX_ZOOM);
        } else {
            latitudeStep = LONGITUDE_STEP.get(roundedZoom);
        }

        // Return the grid size.
        return GridSize.fromLongitudeStep(latitudeStep);
    }
}
