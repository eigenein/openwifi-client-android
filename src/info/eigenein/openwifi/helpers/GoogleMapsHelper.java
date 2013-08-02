package info.eigenein.openwifi.helpers;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import info.eigenein.openwifi.*;

public class GoogleMapsHelper {
    public static boolean check(final Activity activity) {
        if (!isInstalled(activity)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(R.string.dialog_install_google_maps_message);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.button_install, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, final int i) {
                    final Intent intent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.google.android.apps.maps"));
                    activity.startActivity(intent);
                    // Finish the activity so they can't circumvent the check.
                    activity.finish();
                }
            });
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(final DialogInterface dialogInterface) {
                    activity.finish();
                }
            });
            final AlertDialog dialog = builder.create();
            dialog.show();

            // Google Maps is not available right now.
            return false;
        }

        // Google Maps is installed.
        return true;
    }

    private static boolean isInstalled(final Context context)
    {
        try {
            context.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
