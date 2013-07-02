package info.eigenein.openwifi.persistence;

import android.database.sqlite.*;

public abstract class BaseDao {
    protected final SQLiteDatabase database;

    protected BaseDao(final SQLiteDatabase database) {
        this.database = database;
    }

    public abstract void onCreate(final SQLiteDatabase database);

    public void close() {
        database.close();
    }
}
