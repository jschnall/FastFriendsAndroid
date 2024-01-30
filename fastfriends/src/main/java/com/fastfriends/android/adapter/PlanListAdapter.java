package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ClickableSpan;
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
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.text.style.ClickableColorSpan;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.fastfriends.android.text.style.TouchThroughSpan;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class PlanListAdapter extends BaseAdapter implements PageAdapterInterface<Plan> {
    private static final String LOGTAG = PlanListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Plan> mPlans;
    private TagHelper mTagHelper;

    public PlanListAdapter(Activity activity) {
        mActivity = activity;
        mPlans = new ArrayList<Plan>();
        mTagHelper = TagHelper.getInstance();
    }

    @Override
    public int getCount() {
        return mPlans.size();
    }

    @Override
    public Object getItem(int i) {
        return mPlans.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mPlans.get(i).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Plan plan = mPlans.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_plan_item, parent, false);
        } else {
            view = convertView;
        }

        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = plan.getOwnerPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            ImageLoader.getInstance().displayImage(portrait, portraitView);
        }
        portraitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mActivity, ProfileActivity.class);
                intent.putExtra(ProfileActivity.EXTRA_USER_ID, plan.getOwnerId());
                intent.putExtra(ProfileActivity.EXTRA_TITLE, plan.getOwnerName());
                mActivity.startActivity(intent);
            }
        });

        TextView dateView = (TextView) view.findViewById(R.id.date);
        Date date = plan.getCreated();
        dateView.setText(DateHelper.buildShortTimeStamp(mActivity, date, true));

        String ownerName = plan.getOwnerName();
        String ownerStr = mActivity.getResources().getString(R.string.plan_text, ownerName);

        CharSequence body = mTagHelper.markup(mActivity, plan.getText(), plan.getMentions(), TagHelper.SEARCH_PLANS);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(Html.fromHtml(ownerStr));
        builder.append(" ");
        builder.append(body);

        TextView textView = (TextView) view.findViewById(R.id.text);
        // Don't make links clickable, on here, it interferes with clicking list item
        //textView.setMovementMethod(new LinkTouchMovementMethod());
        textView.setText(builder);

        view.setClickable(false);

        return view;
    }

    @Override
    public void addPage(Page<Plan> planPage) {
        mPlans.addAll(planPage.getResults());
        notifyDataSetChanged();
    }

    @Override
    public void reset(Page<Plan> planPage) {
        mPlans.clear();
        addPage(planPage);
    }

    /**
     * Updates a single plan_details.  Note: this item may no longer be ordered properly according to the
     * selected sort criteria.
     * @param position
     * @param plan
     */
    public void updateItem(int position, Plan plan) {
        mPlans.set(position, plan);
        notifyDataSetChanged();
    }

    public void addItem(Plan plan) {
        mPlans.add(plan);
        notifyDataSetChanged();
    }

    public String formatPrice(double price) {
        if (price == 0) {
            return mActivity.getString(R.string.free);
        }
        return NumberFormat.getCurrencyInstance().format(price);
    }

}