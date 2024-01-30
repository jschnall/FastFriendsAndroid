package com.fastfriends.android.adapter;

import android.app.Activity;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.TagHelper;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.text.style.LinkTouchMovementMethod;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class HistoryListAdapter extends BaseAdapter {
    private static final String LOGTAG = HistoryListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Object> mHistory;
    private TagHelper mTagHelper;

    public HistoryListAdapter(Activity activity) {
        mActivity = activity;
        mHistory = new ArrayList<Object>();
        mTagHelper = TagHelper.getInstance();
    }

    @Override
    public int getCount() {
        return mHistory.size();
    }

    @Override
    public Object getItem(int i) {
        return mHistory.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object item = mHistory.get(position);

        if (item instanceof Event) {
            Event event = (Event) item;
            return getEventView(position, convertView, parent, event);
        } else if (item instanceof Plan) {
            Plan plan = (Plan) item;
            return getPlanView(position, convertView, parent, plan);
        }

        throw new UnsupportedOperationException("History item must be an Event or Plan.");
    }

    public View getEventView(int position, View convertView, ViewGroup parent, Event event) {
        View view;
        if (convertView == null || convertView.findViewById(R.id.list_event_item) == null) {
            // No view to convert, or it is not an Event item
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_event_item, parent, false);
        } else {
            view = convertView;
        }

        String imageUrl = event.getImage();
        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
        // TODO base default backround on category tags
        imageView.setImageResource(R.drawable.event_default_bg);
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().displayImage(imageUrl, imageView);
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(event.getName());

        //TextView costView = (TextView) view.findViewById(R.id.cost);
        //Price price = event.getPrice();
        //costView.setText(PriceHelper.formatPrice(mActivity, price.getCurrencyCode(), price.getAmount()));

        TextView locationView = (TextView) view.findViewById(R.id.location);
        locationView.setText(event.getLocation().getName());

        TextView dateView = (TextView) view.findViewById(R.id.date);
        Date startDate = event.getStartDate();
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd");
        DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT);

        dateView.setText(dateFormat.format(startDate) + " at " + timeFormat.format(startDate));

        View updateView = view.findViewById(R.id.updated);
        if (event.isModified()) {
            updateView.setVisibility(View.VISIBLE);
        } else {
            updateView.setVisibility(View.GONE);
        }

        return view;
    }

    public View getPlanView(int position, View convertView, ViewGroup parent, Plan plan) {
        View view;
        if (convertView == null || convertView.findViewById(R.id.list_history_plan_item) == null) {
            // No view to convert, or it is not a Plan item
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_history_plan_item, parent, false);
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
        textView.setText(body);

        return view;
    }

    public void addPage(Page<Object> page) {
        if (page != null) {
            mHistory.addAll(page.getResults());
            notifyDataSetChanged();
        }
    }

    public void reset(Page<Object> page) {
        mHistory.clear();
        addPage(page);
    }

    public String formatPrice(double price) {
        if (price == 0) {
            return mActivity.getString(R.string.free);
        }
        return NumberFormat.getCurrencyInstance().format(price);
    }
}