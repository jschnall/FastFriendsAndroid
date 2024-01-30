package com.fastfriends.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.fragment.AlbumFragment;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.model.Resource;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class ResourceListAdapter extends BaseAdapter {
    private static final String LOGTAG = ResourceListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Resource> mResources;
    private HashSet<Long> mSelectedItemIds = new HashSet<Long>();
    private int mDisplayType;

    public ResourceListAdapter(Activity activity, int displayType) {
        mActivity = activity;
        mResources = new ArrayList<Resource>();
        mDisplayType = displayType;
    }

    @Override
    public int getCount() {
        return mResources.size();
    }

    @Override
    public Object getItem(int i) {
        return mResources.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mResources.get(i).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Resource resource = mResources.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            if (mDisplayType == AlbumFragment.DISPLAY_GRID) {
                view = inflater.inflate(R.layout.grid_resource_item, parent, false);
            } else {
                view = inflater.inflate(R.layout.list_resource_item, parent, false);
            }
        } else {
            view = convertView;
        }

        populateView(resource, view);
        view.setTag(resource.getId());

        return view;
    }

    private void populateView(Resource resource, View view) {
        final ImageView imageView = (ImageView) view.findViewById(R.id.portrait);
        final String url = resource.getThumbnail();

        boolean loadImage = false;
        Object tag = view.getTag();
        if (tag == null) {
            loadImage = true;
        } else {
            long resourceId = (Long) tag;
            if (resourceId != resource.getId()) {
                loadImage = true;
            }
        }
        if (loadImage) {
            imageView.setImageResource(R.drawable.ic_picture);
            if (url != null) {
                ImageLoader.getInstance().displayImage(url, imageView);
            }
        }

        View selectorView = view.findViewById(R.id.selector);
        if (selectorView != null) {
            if (isItemSelected(resource.getId())) {
                selectorView.setVisibility(View.VISIBLE);
            } else {
                selectorView.setVisibility(View.GONE);
            }
        }

        TextView textView = (TextView) view.findViewById(R.id.text);
        String caption = resource.getCaption();
        if (TextUtils.isEmpty(caption)) {
            textView.setVisibility(View.GONE);
        } else {
            CharSequence body = TagHelper.getInstance().markup(mActivity, caption, resource.getMentions(), TagHelper.SEARCH_PLANS);
            textView.setText(body);
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void reset(List<Resource> resources) {
        mResources = resources;
        notifyDataSetChanged();
    }

    // Contextual action mode
    public int addSelection(long id) {
        mSelectedItemIds.add(id);
        return mSelectedItemIds.size();
    }

    public int removeSelection(long id) {
        mSelectedItemIds.remove(id);
        return mSelectedItemIds.size();
    }

    public void clearSelection() {
        mSelectedItemIds.clear();
    }

    public boolean isItemSelected(long id) {
        return mSelectedItemIds.contains(id);
    }

    public List<Long> getSelectedItemIds() {
        return new ArrayList<Long>(mSelectedItemIds);
    }
}
