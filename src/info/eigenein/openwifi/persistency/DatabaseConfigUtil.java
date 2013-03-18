package info.eigenein.openwifi.persistency;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;
import info.eigenein.openwifi.persistency.entities.StoredLocation;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;

/**
 * ORMLite configuration update utility.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[] {
            StoredScanResult.class,
            StoredLocation.class,
    };

    public static void main(String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}
