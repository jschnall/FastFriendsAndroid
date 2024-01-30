package com.fastfriends.android.fragment.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Comment;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import retrofit.RetrofitError;

/**
 * Created by jschnall on 2/20/14.
 */
public class CommentEditDialogFragment extends DialogFragment {
    private final static String LOGTAG = CommentEditDialogFragment.class.getSimpleName();

    // Fragment launch args
    public static final String ARG_COMMENT = "comment";

    private Comment mComment;

    // Progress
    private View mStatusLayout;
    private TextView mStatusTextView;

    public interface CommentEditListener {
        public void onEdited(Comment comment);
    }
    private CommentEditListener mCommentEditListener;

    private class EditCommentTask extends AsyncTask<Comment, Void, String> {
        Comment newComment;

        @Override
        protected void onPreExecute() {
            showProgress(true, getString(R.string.progress_please_wait));
        }

        @Override
        protected String doInBackground(Comment... params) {
            Comment comment = params[0];

            try {
                AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(getActivity());
                if (authToken != null) {
                    FastFriendsWebService webService = WebServiceManager.getWebService();
                    newComment = webService.editComment(comment.getId(), authToken.getAuthHeader(), comment);
                }
            } catch (RetrofitError e) {
                Log.e(LOGTAG, "Can't edit comment.", e);
                return e.getMessage();
            } catch (Exception e) {
                Log.e(LOGTAG, "Can't edit comment.", e);
                return e.getMessage();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            showProgress(false, null);
            if (error == null) {
                if (mCommentEditListener != null) {
                    mCommentEditListener.onEdited(newComment);
                    CommentEditDialogFragment.this.dismiss();
                }
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private EditCommentTask mEditCommentTask;
    public CommentEditDialogFragment() {
        // Required empty constructor
    }

    public static CommentEditDialogFragment newInstance(Comment comment) {
        CommentEditDialogFragment fragment = new CommentEditDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COMMENT, comment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mComment = bundle.getParcelable(ARG_COMMENT);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();

        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_comment_edit, null);
        final EditText commentView = (EditText) layout.findViewById(R.id.comment);
        commentView.setText(mComment.getMessage());

        mStatusLayout = layout.findViewById(R.id.status);
        mStatusTextView = (TextView) layout.findViewById(R.id.status_message);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.edit_comment);
        builder.setView(layout);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setPositiveButton(R.string.action_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mComment.setMessage(commentView.getText().toString());
                mEditCommentTask = new EditCommentTask();
                mEditCommentTask.execute(mComment);
            }
        });

        return builder.create();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CommentEditListener) {
            mCommentEditListener = (CommentEditListener) getParentFragment();
        }
        else if (activity instanceof CommentEditListener) {
            mCommentEditListener = (CommentEditListener) activity;
        }
    }

    public void showProgress(final boolean show, String message) {
        mStatusTextView.setText(message);
        mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}