package com.fastfriends.android.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.model.DBManager;
import com.fastfriends.android.model.Tag;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jschnall on 2/21/14.
 */
public class TagPickerAdapter extends BaseExpandableListAdapter {
    public static final String LOGTAG = TagPickerAdapter.class.getSimpleName();

    Context mContext;
    List<Tag> mGroups;
    List<List<Tag>> mChildren;
    HashSet<String> mPickedTags;

    public interface OnTagSelectedListener {
        public void onTagAdded(String tagName);
        public void onTagRemoved(String tagName);
    }
    OnTagSelectedListener mOnTagSelectedListener;

    public TagPickerAdapter(Context context, List<String> pickedTags, OnTagSelectedListener listener) {
        super();
        mContext = context;

        mPickedTags = new HashSet<String>(pickedTags);
        mGroups = new ArrayList<Tag>();
        mChildren = new ArrayList<List<Tag>>();

        mOnTagSelectedListener = listener;

        refreshTags();
    }

    private void refreshTags() {
        List<Tag> AllTags = DBManager.getAll(Tag.class);

        if (AllTags == null) {
            return;
        }
        for (Tag tag : AllTags) {
            if (tag.getParentId() <= 0) {
                mGroups.add(tag);
            }
        }
        for (Tag group : mGroups) {
            long id = group.getId();
            List<Tag> subList = new ArrayList<Tag>();
            for (Tag tag : AllTags) {
                if (tag.getParentId() == id) {
                    subList.add(tag);
                }
            }
            mChildren.add(subList);
        }
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildren.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        Tag child = (Tag) getChild(groupPosition, childPosition);
        return child.getId();
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return mChildren.get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        Tag group = mGroups.get(groupPosition);
        return group.getId();
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        Tag tag = mChildren.get(groupPosition).get(childPosition);
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_tag_child, parent, false);
        }

        populateView(tag, view);

        return view;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        Tag tag = mGroups.get(groupPosition);
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_tag_group, parent, false);
        }

        ImageView expandView = (ImageView) view.findViewById(R.id.expand);
        if (getChildrenCount(groupPosition) == 0) {
            expandView.setVisibility(View.INVISIBLE);
        } else {
            expandView.setVisibility(View.VISIBLE);
        }
        if (isExpanded) {
            expandView.setImageResource(R.drawable.ic_expand);
        } else {
            expandView.setImageResource(R.drawable.next);
        }

        populateView(tag, view);

        return view;
    }

    private void populateView(final Tag tag, View view) {
        final ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        String iconUri = tag.getIconUrl();
        iconView.setImageDrawable(null);
        if (!TextUtils.isEmpty(iconUri)) {
            ImageLoader.getInstance().loadImage(iconUri, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    iconView.setImageBitmap(loadedImage);
                }
            });
        }

        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(tag.getName());

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(mPickedTags.contains(tag.getName()));
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                String tagName = tag.getName();
                if (checked) {
                    mPickedTags.add(tag.getName());
                    if (mOnTagSelectedListener != null) {
                        mOnTagSelectedListener.onTagAdded(tagName);
                    }
                } else {
                    mPickedTags.remove(tag.getName());
                    if (mOnTagSelectedListener != null) {
                        mOnTagSelectedListener.onTagRemoved(tagName);
                    }
                }

            }
        });
    }

    public HashSet<String> getPickedTags() {
        return mPickedTags;
    }

    public void removePickedTag(String tagName) {
        if (mPickedTags.remove(tagName)) {
            if (mOnTagSelectedListener != null) {
                mOnTagSelectedListener.onTagRemoved(tagName);
            }
            notifyDataSetChanged();
        }
    }

}
