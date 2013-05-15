package info.eigenein.openwifi.receivers;

import android.os.*;

/**
 * Used to receive notifications from the sync service.
 */
public class SyncIntentServiceResultReceiver extends ResultReceiver {
    private final Receiver receiver;

    public SyncIntentServiceResultReceiver(Receiver receiver) {
        super(new Handler());

        this.receiver = receiver;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        }
    }

    /**
     * Represents the receiver of the notifications.
     */
    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle data);
    }
}
