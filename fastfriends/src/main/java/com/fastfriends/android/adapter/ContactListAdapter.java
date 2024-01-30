package com.fastfriends.android.adapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.model.Contact;
import com.fastfriends.android.model.Event;
import com.fastfriends.android.model.Page;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by jschnall on 2/6/14.
 */
public class ContactListAdapter extends BaseAdapter {
    private static final String LOGTAG = ContactListAdapter.class.getSimpleName();

    private Activity mActivity;
    private List<Contact> mContacts;
    private HashSet<Contact> mSelectedContacts;

    //public class NameComparator implements Comparator<Contact>
    //{
    //    public int compare(Contact left, Contact right) {
    //        return left.getDisplayName().compareTo(right.getDisplayName());
    //    }
    //}

    public ContactListAdapter(Activity activity) {
        mActivity = activity;
        mContacts = new ArrayList<Contact>();
        mSelectedContacts = new HashSet<Contact>();
    }

    @Override
    public int getCount() {
        return mContacts.size();
    }

    @Override
    public Object getItem(int i) {
        return mContacts.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Contact contact = mContacts.get(position);
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);
            view = inflater.inflate(R.layout.list_contact_item, parent, false);
        } else {
            view = convertView;
        }


        ImageView portraitView = (ImageView) view.findViewById(R.id.portrait);
        String portrait = contact.getPortrait();
        if (portrait == null) {
            portraitView.setImageResource(R.drawable.ic_person);
        } else {
            portraitView.setImageDrawable(null);
            ImageLoader.getInstance().displayImage(contact.getPortrait(), portraitView);
        }

        TextView nameView = (TextView) view.findViewById(R.id.name);
        nameView.setText(contact.getFirstName() + " " + contact.getLastName());

        View selector = view.findViewById(R.id.selector);
        if (mSelectedContacts.contains(contact)) {
            selector.setVisibility(View.VISIBLE);
        } else {
            selector.setVisibility(View.GONE);
        }

        return view;
    }


    /*
    public void initContacts(Context context) {
        mContacts.clear();

        HashMap<String, Contact> contacts = new HashMap<String, Contact>();
        HashSet<String> emailHashSet = new HashSet<String>();

        ContentResolver cr = context.getContentResolver();
        String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_THUMBNAIL_URI,
                ContactsContract.CommonDataKinds.Email.DATA,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                ContactsContract.CommonDataKinds.Photo.HAS_PHONE_NUMBER};
        String order = "CASE WHEN "
                + ContactsContract.Contacts.DISPLAY_NAME
                + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                + ContactsContract.Contacts.DISPLAY_NAME
                + ", "
                + ContactsContract.CommonDataKinds.Email.DATA
                + " COLLATE NOCASE";
        //String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
        String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE '' AND " +
                ContactsContract.CommonDataKinds.Photo.HAS_PHONE_NUMBER + " = 1";
        Cursor c = null;
        try {
            c = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);
            if (c.moveToFirst()) {
                do {
                    String email = c.getString(3);

                    // keep unique emails only
                    if (emailHashSet.add(email.toLowerCase())) {
                        String name = c.getString(1);
                        if (contacts.containsKey(name)) {
                            // Already added contact with this name, add email to them
                            contacts.get(name).getEmails().add(email);
                        } else {
                            // Add new contact
                            Contact contact = new Contact();
                            contact.setName(name);
                            contact.setPortrait(c.getString(2));
                            contact.getEmails().add(email);
                            contacts.put(name, contact);
                        }
                    }
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Can't get contacts", e);
        } finally {
            if (c != null) {
                c.close();
            }
        }

        mContacts.addAll(contacts.values());
        Collections.sort(mContacts, new NameComparator());
    }
    */

    public void addPage(Page<Contact> contactPage) {
        mContacts.addAll(contactPage.getResults());
        notifyDataSetChanged();
    }

    public void refresh(List<Contact> contacts) {
        mContacts.clear();
        if (contacts != null) {
            mContacts.addAll(contacts);
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(int index) {
        Contact contact = mContacts.get(index);

        if(mSelectedContacts.contains(contact)) {
            mSelectedContacts.remove(contact);
        } else {
            mSelectedContacts.add(contact);
        }

        notifyDataSetChanged();
    }

    public List<Contact> getContacts() {
        return mContacts;
    }

    public List<Long> getSelectedUserIds() {
        List<Long> userIds = new ArrayList<Long>();

        for (Contact contact : mSelectedContacts) {
            userIds.add(contact.getUserId());
        }

        return userIds;
    }
}