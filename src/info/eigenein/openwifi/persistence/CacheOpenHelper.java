package info.eigenein.openwifi.persistence;

import android.content.*;
import android.database.sqlite.*;
import android.util.*;

public final class CacheOpenHelper extends SQLiteOpenHelper {

    public static class DatabaseVersion {

        public static final int QUADTREES = 5;
    }

    private static final String LOG_TAG = CacheOpenHelper.class.getCanonicalName();

    private static final int DATABASE_VERSION = DatabaseVersion.QUADTREES;
    private static final String DATABASE_NAME = "cache.db";

    private static CacheOpenHelper instance;

    private final Context context;

    /**
     * Lazy singleton.
     * http://touchlabblog.tumblr.com/post/24474750219/single-sqlite-connection
     */
    public static synchronized CacheOpenHelper getInstance(final Context context) {
        if (instance == null) {
            instance = new CacheOpenHelper(context);
        }
        return instance;
    }

    private CacheOpenHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        new MyScanResult.Dao(database).onCreate(database);
    }

    @Override
    public void onUpgrade(
            final SQLiteDatabase database,
            final int oldVersion,
            final int newVersion) {
        Log.i(LOG_TAG + ".onUpgrade", String.format("%s, %s", oldVersion, newVersion));
        new MyScanResult.Dao(database).onUpgrade(database, context, oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(
            final SQLiteDatabase database,
            final int oldVersion,
            final int newVersion) {
        onUpgrade(database, oldVersion, newVersion);
    }

    public MyScanResult.Dao getMyScanResultDao() {
        return new MyScanResult.Dao(getWritableDatabase());
    }
}
