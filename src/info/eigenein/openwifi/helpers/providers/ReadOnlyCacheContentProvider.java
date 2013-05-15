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
    public ParcelFileDescriptor openFile(final Uri uri, final String mode) throws FileNotFoundException {
        Log.i(LOG_TAG + ".openFile ", uri.toString());

        final File cacheDir = getContext().getCacheDir();
        final File privateFile = new File(cacheDir, uri.getLastPathSegment());

        return ParcelFileDescriptor.open(privateFile, ParcelFileDescriptor.MODE_READ_ONLY);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Override
    public Cursor query(
            final Uri uri,
            final String[] strings,
            final String s,
            final String[] strings2,
            final String s2) {
        return null;
    }

    @Override
    public String getType(final Uri uri) {
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(final Uri uri, final String s, final String[] strings) {
        return 0;
    }

    @Override
    public int update(
            final Uri uri,
            final ContentValues contentValues,
            final String s,
            final String[] strings) {
        return 0;
    }
}
