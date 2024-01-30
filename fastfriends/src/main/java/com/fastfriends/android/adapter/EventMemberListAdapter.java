package com.fastfriends.android.adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.fragment.EventMemberListFragment;
import com.fastfriends.android.model.EventMember;
import com.fastfriends.android.model.MemberPage;
import com.fastfriends.android.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class EventMemberListAdapter extends BaseAdapter {
    private static final String LOGTAG = EventMemberListAdapter.class.getSimpleName();

    private static final int HEADER_ACCEPTED = -1;
    private static final int HEADER_REQUESTED = -2;
    private static final int HEADER_INVITED = -3;

    private static final int HEADER_FRIEND = -4;
    private static final int HEADER_ACQUAINTANCE = -5;
    private static final int HEADER_OTHER = -6;

    private FragmentActivity mActivity;
    private List<EventMember> mMembers;
    private List<ListItem> mItems;
    private long mOwnerId;
    private long mCurrentUserId;
    private boolean mEventEnded;
    private boolean mShowHeaders;
    private int mCategory;

    private int mFriendCount;
    private int mAcquaintanceCount;
    private int mOtherMemberCount;

    private int mAcceptedCount;
    private int mRequestedCount;
    private int mInvitedCount;

    public interface ApproveMemberListener {
        public void onApproveMember(EventMember member, boolean approved);
    }
    private ApproveMemberListener mApproveMemberListener;

    public static interface ListItem {
        public static final int TYPE_MEMBER = 0;
        public static final int TYPE_FRIEND_HEADER = 1;
        public static final int TYPE_STATUS_HEADER = 2;

        public abstract int getType();
        public abstract long getId();
    }

    public static class HeaderItem implements ListItem {
        private long mId;
        private int mCount;

        public HeaderItem(long id, int count) {
            mId = id;
            mCount = count;
        }

        @Override
        public int getType() {
            return TYPE_FRIEND_HEADER;
        }

        @Override
        public long getId() {
            return mId;
        }

        public void setId(long id) {
            mId = id;
        }

        public int getCount() {
            return mCount;
        }

        public void setCount(int count) {
            mCount = count;
        }
    }

    public static class MemberItem implements ListItem {
        private EventMember mEventMember;

        public MemberItem(EventMember eventMember) {
            mEventMember = eventMember;
        }

        @Override
        public long getId() {
            return mEventMember.getId();
        }

        @Override
        public int getType() {
            return TYPE_MEMBER;
        }

        public EventMember getEventMember() {
            return mEventMember;
        }

        public void setEventMember(EventMember eventMember) {
            mEventMember = eventMember;
        }
    }

    public EventMemberListAdapter(FragmentActivity activity, ApproveMemberListener listener, boolean eventEnded, boolean showHeaders) {
        mActivity = activity;
        mApproveMemberListener = listener;
        mEventEnded = eventEnded;
        mShowHeaders = showHeaders;

        mMembers = new ArrayList<EventMember>();
        mItems = new ArrayList<ListItem>();

        SharedPreferences prefs = Settings.getSharedPreferences();
        mCurrentUserId = prefs.getLong(Settings.USER_ID, 0);
    }

    public void addPage(MemberPage<EventMember> eventMemberPage, long ownerId, int category) {
        if (eventMemberPage == null) {
            Log.d(LOGTAG, "EventMember page is null");
            return;
        }
        List<EventMember> members = eventMemberPage.getResults();
        mFriendCount = eventMemberPage.getCloseFriendCount();
        mAcquaintanceCount = eventMemberPage.getFriendCount();
        mOtherMemberCount = eventMemberPage.getOtherMemberCount();

        mAcceptedCount = eventMemberPage.getAcceptedCount();
        mRequestedCount = eventMemberPage.getRequestedCount();
        mInvitedCount = eventMemberPage.getInvitedCount();

        mOwnerId = ownerId;
        mCategory = category;

        mMembers.addAll(members);
        updateItems();
        notifyDataSetChanged();
    }

    public void reset(MemberPage<EventMember> eventMemberPage, long ownerId, int category) {
        mMembers.clear();
        addPage(eventMemberPage, ownerId, category);
    }

    private void updateItems() {
        mItems.clear();

        // Assume EventMembers are already sorted by the server
        if (mCategory == EventMemberListFragment.CATEGORY_ALL) {
            // Show status headers
            boolean acceptedHeaderAdded = false;
            boolean requestedHeaderAdded = false;
            boolean invitedHeaderAdded = false;
            for (EventMember member : mMembers) {
                if (mShowHeaders) {
                    if (!acceptedHeaderAdded && member.getStatus().equals(EventMember.ACCEPTED)) {
                        // First accepted member
                        HeaderItem headerItem = new HeaderItem(HEADER_ACCEPTED, mAcceptedCount);
                        mItems.add(headerItem);
                        acceptedHeaderAdded = true;
                    } else if (!requestedHeaderAdded && member.getStatus().equals(EventMember.REQUESTED)) {
                        // First requested member
                        HeaderItem headerItem = new HeaderItem(HEADER_REQUESTED, mRequestedCount);
                        mItems.add(headerItem);
                        requestedHeaderAdded = true;
                    } else if (!invitedHeaderAdded && member.getStatus().equals(EventMember.INVITED)) {
                        // First invited member
                        HeaderItem headerItem = new HeaderItem(HEADER_INVITED, mInvitedCount);
                        mItems.add(headerItem);
                        invitedHeaderAdded = true;
                    }
                }
                MemberItem memberItem = new MemberItem(member);
                mItems.add(memberItem);
            }
        } else {
            // Show friend/acquaintance/other headers
            boolean friendHeaderAdded = false;
            boolean acquaintanceHeaderAdded = false;
            boolean otherHeaderAdded = false;
            for (EventMember member : mMembers) {
                if (mShowHeaders) {
                    if (!friendHeaderAdded && member.isClose()) {
                        // First close friend
                        HeaderItem headerItem = new HeaderItem(HEADER_FRIEND, mFriendCount);
                        mItems.add(headerItem);
                        friendHeaderAdded = true;
                    } else if (!acquaintanceHeaderAdded && !member.isClose() && member.isFriend()) {
                        // First acquaintance
                        HeaderItem headerItem = new HeaderItem(HEADER_ACQUAINTANCE, mAcquaintanceCount);
                        mItems.add(headerItem);
                        acquaintanceHeaderAdded = true;
                    } else if (!otherHeaderAdded && !member.isFriend() && !member.isClose()) {
                        // First other member
                        HeaderItem headerItem = new HeaderItem(HEADER_OTHER, mOtherMemberCount);
                        mItems.add(headerItem);
                        otherHeaderAdded = true;
                    }
                }
                MemberItem memberItem = new MemberItem(member);
                mItems.add(memberItem);
            }
        }
    }

    private boolean isEventOwner(long userId) {
        return userId == mOwnerId;
    }

    public EventMember getMember(long userId) {
        for (EventMember member : mMembers) {
            if (member.getUserId() == userId) {
                return member;
            }
        }
        return null;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mItems.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = mItems.get(position);
        switch (item.getType()) {
            case ListItem.TYPE_FRIEND_HEADER:
            case ListItem.TYPE_STATUS_HEADER:
                return getHeaderView(position, convertView, parent, (HeaderItem) item);
            case ListItem.TYPE_MEMBER:
                return getEventView(position, convertView, parent, ((MemberItem) item).getEventMember());
        }
        return null;
    }

    public View getHeaderView(int position, View convertView, ViewGroup parent, HeaderItem headerItem) {
        View view;
        if (convertView == null || convertView.findViewById(R.id.friend_header) == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_event_member_friend_header, parent, false);
        } else {
            view = convertView;
        }

        int count = headerItem.getCount();
        TextView titleView = (TextView) view.findViewById(R.id.members_title);
        switch ((int) headerItem.getId()) {
            case HEADER_FRIEND: {
                titleView.setText(mActivity.getString(R.string.friend_title, headerItem.getCount()));
                break;
            }
            case HEADER_ACQUAINTANCE: {
                titleView.setText(mActivity.getString(R.string.acquaintance_title, headerItem.getCount()));
                break;
            }
            case HEADER_OTHER: {
                titleView.setText(mActivity.getString(R.string.other_title, headerItem.getCount()));
                break;
            }
            case HEADER_ACCEPTED: {
                titleView.setText(mActivity.getString(R.string.accepted_member_title, headerItem.getCount()));
                break;
            }
            case HEADER_REQUESTED: {
                titleView.setText(mActivity.getString(R.string.requested_member_title, headerItem.getCount()));
                break;
            }
            case HEADER_INVITED: {
                titleView.setText(mActivity.getString(R.string.invited_member_title, headerItem.getCount()));
                break;
            }
        }

        return view;
    }

    public View getEventView(int position, View convertView, ViewGroup parent, final EventMember member) {
        View view;
        if (convertView == null || convertView.findViewById(R.id.list_event_item) == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_event_member_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = member.getPortrait();
        if (TextUtils.isEmpty(portrait)) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(portrait, portraitView);
        }

        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(member.getDisplayName());

        TextView statusView = (TextView) view.findViewById(R.id.owner);
        TextView mutualFriendView = (TextView) view.findViewById(R.id.friend_count);
        int mutualFriendCount =  member.getMutualFriendCount();
        mutualFriendView.setText(mActivity.getResources().getQuantityString(R.plurals.mutual_friends, mutualFriendCount, mutualFriendCount));
        View acceptLayout = view.findViewById(R.id.accept_layout);
        View portraitOverlay = view.findViewById(R.id.portrait_overlay);
        if (EventMember.REQUESTED.equals(member.getStatus())) {
            statusView.setVisibility(View.VISIBLE);
            statusView.setText(R.string.requested);

            if (isEventOwner(mCurrentUserId) && !mEventEnded) {
                // Display join request to event owner
                acceptLayout.setVisibility(View.VISIBLE);
                mutualFriendView.setVisibility(View.GONE);
                portraitOverlay.setVisibility(View.VISIBLE);
                portraitOverlay.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(mActivity, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.EXTRA_USER_ID, member.getUserId());
                        intent.putExtra(ProfileActivity.EXTRA_TITLE, member.getDisplayName());
                        mActivity.startActivity(intent);
                    }
                });
                nameView.setTextColor(mActivity.getResources().getColor(R.color.transparent_dark_grey));
                statusView.setTextColor(mActivity.getResources().getColor(R.color.transparent_medium_grey));

                final Button acceptButton = (Button) view.findViewById(R.id.accept);
                final Button declineButton = (Button) view.findViewById(R.id.decline);
                acceptButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO disable buttons while submitting
                        mApproveMemberListener.onApproveMember(member, true);
                    }
                });
                declineButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO disable buttons while submitting
                        mApproveMemberListener.onApproveMember(member, false);
                    }
                });
            } else { //if (mCurrentUserId == member.getUserId()) { // Display join request to requester
                // Display join request to everyone else
                acceptLayout.setVisibility(View.GONE);
                mutualFriendView.setVisibility(View.VISIBLE);
                portraitOverlay.setVisibility(View.VISIBLE);
                portraitOverlay.setOnClickListener(null);
                portraitOverlay.setClickable(false);
                nameView.setTextColor(mActivity.getResources().getColor(R.color.transparent_dark_grey));
                mutualFriendView.setTextColor(mActivity.getResources().getColor(R.color.transparent_dark_grey));
                statusView.setTextColor(mActivity.getResources().getColor(R.color.transparent_medium_grey));
            }
        } else if (EventMember.INVITED.equals(member.getStatus())) {
            statusView.setVisibility(View.VISIBLE);
            statusView.setText(R.string.invited);

            // Display invite request
            acceptLayout.setVisibility(View.GONE);
            mutualFriendView.setVisibility(View.VISIBLE);
            portraitOverlay.setVisibility(View.VISIBLE);
            portraitOverlay.setOnClickListener(null);
            portraitOverlay.setClickable(false);
            nameView.setTextColor(mActivity.getResources().getColor(R.color.transparent_dark_grey));
            mutualFriendView.setTextColor(mActivity.getResources().getColor(R.color.transparent_dark_grey));
            statusView.setTextColor(mActivity.getResources().getColor(R.color.transparent_medium_grey));
        } else {
            // Accepted member (owner is always accepted)
            if (isEventOwner(member.getUserId())) {
                statusView.setVisibility(View.VISIBLE);
                statusView.setText(R.string.owner);
            } else {
                statusView.setVisibility(View.GONE);
            }

            acceptLayout.setVisibility(View.GONE);
            mutualFriendView.setVisibility(View.VISIBLE);
            portraitOverlay.setVisibility(View.GONE);
            portraitOverlay.setOnClickListener(null);
            portraitOverlay.setClickable(false);
            nameView.setTextColor(mActivity.getResources().getColor(R.color.dark_grey));
            mutualFriendView.setTextColor(mActivity.getResources().getColor(R.color.dark_grey));
            statusView.setTextColor(mActivity.getResources().getColor(R.color.medium_grey));
        }

        View dividerView = view.findViewById(R.id.divider);
        if (mShowHeaders) {
            dividerView.setVisibility(View.VISIBLE);
        } else {
            dividerView.setVisibility(View.GONE);
        }

        view.setClickable(false);
        return view;
    }
}