package info.eigenein.openwifi.listeners;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import info.eigenein.openwifi.helpers.*;

/**
 * Notifies the location tracker with location updates.
 */
public class DefaultLocationListener implements LocationListener {

    private static final DefaultLocationListener instance = new DefaultLocationListener();

    public static DefaultLocationListener getInstance() {
        return instance;
    }

    @Override
    public void onLocationChanged(final Location location) {
        CurrentLocationTracker.getInstance().notifyLocationChanged(location);
    }

    @Override
    public void onStatusChanged(final String s, final int i, final Bundle bundle) {
        // Do nothing.
    }

    @Override
    public void onProviderEnabled(final String s) {
        // Do nothing.
    }

    @Override
    public void onProviderDisabled(final String s) {
        // Do nothing.
    }
}
