package info.eigenein.openwifi.helpers.ui;

import android.content.*;
import android.net.*;

public class SocialNetworksHelper {
    public static void gotoVKontakte(final Context context) {
        Intent intent;
        // try {
        //     getPackageManager().getPackageInfo("com.vkontakte.android", 0);
        //     vkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(
        //             "fb://group/113471102159744"));
        // } catch (Exception e) {
        intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vk.com/owifi"));
        // }
        context.startActivity(intent);
    }

    public static void gotoFacebook(final Context context) {
        Intent intent;
        try {
            context.getPackageManager().getPackageInfo("com.facebook.katana", 0);
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "fb://group/113471102159744"));
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://www.facebook.com/groups/openwifi.project/"));
        }
        context.startActivity(intent);
    }

    public static void gotoTwitter(final Context context) {
        Intent intent;
        try {
            context.getPackageManager().getPackageInfo("com.twitter.android", 0);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.twitter.android", "com.twitter.android.ProfileActivity");
            intent.putExtra("user_id", 1436816953L);
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/owifi"));
        }
        context.startActivity(intent);
    }

    public static void gotoGooglePlus(final Context context) {
        Intent intent;
        try {
            context.getPackageManager().getPackageInfo("com.google.android.apps.plus", 0);
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setClassName("com.google.android.apps.plus", "com.google.android.apps.plus.phone.UrlGatewayActivity");
            intent.putExtra("customAppUri", "communities/106829221677020050162");
        } catch (Exception e) {
            intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://plus.google.com/communities/106829221677020050162"));
        }
        context.startActivity(intent);
    }
}
