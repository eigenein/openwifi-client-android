package info.eigenein.openwifi.helpers.entities;

/**
 * Represents a wireless network in a cluster.
 */
public class Network {
    private final String ssid;

    private final String[] bssids;

    public Network(String ssid, String[] bssids) {
        this.ssid = ssid;
        this.bssids = bssids;
    }

    public String getSsid() {
        return ssid;
    }
}
