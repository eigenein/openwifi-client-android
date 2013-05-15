package info.eigenein.openwifi.helpers.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.activities.NetworkSetActivity;
import info.eigenein.openwifi.helpers.comparators.ClusterComparator;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.entities.Network;
import info.eigenein.openwifi.helpers.location.L;
import info.eigenein.openwifi.helpers.ui.VibratorHelper;
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
    public boolean onTap(final GeoPoint geoPoint, final MapView mapView) {
        // Ignore pinch-zoom.
        if (isPinch) {
            return false;
        }

        final Context context = mapView.getContext();
        // Vibrate in response.
        VibratorHelper.vibrate(context);

        final double latitude = L.fromE6(geoPoint.getLatitudeE6());
        final double longitude = L.fromE6(geoPoint.getLongitudeE6());
        Log.d(LOG_TAG, "onTap " + latitude + " " + longitude);

        // List of the networks under tap.
        final HashSet<Network> networkSet = new HashSet<Network>();
        final float[] distanceArray = new float[1];
        for (Object overlayObject : overlays) {
            final Cluster cluster = ((ClusterOverlay)overlayObject).getCluster();
            final Area area = cluster.getArea();
            Location.distanceBetween(latitude, longitude, area.getLatitude(), area.getLongitude(), distanceArray);
            if (distanceArray[0] < area.getAccuracy()) {
                for (Network network : cluster) {
                    Log.d(LOG_TAG, network.getSsid() + " (" + network.size() + " BSSIDs)");
                    networkSet.add(network);
                }
            }
        }
        Log.i(LOG_TAG, networkSet.size() + " network(s) tapped.");

        if (networkSet.size() == 0) {
            Toast.makeText(context, R.string.no_networks_here, Toast.LENGTH_SHORT).show();
            return true;
        }

        // Start network set activity with the selected networks.
        final Intent networkSetActivityIntent = new Intent(context, NetworkSetActivity.class);
        final Bundle networkSetActivityBundle = new Bundle();
        networkSetActivityBundle.putSerializable(NetworkSetActivity.NETWORK_SET_KEY, networkSet);
        networkSetActivityIntent.putExtras(networkSetActivityBundle);
        context.startActivity(networkSetActivityIntent);

        return true;
    }

    /**
     * http://stackoverflow.com/questions/4806061/how-do-i-respond-to-a-tap-on-an-android-mapview-but-ignore-pinch-zoom
     */
    @Override
    public boolean onTouchEvent(final MotionEvent e, final MapView mapView)
    {
        int fingers = e.getPointerCount();
        if(e.getAction() == MotionEvent.ACTION_DOWN ) {
            // Touch DOWN, don't know if it's a pinch yet.
            isPinch = false;
        }
        if(e.getAction() == MotionEvent.ACTION_MOVE && fingers == 2) {
            // Two fingers, def a pinch
            isPinch = true;
        }
        return super.onTouchEvent(e, mapView);
    }

    @Override
    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
        for (final Object overlay : overlays) {
            ((ClusterOverlay)overlay).draw(canvas, mapView, shadow);
        }
    }

    public void clearClusterOverlays() {
        overlays.clear();
    }

    public void addClusterOverlay(final ClusterOverlay overlay) {
        overlays.add(overlay);
    }
}
