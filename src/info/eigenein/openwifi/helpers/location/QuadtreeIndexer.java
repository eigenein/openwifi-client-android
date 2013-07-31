package info.eigenein.openwifi.helpers.location;


public class QuadtreeIndexer {

    private static class MapBoundsE6 {
        /**
         * Minimum latitude (inclusive).
         */
        public static final int SOUTH = -90000000;

        /**
         * Minimum longitude (inclusive).
         */
        public static final int WEST = -180000000;

        /**
         * Maximum latitude (exclusive).
         */
        public static final int NORTH = 90000001;

        /**
         * Maximum longitude (exclusive).
         */
        public static final int EAST = 180000001;
    }

    private static final int QUADTREE_ORDER = 28;

    public static long getIndex(final int latitudeE6, final int longitudeE6) {
        return getIndex(latitudeE6, longitudeE6, QUADTREE_ORDER);
    }

    public static long getIndex(final int latitudeE6, final int longitudeE6, final int quadtreeOrder) {
        long currentIndex = 0L;
        // Current quad bounds.
        int currentSouthE6 = MapBoundsE6.SOUTH;
        int currentWestE6 = MapBoundsE6.WEST;
        int currentNorthE6 = MapBoundsE6.NORTH;
        int currentEastE6 = MapBoundsE6.EAST;
        // Indexing.
        for (int currentOrder = quadtreeOrder; currentOrder != 0; currentOrder -= 1) {
            currentIndex <<= 2;
            // Split current quad.
            final int latitudeMiddleE6 = (currentSouthE6 + currentNorthE6) / 2;
            final int longitudeMiddleE6 = (currentWestE6 + currentEastE6) / 2;
            // Find the target quad.
            if (latitudeE6 < latitudeMiddleE6) {
                currentIndex |= 0;
                currentNorthE6 = latitudeMiddleE6;
            } else {
                currentIndex |= 2;
                currentSouthE6 = latitudeMiddleE6;
            }
            if (longitudeE6 < longitudeMiddleE6) {
                currentIndex |= 0;
                currentEastE6 = longitudeMiddleE6;
            } else {
                currentIndex |= 1;
                currentWestE6 = longitudeMiddleE6;
            }
        }
        // Return the index.
        return currentIndex;
    }
}
