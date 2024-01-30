package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.fastfriends.android.R;
import com.fastfriends.android.Settings;
import com.fastfriends.android.activity.MapActivity;
import com.fastfriends.android.adapter.LocationAdapter;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.PriceHelper;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.EventSearchFilter;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Point;
import com.fastfriends.android.model.Price;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by jschnall on 2/20/14.
 */
public class EventFilterDialogFragment extends DialogFragment implements NumberPickerDialogFragment.NumberPickerDialogHandler{
    private final static String LOGTAG = EventFilterDialogFragment.class.getSimpleName();

    // Fragment launch args
    public static final String ARG_EVENT_FILTER = "event_filter";

    private final static int MIN_PRICE = 1;
    private final static int MAX_PRICE = 2;
    private final static int MIN_MEMBERS = 3;
    private final static int MAX_MEMBERS = 4;

    private final static String DATE_PICKER_FRAGMENT = "date_picker";
    public static final int REQUEST_SELECT_LOCATION = 1;

    private EventSearchFilter mOldEventFilter;
    private EventSearchFilter mNewEventFilter;

    private Button mStartDateView;
    private Button mEndDateView;
    private Button mMinPriceView;
    private Button mMaxPriceView;
    private View mDistanceLayout;
    private TextView mDistanceView;
    private SeekBar mDistanceSeekBar;
    private Spinner mLocationView;
    private Button mMinSizeView;
    private Button mMaxSizeView;

    private LocationAdapter mLocationAdapter;
    private int mLastLocationSelectedIndex;
    private Location mLastLocation;

