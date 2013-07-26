package info.eigenein.openwifi.helpers.entities;

import java.io.Serializable;
import java.util.*;

/**
 * Represents a wireless network in a cluster.
 */
public class Network implements Serializable {
    private final String ssid;

    private final Collection<String> bssids;

    public Network(final String ssid, final Collection<String> bssids) {
        this.ssid = ssid;
        this.bssids = new ArrayList<String>(bssids);
    }

    /**
     * Gets the network name (SSID).
     */
    public String getSsid() {
        return ssid;
    }

    @Override
    public int hashCode() {
        return ssid.hashCode();
    }

    @Override
    public boolean equals(final Object o) {
        return ssid.equals(((Network)o).getSsid());
    }
}
