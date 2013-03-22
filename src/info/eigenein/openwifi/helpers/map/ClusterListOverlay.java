package info.eigenein.openwifi.helpers.map;

import android.graphics.Canvas;
import android.util.Log;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import info.eigenein.openwifi.helpers.comparators.ClusterComparator;
import org.apache.commons.collections.buffer.PriorityBuffer;

/**
 * Maintains cluster overlays.
 */
public class ClusterListOverlay extends Overlay {
    private static final String LOG_TAG = ClusterListOverlay.class.getCanonicalName();

    /**
     * Priority buffer of overlays to draw them sorted by accuracy descending.
     */
    private final PriorityBuffer overlays = new PriorityBuffer(ClusterComparator.getInstance());

    @Override
    public boolean onTap(GeoPoint geoPoint, MapView mapView) {
        Log.d(LOG_TAG + ".onTap", "super " + super.onTap(geoPoint, mapView));
        return true;
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
