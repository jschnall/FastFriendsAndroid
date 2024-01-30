package com.fastfriends.android.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class MutualFriendListAdapter extends BaseAdapter implements PageAdapterInterface<Friend> {
    private static final String LOGTAG = MutualFriendListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Friend> mFriends;

    public MutualFriendListAdapter(Activity activity) {
        mActivity = activity;
        mFriends = new ArrayList<Friend>();
    }

    @Override
    public int getCount() {
        return mFriends.size();
    }

    @Override
    public Object getItem(int i) {
        return mFriends.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mFriends.get(i).getUserId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Friend friend = mFriends.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_mutual_friend_item, parent, false);
        } else {
            view = convertView;
        }


        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = friend.getPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(friend.getPortrait(), portraitView);
        }

        TextView nameView = (TextView) view.findViewById(R.id.display_name);
        nameView.setText(friend.getUserName());
        view.setClickable(false);

        return view;
    }

    public void addPage(Page<Friend> friendPage) {
        mFriends.addAll(friendPage.getResults());
        notifyDataSetChanged();
    }

    public void reset(Page<Friend> friendPage) {
        mFriends.clear();
        addPage(friendPage);
    }

}