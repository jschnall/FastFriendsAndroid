package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class MessageListAdapter extends BaseAdapter {
    private static final String LOGTAG = MessageListAdapter.class.getSimpleName();

    private static final long DAY_IN_MS = 1000 * 60 * 60 * 24;

    private Activity mActivity;
    private long mUserId;
    private List<Message> mMessages;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            long tag = (Long) view.getTag();
            Intent intent = new Intent(mActivity, ProfileActivity.class);
            if (tag == mUserId) {
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, mUserId);
            } else {
                SharedPreferences prefs = Settings.getSharedPreferences();
                long userId = prefs.getLong(Settings.USER_ID, 0);
                String userName = prefs.getString(Settings.DISPLAY_NAME, null);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, userId);
                intent.putExtra(ProfileActivity.EXTRA_TITLE, userName);
            }
            mActivity.startActivity(intent);
        }
    };

    public MessageListAdapter(Activity activity, long userId) {
        mActivity = activity;
        mUserId = userId;
        mMessages = new ArrayList<Message>();
    }

    @Override
    public int getCount() {
        return mMessages.size();
    }

    @Override
    public Object getItem(int i) {
        return mMessages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mMessages.get(i).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = mMessages.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_message_item, parent, false);
        } else {
            view = convertView;
        }

        // Extra padding at top and bottom of list
        Resources res = mActivity.getResources();
        int horizontalPadding = (int) res.getDimension(R.dimen.list_padding_horizontal);
        int verticalPadding = (int) res.getDimension(R.dimen.list_padding_vertical);
        if (position == 0) {
            view.setPadding(horizontalPadding, horizontalPadding, horizontalPadding, verticalPadding);
        } else if (position == mMessages.size() - 1) {
            view.setPadding(horizontalPadding, verticalPadding, horizontalPadding, horizontalPadding);
        } else {
            view.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        }

        String portrait = message.getSenderPortrait();
        final ImageView leftImageView = (ImageView) view.findViewById(R.id.left_image);
        final ImageView rightImageView = (ImageView) view.findViewById(R.id.right_image);
        View leftLayout = view.findViewById(R.id.left_message_layout);
        View rightLayout = view.findViewById(R.id.right_message_layout);
        if (message.getSender() == mUserId) {
            // Other User
            leftImageView.setVisibility(View.VISIBLE);
            leftImageView.setOnClickListener(mOnClickListener);
            leftImageView.setTag(message.getSender());
            rightImageView.setVisibility(View.GONE);
            leftLayout.setVisibility(View.VISIBLE);
            rightLayout.setVisibility(View.GONE);
            if (TextUtils.isEmpty(portrait)) {
                leftImageView.setImageResource(R.drawable.ic_person);
            } else {
                ImageLoader.getInstance().displayImage(portrait, leftImageView);
            }

            TextView messageView = (TextView) view.findViewById(R.id.left_message);
            messageView.setText(message.getMessage());

            ProgressBar progressView = (ProgressBar) view.findViewById(R.id.left_progress);
            TextView dateView = (TextView) view.findViewById(R.id.left_date);
            Date sent = message.getSent();
            if (sent == null) {
                progressView.setVisibility(View.VISIBLE);
                dateView.setVisibility(View.GONE);
            } else {
                String dateStr = formatDate(message.getSent());
                dateView.setText(dateStr);

                progressView.setVisibility(View.GONE);
                dateView.setVisibility(View.VISIBLE);
            }
        } else {
            // Current User
            leftImageView.setVisibility(View.GONE);
            rightImageView.setVisibility(View.VISIBLE);
            rightImageView.setOnClickListener(mOnClickListener);
            rightImageView.setTag(message.getSender());
            leftLayout.setVisibility(View.GONE);
            rightLayout.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(portrait)) {
                rightImageView.setImageResource(R.drawable.ic_person);
            } else {
                ImageLoader.getInstance().displayImage(portrait, rightImageView);
            }

            TextView messageView = (TextView) view.findViewById(R.id.right_message);
            messageView.setText(message.getMessage());

            ProgressBar progressView = (ProgressBar) view.findViewById(R.id.right_progress);
            TextView dateView = (TextView) view.findViewById(R.id.right_date);
            Date sent = message.getSent();
            if (sent == null) {
                progressView.setVisibility(View.VISIBLE);
                dateView.setVisibility(View.GONE);
            } else {
                String dateStr = formatDate(message.getSent());
                dateView.setText(dateStr);

                progressView.setVisibility(View.GONE);
                dateView.setVisibility(View.VISIBLE);
            }

        }

        return view;
    }

    public void addPage(final Page<Message> messagePage) {
        List<Message> messages = messagePage.getResults();
        for (Message message : messages) {
            mMessages.add(0, message);
            notifyDataSetChanged();
        }
    }

    public void reset(Page<Message> messagePage) {
        mMessages.clear();
        addPage(messagePage);
    }

    /**
     * Updates a single message.  Note: this item may no longer be ordered properly according to the
     * selected sort criteria.
     * @param position
     * @param message
     */
    public void updateItem(int position, Message message) {
        mMessages.set(position, message);
        notifyDataSetChanged();
    }

    /**
     * Adds a single message.
     * @param message
     */
    public void addItem(Message message) {
        mMessages.add(message);
        notifyDataSetChanged();
    }

    public void replaceItem(int position, Message message) {
        mMessages.remove(position);
        mMessages.add(position, message);
    }

    private String formatDate(Date date) {
        if (DateHelper.isToday(date)) {
            return mActivity.getString(R.string.today) + " " +
                    DateUtils.formatDateTime(mActivity, date.getTime(), DateUtils.FORMAT_SHOW_TIME);
        }

        int flags;
        Date lastWeek = new Date(System.currentTimeMillis() - (7 * DAY_IN_MS));
        if (DateHelper.isAfterDay(date, lastWeek)) {
            flags = DateUtils.FORMAT_SHOW_WEEKDAY|DateUtils.FORMAT_SHOW_TIME;
        } else {
            flags = DateUtils.FORMAT_SHOW_DATE|DateUtils.FORMAT_SHOW_TIME;
        }

        return DateUtils.formatDateTime(mActivity, date.getTime(), flags);
    }

    public int getItemPosition(long id) {
        for (int position = 0; position < mMessages.size(); position++)
            if (mMessages.get(position).getId() == id)
                return position;
        return 0;
    }
}