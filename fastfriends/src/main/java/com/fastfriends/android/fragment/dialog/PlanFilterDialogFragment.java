package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.MapActivity;
import com.fastfriends.android.adapter.LocationAdapter;
import com.fastfriends.android.model.PlanSearchFilter;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Point;

/**
 * Created by jschnall on 8/25/14.
 */
public class PlanFilterDialogFragment extends DialogFragment {
    private final static String LOGTAG = PlanFilterDialogFragment.class.getSimpleName();

    // Fragment launch args
    public static final String ARG_PLAN_FILTER = "plan_filter";

    public static final int REQUEST_SELECT_LOCATION = 1;

    private PlanSearchFilter mOldPlanFilter;
    private PlanSearchFilter mNewPlanFilter;

    private TextView mDistanceView;
    private SeekBar mDistanceSeekBar;
    private Spinner mLocationView;

    private LocationAdapter mLocationAdapter;
    private int mLastLocationSelectedIndex;
    private Location mLastLocation;

    public interface OnFilterListener {
        public void onFilter(PlanSearchFilter planFilter);
    }
    private OnFilterListener mOnFilterListener;


    public PlanFilterDialogFragment() {
        // Required empty constructor
    }

    public static PlanFilterDialogFragment newInstance(PlanSearchFilter planFilter) {
        PlanFilterDialogFragment fragment = new PlanFilterDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PLAN_FILTER, planFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle == null) {
            mOldPlanFilter = new PlanSearchFilter();
        } else {
            mOldPlanFilter = bundle.getParcelable(ARG_PLAN_FILTER);
        }
        mNewPlanFilter = new PlanSearchFilter(mOldPlanFilter);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (Settings.isMetric()) {
            mNewPlanFilter.setDistanceUnits(PlanSearchFilter.KILOMETERS);
        } else {
            mNewPlanFilter.setDistanceUnits(PlanSearchFilter.MILES);
        }

        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_filter_plan, null);

        int distance = (int) mNewPlanFilter.getDistance();
        mDistanceView = (TextView) layout.findViewById(R.id.distance);
        mDistanceView.setText(formatDistance(distance));
        mDistanceSeekBar = (SeekBar) layout.findViewById(R.id.distance_seekbar);
        mDistanceSeekBar.setMax(100);
        mDistanceSeekBar.setProgress(distance);
        mDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    mDistanceSeekBar.setProgress(1);
                } else {
                    if (PlanSearchFilter.KILOMETERS.equals(mNewPlanFilter.getDistanceUnits())) {
                        mDistanceView.setText(getActivity().getResources().getString(R.string.kilometers, progress));
                    } else {
                        mDistanceView.setText(getActivity().getResources().getString(R.string.miles, progress));
                    }
                    mNewPlanFilter.setDistance(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mLocationView = (Spinner) layout.findViewById(R.id.location);
        mLocationAdapter = new LocationAdapter(activity);
        mLocationView.setAdapter(mLocationAdapter);
        mLocationView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == LocationAdapter.OTHER_LOCATION) {
                    launchMap();
                } else {
                    mLastLocationSelectedIndex = mLocationView.getSelectedItemPosition();
                    if (i == LocationAdapter.MY_LOCATION) {
                        mNewPlanFilter.setLatitude(0.0);
                        mNewPlanFilter.setLongitude(0.0);
                    } else if (i == LocationAdapter.CUSTOM_LOCATION) {
                        Location location = mLocationAdapter.getOtherLocation();
                        Point point = location.getPoint();
                        mNewPlanFilter.setLatitude(point.getLatitude());
                        mNewPlanFilter.setLongitude(point.getLongitude());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                //.setTitle(R.string.dialog_filter_plan_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.action_search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mOnFilterListener != null) {
                            mOnFilterListener.onFilter(mNewPlanFilter);
                        }
                    }
                }).create();

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnFilterListener) {
            mOnFilterListener = (OnFilterListener) getParentFragment();
        }
        else if (activity instanceof OnFilterListener) {
            mOnFilterListener = (OnFilterListener) activity;
        }
    }

    private void launchMap() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(MapActivity.EXTRA_LATITUDE, mNewPlanFilter.getLatitude());
        intent.putExtra(MapActivity.EXTRA_LONGITUDE, mNewPlanFilter.getLongitude());
        startActivityForResult(intent, REQUEST_SELECT_LOCATION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_LOCATION: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Location location = bundle.getParcelable(MapActivity.EXTRA_LOCATION);
                            if (location != null) {
                                mLocationAdapter.setOtherLocation(location);
                                mLocationView.setSelection(LocationAdapter.CUSTOM_LOCATION);
                                Point point = location.getPoint();
                                mNewPlanFilter.setLatitude(point.getLatitude());
                                mNewPlanFilter.setLongitude(point.getLongitude());
                            }
                        }
                    }
                } else {
                    mLocationView.setSelection(mLastLocationSelectedIndex);
                }
                break;
            }
        }
    }

    private String formatDistance(int distance) {
        if (PlanSearchFilter.KILOMETERS.equals(mNewPlanFilter.getDistanceUnits())) {
            return getString(R.string.kilometers, distance);
        }
        return getString(R.string.miles, distance);
    }
}