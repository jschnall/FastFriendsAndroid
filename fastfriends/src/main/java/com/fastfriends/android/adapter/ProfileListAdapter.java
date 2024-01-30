package com.fastfriends.android.adapter;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Friend;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Profile;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 2/6/14.
 */
public class ProfileListAdapter extends BaseAdapter implements PageAdapterInterface<Profile> {
    private static final String LOGTAG = ProfileListAdapter.class.getSimpleName();

    private FragmentActivity mActivity;
    private List<Profile> mProfiles;

    public ProfileListAdapter(FragmentActivity activity) {
        mActivity = activity;
        mProfiles = new ArrayList<Profile>();
    }

    @Override
    public int getCount() {
        return mProfiles.size();
    }

    @Override
    public Object getItem(int i) {
        return mProfiles.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mProfiles.get(i).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Profile profile = mProfiles.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_friend_item, parent, false);
        } else {
            view = convertView;
        }


        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = profile.getPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(portrait, portraitView);
        }

        TextView nameView = (TextView) view.findViewById(R.id.display_name);
        nameView.setText(profile.getDisplayName());
        view.setClickable(false);

        TextView friendCountView = (TextView) view.findViewById(R.id.friend_count);
        int mutualFriendCount =  profile.getMutualFriendCount();
        friendCountView.setText(mActivity.getResources().getQuantityString(R.plurals.mutual_friends, mutualFriendCount, mutualFriendCount));
        view.setClickable(false);

        CheckBox closeCheckBox = (CheckBox) view.findViewById(R.id.close_checkbox);
        closeCheckBox.setVisibility(View.GONE);

        return view;
    }

    public void addPage(Page<Profile> profilePage) {
        mProfiles.addAll(profilePage.getResults());
        notifyDataSetChanged();
    }

    public void reset(Page<Profile> profilePage) {
        mProfiles.clear();
        addPage(profilePage);
    }

    public List<Profile> getFriends() {
        return mProfiles;
    }
}