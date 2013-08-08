package info.eigenein.openwifi.helpers;


import android.util.*;
import info.eigenein.openwifi.tasks.*;

public class QuadtreeIndexer {

    private static final int DEFAULT_QUADTREE_ORDER = 28;

    public static long getIndex(final int latitudeE6, final int longitudeE6) {
        return getIndex(latitudeE6, longitudeE6, DEFAULT_QUADTREE_ORDER);
    }

    public static long getIndex(
            final int latitudeE6,
            final int longitudeE6,
            final int quadtreeOrder) {
        long currentIndex = 0L;
        // Current quad bounds.
        int currentQuadSouthE6 = MapBoundsE6.SOUTH;
        int currentQuadWestE6 = MapBoundsE6.WEST;
        int currentQuadNorthE6 = MapBoundsE6.NORTH;
        int currentQuadEastE6 = MapBoundsE6.EAST;
        // Indexing.
        for (int currentOrder = quadtreeOrder; currentOrder != 0; currentOrder -= 1) {
            currentIndex <<= 2;
            // Split current quad.
            final int latitudeMiddleE6 = (currentQuadSouthE6 + currentQuadNorthE6) / 2;
            final int longitudeMiddleE6 = (currentQuadWestE6 + currentQuadEastE6) / 2;
            // Find the target quad.
            if (latitudeE6 < latitudeMiddleE6) {
                currentIndex |= 0;
                currentQuadNorthE6 = latitudeMiddleE6;
            } else {
                currentIndex |= 2;
                currentQuadSouthE6 = latitudeMiddleE6;
            }
            if (longitudeE6 < longitudeMiddleE6) {
                currentIndex |= 0;
                currentQuadEastE6 = longitudeMiddleE6;
            } else {
                currentIndex |= 1;
                currentQuadWestE6 = longitudeMiddleE6;
            }
        }
        // Return the index.
        return currentIndex;
    }

    /**
     * Represents unshuffled index. Used to find the index of an adjacent quad.
     */
    public static class SplitIndex extends Pair<Long, Long> {

        private static final long DEFAULT_INDEX_MASK = getIndexMask(DEFAULT_QUADTREE_ORDER);

        /**
         * Initializes a {@link SplitIndex} from the quadtree index.
         */
        public static SplitIndex fromIndex(final long index) {
            // Unshuffle bits.
            long t, x = index;
            t = (x ^ (x >> 1)) & 0x2222222222222222L; x = x ^ t ^ (t << 1);
            t = (x ^ (x >> 2)) & 0x0C0C0C0C0C0C0C0CL; x = x ^ t ^ (t << 2);
            t = (x ^ (x >> 4)) & 0x00F000F000F000F0L; x = x ^ t ^ (t << 4);
            t = (x ^ (x >> 8)) & 0x0000FF000000FF00L; x = x ^ t ^ (t << 8);
            t = (x ^ (x >> 16)) & 0x00000000FFFF0000L; x = x ^ t ^ (t << 16);
            return new SplitIndex(x >> 32, x & 0xFFFFFFFFL);
        }

        /**
         * Gets the index of the shifted quad.
         */
        public static long shift(
                final long index,
                final long latitudeIndexShift,
                final long longitudeIndexShift) {
            return fromIndex(index)
                    .shift(latitudeIndexShift, longitudeIndexShift)
                    .toIndex();
        }

        /**
         * Used to test the class.
         */
        public static long shift(
                final long index,
                final long latitudeIndexShift,
                final long longitudeIndexShift,
                final int quadtreeOrder) {
            return fromIndex(index)
                    .shift(latitudeIndexShift, longitudeIndexShift)
                    .toIndex(getIndexMask(quadtreeOrder));
        }

        public SplitIndex(final Long latitudeIndex, final Long longitudeIndex) {
            super(latitudeIndex, longitudeIndex);
        }

        public SplitIndex shift(
                final long latitudeIndexShift,
                final long longitudeIndexShift) {
            return new SplitIndex(
                    value1 + latitudeIndexShift,
                    value2 + longitudeIndexShift);
        }

        /**
         * Gets a quadtree index of the quad.
         */
        public long toIndex() {
            return toIndex(DEFAULT_INDEX_MASK);
        }

        /**
         * Gets a quadtree index of the quad.
         */
        private long toIndex(final long indexMask) {
            long t, index = (value1 << 32) | value2;
            // Shuffle bits.
            t = (index ^ (index >> 16)) & 0x00000000FFFF0000L; index = index ^ t ^ (t << 16);
            t = (index ^ (index >> 8)) & 0x0000FF000000FF00L; index = index ^ t ^ (t << 8);
            t = (index ^ (index >> 4)) & 0x00F000F000F000F0L; index = index ^ t ^ (t << 4);
            t = (index ^ (index >> 2)) & 0x0C0C0C0C0C0C0C0CL; index = index ^ t ^ (t << 2);
            t = (index ^ (index >> 1)) & 0x2222222222222222L; index = index ^ t ^ (t << 1);
            return index & indexMask;
        }

        /**
         * Gets the mask of an index significant bits.
         */
        private static final long getIndexMask(final long quadtreeOrder) {
            return (1L << (quadtreeOrder * 2)) - 1L;
        }
    }

    /**
     * Represents a range query.
     */
    public static class Query {

