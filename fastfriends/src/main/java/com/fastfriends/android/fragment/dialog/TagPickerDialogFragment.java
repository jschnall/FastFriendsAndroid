package com.fastfriends.android.fragment.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import static android.widget.ExpandableListView.OnChildClickListener;
import static android.widget.ExpandableListView.OnGroupClickListener;

import android.widget.TextView;

import com.fastfriends.android.R;
import com.fastfriends.android.adapter.TagPickerAdapter;
import com.fastfriends.android.model.Tag;
import com.fastfriends.android.text.style.ClickableMovementMethod;
import com.fastfriends.android.text.style.TagSpan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by jschnall on 2/20/14.
 */
public class TagPickerDialogFragment extends DialogFragment {
    // Fragment launch args
    public static final String ARG_TAGS = "tags";

    private static final char TAG_SEPARATOR = '\t';

    // UI
    TextView mPickedTagsView;
    ExpandableListView mTagListView;

    TagPickerAdapter mAdapter;
    TagPickerAdapter.OnTagSelectedListener mOnTagSelectedListener;

    public interface OnTagsPickedListener {
        public void onTagsPicked(Set<String> pickedTags);
    }
    OnTagsPickedListener mOnTagsPickedListener;


    public TagPickerDialogFragment() {
        // Required empty constructor
    }

    public static TagPickerDialogFragment newInstance(ArrayList<String> tags) {
        // TODO Safe adding a callback this way? Won't callback fail if system kills fragment?
        TagPickerDialogFragment fragment = new TagPickerDialogFragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_TAGS, tags);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        List<String> pickedTags = null;
        Bundle bundle = getArguments();
        if (bundle != null) {
            pickedTags = bundle.getStringArrayList(ARG_TAGS);
        }

        mOnTagSelectedListener = new TagPickerAdapter.OnTagSelectedListener() {
            @Override
            public void onTagAdded(String addedTagName) {
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(mPickedTagsView.getText());
                builder.append(formatTag(getActivity(), addedTagName));
                builder.append(TAG_SEPARATOR);
                mPickedTagsView.setText(builder);
            }

            @Override
            public void onTagRemoved(String removedTagName) {
                String str = mPickedTagsView.getText().toString();
                String[] tagNames = str.split(String.valueOf(TAG_SEPARATOR));
                SpannableStringBuilder builder = new SpannableStringBuilder();

                for (String tagName : tagNames) {
                    if (removedTagName.equals(tagName)) {
                        continue;
                    }
                    builder.append(formatTag(getActivity(), tagName));
                    builder.append(TAG_SEPARATOR);
                }
                mPickedTagsView.setText(builder);
            }
        };
        mAdapter = new TagPickerAdapter(getActivity(), pickedTags, mOnTagSelectedListener);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        LayoutInflater inflater = getActivity().getLayoutInflater();
        ViewGroup layout =  (ViewGroup) inflater.inflate(R.layout.dialog_pick_tags, null);

        mPickedTagsView = (TextView) layout.findViewById(R.id.picked_tags);
        mPickedTagsView.setMovementMethod(ClickableMovementMethod.getInstance());
        initPickedTags();

        mTagListView = (ExpandableListView) layout.findViewById(R.id.list);
        mTagListView.setGroupIndicator(null);
        mTagListView.setAdapter(mAdapter);
        mTagListView.setOnGroupClickListener(new OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view,
                                        int groupPosition, long id) {
                if (mAdapter.getChildrenCount(groupPosition) == 0) {
                    Tag tag = (Tag) mAdapter.getGroup(groupPosition);
                    CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                    checkBox.toggle();
                } else {
                    if (mTagListView.isGroupExpanded(groupPosition)) {
                        mTagListView.collapseGroup(groupPosition);
                    } else {
                        mTagListView.expandGroup(groupPosition);
                    }
                }
                return true;
            }
        });
        mTagListView.setOnChildClickListener(new OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view,
                                        int groupPosition, int childPosition, long id) {
                Tag tag = (Tag) mAdapter.getChild(groupPosition, childPosition);
                CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
                checkBox.toggle();
                return true;
            }
        });

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(layout)
                //.setTitle(R.string.dialog_pick_tags_title)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                        if (mOnTagsPickedListener != null) {
                            mOnTagsPickedListener.onTagsPicked(mAdapter.getPickedTags());
                        }
                    }
                }).create();

        return dialog;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof OnTagsPickedListener) {
            mOnTagsPickedListener = (OnTagsPickedListener) getParentFragment();
        }
        else if (activity instanceof OnTagsPickedListener) {
            mOnTagsPickedListener = (OnTagsPickedListener) activity;
        }
    }

    private void initPickedTags() {
        Set<String> pickedTags = mAdapter.getPickedTags();
        SpannableStringBuilder builder = new SpannableStringBuilder();

        for (String tagName : pickedTags) {
            builder.append(formatTag(getActivity(), tagName));
            builder.append(TAG_SEPARATOR);
        }
        mPickedTagsView.setText(builder);
    }

    public SpannableString formatTag(Context context, final String source) {
        SpannableString ss = new SpannableString(source);
        ss.setSpan(new TagSpan(context, source, true) {
            @Override
            public void onClick(View view) {
                mAdapter.removePickedTag(source);
            }
        }, 0, source.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        return ss;
    }
}