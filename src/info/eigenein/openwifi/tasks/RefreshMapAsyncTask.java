package info.eigenein.openwifi.tasks;

import android.content.*;
import android.os.*;
import android.util.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.common.cache.*;
import com.google.common.collect.*;
import info.eigenein.openwifi.activities.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.persistence.*;

import java.util.*;

/**
 * Refreshes the map with the cluster markers.
 */
public class RefreshMapAsyncTask extends AsyncTask<
        RefreshMapAsyncTask.Params,
        Void,
        RefreshMapAsyncTask.Network.Cluster.List> {

    private static final String LOG_TAG = RefreshMapAsyncTask.class.getCanonicalName();

    /**
     * Maximum zoom that allows to see a cluster area.
     */
    private static final long CLUSTERING_ADAPTER_ZOOM = 17L;

    private final MainActivity activity;
    private final GoogleMap map;
    private final HashMap<String, Network.Cluster> markerToClusterMapping;

    public RefreshMapAsyncTask(
            final MainActivity activity,
            final GoogleMap map,
            final HashMap<String, Network.Cluster> markerToClusterMapping) {
        this.activity = activity;
        this.map = map;
        this.markerToClusterMapping = markerToClusterMapping;
    }

    @Override
    protected Network.Cluster.List doInBackground(final Params... paramsArray) {
        // Check the arguments.
        if (paramsArray.length != 1) {
            throw new RuntimeException("Invalid number of arguments.");
        }
        // Initialize the query adapter.
        final Params params = paramsArray[0];
        final QueryAdapter queryAdapter = params.getZoom() >= CLUSTERING_ADAPTER_ZOOM ?
                new ClusteringQueryAdapter(activity, this) :
                new NonClusteringQueryAdapter(activity, this);
        // Initialize and execute the query.
        final QuadtreeIndexer.Query query = new QuadtreeIndexer.Query(
                MinimumQuadOrderHelper.getOrder(params.zoom),
                params.getSouthE6(),
                params.getWestE6(),
                params.getNorthE6(),
                params.getEastE6(),
                queryAdapter);
        query.execute();
        // Return the cluster list.
        return queryAdapter.getClusters();
    }

    @Override
    protected void onPostExecute(final Network.Cluster.List clusters) {
        Log.d(LOG_TAG + ".onPostExecute", String.format("%s clusters.", clusters.size()));

        final MapOverlayHelper helper = new MapOverlayHelper(activity, map);
        // Clear all the overlays.
        markerToClusterMapping.clear();
        helper.clear();
        // Add the overlays for the clusters.
        for (final Network.Cluster cluster : clusters) {
            if (cluster.size() != 0) {
                final Marker marker = helper.addCluster(cluster);
                markerToClusterMapping.put(marker.getId(), cluster);
            }
        }

        activity.updateRefreshingScanResultsProgressBar(false);
    }

    /**
     * Represents a visible region.
     */
    public static class Params {

        private final long zoom;
        private final int southE6;
        private final int westE6;
        private final int northE6;
        private final int eastE6;

        public Params(
                final long zoom,
                final int southE6, final int westE6,
                final int northE6, final int eastE6) {
            this.zoom = zoom;
            this.southE6 = southE6;
            this.westE6 = westE6;
            this.northE6 = northE6;
            this.eastE6 = eastE6;
        }

        public long getZoom() {
            return zoom;
        }

        public int getSouthE6() {
            return southE6;
        }

        public int getWestE6() {
            return westE6;
        }

        public int getNorthE6() {
            return northE6;
        }

        public int getEastE6() {
            return eastE6;
        }
    }

    /**
     * Represents a single Wi-Fi network (i.e. access points with the same SSID).
     */
    public static class Network {

        /**
         * Represents a cluster of adjacent networks.
         */
        public static class Cluster {

            private final int size;
            private final int latitudeE6;
            private final int longitudeE6;
            private final Double radius;

            public Cluster(
                    final int size,
                    final int latitudeE6,
                    final int longitudeE6,
                    final Double radius) {
                this.size = size;
                this.latitudeE6 = latitudeE6;
                this.longitudeE6 = longitudeE6;
                this.radius = radius;
            }

            public LatLng getLatLng() {
                return new LatLng(L.fromE6(latitudeE6), L.fromE6(longitudeE6));
            }

            public Double getRadius() {
                return radius;
            }

            public int size() {
                return size;
            }

            /**
             * Represents a list of clusters.
             */
            public static class List extends ArrayList<Cluster> {
                // Nothing.
            }
        }
    }

    /**
     * Contains common query adapter code.
     */
    public static abstract class QueryAdapter implements QuadtreeIndexer.Query.Adapter {

        protected final Context context;

        private final RefreshMapAsyncTask asyncTask;

        private int queryCount;

        public QueryAdapter(
                final Context context,
                final RefreshMapAsyncTask asyncTask) {
            this.context = context;
            this.asyncTask = asyncTask;
        }

        @Override
        public void execute(final QuadtreeIndexer.Query.IndexRange indexRange)
                throws QuadtreeIndexer.Query.StopQueryException {
            throwStopQueryExceptionIfCancelled();
            queryCount += 1;
        }

        @Override
        public boolean isCancelled() {
            return asyncTask.isCancelled();
        }

        @Override
        public int getQueryCount() {
            return queryCount;
        }

        /**
         * Checks if the task is cancelled and throws the exception.
         */
        protected void throwStopQueryExceptionIfCancelled()
                throws QuadtreeIndexer.Query.StopQueryException {
            if (asyncTask.isCancelled()) {
                throw new QuadtreeIndexer.Query.StopQueryException();
            }
        }
    }
}

