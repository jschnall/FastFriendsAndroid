package com.fastfriends.android.adapter;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit.RetrofitError;

import com.fastfriends.android.model.Prediction;

/**
 * Created by jschnall on 4/24/14.
 */
public class PlacesAutoCompleteAdapter extends BaseAdapter implements Filterable {
    private static final String LOGTAG = PlacesAutoCompleteAdapter.class.getSimpleName();

    private static final long RADIUS = 500; // meters

    private FragmentActivity mActivity;
    private GoogleMap mGoogleMap;
    private List<Prediction> mPredictions;
    private Location mMyLocation;

    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((Prediction)(resultValue)).getDescription();
            return str;
        }

        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null) {
                // Retrieve the autocomplete results.
                mPredictions = autoComplete(constraint.toString());

                // Assign the data to the FilterResults
                filterResults.values = mPredictions;
                filterResults.count = mPredictions.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null && results.count > 0) {
                notifyDataSetChanged();
            }
            else {
                notifyDataSetInvalidated();
            }
        }
    };

    public PlacesAutoCompleteAdapter(FragmentActivity activity, GoogleMap googleMap) {
        super();
        mPredictions = new ArrayList<Prediction>();
        mActivity = activity;
        mGoogleMap = googleMap;
    }

    @Override
    public int getCount() {
        return mPredictions.size();
    }

    @Override
    public Object getItem(int position) {
        return mPredictions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Prediction prediction = mPredictions.get(position);

        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_location_item, parent, false);
        } else {
            view = convertView;
        }
        TextView textView = (TextView) view.findViewById(R.id.name);
        textView.setText(prediction.getDescription());

        return view;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public List<Prediction> autoComplete(String input) {
        List<Prediction> predictions = new ArrayList<Prediction>();
        if (TextUtils.isEmpty(input)) {
            return predictions;
        }
        try {
            AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(mActivity);
            if (authToken != null) {
                FastFriendsWebService webService = WebServiceManager.getWebService();

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mMyLocation = mGoogleMap.getMyLocation();
                    }
                });

                if (mMyLocation == null) {
                    String components = "country:" + Locale.getDefault().getISO3Country();
                    predictions = webService.autoCompletePlace(authToken.getAuthHeader(), input, components, null, null);
                } else {
                    String location = mMyLocation.getLatitude() + "," + mMyLocation.getLongitude();
                    predictions = webService.autoCompletePlace(authToken.getAuthHeader(), input, null, location, RADIUS);
                }

            }
        } catch (RetrofitError e) {
            Log.e(LOGTAG, "Can't autoComplete location", e);
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't autoComplete location", e);
        }

        return predictions;
    }
}