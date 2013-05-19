package info.eigenein.openwifi.sync;

import android.content.Context;
import org.apache.http.HttpResponse;

/**
 * Performs syncronization of some entities.
 */
public abstract class Syncer {
    public abstract TaggedRequest getNextRequest(final Context context);

    public abstract boolean processResponse(
            final Context context,
            final TaggedRequest request,
            final HttpResponse response);

    public abstract long getSyncedEntitiesCount();
}