    public interface OnFilterListener {
        public void onFilter(EventSearchFilter eventFilter);
    }
    private OnFilterListener mOnFilterListener;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.start_date: {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mNewEventFilter.getStartDate());
                    FragmentManager fm = getChildFragmentManager();
                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                            .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                                             @Override
                                             public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
                                                 calendar.set(year, month, day);
                                                 mNewEventFilter.setStartDate(calendar.getTime());
                                                 mStartDateView.setText(formatDate(year, month, day));
                                                 updateDates();
                                             }
                                         }, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                            );
                    calendarDatePickerDialog.show(fm, DATE_PICKER_FRAGMENT);
                    break;
                }
                case R.id.end_date: {
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(mNewEventFilter.getEndDate());
                    FragmentManager fm = getChildFragmentManager();
                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                            .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                                             @Override
                                             public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
                                                 calendar.set(year, month, day);
                                                 mNewEventFilter.setEndDate(calendar.getTime());
                                                 mEndDateView.setText(formatDate(year, month, day));
                                                 updateDates();
                                             }
                                         }, calendar.get(Calendar.YEAR),
                                    calendar.get(Calendar.MONTH),
                                    calendar.get(Calendar.DAY_OF_MONTH)
                            );
                    calendarDatePickerDialog.show(fm, DATE_PICKER_FRAGMENT);
                    break;
                }
                case R.id.min_price: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EventFilterDialogFragment.this)
                            .setReference(MIN_PRICE)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Price.MIN_AMOUNT)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.VISIBLE);
                    builder.show();
                    break;
                }
                case R.id.max_price: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EventFilterDialogFragment.this)
                            .setReference(MAX_PRICE)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Price.MIN_AMOUNT)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.VISIBLE);
                    builder.show();
                    break;
                }
                case R.id.min_size: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EventFilterDialogFragment.this)
                            .setReference(MIN_MEMBERS)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Event.MEMBER_LIMIT_MIN)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.GONE);
                    builder.show();
                    break;
                }
                case R.id.max_size: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EventFilterDialogFragment.this)
                            .setReference(MAX_MEMBERS)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Event.MEMBER_LIMIT_MIN)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.GONE);
                    builder.show();
                    break;
                }
            }
        }
    };

    public EventFilterDialogFragment() {
        // Required empty constructor
    }

    public static EventFilterDialogFragment newInstance(EventSearchFilter eventFilter) {
        EventFilterDialogFragment fragment = new EventFilterDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_EVENT_FILTER, eventFilter);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle == null) {
            mOldEventFilter = new EventSearchFilter();
        } else {
            mOldEventFilter = bundle.getParcelable(ARG_EVENT_FILTER);
        }
        mNewEventFilter = new EventSearchFilter(mOldEventFilter);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();

        if (Settings.isMetric()) {
            mNewEventFilter.setDistanceUnits(EventSearchFilter.KILOMETERS);
        } else {
            mNewEventFilter.setDistanceUnits(EventSearchFilter.MILES);
        }

        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_filter_event, null);

        mStartDateView = (Button) layout.findViewById(R.id.start_date);
        mStartDateView.setText(formatDate(mNewEventFilter.getStartDate()));
        mStartDateView.setOnClickListener(mOnClickListener);

        mEndDateView = (Button) layout.findViewById(R.id.end_date);
        mEndDateView.setText(formatDate(mNewEventFilter.getEndDate()));
        mEndDateView.setOnClickListener(mOnClickListener);

        mMinPriceView = (Button) layout.findViewById(R.id.min_price);
        mMinPriceView.setText(formatMinPrice(activity, mNewEventFilter.getCurrencyCode(), mNewEventFilter.getMinPrice()));
        mMinPriceView.setOnClickListener(mOnClickListener);

        mMaxPriceView = (Button) layout.findViewById(R.id.max_price);
        mMaxPriceView.setText(formatMaxPrice(activity, mNewEventFilter.getCurrencyCode(), mNewEventFilter.getMaxPrice()));
        mMaxPriceView.setOnClickListener(mOnClickListener);

        int distance = (int) mNewEventFilter.getDistance();
        mDistanceLayout = layout.findViewById(R.id.distance_layout);
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
                    if (EventSearchFilter.KILOMETERS.equals(mNewEventFilter.getDistanceUnits())) {
                        mDistanceView.setText(getActivity().getResources().getString(R.string.kilometers, progress));
                    } else {
                        mDistanceView.setText(getActivity().getResources().getString(R.string.miles, progress));
                    }
                    mNewEventFilter.setDistance(progress);
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
                        mNewEventFilter.setLatitude(0.0);
                        mNewEventFilter.setLongitude(0.0);
                    } else if (i == LocationAdapter.CUSTOM_LOCATION) {
                        Location location = mLocationAdapter.getOtherLocation();
                        Point point = location.getPoint();
                        mNewEventFilter.setLatitude(point.getLatitude());
                        mNewEventFilter.setLongitude(point.getLongitude());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mMinSizeView = (Button) layout.findViewById(R.id.min_size);
        mMinSizeView.setText(formatMinMembers(mNewEventFilter.getMinSize()));
        mMinSizeView.setOnClickListener(mOnClickListener);

        mMaxSizeView = (Button) layout.findViewById(R.id.max_size);
        mMaxSizeView.setText(formatMaxMembers(mNewEventFilter.getMaxSize()));
        mMaxSizeView.setOnClickListener(mOnClickListener);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                //.setTitle(R.string.dialog_filter_event_title)
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
                            mOnFilterListener.onFilter(mNewEventFilter);
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

    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return formatDate(calendar.getTime());
    }

    private String formatDate(Date date) {
        if (DateHelper.isToday(date)) {
            return getString(R.string.today);
        }
        if (DateHelper.isWithinDaysFuture(date, 1)) {
            return getString(R.string.tomorrow);
        }

        return DateFormat.getDateFormat(getActivity()).format(date);
    }

    private void updateDates() {
        // TODO make sure second date is the same or after first
    }

    private void launchMap() {
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(MapActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(MapActivity.EXTRA_LATITUDE, mNewEventFilter.getLatitude());
        intent.putExtra(MapActivity.EXTRA_LONGITUDE, mNewEventFilter.getLongitude());
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
                                mNewEventFilter.setLatitude(point.getLatitude());
                                mNewEventFilter.setLongitude(point.getLongitude());
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

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        switch (reference) {
            case MIN_MEMBERS: {
                mNewEventFilter.setMinSize(number < EventFilterDialogFragment.MAX_MEMBERS ? number : EventFilterDialogFragment.MAX_MEMBERS);
                mMinSizeView.setText(formatMinMembers(number));
                break;
            }
            case MAX_MEMBERS: {
                mNewEventFilter.setMaxSize(number < EventFilterDialogFragment.MAX_MEMBERS ? number : EventFilterDialogFragment.MAX_MEMBERS);
                mMaxSizeView.setText(formatMaxMembers(number));
                break;
            }
            case MIN_PRICE: {
                mNewEventFilter.setMinPrice(fullNumber < EventFilterDialogFragment.MAX_PRICE ? fullNumber : EventFilterDialogFragment.MAX_PRICE);
                mMinPriceView.setText(formatMinPrice(getActivity(), mNewEventFilter.getCurrencyCode(), fullNumber));
                break;
            }
            case MAX_PRICE: {
                mNewEventFilter.setMaxPrice(fullNumber < EventFilterDialogFragment.MAX_PRICE ? fullNumber : EventFilterDialogFragment.MAX_PRICE);
                mMaxPriceView.setText(formatMaxPrice(getActivity(), mNewEventFilter.getCurrencyCode(), fullNumber));
                break;
            }
        }
    }

    private String formatMinMembers(int size) {
        if (size <= Event.MEMBER_LIMIT_MIN) {
            return getString(R.string.no_min);
        }
        return String.valueOf(size);
    }

    private String formatMaxMembers(int size) {
        if (size >= Event.MEMBER_LIMIT_MAX) {
            return getString(R.string.no_max);
        }
        return String.valueOf(size);
    }

    private String formatMinPrice(Context context, String currencyCode, double amount) {
        if (amount <= Price.MIN_AMOUNT) {
            return getString(R.string.no_min);
        }
        return PriceHelper.formatPrice(context, currencyCode, amount);
    }

    private String formatMaxPrice(Context context, String currencyCode, double amount) {
        if (amount >= Price.MAX_AMOUNT) {
            return context.getString(R.string.no_max);
        }
        return PriceHelper.formatPrice(context, currencyCode, amount);
    }

    private String formatDistance(int distance) {
        if (EventSearchFilter.KILOMETERS.equals(mNewEventFilter.getDistanceUnits())) {
            return getString(R.string.kilometers, distance);
        }
        return getString(R.string.miles, distance);
    }
}