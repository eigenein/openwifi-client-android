package info.eigenein.openwifi.activities;

import android.app.*;
import android.os.Bundle;
import android.support.v4.view.*;
import android.view.*;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.helpers.ui.*;

import java.util.*;

public class HelpActivity extends Activity {
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.help);

        final LayoutInflater inflater = LayoutInflater.from(this);
        final List<View> pages = Arrays.asList(
                inflater.inflate(R.layout.help_welcome, null),
                inflater.inflate(R.layout.help_scan, null)
        );

        final ViewPager viewPager = new ViewPager(this);
        viewPager.setAdapter(new ListPagerAdapter(pages));
        viewPager.setCurrentItem(0); // TODO: extract current item from bundle.

        setContentView(viewPager);

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
            for (int i = 0; i < 2; i++) {
                actionBar.addTab(actionBar.newTab().setText("Tab " + (i + 1)).setTabListener(tabListener));
            }
            // Select the corresponding tab when the user swipes between pages with a touch gesture.
            viewPager.setOnPageChangeListener(
                    new ViewPager.SimpleOnPageChangeListener() {
                        @Override
                        public void onPageSelected(int position) {
                            getActionBar().setSelectedNavigationItem(position);
                        }
                    });
        }
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