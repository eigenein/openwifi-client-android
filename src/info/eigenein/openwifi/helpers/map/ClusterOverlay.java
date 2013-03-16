package info.eigenein.openwifi.helpers.map;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.FloatMath;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.entities.Area;
import info.eigenein.openwifi.helpers.entities.Cluster;
import info.eigenein.openwifi.helpers.location.L;

public class ClusterOverlay extends Overlay {

    private final GeoPoint geoPoint;

    private final Area area;

    private final Bitmap clusterBitmap;

    public ClusterOverlay(Context context, Cluster cluster) {
        this.clusterBitmap = BitmapFactory.decodeResource(
                context.getResources(),
                R.drawable.ic_cluster);
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

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setAlpha(32);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        canvas.drawCircle((float)point.x, (float)point.y, radius, paint);
        canvas.drawBitmap(
                clusterBitmap,
                (float)(point.x - clusterBitmap.getWidth() / 2.0),
                (float)(point.y - clusterBitmap.getHeight() / 2.0),
                new Paint());
    }
}
