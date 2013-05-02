package info.eigenein.openwifi.activities;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import com.google.analytics.tracking.android.EasyTracker;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.scan.ScanResultTracker;

import java.util.ArrayList;
import java.util.HashMap;

public class StatisticsActivity extends ListActivity {
    private static final String[] adapterFrom = { "title" , "text" };

    private static final int[] adapterTo = {android.R.id.text1, android.R.id.text2};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(createAdapter());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
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

    private SimpleAdapter createAdapter() {
        ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

        items.add(createItem(
                R.string.unique_bssid_count,
                Long.toString(ScanResultTracker.getUniqueBssidCount(this))
        ));
        items.add(createItem(
                R.string.unique_ssid_count,
                Long.toString(ScanResultTracker.getUniqueSsidCount(this))
        ));
        items.add(createItem(
                R.string.scan_result_count,
                Long.toString(ScanResultTracker.getScanResultCount(this))
        ));

        return new SimpleAdapter(
                this,
                items,
                android.R.layout.simple_list_item_2,
                adapterFrom,
                adapterTo);
    }

    private HashMap<String, String> createItem(int titleResourceId, String text) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("title", getString(titleResourceId));
        item.put("text", text);
        return item;
    }
}
