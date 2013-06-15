package info.eigenein.openwifi.helpers.ui;

import android.content.*;
import android.graphics.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.entities.*;
import info.eigenein.openwifi.helpers.formatters.*;

import java.util.*;

public class MapOverlayHelper {
    private static final BitmapDescriptor MARKER_BITMAP_DESCRIPTOR =
            BitmapDescriptorFactory.fromResource(R.drawable.ic_cluster);

    private static final int MAX_SNIPPET_LENGTH = 24;

    private final Context context;

    private final GoogleMap map;

    public MapOverlayHelper(final Context context, final GoogleMap map) {
        this.context = context;
        this.map = map;
    }

    public void clear() {
        map.clear();
    }

    public Marker addCluster(final Cluster cluster) {
        // Cluster title.
        final int clusterSize = cluster.size();
        final String clusterTitle = clusterSize == 1 ?
                cluster.iterator().next().getSsid() :
                String.format(
                        "%d %s",
                        clusterSize,
                        context.getString(CountFormatter.format(
                                clusterSize,
                                R.string.overlay_networks_string_1,
                                R.string.overlay_networks_string_2,
                                R.string.overlay_networks_string_3)));
        // Build the snippet.
        final StringBuilder snippetBuilder = new StringBuilder();
        Iterator<Network> networkIterator = cluster.iterator();
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
        // Add the marker.
        final Marker marker = map.addMarker(new MarkerOptions()
                .position(cluster.getArea().getLatLng())
                .title(clusterTitle)
                .icon(MARKER_BITMAP_DESCRIPTOR)
                .anchor(0.5f, 0.5f)
                .snippet(snippetBuilder.toString())
        );
        // Add the circle.
        map.addCircle(new CircleOptions()
                .center(cluster.getArea().getLatLng())
                .radius(cluster.getArea().getAccuracy())
                .fillColor(Color.argb(32, 0, 0, 0))
                .strokeColor(Color.argb(16, 0, 0, 0))
                .strokeWidth(1.0f)
                .zIndex(-1.0f)
        );
        // Return the marker.
        return marker;
    }

    public void addGrid() {
        final GridSize gridSize = GridSizeHelper.get(map.getCameraPosition().zoom);
        final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;

        for (double latitude = bounds.southwest.latitude;
                latitude < bounds.northeast.latitude;
                latitude += gridSize.getLatitideStep()) {
            for (double longitude = bounds.southwest.longitude;
                    longitude < bounds.northeast.longitude;
                    longitude += gridSize.getLongitudeStep()) {
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(latitude, longitude + gridSize.getLongitudeStep()))
                        .add(new LatLng(latitude, longitude))
                        .add(new LatLng(latitude + gridSize.getLatitideStep(), longitude))
                        .color(Color.RED)
                        .width(1.0f)
                );
            }
        }
    }
}
