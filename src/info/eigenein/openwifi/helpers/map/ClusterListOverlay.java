package info.eigenein.openwifi.helpers.map;

import android.graphics.Canvas;
import android.location.Location;
import android.util.Log;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import info.eigenein.openwifi.helpers.comparators.ClusterComparator;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.entities.Network;
import info.eigenein.openwifi.helpers.location.L;
import org.apache.commons.collections.buffer.PriorityBuffer;

import java.util.HashSet;

/**
 * Maintains cluster overlays.
 */
public class ClusterListOverlay extends Overlay {
    private static final String LOG_TAG = ClusterListOverlay.class.getCanonicalName();

    /**
     * Used to track pinch-zoom.
     */
    private boolean isPinch = false;

    /**
     * Priority buffer of overlays to draw them sorted by accuracy descending.
     */
    private final PriorityBuffer overlays = new PriorityBuffer(ClusterComparator.getInstance());

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        // Ignore pinch-zoom.
        if (isPinch) {
            return false;
        }

        double latitude = L.fromE6(geoPoint.getLatitudeE6());
        double longitude = L.fromE6(geoPoint.getLongitudeE6());
        Log.d(LOG_TAG, "onTap " + latitude + " " + longitude);

        // List of the networks under tap.
        HashSet<String> ssids = new HashSet<String>();
        float[] distanceArray = new float[1];
        for (Object overlayObject : overlays) {
            final Cluster cluster = ((ClusterOverlay)overlayObject).getCluster();
            final Area area = cluster.getArea();
            Location.distanceBetween(latitude, longitude, area.getLatitude(), area.getLongitude(), distanceArray);
            if (distanceArray[0] < area.getAccuracy()) {
                for (Network network : cluster) {
                    Log.d(LOG_TAG, network.getSsid() + " (" + network.size() + " BSSIDs)");
                    ssids.add(network.getSsid());
                }
            }
        }
        Log.i(LOG_TAG, ssids.size() + " network(s) tapped.");

        return true;
    }

    /**
     * http://stackoverflow.com/questions/4806061/how-do-i-respond-to-a-tap-on-an-android-mapview-but-ignore-pinch-zoom
     */
    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView)
    {
        int fingers = e.getPointerCount();
        if(e.getAction() == MotionEvent.ACTION_DOWN ) {
            // Touch DOWN, don't know if it's a pinch yet.
            isPinch = false;
        }
        if(e.getAction()==MotionEvent.ACTION_MOVE && fingers == 2) {
            // Two fingers, def a pinch
            isPinch = true;
        }
        return super.onTouchEvent(e, mapView);
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        for (Object overlay : overlays) {
            ((ClusterOverlay)overlay).draw(canvas, mapView, shadow);
        }
    }

    public void clearClusterOverlays() {
        overlays.clear();
    }

    public void addClusterOverlay(ClusterOverlay overlay) {
        overlays.add(overlay);
    }
}
