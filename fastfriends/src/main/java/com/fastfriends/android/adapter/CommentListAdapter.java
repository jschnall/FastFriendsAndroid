package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jschnall on 2/6/14.
 */
public class CommentListAdapter extends BaseAdapter {
    private static final String LOGTAG = CommentListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Comment> mComments;
    private Map<Long, Boolean> mExpandedMap;
    private boolean mPlan;
    private TagHelper mTagHelper;

    //private TagHelper mTagHelper;

    public static long FIVE_SECONDS = 5000; // ms

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.portrait: {
                    Comment comment = (Comment) view.getTag();
                    Intent intent = new Intent(mActivity, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.EXTRA_USER_ID, comment.getOwnerId());
                    intent.putExtra(ProfileActivity.EXTRA_TITLE, comment.getOwnerName());
                    mActivity.startActivity(intent);
                    break;
                }
                case R.id.inner_layout: {
                    Comment comment = (Comment) view.getTag();
                    toggleExpand(comment);
                    break;
                }
            }
        }
    };

    private View.OnLongClickListener mOnLongClickListener;

    public CommentListAdapter(Activity activity, boolean plan, View.OnLongClickListener onLongClickListener) {
        mActivity = activity;
        mComments = new ArrayList<Comment>();
        mExpandedMap = new HashMap<Long, Boolean>();
        mPlan = plan;
        mOnLongClickListener = onLongClickListener;
        mTagHelper = TagHelper.getInstance();
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int i) {
        return mComments.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mComments.get(i).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Comment comment = mComments.get(position);
        boolean expanded = mExpandedMap.containsKey(comment.getId());

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_comment_item, parent, false);
        } else {
            view = convertView;
        }

        boolean loadImage = false;
        Comment tag = (Comment) view.getTag();
        if (tag == null) {
            loadImage = true;
        } else {
            long oldId = tag.getId();
            if (oldId != comment.getId()) {
                loadImage = true;
            }
        }

        final ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        portraitView.setOnClickListener(mOnClickListener);
        portraitView.setTag(comment);
        String creatorImageUrl = comment.getOwnerImage();
        if (loadImage) {
            if (TextUtils.isEmpty(creatorImageUrl)) {
                portraitView.setImageResource(R.drawable.ic_person);
            } else {
                ImageLoader.getInstance().displayImage(creatorImageUrl, portraitView);
            }
        }

        TextView dateView = (TextView) view.findViewById(R.id.date);
        String timeStamp = DateHelper.buildShortTimeStamp(mActivity, comment.getUpdated(), false);
        dateView.setText(timeStamp);
        TextView editedView = (TextView) view.findViewById(R.id.edited);
        if (comment.getUpdated().getTime() - comment.getCreated().getTime() > FIVE_SECONDS) {
            editedView.setText(R.string.edited);
        } else {
            editedView.setText(null);
        }

        TextView creatorNameView = (TextView) view.findViewById(R.id.owner);

        CharSequence body = null;
        if (mPlan) {
            body = mTagHelper.markup(mActivity, comment.getMessage(), comment.getMentions(), TagHelper.SEARCH_PLANS);
        } else {
            body = mTagHelper.markup(mActivity, comment.getMessage(), comment.getMentions(), TagHelper.SEARCH_EVENTS);
        }

        final TextView messageView = (TextView) view.findViewById(R.id.message);
        messageView.setMovementMethod(new LinkTouchMovementMethod());
        messageView.setText(body);

        View innerLayout = view.findViewById(R.id.inner_layout);
        innerLayout.setOnClickListener(mOnClickListener);

        // Expand or collapse card
        Resources res = mActivity.getResources();
        final int collapsedLines = res.getInteger(R.integer.collapsed_comment_lines);
        //int padding = (int) res.getDimension(R.dimen.card_gutter_horizontal);
        //int shadow = (int) res.getDimension(R.dimen.card_shadow);
        int collapsedPadding = (int) res.getDimension(R.dimen.collapsed_comment_padding);
        //if (expanded || position == mComments.size() - 1) {
            // Last item cannot be collapsed
            //if (position == 0) {
            //    view.setPadding(padding, padding, padding, padding);
            //} else {
            //    view.setPadding(padding, 0, padding, padding);
            //}
            portraitView.setVisibility(View.VISIBLE);
            creatorNameView.setText(comment.getOwnerName());
            creatorNameView.setTextAppearance(mActivity, R.style.Text);

            messageView.setVisibility(View.VISIBLE);
            messageView.setOnLongClickListener(mOnLongClickListener);
            innerLayout.setOnLongClickListener(mOnLongClickListener);
        //} else {
        //    if (position == 0) {
        //        view.setPadding(padding, padding, padding, collapsedPadding);
        //    } else {
        //        view.setPadding(padding, 0, padding, collapsedPadding);
        //    }
        //    portraitView.setVisibility(View.GONE);
        //    creatorNameView.setText(buildCollapsedText(comment));
        //    creatorNameView.setTextAppearance(mActivity, R.style.Text_Muted);

        //    messageView.setVisibility(View.GONE);
        //    innerLayout.setOnLongClickListener(null);
        //}

        view.setTag(comment);
        innerLayout.setTag(comment);
        messageView.setTag(comment);
        return view;
    }

    public void addPage(Page<Comment> commentPage) {
        if (commentPage == null) {
            Log.d(LOGTAG, "Comment page is null");
            return;
        }
        mComments.addAll(commentPage.getResults());
        notifyDataSetChanged();
    }

    public void reset(Page<Comment> commentPage) {
        mComments.clear();
        mExpandedMap.clear();

        if (commentPage == null) {
            Log.d(LOGTAG, "Comment page is null");
            return;
        }

        // Expand last comment
        List<Comment> comments = commentPage.getResults();
        int size = comments.size();
        if (size > 0) {
            Comment lastComment = comments.get(size - 1);
            mExpandedMap.put(lastComment.getId(), true);
        }
        addPage(commentPage);
    }

    /**
     * Updates a single comment.  Note: this item may no longer be ordered properly according to the
     * selected sort criteria.
     * @param position
     * @param comment
     */
    public void updateItem(int position, Comment comment) {
        mComments.set(position, comment);
        notifyDataSetChanged();
    }

    /**
     * Adds a single comment.
     * @param newComment
     */
    public void addItem(Comment newComment) {
        // Slow cruddy check to avoid duplicate comments added
        for (Comment comment : mComments) {
            if (comment.getId() == newComment.getId()) {
                return;
            }
        }

        if (mPlan) {
            mComments.add(newComment);
        } else {
            mComments.add(0, newComment);
        }
        mExpandedMap.put(newComment.getId(), true);
        notifyDataSetChanged();
    }

    public void removeItem(long commentId) {
        int position = positionWithId(commentId);
        if (position >= 0) {
            mComments.remove(position);
            mExpandedMap.remove(commentId);
            notifyDataSetChanged();
        }
    }

    public void replaceItem(Comment comment) {
        if (comment == null) {
            return;
        }
        int position = positionWithId(comment.getId());
        if (position >= 0) {
            mComments.set(position, comment);
            notifyDataSetChanged();
        }
    }

    public int positionWithId(long id) {
        int i = 0;
        for (Comment comment : mComments) {
            if (comment.getId() == id) {
                return i;
            }
            i++;
        }
        return -1;
    }

    public Comment getItemById(long id) {
        int position = positionWithId(id);
        if (position < 0) {
            return null;
        }
        return mComments.get(position);
    }

    public void clearItems() {
        mComments.clear();
        mExpandedMap.clear();
        notifyDataSetChanged();
    }

    private void toggleExpand(Comment comment) {
        long id = comment.getId();
        if (mExpandedMap.remove(id) == null) {
            // No entry, was
            mExpandedMap.put(id, true);
        }
        notifyDataSetChanged();
    }

    private Spannable buildCollapsedText(Comment comment) {
        String ownerName = comment.getOwnerName();
        String ownerStr = mActivity.getResources().getString(R.string.plan_text, ownerName);

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(ownerStr));
        builder.append(" ");
        builder.append(comment.getMessage());

        return builder;
    }
}