package info.eigenein.openwifi.helpers.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of clusters.
 */
public class ClusterList implements Iterable<Cluster> {
    private final List<Cluster> clusters = new ArrayList<Cluster>();

    @Override
    public Iterator<Cluster> iterator() {
        return clusters.iterator();
    }

    public void add(Cluster cluster) {
        clusters.add(cluster);
    }
}
