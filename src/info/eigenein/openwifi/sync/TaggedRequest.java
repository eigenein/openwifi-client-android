package info.eigenein.openwifi.sync;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Contains the HTTP request together with optional tag object.
 */
public class TaggedRequest {
    private final HttpUriRequest request;

    private final Object tag;

    public TaggedRequest(HttpUriRequest request, Object tag) {
        this.request = request;
        this.tag = tag;
    }

    public HttpUriRequest getRequest() {
        return request;
    }

    public Object getTag() {
        return tag;
    }
}
