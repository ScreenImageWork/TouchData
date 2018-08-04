package com.kedacom.touchdata.whiteboard.helper;

import android.view.MotionEvent;

public interface IHelper {

	boolean onTouchEvent(MotionEvent event);
	
	void onDestory();
	
}
