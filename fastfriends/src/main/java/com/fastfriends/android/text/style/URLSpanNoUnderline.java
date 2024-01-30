package com.fastfriends.android.text.style;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.TextView;

import com.fastfriends.android.R;

/**
 * Created by jschnall on 1/30/14.
 */
public class URLSpanNoUnderline extends URLSpan {
    private Integer mColor = null;

    public URLSpanNoUnderline(String url) {
        super(url);
    }

    public URLSpanNoUnderline(String url, int color) {
        super(url);
        mColor = color;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(false);
        if (mColor != null) {
            ds.setColor(mColor);
        }
    }

    @Override
    public void onClick(View widget) {
        //startWebView(getURL());
    }

    public static void removeUnderlines(Context context, TextView textView) {
        Spannable s = new SpannableString(textView.getText());
        URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
        for (URLSpan span: spans) {
            int start = s.getSpanStart(span);
            int end = s.getSpanEnd(span);
            s.removeSpan(span);
            span = new URLSpanNoUnderline(span.getURL(), context.getResources().getColor(R.color.dark_orange));
            s.setSpan(span, start, end, 0);
        }
        textView.setText(s);
    }
}