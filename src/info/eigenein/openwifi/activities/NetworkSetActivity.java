package info.eigenein.openwifi.activities;

import android.app.ListActivity;
import android.content.res.*;
import android.graphics.drawable.*;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import com.google.analytics.tracking.android.EasyTracker;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.helpers.entities.Network;
import info.eigenein.openwifi.helpers.ui.VibratorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NetworkSetActivity extends ListActivity {
    private static final String LOG_TAG = NetworkSetActivity.class.getCanonicalName();

    public static final String NETWORK_SET_KEY = "networkSet";

    private static final String[] adapterFrom = { "network_name" };

    private static final int[] adapterTo = { R.id.network_list_item_name };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildHelper.isHoneyComb()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Bundle networkSetActivityBundle = getIntent().getExtras();
        if (networkSetActivityBundle != null) {
            final HashSet<Network> networkSet = (HashSet<Network>)networkSetActivityBundle.getSerializable(NETWORK_SET_KEY);
            setListAdapter(createAdapter(networkSet));
        } else {
            setListAdapter(createAdapter(null));
        }

        final ListView listView = getListView();

        if (!BuildHelper.isHoneyComb()) {
            // Apply theme.
            final Resources resources = getResources();
            listView.setBackgroundColor(resources.getColor(R.color.screen_background_holo_light));
            listView.setDivider(resources.getDrawable(R.drawable.divider_horizontal_holo_light));
            listView.setDividerHeight(1);
        }

        registerForContextMenu(listView);
    }

    @Override
    public void onStart() {
        super.onStart();

        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onCreateContextMenu(
            ContextMenu menu,
            View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Log.d(LOG_TAG, "onCreateContextMenu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.network_context_menu, menu);

        // Obtain selected SSID.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        HashMap<String, String> listItem =
                (HashMap<String, String>)getListAdapter().getItem(info.position);

        // Update the menu.
        MenuItem ignoreNetworkMenuItem = menu.findItem(R.id.ignore_network_menu_item);
        ignoreNetworkMenuItem.setTitle(String.format(
                ignoreNetworkMenuItem.getTitle().toString(),
                listItem.get("network_name")
        ));
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

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        VibratorHelper.vibrate(l.getContext());
    }

    /**
     * Creates the adapter for the list.
     */
    private SimpleAdapter createAdapter(HashSet<Network> networkSet) {
        ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

        for (Network network : networkSet) {
            items.add(createItem(network));
        }

        return new SimpleAdapter(
                this,
                items,
                R.layout.network_list_item,
                adapterFrom,
                adapterTo);
    }

    /**
     * Creates the list item.
     */
    private HashMap<String, String> createItem(Network network) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("network_name", network.getSsid());
        return item;
    }
}
