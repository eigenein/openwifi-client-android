package info.eigenein.openwifi.helpers.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Used to provide access to the internal cache.
 */
public class ReadOnlyCacheContentProvider extends ContentProvider {
    private static final String LOG_TAG = ReadOnlyCacheContentProvider.class.getCanonicalName();

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        Log.i(LOG_TAG + ".openFile ", uri.toString());

        File cacheDir = getContext().getCacheDir();
        File privateFile = new File(cacheDir, uri.getLastPathSegment());

        return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
