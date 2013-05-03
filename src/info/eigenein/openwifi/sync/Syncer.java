package info.eigenein.openwifi.sync;

import android.content.Context;
import org.apache.http.HttpResponse;

/**
 * Performs syncronization of some entities.
 */
public abstract class Syncer {
    public abstract TaggedRequest getNextRequest(Context context);

    public abstract boolean processResponse(
            Context context,
            TaggedRequest request,
            HttpResponse response);

    public abstract int getSyncedEntitiesCount();
}
