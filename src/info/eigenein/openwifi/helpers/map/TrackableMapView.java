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

    private List<MapViewListener> listeners = new ArrayList<MapViewListener>();

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(android.content.Context context, android.util.AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @SuppressWarnings("UnusedDeclaration")
    public TrackableMapView(android.content.Context context, java.lang.String apiKey) {
        super(context, apiKey);
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
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
    public boolean onTouchEvent(android.view.MotionEvent event)
    {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP)
        {
            Log.d(LOG_TAG + ".onTouchEvent", "" + event.getAction());
            isMapMoving = true;
        }
        return true;
    }

    public void addMovedOrZoomedObserver(MapViewListener listener) {
        listeners.add(listener);
    }

    /**
     * Tells that the moved or zoomed event should be fired when the next
     * stable location and zoom are drawn.
     */
    public void invalidateMovedOrZoomed() {
        Log.d(LOG_TAG + ".invalidateMovedOrZoomed", "isMapMoving = " + isMapMoving);
        isMapMoving = true;
    }

    private void fireMovedOrZoomed() {
        Log.d(LOG_TAG + ".fireMovedOrZoomed ", "center: " + getMapCenter() + ", zoomLevel: " + getZoomLevel());

        for (MapViewListener listener : listeners) {
            synchronized (listener) {
                listener.onMovedOrZoomed();
            }
        }
    }
}
