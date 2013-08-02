package info.eigenein.openwifi.helpers.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Represents a list of clusters.
 */
@Deprecated
public class ClusterList implements Iterable<Cluster> {
    private final List<Cluster> clusters = new ArrayList<Cluster>();

    @Override
    public Iterator<Cluster> iterator() {
        return clusters.iterator();
    }

    public void add(final Cluster cluster) {
        clusters.add(cluster);
    }

    @Override
    public String toString() {
        return String.format(
                "%s[%s]",
                ClusterList.class.getSimpleName(),
                clusters.size());
    }
}
