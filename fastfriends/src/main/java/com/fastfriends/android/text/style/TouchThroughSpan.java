package com.fastfriends.android.text.style;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextPaint;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.fastfriends.android.R;

/**
 * Created by jschnall on 2/24/14.
 */
public class TouchThroughSpan extends TouchableSpan {
    Context mContext;
    View mView;

    public TouchThroughSpan(Context context, View view) {
        mContext = context;
        mView = view;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        Resources res = mContext.getResources();
        ds.setShadowLayer(0, 0, 0, res.getColor(R.color.transparent));
    }

    @Override
    public boolean onTouch(View widget, MotionEvent event) {
        int action = event.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                mView.setPressed(true);
                break;
            case MotionEvent.ACTION_UP:
                mView.performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mView.setPressed(false);
                break;
        }
        return false;
    }
}
