package info.eigenein.openwifi.helpers.entities;

import java.util.*;

/**
 * A cluster of several access points.
 */
@Deprecated
public class Cluster implements Iterable<Network> {
    private final ArrayList<Network> networks;

    private final Area area;

    public Cluster(final Area area, final Collection<Network> networks) {
        this.area = area;
        this.networks = new ArrayList<Network>(networks);
    }

    public Area getArea() {
        return area;
    }

    public ArrayList<Network> getNetworks() {
        return networks;
    }

    public int size() {
        return networks.size();
    }

    @Override
    public Iterator<Network> iterator() {
        return networks.iterator();
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