/**
 * Used to render caches at a high zoom level.
 */
class NonClusteringQueryAdapter extends RefreshMapAsyncTask.QueryAdapter {

    private static LocalCache cache;

    private RefreshMapAsyncTask.Network.Cluster.List clusters =
            new RefreshMapAsyncTask.Network.Cluster.List();

    private static synchronized LocalCache getCache(final Context context) {
        if (cache == null) {
            cache = new LocalCache(CacheOpenHelper.getInstance(context).getMyScanResultDao());
        }
        return cache;
    }

    public NonClusteringQueryAdapter(
            final Context context,
            final RefreshMapAsyncTask asyncTask) {
        super(context, asyncTask);
    }

    @Override
    public void execute(final QuadtreeIndexer.Query.IndexRange indexRange)
            throws QuadtreeIndexer.Query.StopQueryException {
        super.execute(indexRange);

        final LocalCache cache = getCache(context);
        final RefreshMapAsyncTask.Network.Cluster cluster =
                cache.queryClusterByQuadtreeIndex(indexRange);
        clusters.add(cluster);
    }

    public RefreshMapAsyncTask.Network.Cluster.List getClusters() {
        return clusters;
    }

    /**
     * Used to cache {@link MyScanResult.Dao}.
     */
    private static class LocalCache {

        private static final String LOG_TAG = LocalCache.class.getCanonicalName();

        private static final long CACHE_SIZE = 1024L;

        private final LoadingCache<
                QuadtreeIndexer.Query.IndexRange,
                RefreshMapAsyncTask.Network.Cluster> cache;

        /**
         * Initializes a cache with the specified DAO.
         */
        public LocalCache(final MyScanResult.Dao dao) {
            this.cache = CacheBuilder.newBuilder()
                    .maximumSize(CACHE_SIZE)
                    .recordStats()
                    .build(new CacheLoader<QuadtreeIndexer.Query.IndexRange, RefreshMapAsyncTask.Network.Cluster>() {
                        @Override
                        public RefreshMapAsyncTask.Network.Cluster load(
                                final QuadtreeIndexer.Query.IndexRange indexRange)
                                throws Exception {
                            return dao.queryClusterByQuadtreeIndex(indexRange);
                        }
                    });
        }

        public RefreshMapAsyncTask.Network.Cluster queryClusterByQuadtreeIndex(
                final QuadtreeIndexer.Query.IndexRange indexRange) {
            final RefreshMapAsyncTask.Network.Cluster cluster = cache.getUnchecked(indexRange);
            final CacheStats stats = cache.stats();
            Log.d(LOG_TAG + ".queryClusterByQuadtreeIndex", String.format(
                    "CacheStats[hitRate=%.3f, averageLoadPenalty=%.3fs]",
                    stats.hitRate(),
                    stats.averageLoadPenalty() / 1.0e9));
            return cluster;
        }
    }
}

/**
 * Used to render clusters at a low zoom level.
 */
class ClusteringQueryAdapter extends RefreshMapAsyncTask.QueryAdapter {

    private static final String LOG_TAG = ClusteringQueryAdapter.class.getCanonicalName();

    /**
     * Maps a quadtree index to a scan result.
     */
    private final Multimap<Long, MyScanResult> resultsMultimap = ArrayListMultimap.create();

    public ClusteringQueryAdapter(
            final Context context,
            final RefreshMapAsyncTask asyncTask) {
        super(context, asyncTask);
    }

    @Override
    public void execute(final QuadtreeIndexer.Query.IndexRange indexRange)
            throws QuadtreeIndexer.Query.StopQueryException {
        super.execute(indexRange);

        // Query the scan results.
        final List<MyScanResult> results = CacheOpenHelper
                .getInstance(context)
                .getMyScanResultDao()
                .queryScanResultsByQuadtreeIndex(indexRange);
        Log.d(LOG_TAG + ".execute", String.format(
                "Got %s results.",
                results.size()));
        // Move the results into the multimap.
        // TODO: determine the cluster diameter and the corresponding shift and index mask.
        // TODO: use this index mask to put the results into the multimap.
    }

    @Override
    public RefreshMapAsyncTask.Network.Cluster.List getClusters() {
        // TODO: go through the multimap and move the scan results into the clusters.
        // TODO: http://habrahabr.ru/post/138185/
        // TODO: compute the position and the radius of each cluster.
        // TODO: group the scan results in each cluster by SSID.
        return new RefreshMapAsyncTask.Network.Cluster.List();
    }
}

/**
 * Maps zoom to minimal quad order.
 */
class MinimumQuadOrderHelper {

    private static final HashMap<Long, Integer> MAPPING =
            new HashMap<Long, Integer>();

    static {
        MAPPING.put(21L, 6);
        MAPPING.put(20L, 7);
        MAPPING.put(19L, 8);
        MAPPING.put(18L, 9);
        MAPPING.put(17L, 10);
        MAPPING.put(16L, 11);
        MAPPING.put(15L, 12);
        MAPPING.put(14L, 13);
        MAPPING.put(13L, 14);
        MAPPING.put(12L, 15);
        MAPPING.put(11L, 16);
        MAPPING.put(10L, 17);
        MAPPING.put(9L, 18);
        MAPPING.put(8L, 19);
        MAPPING.put(7L, 20);
        MAPPING.put(6L, 21);
        MAPPING.put(5L, 22);
        MAPPING.put(4L, 23);
        MAPPING.put(3L, 24);
    }

    public static int getOrder(final long zoom) {
        if (zoom > 21L) {
            return 22;
        } else if (zoom < 3L) {
            return 2;
        } else {
            return MAPPING.get(zoom);
        }
    }
}