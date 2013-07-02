package info.eigenein.openwifi.persistence;

import android.util.*;

/**
 * Provides the ability to cancel an operation in progress.
 */
public final class CancellationToken {
    private static final String LOG_TAG = CancellationToken.class.getCanonicalName();

    private volatile boolean cancelled;

    /**
     * Cancel the operation.
     */
    public synchronized void cancel() {
        Log.d(LOG_TAG + ".cancel@" + hashCode(), "Cancelled.");

        this.cancelled = true;
    }

    /**
     * Returns true if the operation has been canceled.
     */
    public synchronized boolean isCancelled() {
        Log.d(LOG_TAG + ".isCancelled@" + hashCode(), String.format("Cancelled: %s.", cancelled));

        return cancelled;
    }
}
