package info.eigenein.openwifi.helpers;

import android.content.*;
import android.util.*;
import info.eigenein.openwifi.*;
import org.apache.http.*;
import org.apache.http.conn.scheme.*;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.*;
import org.apache.http.params.*;
import org.apache.http.protocol.*;

import java.io.*;
import java.security.*;

public class SyncHttpClient extends DefaultHttpClient {
    private final static String LOG_TAG = SyncHttpClient.class.getCanonicalName();

    private final static char[] TRUST_STORE_PASSWORD = "St6qe5en".toCharArray();

    private final static String ACCEPT_ENCODING_HEADER = "Accept-Encoding";
    private final static String ENCODING_GZIP = "gzip";

    private final static HttpParams HTTP_PARAMS = new BasicHttpParams();

    private final Context context;

    static {
        // Use HTTP 1.1.
        HttpProtocolParams.setVersion(HTTP_PARAMS, HttpVersion.HTTP_1_1);
        // Set up timeouts.
        HttpConnectionParams.setConnectionTimeout(HTTP_PARAMS, 20000);
        HttpConnectionParams.setSoTimeout(HTTP_PARAMS, 20000);
    }

    public SyncHttpClient(Context context) {
        super(HTTP_PARAMS);

        this.context = context;

        // Set up connection keep alive strategy.
        setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());

        // Set up HTTPS.
        getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", createSslSocketFactory(), 443)
        );

        // Set up GZip compression.
        // See http://stackoverflow.com/a/11402915/359730
        addRequestInterceptor(new HttpRequestInterceptor() {
            public void process(HttpRequest request, HttpContext context) {
                // Add header to accept GZip content.
                if (!request.containsHeader(ACCEPT_ENCODING_HEADER)) {
                    request.addHeader(ACCEPT_ENCODING_HEADER, ENCODING_GZIP);
                }
            }
        });
        addResponseInterceptor(new HttpResponseInterceptor() {
            public void process(HttpResponse response, HttpContext context) {
                // Inflate any responses compressed with GZip.
                final HttpEntity entity = response.getEntity();
                final Header contentEncoding = entity.getContentEncoding();
                if (contentEncoding != null) {
                    for (final HeaderElement element : contentEncoding.getElements()) {
                        if (element.getName().equalsIgnoreCase(ENCODING_GZIP)) {
                            response.setEntity(new InflatingEntity(response.getEntity()));
                            break;
                        }
                    }
                }
            }
        });
    }

    /**
     * Initializes a new instance of SSL socket factory.
     */
    private SSLSocketFactory createSslSocketFactory() {
        try {
            // Load the trust store.
            final KeyStore trustStore = loadKeyStore(R.raw.trust_store, TRUST_STORE_PASSWORD);
            // Create and return the SSL socket factory.
            return new SSLSocketFactory(trustStore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL socket factory.", e);
        }
    }

    /**
     * Loads the key store from the specified resource.
     */
    private KeyStore loadKeyStore(final int resourceId, final char[] password) {
        try {
            // Initialize the key store.
            final KeyStore keyStore = KeyStore.getInstance("BKS");
            // Load the resource with the key store.
            final InputStream trustStoreInputStream =
                    context.getResources().openRawResource(resourceId);
            // Load the key store.
            try {
                keyStore.load(trustStoreInputStream, password);
            } finally {
                trustStoreInputStream.close();
            }
            // Done.
            Log.d(LOG_TAG + ".loadKeyStore", "Loaded " + keyStore.size() + " key(s).");
            return keyStore;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load the key store..", e);
        }
    }
}
