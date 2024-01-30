package com.fastfriends.android.text.style;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.StateSet;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.fastfriends.android.R;

/**
 * Created by jschnall on 2/24/14.
 */
public class TagSpan extends ClickableImageSpan {

    public TagSpan(Context context, String text, boolean showButton) {
        super(createStateListDrawables(context, text, showButton),
                ImageSpan.ALIGN_BASELINE);
    }

    public static TextView createOffScreenTextView(Context context, String text, boolean showButton, int resId, int textSize){
        // Creating offscreen textview dynamically
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(textSize);
        textView.setBackgroundResource(resId);
        if (showButton) {
            textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.remove_tag, 0);
            textView.setCompoundDrawablePadding((int) context.getResources().getDimension(R.dimen.tag_padding));
        }
        return textView;
    }

    public static BitmapDrawable convertViewToDrawable(Context context, View view) {

        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap b = Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        c.translate(-view.getScrollX(), -view.getScrollY());
        view.draw(c);
        view.setDrawingCacheEnabled(true);
        Bitmap cacheBmp = view.getDrawingCache();
        Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
        view.destroyDrawingCache();

        BitmapDrawable drawable = new BitmapDrawable(context.getResources(), viewBmp);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        return drawable;
    }

    public static StateListDrawable createStateListDrawables(Context context, String text, boolean showButton) {
        Drawable normal = convertViewToDrawable(context, createOffScreenTextView(context, text, showButton, R.drawable.tag_bg, 16));
        Drawable pressed = convertViewToDrawable(context, createOffScreenTextView(context, text, showButton, R.drawable.tag_bg_pressed, 16));

        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        stateListDrawable.addState(new int[]{android.R.attr.state_focused}, pressed);
        stateListDrawable.addState(new int[] {}, normal);
        stateListDrawable.setBounds(0, 0, stateListDrawable.getIntrinsicWidth(), stateListDrawable.getIntrinsicHeight());

        return stateListDrawable;
    }

    @Override
    public void onClick(View view) {

    }
}
