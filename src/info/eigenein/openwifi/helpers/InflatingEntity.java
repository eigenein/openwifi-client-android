package info.eigenein.openwifi.helpers;

import org.apache.http.*;
import org.apache.http.entity.*;

import java.io.*;
import java.util.zip.*;

/**
 * Simple {@link HttpEntityWrapper} that inflates the wrapped
 * {@link HttpEntity} by passing it through {@link GZIPInputStream}.
 * See https://androidto.googlecode.com/svn-history/r2/trunk/src/com/google/android/apps/iosched/service/SyncService.java
 */
public class InflatingEntity extends HttpEntityWrapper {
    public InflatingEntity(final HttpEntity wrapped) {
        super(wrapped);
    }

    @Override
    public InputStream getContent() throws IOException {
        return new GZIPInputStream(wrappedEntity.getContent());
    }

    @Override
    public long getContentLength() {
        return -1;
    }
}