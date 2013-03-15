package info.eigenein.openwifi.helpers.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Notifies the location tracker with location updates.
 */
public class DefaultLocationListener implements LocationListener {
    private static final DefaultLocationListener instance = new DefaultLocationListener();

    public static DefaultLocationListener getInstance() {
        return instance;
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationTracker.getInstance().notifyLocationChanged(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        // Do nothing.
    }

    @Override
    public void onProviderEnabled(String s) {
        // Do nothing.
    }

    @Override
    public void onProviderDisabled(String s) {
        // Do nothing.
    }
}
