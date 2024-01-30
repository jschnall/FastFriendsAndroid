package com.fastfriends.android.text.style;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by jschnall on 6/6/14.
 */
public class ClickableMovementMethod extends LinkMovementMethod {
    private ClickableImageSpan mClickableImageSpan = null;
    private static ClickableMovementMethod sInstance;
    private int x;
    private int y;

    public static ClickableMovementMethod getInstance() {
        if (sInstance == null) {
            sInstance = new ClickableMovementMethod();
        }
        return sInstance;
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer,
                                MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP ||
                action == MotionEvent.ACTION_DOWN) {
            x = (int) event.getX();
            y = (int) event.getY();

            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();

            x += widget.getScrollX();
            y += widget.getScrollY();

            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
            ClickableImageSpan[] imageSpans = buffer.getSpans(off, off, ClickableImageSpan.class);

            if (link.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    link[0].onClick(widget);
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(link[0]),
                            buffer.getSpanEnd(link[0]));
                }

                return true;
            } else if (imageSpans.length != 0) {
                if (action == MotionEvent.ACTION_UP) {
                    imageSpans[0].onClick(widget);
                    imageSpans[0].getDrawable().setState(new int[]{});
                    mClickableImageSpan = null;
                } else if (action == MotionEvent.ACTION_DOWN) {
                    Selection.setSelection(buffer,
                            buffer.getSpanStart(imageSpans[0]),
                            buffer.getSpanEnd(imageSpans[0]));
                    imageSpans[0].getDrawable().setState(new int[]{android.R.attr.state_pressed});
                    mClickableImageSpan = imageSpans[0];
                }
                return true;
            } else {
                if (mClickableImageSpan != null) {
                    mClickableImageSpan.getDrawable().setState(new int[]{});
                    mClickableImageSpan = null;
                }
                Selection.removeSelection(buffer);
            }
        } else {
            float xDist = Math.abs(event.getX() - x);
            float yDist = Math.abs(event.getY() - y);
            int distance = (int) Math.sqrt(Math.pow(xDist, 2) + Math.pow(yDist, 2));
            if (action != MotionEvent.ACTION_MOVE || distance > 50) {
                if (mClickableImageSpan != null) {
                    mClickableImageSpan.getDrawable().setState(new int[]{});
                    mClickableImageSpan = null;
                }
                Selection.removeSelection(buffer);
            }
        }

        return super.onTouchEvent(widget, buffer, event);
    }
}