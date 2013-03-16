package info.eigenein.openwifi.helpers.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A cluster of several access points.
 */
public class Cluster implements Iterable<AccessPoint> {
    private final List<AccessPoint> accessPoints = new ArrayList<AccessPoint>();

    private final Area area;

    public Cluster(Area area) {
        this.area = area;
    }

    @Override
    public Iterator<AccessPoint> iterator() {
        return accessPoints.iterator();
    }

    public void add(AccessPoint accessPoint) {
        accessPoints.add(accessPoint);
    }

    public int size() {
        return accessPoints.size();
    }
}
