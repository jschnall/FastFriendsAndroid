package com.fastfriends.android.text.style;

import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;
import android.view.View;

/**
 * Created by jschnall on 6/6/14.
 */
public abstract class ClickableImageSpan extends ImageSpan {
    public ClickableImageSpan(Drawable drawable) {
        super(drawable);
    }

    public ClickableImageSpan(Drawable drawable, int verticalAlignment) {
        super(drawable, verticalAlignment);
    }

    public abstract void onClick(View view);
}