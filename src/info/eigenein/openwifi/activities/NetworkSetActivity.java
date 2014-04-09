package info.eigenein.openwifi.activities;

import android.annotation.*;
import android.app.*;
import android.content.res.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.google.analytics.tracking.android.*;
import info.eigenein.openwifi.*;
import info.eigenein.openwifi.helpers.*;
import info.eigenein.openwifi.tasks.*;

import java.util.*;

public class NetworkSetActivity extends ListActivity {
    private static final String LOG_TAG = NetworkSetActivity.class.getCanonicalName();

    public static final String NETWORK_SET_KEY = "networkSet";

    private static final String[] adapterFrom = { "network_name" };

    private static final int[] adapterTo = { R.id.network_list_item_name };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildHelper.isHoneyComb()) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final Bundle networkSetActivityBundle = getIntent().getExtras();
        if (networkSetActivityBundle != null) {
            final ArrayList<RefreshMapAsyncTask.Network> networkSet =
                    (ArrayList<RefreshMapAsyncTask.Network>)networkSetActivityBundle.getSerializable(NETWORK_SET_KEY);
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
            final ContextMenu menu,
            final View v,
            final ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        Log.d(LOG_TAG, "onCreateContextMenu");

        final MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.network_context_menu, menu);

        // Obtain selected SSID.
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        final HashMap<String, String> listItem =
                (HashMap<String, String>)getListAdapter().getItem(info.position);

        // Update the menu.
        final MenuItem ignoreNetworkMenuItem = menu.findItem(R.id.menu_item_ignore_network);
        ignoreNetworkMenuItem.setTitle(String.format(
                ignoreNetworkMenuItem.getTitle().toString(),
                listItem.get("network_name")
        ));

        // TODO: not implemented.
        final MenuItem.OnMenuItemClickListener notImplementedListener = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Toast.makeText(
                        NetworkSetActivity.this,
                        R.string.toast_feature_is_not_implemented,
                        Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        };
        ignoreNetworkMenuItem.setOnMenuItemClickListener(notImplementedListener);
        menu.findItem(R.id.menu_item_ignore_similar_networks).setOnMenuItemClickListener(notImplementedListener);
        menu.findItem(R.id.menu_item_show_network_details).setOnMenuItemClickListener(notImplementedListener);
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

    @Override
    protected void onListItemClick(final ListView l, final View v, final int position, final long id) {
        Toast.makeText(this, R.string.toast_feature_is_not_implemented, Toast.LENGTH_SHORT).show();
        VibratorHelper.vibrate(l.getContext());
    }

    /**
     * Creates the adapter for the list.
     */
    private SimpleAdapter createAdapter(final Collection<RefreshMapAsyncTask.Network> networks) {
        final ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

        for (final RefreshMapAsyncTask.Network network : networks) {
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
    private HashMap<String, String> createItem(final RefreshMapAsyncTask.Network network) {
        final HashMap<String, String> item = new HashMap<String, String>();
        item.put("network_name", network.getSsid());
        return item;
    }
}
