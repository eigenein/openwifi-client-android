package info.eigenein.openwifi.helpers;


import android.util.*;
import info.eigenein.openwifi.tasks.*;

public class QuadtreeIndexer {

    private static final int QUADTREE_ORDER = 28;

    public static long getIndex(final int latitudeE6, final int longitudeE6) {
        return getIndex(latitudeE6, longitudeE6, QUADTREE_ORDER);
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
     * Represents a range query.
     */
    public static class Query {

        private static final String LOG_TAG = Query.class.getCanonicalName();

        /**
         * Performs the query on a DAO.
         */
        public interface Adapter {
            /**
             * Executes the query on an underlying DAO.
             */
            public void execute(final long leftIndex, final long rightIndex)
                    throws StopQueryException;

            /**
             * Gets if the query is cancelled.
             */
            public boolean isCancelled();

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
                        QUADTREE_ORDER,
                        MapBoundsE6.SOUTH, MapBoundsE6.WEST,
                        MapBoundsE6.NORTH, MapBoundsE6.EAST);
            } catch (StopQueryException e) {
                Log.d(LOG_TAG + ".execute", "StopQueryException");
            } finally {
                Log.d(LOG_TAG + ".execute", String.format(
                        "Executed in %sms.",
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
                        "[currentOrder=%s, leftIndex=%s, rightIndex=%s]",
                        currentOrder,
                        Long.toHexString(leftIndex),
                        Long.toHexString(rightIndex)));
                adapter.execute(leftIndex, rightIndex);
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
