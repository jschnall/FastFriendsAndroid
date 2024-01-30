package com.fastfriends.android.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.model.Conversation;
import com.fastfriends.android.model.Message;
import com.fastfriends.android.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class ConversationListAdapter extends BaseAdapter implements PageAdapterInterface<Conversation> {
    private static final String LOGTAG = ConversationListAdapter.class.getSimpleName();

    private Activity mActivity;
    private String mCategory;
    private List<Conversation> mConversations;
    private HashSet<Long> mSelectedItemIds = new HashSet<Long>();

    public ConversationListAdapter(Activity activity, String category) {
        mActivity = activity;
        mCategory = category;
        mConversations = new ArrayList<Conversation>();
    }

    @Override
    public int getCount() {
        return mConversations.size();
    }

    @Override
    public Object getItem(int i) {
        return mConversations.get(i);
    }

    @Override
    public long getItemId(int i) {
        if (Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
            return mConversations.get(i).getSender();
        } else {
            // Sent or drafts
            return mConversations.get(i).getReceiver();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Conversation conversation = mConversations.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_conversation_item, parent, false);
        } else {
            view = convertView;
        }

        // TODO having a bg interferes with default long press anim, create new anim state drawable
        if (!conversation.isOpened() && Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
            view.setBackgroundResource(R.drawable.selector_conversation_unread);
        } else {
            view.setBackgroundDrawable(null);
        }

        String iconUri;
        if (Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
            iconUri = conversation.getSenderPortrait();
        } else {
            iconUri = conversation.getReceiverPortrait();
        }

        final ImageView imageView = (ImageView) view.findViewById(R.id.portrait);
        if (!TextUtils.isEmpty(iconUri)) {
            ImageLoader.getInstance().displayImage(iconUri, imageView);
        } else {
            imageView.setImageResource(R.drawable.ic_person);
        }

        View selectorView = view.findViewById(R.id.selector);
        if (selectorView != null) {
            if (isItemSelected(getItemId(position))) {
                selectorView.setVisibility(View.VISIBLE);
            } else {
                selectorView.setVisibility(View.GONE);
            }
        }

        TextView nameView = (TextView) view.findViewById(R.id.name);
        if (mCategory == Conversation.CATEGORY_RECEIVED) {
            nameView.setText(conversation.getSenderName());
        } else {
            nameView.setText(conversation.getReceiverName());
        }

        TextView dateView = (TextView) view.findViewById(R.id.date);
        Date date;
        if (mCategory == Conversation.CATEGORY_DRAFTS) {
            date = conversation.getCreated();
        } else {
            date = conversation.getSent();
        }
        dateView.setText(DateHelper.buildShortTimeStamp(mActivity, date, false));

        TextView messageView = (TextView) view.findViewById(R.id.message);
        messageView.setText(conversation.getMessage());
        if (conversation.isReplied() && Conversation.CATEGORY_RECEIVED.equals(mCategory)) {
            // Show replied indicator
            SpannableString ss = new SpannableString("0" + conversation.getMessage());
            Drawable d = mActivity.getResources().getDrawable(R.drawable.ic_action_reply);
            d.setBounds(0, 0, d.getIntrinsicWidth() / 2, d.getIntrinsicHeight() / 2);
            ImageSpan span = new ImageSpan(d, ImageSpan.ALIGN_BASELINE);
            ss.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            messageView.setText(ss);
        } else {
            messageView.setText(conversation.getMessage());
        }


        return view;
    }

    public void addPage(Page<Conversation> conversationPage) {
        mConversations.addAll(conversationPage.getResults());
        notifyDataSetChanged();
    }

    public void reset(Page<Conversation> conversationPage) {
        mConversations.clear();
        addPage(conversationPage);
    }

    /**
     * Updates a single conversation.  Note: this item may no longer be ordered properly according to the
     * selected sort criteria.
     * @param position
     * @param conversation
     */
    public void updateItem(int position, Conversation conversation) {
        mConversations.set(position, conversation);
        notifyDataSetChanged();
    }

    /**
     * Adds a single conversation.
     * @param conversation
     */
    public void addItem(Conversation conversation) {
        mConversations.add(conversation);
        notifyDataSetChanged();
    }

    // Contextual action mode
    public void addSelection(long id) {
        mSelectedItemIds.add(id);
    }

    public void removeSelection(long id) {
        mSelectedItemIds.remove(id);
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