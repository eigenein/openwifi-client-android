package info.eigenein.openwifi.tasks;

import android.os.*;
import android.util.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.common.collect.*;
import info.eigenein.openwifi.activities.*;
import info.eigenein.openwifi.helpers.entities.*;
import info.eigenein.openwifi.helpers.location.*;
import info.eigenein.openwifi.helpers.ui.*;
import info.eigenein.openwifi.persistence.*;

import java.util.*;

/**
 * Used to aggregate the scan results from the application database.
 */
public class RefreshMapAsyncTask extends AsyncTask<Void, Void, ClusterList> {
    private final String LOG_TAG = RefreshMapAsyncTask.class.getCanonicalName();

    private final MainActivity activity;
    private final GoogleMap map;

    private final HashMap<String, Cluster> markerToClusterCache;

    private final double minLatitude;
    private final double minLongitude;
    private final double maxLatitude;
    private final double maxLongitude;

    private final GridSize gridSize;

    /**
     * Groups scan results into the grid by their location.
     */
    private final Table<Integer, Integer, List<MyScanResult>> cellToScanResultCache = HashBasedTable.create();

    public RefreshMapAsyncTask(
            final MainActivity activity,
            final GoogleMap map,
            final HashMap<String, Cluster> markerToClusterCache,
            final double minLatitude,
            final double minLongitude,
            final double maxLatitude,
            final double maxLongitude,
            final GridSize gridSize) {
        Log.d(LOG_TAG, String.format(
                "RefreshMapAsyncTask[minLat=%s, minLon=%s, maxLat=%s, maxLon=%s, gridSize=%s]",
                minLatitude,
                minLongitude,
                maxLatitude,
                maxLongitude,
                gridSize));
        this.activity = activity;
        this.map = map;
        this.markerToClusterCache = markerToClusterCache;
        this.minLatitude = minLatitude;
        this.minLongitude = minLongitude;
        this.maxLatitude = maxLatitude;
        this.maxLongitude = maxLongitude;
        this.gridSize = gridSize;
    }

    @Override
    protected ClusterList doInBackground(final Void... params) {
        // Retrieve scan results.
        final MyScanResultDao dao = CacheOpenHelper.getInstance(activity).getMyScanResultDao();
        final Collection<MyScanResult> scanResults = dao.queryByLocation(minLatitude, minLongitude, maxLatitude, maxLongitude);
        // Process them if we're still not cancelled.
        if (isCancelled()) {
            return null;
        }
        for (final MyScanResult scanResult : scanResults) {
            // Check if we're cancelled.
            if (isCancelled()) {
                return null;
            }
            addScanResult(scanResult);
        }
        return buildClusterList();
    }

    @Override
    protected synchronized void onPostExecute(final ClusterList clusterList) {
        Log.d(LOG_TAG + ".onPostExecute", clusterList.toString());

        final MapOverlayHelper helper = new MapOverlayHelper(activity, map);
        // Clear all the overlays.
        markerToClusterCache.clear();
        helper.clear();
        // Add the overlays for the clusters.
        for (final Cluster cluster : clusterList) {
            final Marker marker = helper.addCluster(cluster);
            markerToClusterCache.put(marker.getId(), cluster);
        }

        activity.updateRefreshingScanResultsProgressBar(false);
    }

    @Override
    protected void onCancelled(final ClusterList result) {
        Log.d(LOG_TAG + ".onCancelled", "cancelled");
    }

    private void addScanResult(final MyScanResult scanResult) {
        final int latitudeKey = (int)Math.floor(scanResult.getLatitude() / gridSize.getLatitideStep());
        final int longitudeKey = (int)Math.floor(scanResult.getLongitude() / gridSize.getLongitudeStep());

        List<MyScanResult> scanResults = cellToScanResultCache.get(latitudeKey, longitudeKey);
        if (scanResults == null) {
            scanResults = new ArrayList<MyScanResult>();
            cellToScanResultCache.put(latitudeKey, longitudeKey, scanResults);
        }

        scanResults.add(scanResult);
    }

    private ClusterList buildClusterList() {
        final ClusterList clusterList = new ClusterList();

        // Iterate through grid cells.
        for (final List<MyScanResult> cellResults : cellToScanResultCache.values()) {
            // Check if we're cancelled.
            if (isCancelled()) {
                return null;
            }

            final Multimap<String, String> ssidToBssidCache = HashMultimap.create();

            LocationProcessor locationProcessor = new LocationProcessor();
            for (final MyScanResult scanResult : cellResults) {
                // Check if we're cancelled.
                if (isCancelled()) {
                    return null;
                }
                // Union the scan results by SSID.
                ssidToBssidCache.put(scanResult.getSsid(), scanResult.getBssid());
                // Track the location.
                locationProcessor.add(scanResult);
            }

            // Initialize a cluster.
            final Area area = locationProcessor.getArea();
            final List<Network> networks = new ArrayList<Network>();
            // And fill it with networks.
            for (final Map.Entry<String, Collection<String>> entry : ssidToBssidCache.asMap().entrySet()) {
                // Check if we're cancelled.
                if (isCancelled()) {
                    return null;
                }
                networks.add(new Network(entry.getKey(), entry.getValue()));
            }
            // Finally, insert the cluster to the cluster list.
            final Cluster cluster = new Cluster(area, networks);
            clusterList.add(cluster);
            Log.d(LOG_TAG, "clusterList.insert " + cluster);
        }

        return clusterList;
    }
}
