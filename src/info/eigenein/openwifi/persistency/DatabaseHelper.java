package info.eigenein.openwifi.persistency;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.persistency.entities.StoredLocation;
import info.eigenein.openwifi.persistency.entities.StoredScanResult;

import java.sql.SQLException;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String LOG_TAG = DatabaseHelper.class.getCanonicalName();

    private static final String DATABASE_NAME = "cache.db";

    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        Log.i(LOG_TAG, "onCreate");
        try {
            TableUtils.createTable(connectionSource, StoredLocation.class);
            TableUtils.createTable(connectionSource, StoredScanResult.class);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to create database.", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase sqLiteDatabase,
            ConnectionSource connectionSource,
            int oldVersion,
            int newVersion) {
        // TODO.
    }
}
