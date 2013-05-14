package info.eigenein.openwifi.helpers.io;

import android.content.Context;
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
    private final static HttpParams httpParams = new BasicHttpParams();

    private final Context context;

    static {
        // Use HTTP 1.1.
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
    }

    public SyncHttpClient(Context context) {
        super(httpParams);

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
            // Initialize the trust store.
            final KeyStore trustStore = KeyStore.getInstance("BKS");
            // Load the resource with the trust store.
            final InputStream trustStoreInputStream =
                    context.getResources().openRawResource(R.raw.trust_store);
            // Load the key store.
            try {
                trustStore.load(trustStoreInputStream, "St6qe5en".toCharArray());
            } finally {
                trustStoreInputStream.close();
            }
            // Create the socket factory with the specified trust store.
            return new SSLSocketFactory(trustStore);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL socket factory.", e);
        }
    }
}
