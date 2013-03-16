package info.eigenein.openwifi.helpers.ui;

import android.content.Context;
import info.eigenein.openwifi.R;
import ru.yandex.yandexmapkit.map.MapLayer;

import java.util.HashMap;
import java.util.List;

public class MapLayerHelper {
    private static HashMap<String, Integer> resourceIds = new HashMap<String, Integer>();

    static {
        resourceIds.put("map", R.string.map_layer_map);
        resourceIds.put("sat,skl", R.string.map_layer_sat_skl);
        resourceIds.put("pmap", R.string.map_layer_pmap);
    }

    public static CharSequence[] getMapLayerNames(Context context, List mapLayers) {
        CharSequence[] names = new CharSequence[mapLayers.size()];

        for (int i = 0; i < mapLayers.size(); i++) {
            MapLayer mapLayer = (MapLayer)mapLayers.get(i);
            Integer id = resourceIds.get(mapLayer.requestName);
            // Return localized string if available.
            if (id != null) {
                names[i] = context.getString(id);
            } else {
                names[i] = mapLayer.name;
            }
        }

        return names;
    }
}
