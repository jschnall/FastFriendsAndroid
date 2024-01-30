package com.fastfriends.android.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.fragment.NavigationDrawerFragment;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedVignetteBitmapDisplayer;

/**
 * Created by jschnall on 2/12/14.
 */
public class NavigationDrawerAdapter extends BaseAdapter {
    private static final String LOGTAG = NavigationDrawerAdapter.class.getSimpleName();

    private Context mContext;
    private String[] mItems;
    private TypedArray mIcons;
    private int mSelectedItem;

    public NavigationDrawerAdapter(Context context) {
        mContext = context;
        Resources res = context.getResources();
        mItems = res.getStringArray(R.array.navigation_drawer_items);
        mIcons = res.obtainTypedArray(R.array.navigation_drawer_icons);
    }

    @Override
    public int getCount() {
        return mItems.length;
    }

    @Override
    public Object getItem(int i) {
        return mItems[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        SharedPreferences prefs = Settings.getSharedPreferences();
        String title = mItems[index];

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_drawer_item, parent, false);
        } else {
            view = convertView;
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView nameView = (TextView) view.findViewById(R.id.name);

        nameView.setText(title);
        nameView.setCompoundDrawablesWithIntrinsicBounds(mIcons.getDrawable(index), null, null, null);

        TextView countView = (TextView) view.findViewById(R.id.count);
        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        if (index == NavigationDrawerFragment.MESSAGES) {
            int count = prefs.getInt(Settings.UNREAD_MESSAGE_COUNT, 0);
            if (count > 0) {
                countView.setVisibility(View.VISIBLE);
                countView.setText(String.valueOf(count));
            } else {
                countView.setVisibility(View.GONE);
            }

            nameView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.GONE);
            portraitView.setVisibility(View.GONE);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        } else if (index == NavigationDrawerFragment.MY_PROFILE) {
            String displayName = prefs.getString(Settings.DISPLAY_NAME, null);
            nameView.setVisibility(View.GONE);
            titleView.setVisibility(View.VISIBLE);
            if (displayName != null) {
                titleView.setText(displayName);
            }

            countView.setVisibility(View.GONE);

            portraitView.setVisibility(View.VISIBLE);
            String portrait = prefs.getString(Settings.USER_PORTRAIT, null);
            if (portrait == null) {
                portraitView.setImageResource(R.drawable.ic_person);
            } else {
                ImageLoader.getInstance().displayImage(portrait, portraitView);
            }
            view.setBackgroundResource(R.drawable.selector_profile_drawer_item);
        } else {
            nameView.setVisibility(View.VISIBLE);
            titleView.setVisibility(View.GONE);
            portraitView.setVisibility(View.GONE);
            countView.setVisibility(View.GONE);
            view.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }

        nameView.setSelected(index == mSelectedItem);

        return view;
    }

    public int getSelectedItem() {
        return mSelectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        mSelectedItem = selectedItem;
        notifyDataSetChanged();
    }
}
