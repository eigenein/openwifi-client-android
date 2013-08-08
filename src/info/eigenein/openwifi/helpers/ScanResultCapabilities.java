package info.eigenein.openwifi.helpers;

/**
 * Represents an access point capabilities.
 */
public class ScanResultCapabilities {
    private final boolean secured;

    /**
     * Parses the capabilities string.
     */
    public static ScanResultCapabilities fromString(final String capabilities) {
        return new ScanResultCapabilities(
                capabilities.contains("WEP") ||
                        capabilities.contains("WPA") ||
                        capabilities.contains("WPA2")
        );
    }

    private ScanResultCapabilities(final boolean secured) {
        this.secured = secured;
    }

    /**
     * Gets whether an access point is secured.
     */
    public boolean isSecured() {
        return this.secured;
    }
}
