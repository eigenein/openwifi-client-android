package info.eigenein.openwifi.helpers.map;

import android.content.Context;
import android.graphics.*;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.location.L;

public class ClusterOverlay extends Overlay {
    private static final float TEXT_OFFSET = 8.0f;

    private static final Paint defaultPaint = new Paint();

    private final String clusterSizeString;

    private final GeoPoint geoPoint;

    private final Area area;

    private final Bitmap clusterBitmap;

    static
    {
        defaultPaint.setTextSize(16.0f);
    }

    public ClusterOverlay(Context context, Cluster cluster) {
        this.clusterBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.ic_cluster);
        this.clusterSizeString = Integer.toString(cluster.size());
        this.area = cluster.getArea();
        this.geoPoint = new GeoPoint(
                L.toE6(area.getLatitude()),
                L.toE6(area.getLongitude()));
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if(shadow) {
            // Ignore the shadow layer.
            return;
        }

        final Projection projection = mapView.getProjection();

        Point point = new Point();
        projection.toPixels(geoPoint, point);

        final float radius = (float)(
                projection.metersToEquatorPixels(area.getAccuracy()) *
                        (1.0 / Math.cos(Math.toRadians(area.getLatitude()))));

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.BLACK);
        circlePaint.setAlpha(32);
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);

        final float x = (float)point.x;
        final float y = (float)point.y;

        canvas.drawCircle(x, y, radius, circlePaint);
        canvas.drawBitmap(
                clusterBitmap,
                x - clusterBitmap.getWidth() / 2.0f,
                y - clusterBitmap.getHeight() / 2.0f,
                defaultPaint);
        canvas.drawText(clusterSizeString, x + TEXT_OFFSET, y - TEXT_OFFSET, defaultPaint);
    }
}
