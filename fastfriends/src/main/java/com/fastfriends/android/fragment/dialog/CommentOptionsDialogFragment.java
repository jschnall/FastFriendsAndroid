package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.widget.ArrayAdapter;

import com.fastfriends.android.R;

/**
 * Created by jschnall on 2/20/14.
 */
public class CommentOptionsDialogFragment extends DialogFragment {
    // Fragment launch args
    public static final String ARG_ID = "id";

    // Must align with R.arrays.comment_options
    public static final int DELETE = 0;
    public static final int EDIT = 1;

    private long mCommentId;

    public interface CommentOptionsListener {
        public void onEditComment(long commentId);
        public void onDeleteComment(long commentId);
    }
    private CommentOptionsListener mCommentOptionsListener;

    public CommentOptionsDialogFragment() {
        // Required empty constructor
    }

    public static CommentOptionsDialogFragment newInstance(long id) {
        CommentOptionsDialogFragment fragment = new CommentOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mCommentId = bundle.getLong(ARG_ID);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                R.array.comment_options, R.layout.option_item);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.comment_options);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int position) {
                if (mCommentOptionsListener != null) {
                    switch (position) {
                        case DELETE:
                            mCommentOptionsListener.onDeleteComment(mCommentId);
                            break;
                        case EDIT:
                            mCommentOptionsListener.onEditComment(mCommentId);
                            break;
                    }
                }
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CommentOptionsListener) {
            mCommentOptionsListener = (CommentOptionsListener) getParentFragment();
        }
        else if (activity instanceof CommentOptionsListener) {
            mCommentOptionsListener = (CommentOptionsListener) activity;
        }
    }

}