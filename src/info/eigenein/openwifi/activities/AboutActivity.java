package info.eigenein.openwifi.activities;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.os.*;
import android.text.*;
import android.text.method.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.analytics.tracking.android.*;
import com.google.android.gms.common.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.*;

public class AboutActivity extends Activity {
    private static final String LOG_TAG = AboutActivity.class.getCanonicalName();

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        if (BuildHelper.isHoneyComb()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Hide the logo in landscape orientation.
        if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT) {
            findViewById(R.id.logo_image_view).setVisibility(View.GONE);
        }

        // Update version name text view.
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView versionTextView = (TextView)findViewById(R.id.about_version_text_view);
            versionTextView.setText(String.format(getString(R.string.text_view_version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
        }

        // Linkify copyright.
        final TextView copyrightTextView = (TextView)findViewById(R.id.about_copyright_text_view);
        copyrightTextView.setText(Html.fromHtml(getString(R.string.text_view_about_copyright)));
        copyrightTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Open source licenses link.
        final View openSourceLicensesView = findViewById(R.id.text_view_open_source_licenses);
        openSourceLicensesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                final Dialog dialog = new AlertDialog.Builder(AboutActivity.this)
                        .setTitle(R.string.dialog_title_open_source_licenses)
                        .setMessage(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(AboutActivity.this))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, final int i) {
                                dialogInterface.dismiss();
                            }
                        })
                        .setCancelable(true)
                        .create();
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
            }
        });

        // Linkify feedback link.
        final TextView feedbackLinkTextView = (TextView)findViewById(R.id.feedback_link_text_view);
        feedbackLinkTextView.setText(Html.fromHtml(getString(R.string.text_view_feedback)));
        feedbackLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Social network links.
        findViewById(R.id.goto_vkontakte_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                VibratorHelper.vibrate(AboutActivity.this);
                SocialNetworksHelper.gotoVKontakte(AboutActivity.this);
            }
        });
        findViewById(R.id.goto_facebook_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                VibratorHelper.vibrate(AboutActivity.this);
                SocialNetworksHelper.gotoFacebook(AboutActivity.this);
            }
        });
        findViewById(R.id.goto_twitter_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                VibratorHelper.vibrate(AboutActivity.this);
                SocialNetworksHelper.gotoTwitter(AboutActivity.this);
            }
        });
        findViewById(R.id.goto_google_plus_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                VibratorHelper.vibrate(AboutActivity.this);
                SocialNetworksHelper.gotoGooglePlus(AboutActivity.this);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        EasyTracker.getInstance().activityStop(this);
    }
}
