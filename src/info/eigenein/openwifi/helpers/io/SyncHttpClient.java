package info.eigenein.openwifi.helpers.io;

import android.content.Context;
import android.util.Log;
import info.eigenein.openwifi.R;
import org.apache.http.HttpVersion;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.InputStream;
import java.security.KeyStore;

public class SyncHttpClient extends DefaultHttpClient {
    private final static String LOG_TAG = SyncHttpClient.class.getCanonicalName();

    private final static char[] TRUST_STORE_PASSWORD = "St6qe5en".toCharArray();

    private final static String KEY_STORE_PASSWORD = "rkJNiD2Mew9mYBo1";

    private final static HttpParams HTTP_PARAMS = new BasicHttpParams();

    private final Context context;

    static {
        // Use HTTP 1.1.
        HttpProtocolParams.setVersion(HTTP_PARAMS, HttpVersion.HTTP_1_1);
    }

    public SyncHttpClient(Context context) {
        super(HTTP_PARAMS);

        this.context = context;

        setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        getConnectionManager().getSchemeRegistry().register(
                new Scheme("https", createSslSocketFactory(), 443)
        );
    }

    /**
     * Initializes a new instance of SSL socket factory.
     */
    private SSLSocketFactory createSslSocketFactory() {
        try {
            // Load the key and trust stores.
            final KeyStore keyStore = loadKeyStore(R.raw.key_store, KEY_STORE_PASSWORD.toCharArray());
            final KeyStore trustStore = loadKeyStore(R.raw.trust_store, TRUST_STORE_PASSWORD);
            // Create and return the SSL socket factory.
            return new SSLSocketFactory(keyStore, KEY_STORE_PASSWORD, trustStore);
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
