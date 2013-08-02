package info.eigenein.openwifi.helpers;

import android.app.*;
import android.content.*;
import android.util.*;
import com.google.android.gms.common.*;

public class GooglePlayServicesHelper {
    private static final String LOG_TAG = GooglePlayServicesHelper.class.getCanonicalName();

    public static boolean check(final Activity activity) {
        final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        Log.d(LOG_TAG + ".check", String.format("Error code: %s.", errorCode));

        if (errorCode != ConnectionResult.SUCCESS) {
            Log.i(LOG_TAG + ".check", "Initializing the error dialog ...");

            final Dialog dialog = GooglePlayServicesUtil.getErrorDialog(errorCode, activity, 0);
            dialog.setCancelable(true);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(final DialogInterface dialogInterface) {
                    // We can't work without Google Play Services.
                    activity.finish();
                }
            });
            dialog.show();

            // Google Play services are not available right now.
            return false;
        }

        // Google Play services are installed.
        return true;
    }
}
