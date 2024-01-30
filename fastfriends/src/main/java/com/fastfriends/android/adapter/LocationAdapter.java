package com.fastfriends.android.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jschnall on 6/18/14.
 */
public class LocationAdapter extends BaseAdapter {
    // These must align with R.array.event_location_choices
    public static final int MY_LOCATION = 0;
    public static final int OTHER_LOCATION = 1;
    public static final int CUSTOM_LOCATION = 2;

    private Context mContext;
    private List<String> mChoices;

    private Location mOtherLocation;

    public LocationAdapter(Context context) {
        super();
        mContext = context;
        mChoices = new ArrayList();
        mChoices.addAll(Arrays.asList(context.getResources().getStringArray(R.array.event_location_choices)));
    }

    @Override
    public int getCount() {
        return mChoices.size();
    }

    @Override
    public Object getItem(int i) {
        return mChoices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String choice = mChoices.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        } else {
            view = convertView;
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(choice);

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        String choice = mChoices.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(choice);
        if (position == OTHER_LOCATION) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_place, 0);
        } else {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        }
        return view;
    }

    public void setOtherLocation(Location location) {
        mOtherLocation = location;
        if (mChoices.size() > CUSTOM_LOCATION) {
            mChoices.set(CUSTOM_LOCATION, location.getStreetAddress());
        } else {
            mChoices.add(CUSTOM_LOCATION, location.getStreetAddress());
        }
        notifyDataSetChanged();
    }

    public Location getOtherLocation() {
        return mOtherLocation;
    }
}
