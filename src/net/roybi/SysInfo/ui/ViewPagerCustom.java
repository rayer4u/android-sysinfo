package net.roybi.SysInfo.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerCustom extends ViewPager {
	private float xDistance, yDistance, lastX, lastY;
	
	public ViewPagerCustom(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	
	
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		super.onInterceptTouchEvent(ev);
//		return false;
//	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = yDistance = 0f;
			lastX = ev.getX();
			lastY = ev.getY();
			getParent().requestDisallowInterceptTouchEvent(true);
			break;
		case MotionEvent.ACTION_MOVE:
			final float curX = ev.getX();
			final float curY = ev.getY();
			xDistance += Math.abs(curX - lastX);
			yDistance += Math.abs(curY - lastY);
			lastX = curX;
			lastY = curY;
			if (xDistance < yDistance)
				getParent().requestDisallowInterceptTouchEvent(false);
		}

		return super.onInterceptTouchEvent(ev);
	}

}
