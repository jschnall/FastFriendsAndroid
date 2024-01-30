package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fastfriends.android.FastFriendsApplication;
import com.fastfriends.android.R;
import com.fastfriends.android.adapter.ContactListAdapter;
import com.fastfriends.android.helper.SharingHelper;
import com.fastfriends.android.model.AuthToken;
import com.fastfriends.android.model.Contact;
import com.fastfriends.android.model.Page;
import com.fastfriends.android.web.FastFriendsWebService;
import com.fastfriends.android.web.WebServiceManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;

/**
 * Created by jschnall on 2/20/14.
 */
public class ImportContactsDialogFragment extends DialogFragment {
    private final static String LOGTAG = ImportContactsDialogFragment.class.getSimpleName();
    private final static String EMAIL_FILE_NAME = "emails";

    // Fragment launch args
    public static final String ARG_EXISTING_CONTACTS = "existing_contacts";

    private List<String> mExistingContacts; // Previously imported emails
    private List<String> mNewContacts;  // Selected for import now

    // Progress
    private View mStatusLayout;
    private TextView mStatusTextView;

    private ListView mListView;
    private ContactListAdapter mListAdapter;
    private View mEmptyLayout;
    private Button mInviteButton;
    AlertDialog mDialog;

    public interface OnSelectedListener {
        public void onSelected(List<Long> userIds);
    }
    private OnSelectedListener mOnSelectedListener;

    private class FindContactsTask extends AsyncTask<String, Void, String> {
        List<Contact> mContacts = null;

        @Override
        protected void onPreExecute() {
            showProgress(true, getString(R.string.progress_please_wait));
        }

        @Override
        protected String doInBackground(String... params) {
            FragmentActivity activity = getActivity();
            if (activity != null && writeEmailFile(activity)) {
                try {
                    if (activity != null) {
                        AuthToken authToken = FastFriendsApplication.getInstance().getAuthToken(activity);
                        if (authToken != null) {
                            FastFriendsWebService webService = WebServiceManager.getWebService();

                            File file = activity.getFileStreamPath(EMAIL_FILE_NAME);
                            TypedFile typedFile = new TypedFile("application/octet-stream", file);
                            mContacts = webService.findContacts(authToken.getAuthHeader(), typedFile);
                        }
                    }
                } catch (RetrofitError e) {
                    Log.e(LOGTAG, "Can't find contacts.", e);
                    return e.getMessage();
                } catch (Exception e) {
                    Log.e(LOGTAG, "Can't find contacts.", e);
                    return e.getMessage();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String error) {
            showProgress(false, null);
            if (error == null) {
                mListAdapter.refresh(mContacts);
            } else {
                Activity activity = getActivity();
                if (activity != null) {
                    Toast.makeText(activity, error, Toast.LENGTH_SHORT).show();
                }
            }

            if (mListAdapter.getCount() > 0) {
                mListView.setVisibility(View.VISIBLE);
                mEmptyLayout.setVisibility(View.GONE);
            } else {
                mListView.setVisibility(View.GONE);
                mEmptyLayout.setVisibility(View.VISIBLE);
            }
        }
    }
    FindContactsTask mFindContactsTask;

    public ImportContactsDialogFragment() {
        // Required empty constructor
    }

    public static ImportContactsDialogFragment newInstance(String[] emails) {
        ImportContactsDialogFragment fragment = new ImportContactsDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_EXISTING_CONTACTS, emails);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExistingContacts = new ArrayList<String>();
        mNewContacts = new ArrayList<String>();

        Bundle bundle = getArguments();
        if (bundle != null) {
            String[] emails = bundle.getStringArray(ARG_EXISTING_CONTACTS);
            if (emails != null) {
                mExistingContacts.addAll(Arrays.asList(emails));
            }
        }

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity activity = getActivity();

        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_contacts_import, null);

        mEmptyLayout = layout.findViewById(R.id.empty_layout);
        mInviteButton = (Button) layout.findViewById(R.id.invite_friends);
        mInviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharingHelper helper = new SharingHelper(activity,
                        activity.getString(R.string.invite_contacts),
                        activity.getString(R.string.invite_subject),
                        activity.getString(R.string.invite_message));
                helper.createChooser();
            }
        });

        mStatusLayout = layout.findViewById(R.id.status);
        mStatusTextView = (TextView) layout.findViewById(R.id.status_message);

        mListView = (ListView) layout.findViewById(R.id.list);
        mListAdapter = new ContactListAdapter(activity);
        mListView.setAdapter(mListAdapter);

        mDialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                .setTitle(R.string.dialog_contacts_import_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.import_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mOnSelectedListener != null) {
                            mOnSelectedListener.onSelected(mListAdapter.getSelectedUserIds());
                        }
                    }
                }).create();

        // If listener used in setAdapter above, the dialog is dismissed on click
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListAdapter.toggleSelection(position);
                if (mListAdapter.getSelectedUserIds().size() > 0) {
                    mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(true);
                } else {
                    mDialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);
                }

            }
        });

        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                mDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        });

        mFindContactsTask = new FindContactsTask();
        mFindContactsTask.execute();

        return mDialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnSelectedListener) {
            mOnSelectedListener = (OnSelectedListener) getParentFragment();
        }
        else if (activity instanceof OnSelectedListener) {
            mOnSelectedListener = (OnSelectedListener) activity;
        }
    }

    public boolean writeEmailFile(Context context) {
        HashSet<String> emailHashSet = new HashSet<String>();

        ContentResolver cr = context.getContentResolver();
        String[] PROJECTION = new String[]{ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                //ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Email.TYPE,
                //ContactsContract.CommonDataKinds.Email.HAS_PHONE_NUMBER
        };
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        //String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE '' AND " +
        //        ContactsContract.CommonDataKinds.Photo.HAS_PHONE_NUMBER + " = 1";
        Cursor c = null;
        FileOutputStream outputStream = null;
        try {
            c = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
            if (c.moveToFirst()) {
                outputStream = context.openFileOutput(EMAIL_FILE_NAME, Context.MODE_PRIVATE);
                PrintWriter writer = new PrintWriter(outputStream);

                String email = c.getString(2);
                writer.write(email);
                while (c.moveToNext()) {
                    email = c.getString(2);

                    // keep unique emails only
                    if (emailHashSet.add(email.toLowerCase())) {
                        writer.write("\n" + email);
                    }
                }
                return true;
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't write contacts file", e);
        } finally {
            if (c != null) {
                c.close();
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                    outputStream.getFD().sync();
                    outputStream.close();
                } catch (Exception e) {
                    // Do nothing
                }
            }
        }
        return false;
    }

    public void showProgress(final boolean show, String message) {
        mStatusTextView.setText(message);
        mStatusLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}