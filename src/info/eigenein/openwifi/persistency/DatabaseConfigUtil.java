package info.eigenein.openwifi.persistency;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

/**
 * ORMLite configuration update utility.
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {
    private static final Class<?>[] classes = new Class[] {
            MyScanResult.class,
    };

    public static void main(final String[] args) throws Exception {
        writeConfigFile("ormlite_config.txt", classes);
    }
}
