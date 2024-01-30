package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.MapActivity;
import com.fastfriends.android.fragment.FacebookLoginFragment;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.DistanceHelper;
import com.fastfriends.android.helper.PriceHelper;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.model.Point;
import com.fastfriends.android.model.Price;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class EventListAdapter extends BaseAdapter implements PageAdapterInterface<Event> {
    private static final String LOGTAG = EventListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Event> mEvents;

    public EventListAdapter(Activity activity) {
        mActivity = activity;
        mEvents = new ArrayList<Event>();
    }

    @Override
    public int getCount() {
        return mEvents.size();
    }

    @Override
    public Object getItem(int i) {
        return mEvents.get(i);
    }

    @Override
    public long getItemId(int i) {
        return mEvents.get(i).getId();
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Event event = mEvents.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_event_item, parent, false);
        } else {
            view = convertView;
        }

        TextView distanceView = (TextView) view.findViewById(R.id.distance);
        distanceView.setText(DistanceHelper.formatDistance(mActivity, event.getDistance(), Settings.isMetric()));

        String imageUrl = event.getImage();
        final ImageView imageView = (ImageView) view.findViewById(R.id.image);
        imageView.setTag(imageUrl);
        imageView.setImageResource(R.drawable.event_default_bg);
        if (!TextUtils.isEmpty(imageUrl)) {
            ImageLoader.getInstance().loadImage(imageUrl, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    String tag = (String) imageView.getTag();
                    if (imageUri.equals(tag)) {
                        imageView.setImageBitmap(loadedImage);
                    }
                }
            });
        }

        TextView titleView = (TextView) view.findViewById(R.id.title);
        titleView.setText(event.getName());

        //TextView costView = (TextView) view.findViewById(R.id.cost);
        //Price price = event.getPrice();
        //costView.setText(PriceHelper.formatPrice(mActivity, price.getCurrencyCode(), price.getAmount()));

        TextView locationView = (TextView) view.findViewById(R.id.location);
        locationView.setText(formatLocation(event.getLocation()));

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

        view.setTag(event);

        return view;
    }

    public void addPage(Page<Event> eventPage) {
        mEvents.addAll(eventPage.getResults());
        notifyDataSetChanged();
    }

    public void reset(Page<Event> eventPage) {
        mEvents.clear();
        addPage(eventPage);
    }

    /**
     * Updates a single event_details.  Note: this item may no longer be ordered properly according to the
     * selected sort criteria.
     * @param position
     * @param event
     */
    public void updateItem(int position, Event event) {
        mEvents.set(position, event);
        notifyDataSetChanged();
    }

    public void addItem(Event event) {
        mEvents.add(event);
        notifyDataSetChanged();
    }

    public String formatPrice(double price) {
        if (price == 0) {
            return mActivity.getString(R.string.free);
        }
        return NumberFormat.getCurrencyInstance().format(price);
    }

    public static String formatLocation(Location location) {
        String name = location.getName();
        String subThoroughfare = location.getSubThoroughfare();
        String thoroughfare = location.getThoroughfare();
        if (!TextUtils.isEmpty(name) && !name.equals(subThoroughfare)) {
            return name;
        } else if (!TextUtils.isEmpty(subThoroughfare) && !TextUtils.isEmpty(thoroughfare)) {
            return subThoroughfare + " " + thoroughfare;
        }
        Point point = location.getPoint();
        return MapActivity.formatLatLng(point.getLatitude(), point.getLongitude());
    }
}