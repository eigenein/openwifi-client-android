package info.eigenein.openwifi.helpers.map;

import android.graphics.Canvas;
import android.util.Log;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import java.util.ArrayList;
import java.util.List;

public class TrackableMapView extends MapView {
    private static final String LOG_TAG = TrackableMapView.class.getCanonicalName();

    private int oldZoomLevel = -1;

    private GeoPoint oldMapCenter = null;

    private boolean isMapMoving = false;

    private final List<MapViewListener> listeners = new ArrayList<MapViewListener>();

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(
            final android.content.Context context,
            final android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(
            final android.content.Context context,
            final android.util.AttributeSet attrs,
            final int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(
            final android.content.Context context,
            final java.lang.String apiKey) {
        super(context, apiKey);
    }

    @Override
    public void dispatchDraw(final Canvas canvas) {
        super.dispatchDraw(canvas);

        if (isMapMoving) {
            if (!getMapCenter().equals(oldMapCenter)) {
                oldMapCenter = getMapCenter();
            } else if (getZoomLevel() != oldZoomLevel) {
                oldZoomLevel = getZoomLevel();
            } else {
                isMapMoving = false;
                Log.d(LOG_TAG + ".dispatchDraw", "fireMovedOrZoomed");
                fireMovedOrZoomed();
            }
        }
    }

    /**
     * http://stackoverflow.com/a/7443880/359730
     */
    @Override
    public boolean onTouchEvent(final android.view.MotionEvent event)
    {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d(LOG_TAG + ".onTouchEvent", "" + event.getAction());
            isMapMoving = true;
        }
        return true;
    }

    public void addMovedOrZoomedObserver(final MapViewListener listener) {
        listeners.add(listener);
    }

    /**
     * Tells that the moved or zoomed event should be fired when the next
     * stable location and zoom are drawn.
     */
    public synchronized void invalidateMovedOrZoomed() {
        Log.d(LOG_TAG + ".invalidateMovedOrZoomed", "isMapMoving = " + isMapMoving);
        isMapMoving = true;
    }

    private synchronized void fireMovedOrZoomed() {
        Log.d(LOG_TAG + ".fireMovedOrZoomed ", "center: " + getMapCenter() + ", zoomLevel: " + getZoomLevel());

        for (final MapViewListener listener : listeners) {
            listener.onMovedOrZoomed();
        }
    }
}
