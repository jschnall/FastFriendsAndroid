package com.fastfriends.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.model.Tag;

import java.util.List;

/**
 * Created by jschnall on 6/18/14.
 */
public class CategoryAdapter extends BaseAdapter {
    private static final int ALL_CATEGORIES = 0;
    private Context mContext;
    private List<Tag> mCategories;

    public CategoryAdapter(Context context) {
        super();
        mContext = context;
        mCategories = Tag.getAllCategories();
        Tag allCategoriesTag = new Tag();
        allCategoriesTag.setName(context.getString(R.string.all_categories));
        mCategories.add(0, allCategoriesTag);
    }

    @Override
    public int getCount() {
        return mCategories.size();
    }

    @Override
    public Object getItem(int i) {
        return mCategories.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mCategories.get(i).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Tag tag = mCategories.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(tag.getName());

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        Tag tag = mCategories.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        } else {
            view = convertView;
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(tag.getName());

        return view;
    }

}
