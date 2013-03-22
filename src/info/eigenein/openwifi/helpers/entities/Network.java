package info.eigenein.openwifi.helpers.entities;

import java.util.HashSet;

/**
 * Represents a wireless network in a cluster.
 */
public class Network {
    private final String ssid;

    private final HashSet<String> bssids;

    public Network(String ssid, HashSet<String> bssids) {
        this.ssid = ssid;
        this.bssids = bssids;
    }

    public String getSsid() {
        return ssid;
    }

    public int size() {
        return bssids.size();
    }
}
