package info.eigenein.openwifi.helpers;

import android.support.v4.view.*;
import android.view.*;

import java.util.*;

public class ListPagerAdapter extends PagerAdapter {
    private final List<View> pages;

    public ListPagerAdapter(final List<View> pages){
        this.pages = pages;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Object instantiateItem(final ViewGroup collection, final int position) {
        final View view = pages.get(position);
        collection.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(final ViewGroup collection, final int position, final Object view){
        collection.removeView((View)view);
    }

    @Override
    public boolean isViewFromObject(final View view, final Object o) {
        return view.equals(o);
    }
}
