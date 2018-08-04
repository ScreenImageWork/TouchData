package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.VersionUtils;
import com.touch.touchsdk.TouchEvent;

public class DrawHelper implements IDrawHelper{
	
	private int curDrawType = WhiteBoardUtils.GRAPH_PEN;
	
	//由于Path绘制的方式与其他的不同，因此将其分离出去，以降低该类的复杂度
	private DrawPathGraphHelper mDrawPathGraphHelper;
	private DrawEraseHelper mDrawEraseHelper;
	private DrawAreaEraseHelper mDrawAreaEraseHelper;
	private DrawPathGraphToFBHelper mDrawPathGraphToFBHelper;
    private DrawBrushPenHelper mDrawBrushPenHelper;
	private IHelperListener mHelperListener;

	private boolean lockDraw = false;

	private boolean setDownTouch = false;
	
	public DrawHelper(IHelperListener listener){
		mHelperListener = listener;
		mDrawPathGraphHelper = new DrawPathGraphHelper(mHelperListener);
		mDrawEraseHelper = new DrawEraseHelper(mHelperListener);
		mDrawAreaEraseHelper = new DrawAreaEraseHelper(mHelperListener);
		mDrawPathGraphToFBHelper = new DrawPathGraphToFBHelper(mHelperListener);
		mDrawBrushPenHelper = new DrawBrushPenHelper(mHelperListener);
	}

	@Override
	public void setDrawType(int drawType) {
		curDrawType = drawType;
	}

	public void setLockDraw(boolean isLock){
		if(curDrawType == WhiteBoardUtils.GRAPH_PEN){
			if(VersionUtils.isImix()&&isLock){
				mDrawPathGraphToFBHelper.lock();
			}else if(VersionUtils.isImix()){
				mDrawPathGraphToFBHelper.unLock();
			}
			return;
		}
		lockDraw = isLock;
		if(lockDraw){
			setDownTouch = true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(curDrawType == WhiteBoardUtils.GRAPH_PEN){
			if(VersionUtils.isImix()&&event.getToolType(0) != MotionEvent.TOOL_TYPE_MOUSE){
//			if(VersionUtils.isImix()){
				mDrawPathGraphToFBHelper.onTouchEvent(event);
			}else{
				mDrawPathGraphHelper.onTouchEvent(event);
			}
		}else if(curDrawType == WhiteBoardUtils.GRAPH_ERASE){
			if(!lockDraw) { //防止手势操作之前有误擦除现象
				if(setDownTouch&&event.getActionMasked() != MotionEvent.ACTION_UP){
					event.setAction(MotionEvent.ACTION_DOWN);
					setDownTouch = false;
				}
				mDrawEraseHelper.onTouchEvent(event);
			}else if(event.getActionMasked() == MotionEvent.ACTION_UP||event.getActionMasked() == MotionEvent.ACTION_DOWN){
				mDrawEraseHelper.onTouchEvent(event);
			}
		}else if(curDrawType == WhiteBoardUtils.GRAPH_ERASE_AREA){
			if(!lockDraw) {
				if(setDownTouch){
					event.setAction(MotionEvent.ACTION_DOWN);
					setDownTouch = false;
				}
				mDrawAreaEraseHelper.onTouchEvent(event);
			}
		}
		return true;
	}

	public boolean onTouchEvent(TouchEvent event) {
		return mDrawBrushPenHelper.onTouchEvent(event);
	}


//	public void checkType(){
//		if(curDrawType == WhiteBoardUtils.GRAPH_ERASE){
//			mDrawEraseHelper.displayErasePanel();
//		}
//	}

	@Override
	public void onDestory() {
		mDrawPathGraphHelper.onDestory();
		mDrawEraseHelper.onDestory();

		mDrawPathGraphHelper = null;
		mDrawEraseHelper = null;
	}


}
