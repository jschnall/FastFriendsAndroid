package com.fastfriends.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class FFEditText extends EditText {
	public interface OnImeClosedListener {
		public void onImeClosed(FFEditText editText);
	};
	OnImeClosedListener mOnImeClosedListener = null;
	
	public FFEditText(Context context) {
		super(context);
	}

	public FFEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FFEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onKeyPreIme (int keyCode, KeyEvent event) {
		if (KeyEvent.KEYCODE_BACK == keyCode) {
			if (mOnImeClosedListener != null) {
				mOnImeClosedListener.onImeClosed(this);
			}
		}
		return false;
	}
	
	public OnImeClosedListener getOnImeClosedListener() {
		return mOnImeClosedListener;
	}
	public void setOnImeClosedListener(OnImeClosedListener listener) {
		mOnImeClosedListener = listener;
	}
}
