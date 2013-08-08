package info.eigenein.openwifi.persistence;

import android.content.*;
import android.database.sqlite.*;

public abstract class BaseDao {
    protected final SQLiteDatabase database;

    protected BaseDao(final SQLiteDatabase database) {
        this.database = database;
    }

    public abstract void onCreate(final SQLiteDatabase database);

    public abstract void onUpgrade(
            final SQLiteDatabase database,
            final Context context,
            final int oldVersion,
            final int newVersion);
}
