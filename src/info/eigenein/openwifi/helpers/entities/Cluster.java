package info.eigenein.openwifi.helpers.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A cluster of several access points.
 */
public class Cluster implements Iterable<Network> {
    private final List<Network> networks = new ArrayList<Network>();

    private final Area area;

    public Cluster(final Area area) {
        this.area = area;
    }

    @Override
    public Iterator<Network> iterator() {
        return networks.iterator();
    }

    public Area getArea() {
        return area;
    }

    public void add(final Network network) {
        networks.add(network);
    }

    public int size() {
        return networks.size();
    }

    @Override
    public String toString() {
        // Construct the network SSID list.
        final StringBuilder networksStringBuilder = new StringBuilder();
        final Iterator<Network> networkIterator = networks.iterator();
        while (networkIterator.hasNext()) {
            networksStringBuilder.append(networkIterator.next().getSsid());
            if (!networkIterator.hasNext()) {
                break;
            }
            networksStringBuilder.append(", ");
        }
        // Join everything.
        return String.format(
                "%s[%s, area=%s, networks=[%s]]",
                Cluster.class.getSimpleName(),
                size(),
                area,
                networksStringBuilder.toString()
        );
    }
}
