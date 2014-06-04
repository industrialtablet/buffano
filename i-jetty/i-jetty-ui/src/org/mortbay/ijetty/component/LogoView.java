package org.mortbay.ijetty.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

public class LogoView extends Button {
	private Polygon mPolygon = null;
	public int mW, mH, mX, mY;

	public LogoView(Context context, int pW, int pH, int pX, int pY) {
		super(context);
		init();
		mW = pW;
		mH = pH;
		mX = pX;
		mY = pY;
	}

	public LogoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LogoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		mPolygon = new Polygon();
		this.setFocusable(true);
//		setLayerType(View.LAYER_TYPE_SOFTWARE, null);  
	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			View vParent = ((ScrollShell) getParent());
//			if(getBackground() == null || !this.isEnabled()) {
//				vParent.setTag(null);
//			}else {
//				vParent.setTag(this);
//			}
//		}
//		return false;
//	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Point[] ps = mPolygon.getPoints();
		Path path = new Path();

		path.moveTo(ps[0].x, ps[0].y);
		for (int i = 1; i < ps.length; i++) {
			path.lineTo(ps[i].x, ps[i].y);
		}
		path.lineTo(ps[0].x, ps[0].y);
		path.close();
		try{
		canvas.clipPath(path);
		}catch(Exception e){
//			e.printStackTrace();
		}
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setAlpha(20);
		paint.setStrokeWidth(4.0f);
		canvas.drawLine(ps[0].x, ps[0].y, ps[1].x, ps[1].y, paint);
		canvas.drawLine(ps[1].x, ps[1].y, ps[2].x, ps[2].y, paint);
		canvas.drawLine(ps[2].x, ps[2].y, ps[3].x, ps[3].y, paint);
		canvas.drawLine(ps[3].x, ps[3].y, ps[4].x, ps[4].y, paint);
		canvas.drawLine(ps[4].x, ps[4].y, ps[5].x, ps[5].y, paint);
		canvas.drawLine(ps[5].x, ps[5].y, ps[0].x, ps[0].y, paint);

	}

	
	private void slideview(final View view, final float p1, final float p2) {
		TranslateAnimation animation = new TranslateAnimation(p1, p2, 0, 0);
		// 添加了这行代码的作用时，view移动的时候 会有弹性效果
		animation.setInterpolator(new OvershootInterpolator());
		animation.setDuration(100);
		animation.setStartOffset(100);
		animation.setAnimationListener(new Animation.AnimationListener() {

			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub

			}

			public void onAnimationEnd(Animation animation) {
				int left = view.getLeft() + (int) (p2 - p1);
				int top = view.getTop();
				int width = view.getWidth();
				int height = view.getHeight();
				view.clearAnimation();
				view.layout(left, top, left + width, top + height);

			}
		});
		view.startAnimation(animation);
	}

	
}
