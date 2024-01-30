package com.fastfriends.android.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

/**
 * Created by jschnall on 4/24/14.
 */
public class DelayAutoCompleteTextView extends ClearableAutoCompleteTextView{
    public static final int DEFAULT_DELAY = 900; //ms

    private int mDelay = DEFAULT_DELAY;

    public DelayAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DelayAutoCompleteTextView.super.performFiltering((CharSequence) msg.obj, msg.arg1);
        }
    };

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        mHandler.removeMessages(0);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(0, keyCode, 0, text), mDelay);
    }

    public int getDelay() {
        return mDelay;
    }

    public void setDelay(int delay) {
        mDelay = delay;
    }


}