        private static final String LOG_TAG = Query.class.getCanonicalName();

        public static class IndexRange extends Pair<Long, Long> {

            protected IndexRange(final Long leftIndex, final Long rightIndex) {
                super(leftIndex, rightIndex);
            }

            public Long getLeftIndex() {
                return value1;
            }

            public Long getRightIndex() {
                return value2;
            }
        }

        /**
         * Performs the query on a DAO.
         */
        public interface Adapter {
            /**
             * Executes the query on an underlying DAO.
             */
            public void execute(final IndexRange indexRange) throws StopQueryException;

            /**
             * Gets if the query is cancelled.
             */
            public boolean isCancelled();

            /**
             * Gets the query count.
             */
            public int getQueryCount();

            /**
             * Gets the requested clusters.
             */
            public RefreshMapAsyncTask.Network.Cluster.List getClusters();
        }

        /**
         * Thrown when the query should be stopped.
         */
        public static class StopQueryException extends Exception {
            // Nothing.
        }

        private final int minimumQuadOrder;

        private final int querySouthE6;
        private final int queryWestE6;
        private final int queryNorthE6;
        private final int queryEastE6;

        private final Adapter adapter;

        public Query(
                final int minimumQuadOrder,
                final int southE6, final int westE6,
                final int northE6, final int eastE6,
                final Adapter adapter) {
            this.minimumQuadOrder = minimumQuadOrder;
            this.querySouthE6 = southE6;
            this.queryWestE6 = westE6;
            this.queryNorthE6 = northE6;
            this.queryEastE6 = eastE6;
            this.adapter = adapter;
        }

        public void execute() {
            Log.d(LOG_TAG + ".execute", String.format(
                    "Query[minimumQuadOrder=%s, southE6=%s, westE6=%s, northE6=%s, eastE6=%s]",
                    minimumQuadOrder,
                    querySouthE6,
                    queryWestE6,
                    queryNorthE6,
                    queryEastE6));

            final long queriesStartTime = System.currentTimeMillis();
            try {
                execute(0L,
                        DEFAULT_QUADTREE_ORDER,
                        MapBoundsE6.SOUTH, MapBoundsE6.WEST,
                        MapBoundsE6.NORTH, MapBoundsE6.EAST);
            } catch (StopQueryException e) {
                Log.d(LOG_TAG + ".execute", String.format(
                        "StopQueryException (%s queries already executed).",
                        adapter.getQueryCount()));
            } finally {
                Log.d(LOG_TAG + ".execute", String.format(
                        "Executed %s queries in %sms.",
                        adapter.getQueryCount(),
                        System.currentTimeMillis() - queriesStartTime));
            }
        }

        private void execute(
                final long currentIndex,
                final int currentOrder,
                final int currentQuadSouthE6, final int currentQuadWestE6,
                final int currentQuadNorthE6, final int currentQuadEastE6)
                throws StopQueryException {
            if (adapter.isCancelled()) {
                throw new StopQueryException();
            }
            if (currentOrder != minimumQuadOrder) {
                // Check current bounds against the query.
                if (currentQuadSouthE6 > queryNorthE6) {
                    return;
                }
                if (currentQuadWestE6 > queryEastE6) {
                    return;
                }
                if (currentQuadNorthE6 <= querySouthE6) {
                    return;
                }
                if (currentQuadEastE6 <= queryWestE6) {
                    return;
                }
                // Split current quad.
                final int latitudeMiddleE6 = (currentQuadSouthE6 + currentQuadNorthE6) / 2;
                final int longitudeMiddleE6 = (currentQuadWestE6 + currentQuadEastE6) / 2;
                // Shift the index.
                final long nextIndex = currentIndex << 2;
                // Decrement the order.
                final int nextOrder = currentOrder - 1;
                // Execute recursively for the quads.
                execute(
                        nextIndex | 0,
                        nextOrder,
                        currentQuadSouthE6, currentQuadWestE6,
                        latitudeMiddleE6, longitudeMiddleE6);
                execute(
                        nextIndex | 1,
                        nextOrder,
                        currentQuadSouthE6, longitudeMiddleE6,
                        latitudeMiddleE6, currentQuadEastE6);
                execute(
                        nextIndex | 2,
                        nextOrder,
                        latitudeMiddleE6, currentQuadWestE6,
                        currentQuadNorthE6, longitudeMiddleE6);
                execute(
                        nextIndex | 3,
                        nextOrder,
                        latitudeMiddleE6, longitudeMiddleE6,
                        currentQuadNorthE6, currentQuadEastE6);
            } else {
                // Get the left and the right indexes.
                long leftIndex = currentIndex;
                long rightIndex = currentIndex;
                for (int order = currentOrder; order != 0; order -= 1) {
                    leftIndex <<= 2;
                    rightIndex = (rightIndex << 2) | 3;
                }
                // Execute the query on these index values.
                Log.d(LOG_TAG + ".execute", String.format(
                        "[currentOrder=%s, adapter=%s, indexRange=[leftIndex=%s, rightIndex=%s]]",
                        currentOrder,
                        adapter.getClass().getSimpleName(),
                        Long.toHexString(leftIndex),
                        Long.toHexString(rightIndex)));
                adapter.execute(new IndexRange(leftIndex, rightIndex));
            }
        }
    }

    /**
     * Represents the whole map bounds.
     */
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
}
