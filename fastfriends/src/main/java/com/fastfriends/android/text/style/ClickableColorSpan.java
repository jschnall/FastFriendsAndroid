package com.fastfriends.android.text.style;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * Created by jschnall on 8/18/14.
 */
public abstract class ClickableColorSpan extends ClickableSpan {
    private boolean mIsPressed;
    private int mPressedBackgroundColor;
    private int mNormalTextColor;
    private int mPressedTextColor;

    public ClickableColorSpan(int normalTextColor, int pressedTextColor, int pressedBackgroundColor) {
        mNormalTextColor = normalTextColor;
        mPressedTextColor = pressedTextColor;
        mPressedBackgroundColor = pressedBackgroundColor;
    }

    public void setPressed(boolean isSelected) {
        mIsPressed = isSelected;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
        ds.bgColor = mIsPressed ? mPressedBackgroundColor : 0x00000000;
        ds.setUnderlineText(false);
    }
}