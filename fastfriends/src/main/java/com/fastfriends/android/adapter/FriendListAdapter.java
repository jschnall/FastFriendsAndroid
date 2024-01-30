package com.fastfriends.android.adapter;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateFormat;
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
public class FriendListAdapter extends BaseAdapter implements PageAdapterInterface<Friend> {
    private static final String LOGTAG = FriendListAdapter.class.getSimpleName();

    private FragmentActivity mActivity;
    private List<Friend> mFriends;
    private HashSet<Friend> mSelectedFriends;
    private boolean mDialog; // Whether it is being used in a dialog

    private class SetCloseFriendTask extends AsyncTask<String, Void, String> {
        Friend friend = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            long friendId = Long.valueOf(params[0]);
            boolean close = Boolean.valueOf(params[1]);

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(mActivity);
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    friend = webService.setCloseFriend(friendId, authToken.getAuthHeader(), close);
                }
            } catch (RetrofitError retrofitError) {
                Log.e(LOGTAG, "Can't set close friend.", retrofitError);
                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't set close friend.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            if (error != null) {
                Toast.makeText(mActivity, error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private SetCloseFriendTask mSetCloseFriendTask;


    public FriendListAdapter(FragmentActivity activity, boolean dialog) {
        mActivity = activity;
        mDialog = dialog;
        mFriends = new ArrayList<Friend>();
        mSelectedFriends = new HashSet<Friend>();
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
            view = inflater.inflate(R.layout.list_friend_item, parent, false);
        } else {
            view = convertView;
        }


        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = friend.getPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(portrait, portraitView);
        }

        TextView nameView = (TextView) view.findViewById(R.id.display_name);
        nameView.setText(friend.getUserName());
        view.setClickable(false);

        TextView friendCountView = (TextView) view.findViewById(R.id.friend_count);
        int mutualFriendCount =  friend.getMutualFriendCount();
        friendCountView.setText(mActivity.getResources().getQuantityString(R.plurals.mutual_friends, mutualFriendCount, mutualFriendCount));
        view.setClickable(false);

        CheckBox closeCheckBox = (CheckBox) view.findViewById(R.id.close_checkbox);
        closeCheckBox.setChecked(friend.isClose());

        if (mDialog) {
            closeCheckBox.setEnabled(false);
        } else {
            closeCheckBox.setEnabled(true);
            final long friendId = friend.getId();
            closeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    mSetCloseFriendTask = new SetCloseFriendTask();
                    mSetCloseFriendTask.execute(String.valueOf(friendId), String.valueOf(b));
                }
            });
        }

        View selector = view.findViewById(R.id.selector);
        if (mSelectedFriends.contains(friend)) {
            selector.setVisibility(View.VISIBLE);
        } else {
            selector.setVisibility(View.GONE);
        }

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

    public List<Friend> getFriends() {
        return mFriends;
    }

    public void toggleSelection(int index) {
        Friend friend = mFriends.get(index);

        if(mSelectedFriends.contains(friend)) {
            mSelectedFriends.remove(friend);
        } else {
            mSelectedFriends.add(friend);
        }

        notifyDataSetChanged();
    }


    public List<Long> getSelectedUserIds() {
        List<Long> userIds = new ArrayList<Long>();

        for (Friend friend : mSelectedFriends) {
            userIds.add(friend.getUserId());
        }

        return userIds;
    }
}