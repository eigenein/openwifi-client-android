package info.eigenein.openwifi.helpers;

import android.content.*;
import android.graphics.*;
import android.util.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.tasks.*;

import java.util.*;

public class MapOverlayHelper {

    private static final int MAX_SNIPPET_LENGTH = 24;

    private static final int DARK_CIRCLE_COLOR = Color.argb(32, 0, 0, 0);
    private static final int DARK_CIRCLE_STROKE = Color.argb(16, 0, 0, 0);

    private static final int LIGHT_CIRCLE_COLOR = Color.argb(96, 255, 255, 255);
    private static final int LIGHT_CIRCLE_STROKE = Color.argb(48, 255, 255, 255);

    private static final float TEXT_SIZE = 12.0f;

    private static final Paint DEFAULT_PAINT = new Paint();
    /**
     * Used to draw text outline.
     */
    private static final Paint STROKE_PAINT = new Paint();

    private final Context context;
    private final GoogleMap map;

    private final boolean isDark;
    /**
     * Stores a bitmap for each cluster size.
     */
    private final SparseArray<BitmapDescriptor> descriptorCache = new SparseArray<BitmapDescriptor>();
    private final float density;

    static {
        DEFAULT_PAINT.setAntiAlias(true);
        DEFAULT_PAINT.setColor(Color.BLACK);
        DEFAULT_PAINT.setTextSize(TEXT_SIZE);
        DEFAULT_PAINT.setTypeface(Typeface.DEFAULT_BOLD);

        STROKE_PAINT.setAntiAlias(true);
        STROKE_PAINT.setColor(Color.WHITE);
        STROKE_PAINT.setStrokeWidth(3.0f);
        STROKE_PAINT.setStyle(Paint.Style.STROKE);
        STROKE_PAINT.setTextSize(TEXT_SIZE);
        STROKE_PAINT.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public MapOverlayHelper(final Context context, final GoogleMap map) {
        this.context = context;
        this.map = map;

        // Used to draw the icon and the circle.
        this.isDark = map.getMapType() == GoogleMap.MAP_TYPE_NORMAL;
        // Used to draw the text.
        this.density = context.getResources().getDisplayMetrics().density;
    }

    /**
     * Clears the map.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Adds the cluster marker and circle to the map.
     */
    public Marker addCluster(final RefreshMapAsyncTask.Network.Cluster cluster) {
        final LatLng clusterPosition = cluster.getLatLng();
        // Add the marker.
        final MarkerOptions markerOptions = new MarkerOptions()
                .position(clusterPosition)
                .title(getClusterTitle(cluster))
                .icon(getClusterIcon(cluster))
                .anchor(0.5f, 0.5f);
        if (cluster.networks() != null) {
            // Put network names onto the snippet.
            markerOptions.snippet(getClusterSnippet(cluster.networks()));
        }
        // Add the marker.
        final Marker marker = map.addMarker(markerOptions);
        // Add the circle.
        final Double clusterRadius = cluster.getRadius();
        if (clusterRadius != null) {
            map.addCircle(new CircleOptions()
                    .center(clusterPosition)
                    .radius(clusterRadius)
                    .fillColor(isDark ? DARK_CIRCLE_COLOR : LIGHT_CIRCLE_COLOR)
                    .strokeColor(isDark ? DARK_CIRCLE_STROKE : LIGHT_CIRCLE_STROKE)
                    .strokeWidth(1.0f)
                    .zIndex(-1.0f)
            );
        }
        // Return the marker.
        return marker;
    }

    /**
     * Gets the cluster marker title.
     */
    private String getClusterTitle(final RefreshMapAsyncTask.Network.Cluster cluster) {
        final int clusterSize = cluster.size();
        return clusterSize == 1 && cluster.networks() != null ?
                cluster.networks().iterator().next().getSsid() :
                String.format(
                        "%d %s",
                        clusterSize,
                        context.getString(CountFormatter.format(
                                clusterSize,
                                R.string.overlay_networks_string_1,
                                R.string.overlay_networks_string_2,
                                R.string.overlay_networks_string_3)));

    }

    /**
     * Draws the cluster icon.
     */
    private BitmapDescriptor getClusterIcon(final RefreshMapAsyncTask.Network.Cluster cluster) {
        final int clusterSize = cluster.size();
        // Try to get the cached descriptor.
        final BitmapDescriptor cachedDescriptor = descriptorCache.get(clusterSize);
        if (cachedDescriptor != null) {
            return cachedDescriptor;
        }
        // Draw the icon.
        final Bitmap resourceIcon = BitmapFactory.decodeResource(
                context.getResources(),
                isDark ? R.drawable.ic_cluster : R.drawable.ic_cluster_light);
        // Find the text bounds.
        final String clusterTitle = Integer.toString(clusterSize);
        final Rect textBounds = new Rect();
        STROKE_PAINT.getTextBounds(clusterTitle, 0, clusterTitle.length(), textBounds);
        // Resource icon is immutable. That's why we create a new one.
        final Bitmap icon = Bitmap.createBitmap(
                Math.max(resourceIcon.getWidth(), textBounds.width()),
                Math.max(resourceIcon.getHeight(), textBounds.height()),
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(icon);
        canvas.drawBitmap(
                resourceIcon,
                (icon.getWidth() - resourceIcon.getWidth()) / 2.0f,
                (icon.getHeight() - resourceIcon.getHeight()) / 2.0f,
                DEFAULT_PAINT);
        // Center the text.
        final float textLeft = (icon.getWidth() - textBounds.width()) / 2.0f - textBounds.left;
        final float textTop = (icon.getHeight() - textBounds.height()) / 2.0f - textBounds.top;
        // Draw the text.
        STROKE_PAINT.setTextSize(TEXT_SIZE * density);
        canvas.drawText(clusterTitle, textLeft, textTop, STROKE_PAINT);
        DEFAULT_PAINT.setTextSize(TEXT_SIZE * density);
        canvas.drawText(clusterTitle, textLeft, textTop, DEFAULT_PAINT);
        // Create the descriptor.
        final BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(icon);
        // Put it to the cache an return.
        descriptorCache.put(clusterSize, descriptor);
        return descriptor;
    }

    /**
     * Gets the cluster marker snippet.
     */
    private static String getClusterSnippet(final ArrayList<RefreshMapAsyncTask.Network> networks) {
        final StringBuilder snippetBuilder = new StringBuilder();
        final Iterator<RefreshMapAsyncTask.Network> networkIterator = networks.iterator();
        //noinspection WhileLoopReplaceableByForEach
        while (networkIterator.hasNext()) {
            if (snippetBuilder.length() != 0) {
                snippetBuilder.append(", ");
            }
            if (snippetBuilder.length() < MAX_SNIPPET_LENGTH) {
                snippetBuilder.append(networkIterator.next().getSsid());
            } else {
                snippetBuilder.append("...");
                break;
            }
        }
        return snippetBuilder.toString();
    }
}
