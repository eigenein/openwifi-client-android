package info.eigenein.openwifi.persistence;

import android.content.*;
import android.database.sqlite.*;

public final class CacheOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "cache.db";

    private static CacheOpenHelper instance;

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

    public CacheOpenHelper(final Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(final SQLiteDatabase database) {
        new MyScanResultDao(database).onCreate(database);
    }

    @Override
    public void onUpgrade(
            final SQLiteDatabase database,
            final int oldVersion,
            final int newVersion) {
        // TODO.
    }

    public MyScanResultDao getMyScanResultDao() {
        return new MyScanResultDao(getWritableDatabase());
    }
}
