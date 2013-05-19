package info.eigenein.openwifi.activities;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.Settings;

public class AboutActivity extends Activity {
    private static final String LOG_TAG = AboutActivity.class.getCanonicalName();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Update version name text view.
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            TextView versionTextView = (TextView)findViewById(R.id.about_version_text_view);
            versionTextView.setText(String.format(getString(R.string.version), versionName));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(LOG_TAG, e.toString());
        }

        // Linkify copyright.
        TextView copyrightTextView = (TextView)findViewById(R.id.about_copyright_text_view);
        copyrightTextView.setText(Html.fromHtml(getString(R.string.about_copyright)));
        copyrightTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Linkify project link.
        TextView projectLinkTextView = (TextView)findViewById(R.id.about_project_link_text_view);
        projectLinkTextView.setText(Html.fromHtml(getString(R.string.about_project_link)));
        projectLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Linkify feedback link.
        TextView feedbackLinkTextView = (TextView)findViewById(R.id.feedback_link_text_view);
        feedbackLinkTextView.setText(Html.fromHtml(getString(R.string.feedback_link)));
        feedbackLinkTextView.setMovementMethod(LinkMovementMethod.getInstance());

        // Client ID.
        TextView clientIdTextView = (TextView)findViewById(R.id.client_id_text_view);
        clientIdTextView.setText(String.format(
                getString(R.string.client_id),
                Settings.with(this).clientId()));
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
