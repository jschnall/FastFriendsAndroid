package com.fastfriends.android.helper;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.View;

import com.fastfriends.android.R;
import com.fastfriends.android.activity.SearchActivity;
import com.fastfriends.android.activity.ProfileActivity;
import com.fastfriends.android.model.Mention;
import com.fastfriends.android.text.style.ClickableColorSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jschnall on 7/3/14.
 */
public class TagHelper {
    private final static String LOGTAG = TagHelper.class.getSimpleName();

    private static final String HASH_TAG_REGEX = "(?i)#[a-z]([a-z0-9_]{0,252}[a-z0-9])?";
    private static final String MENTION_REGEX = "(?i)@[a-z]([a-z0-9_-]{0,62}[a-z0-9])?";

    // SearchTypes
    public final static int SEARCH_EVENTS = 0;
    public final static int SEARCH_FRIENDS = 1;
    public final static int SEARCH_PLANS = 2;

    private static TagHelper mInstance;

    private TagHelper() {
    }

    public static TagHelper getInstance() {
        if (mInstance == null) {
            mInstance = new TagHelper();
        }
        return mInstance;
    }

    /**
     *
     * @param context
     * @param text
     * @param mentions
     * @param searchType What to search when clicked
     * @return
     */
    public CharSequence markup(Context context, CharSequence text, List<Mention> mentions, int searchType) {
        CharSequence newText = markupTags(context, text, searchType);
        return markupMentions(context, newText, mentions);
    }

    /**
     *
     * @param context
     * @param text text to markup
     * @param searchType What to search when clicked
     * @return marked up version of text passed in
     */
    private CharSequence markupTags(final Context context, CharSequence text, final int searchType) {
        Spannable spannable = new SpannableString(text);
        int color = context.getResources().getColor(R.color.dark_orange);
        int pressedColor = context.getResources().getColor(R.color.dark_orange);
        int pressedBackgroundColor = context.getResources().getColor(R.color.light_grey);

        Matcher matcher = Pattern.compile(HASH_TAG_REGEX).matcher(text);

        while (matcher.find()) {
            final String name = matcher.group();
            int len = name.length();
            int index = matcher.start();
            spannable.setSpan(new ClickableColorSpan(color, pressedColor, pressedBackgroundColor) {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, SearchActivity.class);
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_TEXT, name);

                    switch(searchType) {
                        case SEARCH_EVENTS: {
                            intent.putExtra(SearchActivity.EXTRA_CATEGORY, SearchActivity.CATEGORY_EVENTS);
                            break;
                        }
                        case SEARCH_FRIENDS: {
                            intent.putExtra(SearchActivity.EXTRA_CATEGORY, SearchActivity.CATEGORY_PROFILES);
                            break;
                        }
                        case SEARCH_PLANS: {
                            intent.putExtra(SearchActivity.EXTRA_CATEGORY, SearchActivity.CATEGORY_PLANS);
                            break;
                        }
                    }

                    context.startActivity(intent);
                }
            }, index, index + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return spannable;
    }

    private CharSequence markupMentions(final Context context, CharSequence text, List<Mention> mentions) {
        if (mentions == null || mentions.isEmpty()) {
            return text;
        }

        // Replace any mentionedNames that have been changed
        // Simultaneously build map
        Map<String, Mention> mentionMap = new HashMap<String, Mention>();
        List<String> sources = new ArrayList<String>();
        List<CharSequence> replacements = new ArrayList<CharSequence>();
        for (Mention mention : mentions) {
            String mentionedName = "@" + mention.getName();
            String userName = "@" + mention.getUserName();

            mentionMap.put(mentionedName.toLowerCase(), mention);

            if (!mentionedName.equals(userName)) {
                sources.add(mentionedName);
                replacements.add(userName);
            }
        }
        CharSequence newText = TextUtils.replace(text, sources.toArray(new String[sources.size()]),
                replacements.toArray(new CharSequence[replacements.size()]));

        Spannable spannable = new SpannableString(newText);
        int color = context.getResources().getColor(R.color.dark_orange);
        int pressedColor = context.getResources().getColor(R.color.dark_orange);
        int pressedBackgroundColor = context.getResources().getColor(R.color.light_grey);

        Matcher matcher = Pattern.compile(MENTION_REGEX).matcher(newText);

        while (matcher.find()) {
            final String name = matcher.group();
            final Mention mention = mentionMap.get(name.toLowerCase());

            if (mention != null) {
                int len = name.length();
                int index = matcher.start();
                spannable.setSpan(new ClickableColorSpan(color, pressedColor, pressedBackgroundColor) {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        intent.putExtra(ProfileActivity.EXTRA_USER_ID, mention.getUserId());
                        context.startActivity(intent);
                    }
                }, index, index + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return spannable;
    }
}
