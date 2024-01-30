package com.fastfriends.android.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.activity.EditPlanActivity;
import com.fastfriends.android.activity.MapActivity;
import com.fastfriends.android.fragment.dialog.ProgressDialogFragment;
import com.fastfriends.android.helper.LocationHelper;
import com.fastfriends.android.listener.OnShowProgressListener;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Plan;
import com.fastfriends.android.model.Location;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import retrofit.RetrofitError;


public class EditPlanFragment extends Fragment {
    private final static String LOGTAG = EditPlanFragment.class.getSimpleName();

    private static final String ARG_PLAN = "plan";

    // Child Fragments
    private static final String CONFIRM_DISCARD_FRAGMENT = "confirm_discard";
    private static final String PROGRESS_FRAGMENT = "progress";

    public static final int REQUEST_SELECT_LOCATION = 1;

    private Plan mOldPlan;
    private Plan mNewPlan;

    private int mPlanMaxLength;

    // UI
    private EditText mPlanTextView;
    private TextView mCounterView;
    private Button mLocationNameView;

    // Validation
    private boolean mSubmitting = false;
    private TextView mViewToFocus;
    private TextView mFocusedView = null;
    private boolean mHasSubmitted = false;

    private OnShowProgressListener mOnShowProgressListener;
    private ProgressDialogFragment mProgressDialogFragment;

