package info.eigenein.openwifi.helpers.map;

import android.content.Context;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import info.eigenein.openwifi.helpers.location.LocationTracker;

/**
 * Allows to track user's location when the map is active.
 */
public class TrackableMyLocationOverlay extends MyLocationOverlay {
    public TrackableMyLocationOverlay(Context context, MapView mapView) {
        super(context, mapView);
    }

    @Override
    public void onLocationChanged(android.location.Location location) {
        super.onLocationChanged(location);

        LocationTracker.getInstance().notifyLocationChanged(location);
    }
}
