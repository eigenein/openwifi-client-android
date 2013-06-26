package info.eigenein.openwifi.activities;

import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.support.v4.view.*;
import android.view.*;
import android.widget.*;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.enums.*;
import info.eigenein.openwifi.helpers.internal.*;
import info.eigenein.openwifi.helpers.services.*;
import info.eigenein.openwifi.helpers.ui.*;
import info.eigenein.openwifi.services.*;

import java.util.*;

public class HelpActivity extends Activity {
    private static final String LOG_TAG = HelpActivity.class.getCanonicalName();

    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help);

        // Initialize the pages.
        final LayoutInflater inflater = LayoutInflater.from(this);
        final View helpScanView = inflater.inflate(R.layout.help_scan, null);
        final View helpFinishView = inflater.inflate(R.layout.help_finish, null);
        final List<View> pages = Arrays.asList(
                inflater.inflate(R.layout.help_welcome, null),
                helpScanView,
                helpFinishView
        );

        // Initialize the pager.
        final ViewPager viewPager = new ViewPager(this);
        viewPager.setAdapter(new ListPagerAdapter(pages));
        viewPager.setCurrentItem(0); // TODO: extract current item from bundle.

        setContentView(viewPager);

        // Initialize the action bar.
        if (BuildHelper.isHoneyComb()) {
            final ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Specify that tabs should be displayed in the action bar.
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // Create a tab listener that is called when the user changes tabs.
            final ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(final ActionBar.Tab tab, final FragmentTransaction ft) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                public void onTabUnselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
                    // Do nothing.
                }

                public void onTabReselected(final ActionBar.Tab tab, final FragmentTransaction ft) {
                    // Do nothing.
                }
            };
            // Add the tabs, specifying the tab's text and TabListener.
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_help_welcome).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_help_scan).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText(R.string.tab_help_finish).setTabListener(tabListener));
            // Select the corresponding tab when the user swipes between pages with a touch gesture.
            viewPager.setOnPageChangeListener(
                    new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            getActionBar().setSelectedNavigationItem(position);
                        }
                    });
        }

        // Handle "Start your search now".
        final Button startYourSearchNowButton = (Button)helpScanView.findViewById(R.id.button_help_start_scan_now);
        startYourSearchNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VibratorHelper.vibrate(HelpActivity.this);
                ScanIntentService.restart(HelpActivity.this);
                Toast.makeText(HelpActivity.this, R.string.toast_scan_started, Toast.LENGTH_LONG).show();
            }
        });

        // Handle "Log in with Google".
        final Button logInButton = (Button)helpFinishView.findViewById(R.id.button_help_log_in);
        logInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Authenticator.authenticate(HelpActivity.this, true, false, true, true, true, new Authenticator.AuthenticatedHandler() {
                    @Override
                    public void onAuthenticated(
                            final AuthenticationStatus status,
                            final String authToken,
                            final String accountName) {
                        if (status == AuthenticationStatus.AUTHENTICATED) {
                            logInButton.setText(R.string.help_finish_logged_in);
                        }
                    }
                });
            }
        });

        // Handle "Close help".
        final Button closeButton = (Button)helpFinishView.findViewById(R.id.button_help_close);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                HelpActivity.this.onBackPressed();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.help, menu);

        return true;
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

        // Run first-time syncing.
        final Settings settings = Settings.with(this);
        if (settings.syncStatus() == SyncIntentServiceStatus.NOT_SYNCING &&
                settings.lastSyncTime() == 0L) {
            android.util.Log.i(LOG_TAG + ".onStart", "Running first-time syncing.");
            SyncIntentService.start(this, true);
        }

        EasyTracker.getInstance().activityStop(this);
    }
}