package com.fastfriends.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.model.FitHistory;
import com.fastfriends.android.model.Page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jschnall on 6/18/14.
 */
public class FitHistoryListAdapter extends BaseAdapter implements PageAdapterInterface<FitHistory>{
    private static final String TOTAL_TIME = "DURATION";

    Context mContext;

    class FitHistoryItem {
        private String mActivityType;
        private long mTotalTime;
        // Stats unique to this activity type
        // Distance for running/biking/swimming
        // Max bouldering/sport/trad grades for climbing
        private HashMap<String, String> mFields;

        public FitHistoryItem() {
            mFields = new HashMap<String, String>();
        }

        public String getActivityType() {
            return mActivityType;
        }

        public void setActivityType(String activityType) {
            mActivityType = activityType;
        }

        public long getTotalTime() {
            return mTotalTime;
        }

        public void setTotalTime(long totalTime) {
            mTotalTime = totalTime;
        }

        public HashMap<String, String> getFields() {
            return mFields;
        }

        public void setFields(HashMap<String, String> fields) {
            mFields = fields;
        }
    }
    private List<FitHistoryItem> mItems;

    private HashMap mActivityMap;
    private List<FitHistory> mFitHistoryList;

    // TODO match currentUser history items with those of user being viewed

    public FitHistoryListAdapter(Context context) {
        super();
        mContext = context;
        mItems = new ArrayList<FitHistoryItem>();

        mActivityMap = new HashMap<String, FitHistoryItem>();
        mFitHistoryList = new ArrayList<FitHistory>();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FitHistoryItem item = mItems.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.list_fit_history_item, parent, false);
        } else {
            view = convertView;
        }

        String activityType = item.getActivityType();

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        iconView.setImageResource(FitHistory.getIconResource(activityType));

        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(activityType.substring(0,1).toUpperCase() + activityType.substring(1).toLowerCase());

        TextView timeView = (TextView) view.findViewById(R.id.time);
        timeView.setText(DateHelper.formatFitTime(mContext, item.getTotalTime()));

        List<Map.Entry<String, String>> entries = new ArrayList(item.getFields().entrySet());
        int size = entries.size();
        TextView field1View = (TextView) view.findViewById(R.id.field1);
        TextView value1View = (TextView) view.findViewById(R.id.value1);
        TextView field2View = (TextView) view.findViewById(R.id.field2);
        TextView value2View = (TextView) view.findViewById(R.id.value2);
        if (size > 0) {
            Map.Entry<String, String> entry = entries.get(0);
            String key = entry.getKey();
            field1View.setText(key.substring(0,1).toUpperCase() + key.substring(1).toLowerCase());
            value1View.setText(formatValue(key, entry.getValue()));
            field1View.setVisibility(View.VISIBLE);
            value1View.setVisibility(View.VISIBLE);
        } else {
            field1View.setVisibility(View.GONE);
            value1View.setVisibility(View.GONE);
        }
        if (size > 1) {
            Map.Entry<String, String> entry = entries.get(1);
            String key = entry.getKey();
            field2View.setText(key.substring(0,1).toUpperCase() + key.substring(1).toLowerCase());
            value2View.setText(formatValue(key, entry.getValue()));
            field2View.setVisibility(View.VISIBLE);
            value2View.setVisibility(View.VISIBLE);
        } else {
            field2View.setVisibility(View.GONE);
            value2View.setVisibility(View.GONE);
        }

        return view;
    }

    public void addPage(Page<FitHistory> fitHistoryPage) {
        mFitHistoryList.addAll(fitHistoryPage.getResults());
        buildItems();
        notifyDataSetChanged();
    }

    public void reset(Page<FitHistory> fitHistoryPage) {
        mFitHistoryList.clear();
        mActivityMap.clear();
        addPage(fitHistoryPage);
    }

    public void buildItems() {
        for (FitHistory history : mFitHistoryList) {
            String activityType = history.getActivity();
            FitHistoryItem item;
            if (mActivityMap.containsKey(activityType)) {
                // Add this data to the existing historyItem
                item = (FitHistoryItem) mActivityMap.get(activityType);
            } else {
                // Add a new historyItem for this activityType
                item = new FitHistoryItem();
                item.setActivityType(history.getActivity());
                mActivityMap.put(activityType, item);
            }

            if (history.getField().equals(TOTAL_TIME)) {
                item.setTotalTime(Long.valueOf(history.getValue()));
            } else {
                item.getFields().put(history.getField(), history.getValue());
            }
        }
        mItems = new ArrayList<FitHistoryItem>(mActivityMap.values());
    }

    private String formatValue(String key, String value) {
        //if (FitHistory.DISTANCE.equalsIgnoreCase(key)) {
        // In meters, format as miles or kilometers {
        //if (Settings.isMetric()) {
        //    double kilometers = Float.valueOf(value) / 1000;
        //    return mContext.getString(R.string.kilometers, String.format("%.2f", kilometers));
        //} else {
        //    double miles = Float.valueOf(value) * 0.000621371;
        //    return mContext.getString(R.string.miles, String.format("%.2f", miles));
        //}
        //}
        if (FitHistory.STEPS.equalsIgnoreCase(key)) {
            int steps = Integer.valueOf(value);

            // In meters, format as miles or kilometers {
            if (Settings.isMetric()) {
                double kilometers = Float.valueOf(value) / 2000 * 1.61;
                return value + " (" +
                        mContext.getString(R.string.kilometers, String.format("%.2f", kilometers)) +
                        ")";
            } else {
                double miles = Float.valueOf(value) / 2000;
                return value + " (" +
                        mContext.getString(R.string.miles, String.format("%.2f", miles)) +
                        ")";
            }
        }
        return value;
    }
}
