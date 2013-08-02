package info.eigenein.openwifi.tasks;

import android.os.*;

import java.util.*;

/**
 * Refreshes the map with the cluster markers.
 */
public class RefreshMapAsyncTask2
        extends AsyncTask<Void, Void, RefreshMapAsyncTask2.Network.Cluster.List> {

    /**
     * Represents a single Wi-Fi network (i.e. access points with the same SSID).
     */
    public static class Network {

        /**
         * Represets a cluster of adjacent networks.
         */
        public static class Cluster extends ArrayList<Network> {

            /**
             * Represents a list of clusters.
             */
            public static class List extends ArrayList<Cluster> {
                // Nothing.
            }
        }
    }

    @Override
    protected Network.Cluster.List doInBackground(final Void... voids) {
        return null;
    }

    @Override
    protected synchronized void onPostExecute(final Network.Cluster.List clusters) {
        // TODO.
    }
}
