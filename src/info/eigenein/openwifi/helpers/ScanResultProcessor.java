package info.eigenein.openwifi.helpers;

import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.entities.ClusterList;
import info.eigenein.openwifi.helpers.entities.Network;
import info.eigenein.openwifi.persistency.entities.StoredLocation;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;
import org.apache.commons.collections.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processes scan results into a cluster list.
 */
public class ScanResultProcessor {
    private final double gridSize;

    private final MultiKeyMap cache = new MultiKeyMap();

    public ScanResultProcessor(double gridSize) {
        this.gridSize = gridSize;
    }


}
