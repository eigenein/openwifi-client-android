package info.eigenein.openwifi.helpers.map;

import android.content.Context;
import android.graphics.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.formatters.CountFormatter;
import info.eigenein.openwifi.helpers.location.L;

public class ClusterOverlay {

    private static final float TEXT_SIZE = 14.0f;

    /**
     * Used to draw the cluster area.
     */
    private static final Paint circlePaint = new Paint();
    /**
     * Used to draw the bitmap and the text.
     */
    private static final Paint defaultPaint = new Paint();
    /**
     * Used to paint the text outline.
     */
    private static final Paint strokePaint = new Paint();

    private final Cluster cluster;
    private final GeoPoint clusterCenter;

    private final Bitmap clusterBitmap;
    private final String clusterString;

    /**
     * Screen density.
     */
    private final float density;

    static
    {
        defaultPaint.setAntiAlias(true);
        defaultPaint.setColor(Color.BLACK);
        defaultPaint.setTextSize(TEXT_SIZE);
        defaultPaint.setTypeface(Typeface.DEFAULT_BOLD);

        strokePaint.setAntiAlias(true);
        strokePaint.setColor(Color.WHITE);
        strokePaint.setStrokeWidth(4.0f);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setTextSize(TEXT_SIZE);
        strokePaint.setTypeface(Typeface.DEFAULT_BOLD);

        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLACK);
        circlePaint.setAlpha(32);
        circlePaint.setStyle(Paint.Style.FILL);
    }

    public ClusterOverlay(final Context context, final Cluster cluster) {
        this.clusterBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.ic_cluster);
        final int clusterSize = cluster.size();
        // Overlay string.
        if (cluster.size() == 1) {
            // Network name.
            this.clusterString = cluster.iterator().next().getSsid();
        } else {
            // Network count string.
            this.clusterString = String.format(
                    "%d %s",
                    clusterSize,
                    context.getString(CountFormatter.format(
                            clusterSize,
                            R.string.overlay_networks_string_1,
                            R.string.overlay_networks_string_2,
                            R.string.overlay_networks_string_3)));
        }
        // Used to draw the area.
        this.cluster = cluster;
        this.clusterCenter = new GeoPoint(
                L.toE6(cluster.getArea().getLatitude()),
                L.toE6(cluster.getArea().getLongitude()));
        // Used to draw the text.
        this.density = context.getResources().getDisplayMetrics().density;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public void draw(final Canvas canvas, final MapView mapView, final boolean shadow) {
        if(shadow) {
            // Ignore the shadow layer.
            return;
        }

        // Obtain screen coordinates and radius.
        final Projection projection = mapView.getProjection();
        final Point point = new Point();
        projection.toPixels(clusterCenter, point);
        final float radius = (float)(
                projection.metersToEquatorPixels(cluster.getArea().getAccuracy()) *
                        (1.0 / Math.cos(Math.toRadians(cluster.getArea().getLatitude()))));
        final float x = (float)point.x;
        final float y = (float)point.y;

        // Draw the area.
        canvas.drawCircle(x, y, radius, circlePaint);

        // Draw the bitmap.
        final float bitmapLeft = x - clusterBitmap.getWidth() / 2.0f;
        final float bitmapTop = y - clusterBitmap.getHeight() / 2.0f;
        canvas.drawBitmap(clusterBitmap, bitmapLeft, bitmapTop, defaultPaint);

        // Find the text bounds.
        final Rect textBounds = new Rect();
        defaultPaint.getTextBounds(clusterString, 0, clusterString.length(), textBounds);
        final float textLeft = bitmapLeft + clusterBitmap.getWidth();
        final float textTop = y + textBounds.height() / 2.0f;
        // Draw the text.
        strokePaint.setTextSize(TEXT_SIZE * density);
        canvas.drawText(clusterString, textLeft, textTop, strokePaint);
        defaultPaint.setTextSize(TEXT_SIZE * density);
        canvas.drawText(clusterString, textLeft, textTop, defaultPaint);
    }
}