    private LocationManager mLocationManager;
    private android.location.Location mMyLocation;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
            mMyLocation = location;
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
        }

        @Override
        public void onProviderEnabled(String s) {
        }

        @Override
        public void onProviderDisabled(String s) {
        }
    };

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
                    .setTitle(R.string.dialog_confirm_discard_plan_title)
                    .setMessage(R.string.dialog_confirm_discard_plan_message)
                    .setNegativeButton(R.string.dialog_confirm_discard_plan_negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    })
                    .setPositiveButton(R.string.dialog_confirm_discard_plan_positive, new DialogInterface.OnClickListener() {
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

    private class AddPlanTask extends AsyncTask<Void, Void, String> {
        Activity mActivity;

        public AddPlanTask(Activity activity) {
            mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (mNewPlan.getLanguage() == null) {
                    mNewPlan.setLanguage(Locale.getDefault().getLanguage());
                }
                if (TextUtils.isEmpty(mLocationNameView.getText())) {
                    // Location not selected, use their current location
                    Geocoder geocoder = new Geocoder(mActivity, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(mMyLocation.getLatitude(), mMyLocation.getLongitude(), 1);
                    Address address = addresses.get(0);
                    Location location = new Location(address);
                    mNewPlan.setLocation(location);
                }

                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    if (isNew()) {
                        webService.addPlan(authToken.getAuthHeader(), mNewPlan);
                    } else {
                        webService.editPlan(mOldPlan.getId(), authToken.getAuthHeader(), mNewPlan);
                    }
                }
            } catch (RetrofitError retrofitError) {
                if (isNew()) {
                    Log.e(LOGTAG, "Can't create Plan.", retrofitError);
                } else {
                    Log.e(LOGTAG, "Can't edit Plan with id " + mOldPlan.getId(), retrofitError);
                }

                return WebServiceManager.handleRetrofitError(retrofitError);
            } catch (Exception e) {
                if (isNew()) {
                    Log.e(LOGTAG, "Can't create Plan.", e);
                } else {
                    Log.e(LOGTAG, "Can't edit Plan with id " + mOldPlan.getId(), e);
                }
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            mSubmitting = false;
            if (mOnShowProgressListener == null) {
                mProgressDialogFragment.dismiss();
            } else {
                mOnShowProgressListener.showProgress(false, null);
            }

            if (error == null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EditPlanActivity.EXTRA_PLAN, mNewPlan);

                Activity activity = getActivity();
                activity.setResult(Activity.RESULT_OK, resultIntent);
                getActivity().finish();
            } else {
                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private AddPlanTask mAddPlanTask;

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.location: {
                    Intent intent = new Intent(getActivity(), MapActivity.class);
                    intent.putExtra(MapActivity.EXTRA_EDIT_MODE, true);
                    intent.putExtra(MapActivity.EXTRA_LOCATION, mNewPlan.getLocation());
                    startActivityForResult(intent, REQUEST_SELECT_LOCATION);
                    break;                }
            }
        }
    };

    public static EditPlanFragment newInstance(Plan plan) {
        EditPlanFragment fragment = new EditPlanFragment();
        if (plan != null) {
            Bundle args = new Bundle();
            args.putParcelable(ARG_PLAN, plan);
            fragment.setArguments(args);
        }
        return fragment;
    }

    public EditPlanFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Bundle bundle = getArguments();
        if (bundle != null) {
            mOldPlan = bundle.getParcelable(ARG_PLAN);
        }

        mNewPlan = new Plan();
        if (mOldPlan != null) {
            mNewPlan.copy(mOldPlan);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mPlanMaxLength = getActivity().getResources().getInteger(R.integer.plan_text_max_length);

        View layout = inflater.inflate(R.layout.fragment_plan_edit, container, false);

        mPlanTextView = (EditText) layout.findViewById(R.id.text);
        mPlanTextView.setText(mNewPlan.getText());
        mPlanTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mCounterView.setText(editable.length() + " / " + mPlanMaxLength);
            }
        });

        mCounterView = (TextView) layout.findViewById(R.id.counter);
        String text = mNewPlan.getText();
        if (!TextUtils.isEmpty(text)) {
            mCounterView.setText(text.length() + " / " + mPlanMaxLength);
        }

        mLocationNameView = (Button) layout.findViewById(R.id.location);
        mLocationNameView.setOnClickListener(mOnClickListener);
        Location location = mNewPlan.getLocation();
        mLocationNameView.setText(location.getLocality());

        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.edit_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
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
        }
        return false;
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
        imm.hideSoftInputFromWindow(mPlanTextView.getWindowToken(), 0);
        if (mOnShowProgressListener == null) {
            showProgressDialogFragment();
        } else {
            mOnShowProgressListener.showProgress(true, getString(R.string.progress_saving_plan));
        }

        mAddPlanTask = new AddPlanTask(getActivity());
        mAddPlanTask.execute();
    }

    private boolean validateSubmit() {
        mHasSubmitted = true;
        mViewToFocus = null;

        return validate(mPlanTextView, false);
    }

    private boolean validate(TextView view, boolean hideErrorsOnly) {
        if (view == null || !mHasSubmitted) {
            return false;
        }

        switch (view.getId()) {
            case R.id.text: {
                String text = mPlanTextView.getText().toString();
                if (TextUtils.isEmpty(text)) {
                    if (!hideErrorsOnly) {
                        mPlanTextView.setError(getString(R.string.error_plan_text_required));
                        setViewToFocus(mPlanTextView);
                    }
                    return false;
                }
                mNewPlan.setText(text);
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

    @Override
    public void onResume() {
        super.onResume();

        mLocationManager = LocationHelper.startLocationUpdates(getActivity(), mLocationListener);
        mMyLocation = LocationHelper.getLastKnownLocation(mLocationManager);
    }

    @Override
    public void onPause() {
        super.onPause();

        mLocationManager.removeUpdates(mLocationListener);
    }

    private void showProgressDialogFragment() {
        mProgressDialogFragment = ProgressDialogFragment.newInstance(getString(R.string.progress_signing_in));
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
            return !TextUtils.isEmpty(mPlanTextView.getText()) ||
                    !TextUtils.isEmpty(mLocationNameView.getText());
        }

        return !mPlanTextView.getText().toString().equals(mOldPlan.getText()) ||
                mOldPlan.getLocation().getPoint().getLatitude() != mNewPlan.getLocation().getPoint().getLatitude() ||
                mOldPlan.getLocation().getPoint().getLongitude() != mNewPlan.getLocation().getPoint().getLongitude();
    }

    private boolean isNew() {
        return mOldPlan == null;
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_SELECT_LOCATION: {
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle != null) {
                            Location location = bundle.getParcelable(MapActivity.EXTRA_LOCATION);
                            mLocationNameView.setText(location.getLocality());
                            mNewPlan.setLocation(location);
                        }
                    }
                }
                break;
            }
        }
    }
}