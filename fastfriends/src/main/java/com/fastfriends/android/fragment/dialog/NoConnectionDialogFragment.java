package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.fragment.CreateAccountFragment;
import com.fastfriends.android.fragment.ForgotPasswordFragment;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;
import retrofit.RetrofitError;

public class NoConnectionDialogFragment extends DialogFragment {
    private static final String LOGTAG = NoConnectionDialogFragment.class.getSimpleName();

    public interface OnCloseListener {
        public void onClose();
    }
    private OnCloseListener mOnCloseListener;

    public static NoConnectionDialogFragment newInstance() {
        NoConnectionDialogFragment fragment = new NoConnectionDialogFragment();
        //Bundle args = new Bundle();
        //fragment.setArguments(args);
        return fragment;
    }

    public NoConnectionDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        //if (bundle != null) {
        //}
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.error_no_connection)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mOnCloseListener != null) {
                            mOnCloseListener.onClose();
                        }
                    }
                })
                .create();

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOnCloseListener = (OnCloseListener) FastFriendsApplication.getAppContext();
    }
}
