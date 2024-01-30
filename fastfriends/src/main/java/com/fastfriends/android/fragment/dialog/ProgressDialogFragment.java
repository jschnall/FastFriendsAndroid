package com.fastfriends.android.fragment.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;

import com.fastfriends.android.R;

/**
 * Created by jschnall on 2/20/14.
 */
public class ProgressDialogFragment extends DialogFragment {
    // Fragment launch args
    public static final String ARG_MESSAGE = "message";

    String mMessage;

    public ProgressDialogFragment() {
        // Required empty constructor
    }

    public static ProgressDialogFragment newInstance(String message) {
        ProgressDialogFragment fragment = new ProgressDialogFragment ();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        fragment.setArguments(args);
        fragment.setCancelable(false);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMessage = bundle.getString(ARG_MESSAGE);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(mMessage);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);

        // Disable the back button
        DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                                 KeyEvent event) {
                if( keyCode == KeyEvent.KEYCODE_BACK){
                    return true;
                }
                return false;
            }
        };
        dialog.setOnKeyListener(keyListener);
        return dialog;
    }

}