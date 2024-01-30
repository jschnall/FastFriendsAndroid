package com.fastfriends.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerBuilder;
import com.doomonafireball.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.EditEventActivity;
import com.fastfriends.android.activity.MapActivity;
import com.fastfriends.android.fragment.dialog.ProgressDialogFragment;
import com.fastfriends.android.fragment.dialog.TagPickerDialogFragment;
import com.fastfriends.android.helper.DateHelper;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.helper.PriceHelper;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.model.Point;
import com.fastfriends.android.model.Price;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import android.text.format.DateFormat;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import retrofit.RetrofitError;


public class EditEventFragment extends Fragment implements TagPickerDialogFragment.OnTagsPickedListener,
        NumberPickerDialogFragment.NumberPickerDialogHandler {
    private final static String LOGTAG = EditEventFragment.class.getSimpleName();

    private static final String ARG_EVENT = "event";

    // Child Fragments
    private static final String CONFIRM_DISCARD_FRAGMENT = "confirm_discard";
    private static final String PROGRESS_FRAGMENT = "progress";
    private final static String DATE_PICKER_FRAGMENT = "date_picker";
    private final static String TIME_PICKER_FRAGMENT = "time_picker";
    private final static String TAG_PICKER_FRAGMENT = "tag_picker";

    private final static int MEMBER_LIMIT = 1;
    private final static int PRICE = 2;

    public static final int REQUEST_SELECT_LOCATION = 1;

    // These must align with R.array.join_policy_choices
    public static final int POLICY_OPEN = 0;
    public static final int POLICY_APPROVE = 1;
    public static final int POLICY_INVITE = 2;
    public static final int POLICY_OWNER_INVITE = 3;
    public static final int FRIENDS_ONLY = 4;
    private Event mOldEvent;
    private Event mNewEvent;

    private Calendar mStartDate;
    private Calendar mEndDate;

    // UI
    private EditText mEventNameView;
    private Button mPriceView;
    private Button mLocationNameView;
    private Button mStartDateView;
    private Button mStartTimeView;
    private ViewGroup mEndLayout;
    private Button mEndDateView;
    private Button mEndTimeView;
    private Button mTagsView;
    private EditText mDescriptionView;
    private Spinner mJoinPolicyView;
    private ViewGroup mMemberLimitLayout;
    private TextView mMemberLimitTitleView;
    private Button mMemberLimitView;
    private CheckBox mMemberLimitCheckBox;

    // Validation
    private boolean mSubmitting = false;
    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;

    private OnShowProgressListener mOnShowProgressListener;
    private ProgressDialogFragment mProgressDialogFragment;

    private Boolean mDatesModified = false;
    private Boolean mTagsModified = false;

    private Boolean mShowAllFields = false;

    public static class ConfirmDiscardDialogFragment extends DialogFragment {
        FragmentActivity fragmentActivity;

        public ConfirmDiscardDialogFragment() {
            // Required empty public constructor
        }

        public static ConfirmDiscardDialogFragment newInstance() {
            ConfirmDiscardDialogFragment fragment = new ConfirmDiscardDialogFragment();
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dialog_confirm_discard_event_title)
                    .setMessage(R.string.dialog_confirm_discard_event_message)
                    .setNegativeButton(R.string.dialog_confirm_discard_event_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setPositiveButton(R.string.dialog_confirm_discard_event_positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                            getActivity().finish();
                        }
                    })
                    .create();
            return dialog;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            fragmentActivity = (FragmentActivity) activity;
        }

    };

    private class AddEventTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    if (mNewEvent.getLanguage() == null) {
                        mNewEvent.setLanguage(Locale.getDefault().getLanguage());
                    }

                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    if (isNew()) {
                        webService.addEvent(authToken.getAuthHeader(), mNewEvent);
                    } else {
                        webService.editEvent(mOldEvent.getId(), authToken.getAuthHeader(), mNewEvent);
                    }
                }
            } catch (RetrofitError retrofitError) {
                if (isNew()) {
                    Log.e(LOGTAG, "Can't create Event.", retrofitError);
                } else {
                    Log.e(LOGTAG, "Can't edit Event with id " + mOldEvent.getId(), retrofitError);
                }

                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                if (isNew()) {
                    Log.e(LOGTAG, "Can't create Event.", e);
                } else {
                    Log.e(LOGTAG, "Can't edit Event with id " + mOldEvent.getId(), e);
                }
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSubmitting = false;
            if (mOnShowProgressListener == null) {
                if (mProgressDialogFragment != null) {
                    mProgressDialogFragment.dismiss();
                }
            } else {
                mOnShowProgressListener.showProgress(false, null);
            }

            if (error == null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EditEventActivity.EXTRA_EVENT, mNewEvent);

                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private AddEventTask mAddEventTask;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.start_date: {
                    FragmentManager fm = getChildFragmentManager();
                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                            .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
                                    mDatesModified = true;
                                    mStartDate.set(year, month, day);
                                    mStartDateView.setText(formatDate(year, month, day));
                                    fixEndDateTime();
                                }
                            }, mStartDate.get(Calendar.YEAR),
                                    mStartDate.get(Calendar.MONTH),
                                    mStartDate.get(Calendar.DAY_OF_MONTH));
                    calendarDatePickerDialog.show(fm, DATE_PICKER_FRAGMENT);
                    break;
                }
                case R.id.start_time: {
                    FragmentManager fm = getChildFragmentManager();
                    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                            .newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(RadialTimePickerDialog dialog, int hour, int minute) {
                                    mDatesModified = true;
                                    mStartDate.set(mStartDate.get(Calendar.YEAR),
                                            mStartDate.get(Calendar.MONTH),
                                            mStartDate.get(Calendar.DAY_OF_MONTH),
                                            hour, minute);
                                    mStartTimeView.setText(formatTime(hour, minute));
                                    fixEndDateTime();
                                }
                            }, mStartDate.get(Calendar.HOUR_OF_DAY),
                                    mStartDate.get(Calendar.MINUTE),
                                    DateFormat.is24HourFormat(getActivity()));
                    timePickerDialog.show(fm, TIME_PICKER_FRAGMENT);
                    break;
                }
                case R.id.end_date: {
                    FragmentManager fm = getChildFragmentManager();
                    CalendarDatePickerDialog calendarDatePickerDialog = CalendarDatePickerDialog
                            .newInstance(new CalendarDatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(CalendarDatePickerDialog dialog, int year, int month, int day) {
                                    mDatesModified = true;
                                    mEndDate.set(year, month, day);
                                    mEndDateView.setText(formatDate(year, month, day));
                                    fixEndDateTime();
                                }
                            }, mEndDate.get(Calendar.YEAR),
                                    mEndDate.get(Calendar.MONTH),
                                    mEndDate.get(Calendar.DAY_OF_MONTH));
                    calendarDatePickerDialog.show(fm, DATE_PICKER_FRAGMENT);
                    break;
                }
                case R.id.end_time: {
                    FragmentManager fm = getChildFragmentManager();
                    RadialTimePickerDialog timePickerDialog = RadialTimePickerDialog
                            .newInstance(new RadialTimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(RadialTimePickerDialog dialog, int hour, int minute) {
                                    mDatesModified = true;
                                    mEndDate.set(mEndDate.get(Calendar.YEAR),
                                            mEndDate.get(Calendar.MONTH),
                                            mEndDate.get(Calendar.DAY_OF_MONTH),
                                            hour, minute);
                                    mEndTimeView.setText(formatTime(hour, minute));
                                    fixEndDateTime();
                                }
                            }, mEndDate.get(Calendar.HOUR_OF_DAY), mEndDate.get(Calendar.MINUTE),
                                    DateFormat.is24HourFormat(getActivity()));
                    timePickerDialog.show(fm, TIME_PICKER_FRAGMENT);
                    break;
                }
                case R.id.tags: {
                    FragmentManager fm = getChildFragmentManager();
                    TagPickerDialogFragment tagPickerDialog = TagPickerDialogFragment.newInstance((ArrayList<String>) mNewEvent.getTagNames());
                    tagPickerDialog.show(fm, TAG_PICKER_FRAGMENT);
                    break;
                }
                case R.id.location: {
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_EDIT_MODE, true);
                    intent.putExtra(MapActivity.EXTRA_LOCATION, mNewEvent.getLocation());
                    startActivityForResult(intent, REQUEST_SELECT_LOCATION);
                    break;
                }
                case R.id.member_limit: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EditEventFragment.this)
                            .setReference(MEMBER_LIMIT)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Event.MEMBER_LIMIT_MIN)
                            .setMinNumber(Event.MEMBER_LIMIT_MAX)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.GONE);
                    builder.show();
                    break;
                }
                case R.id.price: {
                    NumberPickerBuilder builder = new NumberPickerBuilder()
                            .setFragmentManager(getChildFragmentManager())
                            .setTargetFragment(EditEventFragment.this)
                            .setReference(PRICE)
                            .setStyleResId(R.style.BetterPickersDialogFragment_Light)
                            .setMinNumber(Price.MIN_AMOUNT)
                            .setMaxNumber(Price.MAX_AMOUNT)
                            .setPlusMinusVisibility(View.GONE)
                            .setDecimalVisibility(View.VISIBLE);
                    builder.show();
                    break;
                }
            }
        }
    };

    public static EditEventFragment newInstance(Event event) {
        EditEventFragment fragment = new EditEventFragment();
        if (event != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_EVENT, event);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public EditEventFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mOldEvent = bundle.getParcelable(ARG_EVENT);
        }

        mNewEvent = new Event();
        if (mOldEvent != null) {
            mNewEvent.copy(mOldEvent);
        }
        initDates();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_event_edit, container, false);

        mEventNameView = (EditText) layout.findViewById(R.id.title);
        mEventNameView.setText(mNewEvent.getName());

        mPriceView = (Button) layout.findViewById(R.id.price);
        Price price = mNewEvent.getPrice();
        if (Math.abs(price.getAmount()) > 0.000001) {
            mPriceView.setText(PriceHelper.formatPrice(getActivity(), price.getCurrencyCode(), price.getAmount()));
        }
        mPriceView.setOnClickListener(mOnClickListener);

        mLocationNameView = (Button) layout.findViewById(R.id.location);
        mLocationNameView.setOnClickListener(mOnClickListener);
        Location location = mNewEvent.getLocation();
        mLocationNameView.setText(location.getStreetAddress());

        mStartDateView = (Button) layout.findViewById(R.id.start_date);
        mStartDateView.setOnClickListener(mOnClickListener);
        mStartDateView.setText(formatDate(mStartDate));

        mStartTimeView = (Button) layout.findViewById(R.id.start_time);
        mStartTimeView.setOnClickListener(mOnClickListener);
        mStartTimeView.setText(formatTime(mStartDate));

        mEndLayout = (ViewGroup) layout.findViewById(R.id.end_layout);

        mEndDateView = (Button) layout.findViewById(R.id.end_date);
        mEndDateView.setOnClickListener(mOnClickListener);
        mEndDateView.setText(formatDate(mEndDate));

        mEndTimeView = (Button) layout.findViewById(R.id.end_time);
        mEndTimeView.setOnClickListener(mOnClickListener);
        mEndTimeView.setText(formatTime(mEndDate));

        mTagsView = (Button) layout.findViewById(R.id.tags);
        mTagsView.setText(formatTags(mNewEvent.getTagNames()));
        mTagsView.setOnClickListener(mOnClickListener);

        mJoinPolicyView = (Spinner) layout.findViewById(R.id.join_policy);
        ArrayAdapter<CharSequence> policyAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.join_policy_choices, R.layout.spinner_item);
        policyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mJoinPolicyView.setAdapter(policyAdapter);
        mJoinPolicyView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                switch(position) {
                    case POLICY_OPEN: {
                        mNewEvent.setJoinPolicy(Event.OPEN);
                        break;
                    }
                    case POLICY_APPROVE: {
                        mNewEvent.setJoinPolicy(Event.OWNER_APPROVAL);
                        break;
                    }
                    case POLICY_INVITE: {
                        mNewEvent.setJoinPolicy(Event.INVITE_ONLY);
                        break;
                    }
                    case POLICY_OWNER_INVITE: {
                        mNewEvent.setJoinPolicy(Event.OWNER_INVITE_ONLY);
                        break;
                    }
                    case FRIENDS_ONLY: {
                        mNewEvent.setJoinPolicy(Event.FRIENDS_ONLY);
                        break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        mMemberLimitLayout = (ViewGroup) layout.findViewById(R.id.member_limit_layout);
        mMemberLimitTitleView = (TextView) layout.findViewById(R.id.member_limit_title);
        mMemberLimitView = (Button) layout.findViewById(R.id.member_limit);
        mMemberLimitView.setOnClickListener(mOnClickListener);
        mMemberLimitCheckBox = (CheckBox) layout.findViewById(R.id.member_limit_checkbox);
        mMemberLimitCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mMemberLimitView.setEnabled(checked);
                if (checked) {
                    mMemberLimitTitleView.setTextAppearance(getActivity(), R.style.Text_Large);
                    String limitStr = mMemberLimitView.getText().toString();
                    if (!TextUtils.isEmpty(limitStr)) {
                        mNewEvent.setMaxMembers(Integer.valueOf(mMemberLimitView.getText().toString()));
                    }
                } else {
                    mMemberLimitTitleView.setTextAppearance(getActivity(), R.style.Text_Muted_Large);
                    mNewEvent.setMaxMembers(-1);
                }
            }
        });

        mDescriptionView = (EditText) layout.findViewById(R.id.description);
        mDescriptionView.setText(mNewEvent.getDescription());

        showAllFields(false);

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_event, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mShowAllFields) {
            menu.findItem(R.id.action_more).setVisible(false);
            menu.findItem(R.id.action_less).setVisible(true);
        } else {
            menu.findItem(R.id.action_more).setVisible(true);
            menu.findItem(R.id.action_less).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_cancel: {
                if (isModified()) {
                    showConfirmDiscardDialog();
                } else {
                    getActivity().finish();
                }
                return true;
            }
            case R.id.action_done: {
                submit();
                return true;
            }
            case R.id.action_more: {
                showAllFields(true);
                return true;
            }
            case R.id.action_less: {
                showAllFields(false);
                return true;
            }
        }
        return false;
    }

    private void initDates() {
        mStartDate = Calendar.getInstance();
        mEndDate = Calendar.getInstance();

        Date oldStartDate = null;
        Date oldEndDate = null;
        if (mOldEvent != null) {
            oldStartDate = mOldEvent.getStartDate();
            oldEndDate = mOldEvent.getEndDate();
        }

        if (oldStartDate == null) {
            // Init startDate to upcoming hour
            mStartDate.set(Calendar.HOUR_OF_DAY, mStartDate.get(Calendar.HOUR_OF_DAY) + 1);
            mStartDate.set(Calendar.MINUTE, 0);
        } else {
            mStartDate.setTime(mOldEvent.getStartDate());
        }

        if (oldEndDate == null) {
            // Init endDate to one hour after startDate
            mEndDate.set(Calendar.HOUR_OF_DAY, mEndDate.get(Calendar.HOUR_OF_DAY) + 2);
            mEndDate.set(Calendar.MINUTE, 0);
        } else {
            mEndDate.setTime(mOldEvent.getEndDate());
        }
    }

    private String formatDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);

        return formatDate(calendar);
    }

    private String formatDate(Calendar calendar) {
        if (DateHelper.isToday(calendar)) {
            return getString(R.string.today);
        }
        if (DateHelper.isWithinDaysFuture(calendar, 1)) {
            return getString(R.string.tomorrow);
        }

        Date date = calendar.getTime();
        return DateFormat.getDateFormat(getActivity()).format(date);
    }

    private String formatTime(int hour, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(0, 0, 0, hour, minute);

        return formatTime(calendar);
    }

    private String formatTime(Calendar calendar) {
        return DateFormat.getTimeFormat(getActivity()).format(calendar.getTime());
    }

    private String formatTags(List<String> tags) {
        if (tags.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> iter = tags.iterator();
        builder.append(iter.next());
        while(iter.hasNext()) {
            builder.append(", " + iter.next());
        }
        return builder.toString();
    }


    private void submit() {
        if (mSubmitting) {
            return;
        }
        mSubmitting = true;

        if (!validateSubmit()) {
            if (mViewToFocus != null) {
                mViewToFocus.requestFocus();
                Toast.makeText(getActivity(), mViewToFocus.getError(), Toast.LENGTH_SHORT).show();
            }
            mSubmitting = false;
            return;
        }

        // Hide keyboard and show progress
        Activity activity = getActivity();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEventNameView.getWindowToken(), 0);
        if (mOnShowProgressListener == null) {
            showProgressDialogFragment();
        } else {
            mOnShowProgressListener.showProgress(true, getString(R.string.progress_saving_event));
        }

        mAddEventTask = new AddEventTask();
        mAddEventTask.execute();
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mEventNameView, false)
                & validate(mLocationNameView, false) & validate(mStartDateView, false)
                & validate(mEndDateView, false) & validate(mDescriptionView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null || !mHasSubmitted) {
            return false;
        }

        switch (view.getId()) {
            case R.id.title: {
                String eventName = mEventNameView.getText().toString();
                if (TextUtils.isEmpty(eventName)) {
                    if (!hideErrorsOnly) {
                        mEventNameView.setError(getString(R.string.error_event_name_required));
                        setViewToFocus(mEventNameView);
                    }
                    return false;
                }
                mNewEvent.setName(eventName);
                break;
            }
            case R.id.location: {
                Location location = mNewEvent.getLocation();
                Point point = location.getPoint();
                if (LocationHelper.isEmpty(point.getLatitude(), point.getLongitude())) {
                    if (!hideErrorsOnly) {
                        mLocationNameView.setError(getString(R.string.error_location_required));
                        setViewToFocus(mLocationNameView);
                    }
                    return false;
                }
                break;
            }
            case R.id.start_date: {
                mNewEvent.setStartDate(mStartDate.getTime());

                // Events must be created at least 30 mins in advance.
                Calendar minStart = Calendar.getInstance();
                minStart.add(Calendar.MINUTE, 30);

                if (DateHelper.isBeforeDay(mStartDate,  minStart)) {
                    if (!hideErrorsOnly) {
                        mStartDateView.setError(getString(R.string.error_invalid_start_date));
                        setViewToFocus(mStartDateView);
                    }
                    return false;
                }
                if (DateHelper.isToday(mStartDate) && mStartDate.before(minStart)) {
                    if (!hideErrorsOnly) {
                        mStartTimeView.setError(getString(R.string.error_invalid_start_time));
                        setViewToFocus(mStartTimeView);
                    }
                    return false;
                }
                break;
            }
            case R.id.end_date: {
                if (mEndLayout.getVisibility() == View.VISIBLE) {
                    mNewEvent.setEndDate(mEndDate.getTime());
                    if (DateHelper.isBeforeDay(mEndDate,  mStartDate)) {
                        if (!hideErrorsOnly) {
                            mEndDateView.setError(getString(R.string.error_invalid_end_date));
                            setViewToFocus(mEndDateView);
                        }
                        return false;
                    }
                    if (mEndDate.before(mStartDate)) {
                        if (!hideErrorsOnly) {
                            mEndTimeView.setError(getString(R.string.error_invalid_end_time));
                            setViewToFocus(mEndTimeView);
                        }
                        return false;
                    }
                } else {
                    mNewEvent.setEndDate(null);
                }
                break;
            }
            case R.id.description: {
                mNewEvent.setDescription(mDescriptionView.getText().toString());
                break;
            }
        }

        view.setError(null);
        return true;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mOnShowProgressListener = (OnShowProgressListener) activity;
        } catch (ClassCastException e) {
            Log.d(LOGTAG, "No OnShowProgressListener attached.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnShowProgressListener = null;
    }

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_saving_event));
        getChildFragmentManager().beginTransaction()
                .add(mProgressDialogFragment, PROGRESS_FRAGMENT)
                .commitAllowingStateLoss();
    }

    private void setViewToFocus(TextView viewToFocus) {
        if (mViewToFocus == null) {
            mViewToFocus = viewToFocus;
        }
    }

    private boolean isModified() {
        if (isNew()) {
            return mDatesModified || !TextUtils.isEmpty(mEventNameView.getText()) ||
                    !TextUtils.isEmpty(mLocationNameView.getText()) ||
                    !TextUtils.isEmpty(mTagsView.getText()) ||
                    !TextUtils.isEmpty(mDescriptionView.getText()) ||
                    !TextUtils.isEmpty(mPriceView.getText());
        }

        return mDatesModified || mTagsModified ||
                !mEventNameView.getText().toString().equals(mOldEvent.getName()) ||
                !mLocationNameView.getText().toString().equals(mOldEvent.getLocation().getName()) ||
                !mDescriptionView.getText().toString().equals(mOldEvent.getDescription()) ||
                mNewEvent.getPrice() != mOldEvent.getPrice() ||
                mNewEvent.getMaxMembers() != mOldEvent.getMaxMembers();
    }

    private boolean isNew() {
        return mOldEvent == null;
    }

    public boolean onBackPressed() {
        if (mSubmitting) {
            return true;
        }
        if (isModified()) {
            showConfirmDiscardDialog();
            return true;
        }
        return false;
    }

    private void showConfirmDiscardDialog() {
        FragmentManager fm = getChildFragmentManager();
        ConfirmDiscardDialogFragment dialogFragment = ConfirmDiscardDialogFragment.newInstance();
        dialogFragment.show(fm, CONFIRM_DISCARD_FRAGMENT);
    }

    private void fixEndDateTime() {
        // If end date is before start date reset it to the start date + 1 hr
        if (mEndDate.before(mStartDate)) {
            mEndDate.set(Calendar.HOUR_OF_DAY, mStartDate.get(Calendar.HOUR_OF_DAY) + 1);
            mEndDate.set(Calendar.MINUTE, 0);
            mEndDateView.setText(formatDate(mEndDate));
            mEndTimeView.setText(formatTime(mEndDate));
        }
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
                                mNewEvent.setLocation(location);
                                mLocationNameView.setText(location.getStreetAddress());
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onTagsPicked(Set<String> pickedTags) {
        mTagsModified = true;
        List<String> tagNames = mNewEvent.getTagNames();
        tagNames.clear();
        tagNames.addAll(pickedTags);
        mTagsView.setText(formatTags(tagNames));
    }

    private void showAllFields(boolean show) {
        mShowAllFields = show;

        if (show) {
            mEndLayout.setVisibility(View.VISIBLE);
            mMemberLimitLayout.setVisibility(View.VISIBLE);
            mPriceView.setVisibility(View.VISIBLE);
        } else {
            mEndLayout.setVisibility(View.GONE);
            mMemberLimitLayout.setVisibility(View.GONE);
            mPriceView.setVisibility(View.GONE);
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    @Override
    public void onDialogNumberSet(int reference, int number, double decimal, boolean isNegative, double fullNumber) {
        switch (reference) {
            case MEMBER_LIMIT: {
                mNewEvent.setMaxMembers(number);
                mMemberLimitView.setText(String.valueOf(number));
                break;
            }
            case PRICE: {
                Price price = mNewEvent.getPrice();
                price.setAmount(fullNumber);
                mPriceView.setText(PriceHelper.formatPrice(getActivity(), price.getCurrencyCode(), price.getAmount()));
                break;
            }
        }
    }
}