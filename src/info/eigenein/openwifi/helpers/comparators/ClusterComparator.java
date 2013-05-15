package info.eigenein.openwifi.helpers.comparators;

import info.eigenein.openwifi.helpers.map.ClusterOverlay;

import java.util.Comparator;

public class ClusterComparator implements Comparator {
    private static final ClusterComparator instance = new ClusterComparator();

    public static ClusterComparator getInstance() {
        return instance;
    }

    @Override
    public int compare(final Object o, final Object o2) {
        float accuracy = ((ClusterOverlay)o).getCluster().getArea().getAccuracy();
        float accuracy2 = ((ClusterOverlay)o2).getCluster().getArea().getAccuracy();
        // Sort from greater accuracy down to less accuracy.
        return accuracy < accuracy2 ? 1 : -1;
    }
}
