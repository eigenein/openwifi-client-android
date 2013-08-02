package info.eigenein.openwifi.tasks;

import android.os.*;

import java.util.*;

/**
 * Refreshes the map with the cluster markers.
 */
public class RefreshMapAsyncTask2
        extends AsyncTask<RefreshMapAsyncTask2.Params, Void, RefreshMapAsyncTask2.Network.Cluster.List> {

    /**
     * Represents a visible region.
     */
    public static class Params {

        private final int zoom;
        private final int southE6;
        private final int westE6;
        private final int northE6;
        private final int eastE6;

        public Params(
                final int zoom,
                final int southE6, final int westE6,
                final int northE6, final int eastE6) {
            this.zoom = zoom;
            this.southE6 = southE6;
            this.westE6 = westE6;
            this.northE6 = northE6;
            this.eastE6 = eastE6;
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
    protected Network.Cluster.List doInBackground(final Params... paramsArray) {
        // Check the arguments.
        if (paramsArray.length != 1) {
            throw new RuntimeException("Invalid number of arguments.");
        }
        final Params params = paramsArray[0];

        return null;
    }

    @Override
    protected void onPostExecute(final Network.Cluster.List clusters) {
        // TODO.
    }
}
