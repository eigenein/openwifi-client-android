package info.eigenein.openwifi.helpers.internal;

import android.app.*;
import android.content.*;
import com.google.android.gms.common.*;

public class GooglePlayServicesHelper {
    public static void check(final Activity activity) {
        final int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity);
        if (errorCode != ConnectionResult.SUCCESS) {
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
        }
    }
}
