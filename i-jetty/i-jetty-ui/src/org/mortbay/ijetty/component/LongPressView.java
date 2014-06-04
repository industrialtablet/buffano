package org.mortbay.ijetty.component;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;


public class LongPressView extends ImageButton {
	private int mLastMotionX, mLastMotionY;
	// 是否移动了
	private boolean isMoved;
	// 长按的runnable
	private Runnable mLongPressRunnable;
	// 移动的阈值
	private static final int TOUCH_SLOP = 20;

	// 长按时间
	private static final long TIME = 5 * 1000;

	public LongPressView(Context context) {
		super(context);
		getRunnable();
	}

	public LongPressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		getRunnable();
	}

	public LongPressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		getRunnable();
	}
	
	private void getRunnable() {
		mLongPressRunnable = new Runnable() {

			public void run() {
				performLongClick();
			}
		};
		
//		setOnLongClickListener(new View.OnLongClickListener() {
//			
//			public boolean onLongClick(View v) {
//				// TODO Auto-generated method stub
//				getContext().startActivity(new Intent(getContext(), ChangeServer.class));
//				return false;
//			}
//		});
	}

	public boolean dispatchTouchEvent(MotionEvent event) {
		int x = (int) event.getX();
		int y = (int) event.getY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mLastMotionX = x;
			mLastMotionY = y;
			isMoved = false;
			postDelayed(mLongPressRunnable, TIME);
			break;
		case MotionEvent.ACTION_MOVE:
			if (isMoved)
				break;
			if (Math.abs(mLastMotionX - x) > TOUCH_SLOP
					|| Math.abs(mLastMotionY - y) > TOUCH_SLOP) {
				// 移动超过阈值，则表示移动了
				isMoved = true;
				removeCallbacks(mLongPressRunnable);
			}
			break;
		case MotionEvent.ACTION_UP:
			// 释放了
			removeCallbacks(mLongPressRunnable);
			break;
		}
		return true;
	}
}
