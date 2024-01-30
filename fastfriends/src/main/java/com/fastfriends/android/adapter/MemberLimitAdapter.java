package com.fastfriends.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fastfriends.android.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jschnall on 6/18/14.
 */
public class MemberLimitAdapter extends BaseAdapter {
    public static final int NO_LIMIT = 0;
    public static final int SELECT_LIMIT = 1;
    public static final int CUSTOM_LIMIT = 2;

    private Context mContext;
    private List<String> mChoices;

    public MemberLimitAdapter(Context context) {
        super();
        mContext = context;
        mChoices = new ArrayList();
        mChoices.addAll(Arrays.asList(context.getResources().getStringArray(R.array.member_limit_choices)));
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

        return view;
    }

    public void setCustomLimit(int maxMembers) {
        if (mChoices.size() > CUSTOM_LIMIT) {
            mChoices.set(CUSTOM_LIMIT, String.valueOf(maxMembers));
        } else {
            mChoices.add(CUSTOM_LIMIT, String.valueOf(maxMembers));
        }
        notifyDataSetChanged();
    }
}
