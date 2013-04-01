package info.eigenein.openwifi.activities;

import android.app.ListActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import info.eigenein.openwifi.R;
import info.eigenein.openwifi.helpers.entities.Network;
import info.eigenein.openwifi.helpers.ui.VibratorHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class NetworkSetActivity extends ListActivity {
    public static final String NETWORK_SET_KEY = "networkSet";

    private static final String[] adapterFrom = { "network_name" };

    private static final int[] adapterTo = { R.id.network_list_item_name};

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


        Bundle networkSetActivityBundle = getIntent().getExtras();
        if (networkSetActivityBundle != null) {
            HashSet<Network> networkSet = (HashSet<Network>)networkSetActivityBundle.getSerializable(NETWORK_SET_KEY);
            setListAdapter(createAdapter(networkSet));
        } else {
            setListAdapter(createAdapter(null));
        }
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        VibratorHelper.vibrate(l.getContext());
    }

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

    private HashMap<String, String> createItem(Network network) {
        HashMap<String, String> item = new HashMap<String, String>();
        item.put("network_name", network.getSsid());
        return item;
    }
}
