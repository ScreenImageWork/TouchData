package com.kedacom.touchdata.whiteboard.helper;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleMsgState;

/**
 * Created by zhanglei on 2016/10/26.
 */
public class ScaleHelper implements IGestrueHelper,ScaleGestureDetector.OnScaleGestureListener{

    private ScaleGestureDetector mScaleDetector;

    private IHelperListener mHelperListener;

    private float curScale = 1.0f;

    private float curFocusX = 0;
    private float curFocusY = 0;

    public ScaleHelper(Context context, IHelperListener listener){
        mHelperListener = listener;
        mScaleDetector = new ScaleGestureDetector(context, this);
    }


    public boolean onTouchEvent(MotionEvent event) {

        mScaleDetector.onTouchEvent(event);

        return true;
    }

    @Override
    public void onDestory() {
        mScaleDetector = null;
    }

    @Override
    public void complete() {
        requestPaint(true);
    }

    @Override
    public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
        curScale *= scaleGestureDetector.getScaleFactor();
        curScale = curScale>3f?3f:curScale;
        curScale = curScale<0.5f?0.5f:curScale;

        curFocusX = scaleGestureDetector.getFocusX();
        curFocusY = scaleGestureDetector.getFocusY();

        requestPaint(false);
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
    }

    public void setCurScale(float scale){
        curScale = scale;
    }

    private void requestPaint(boolean isComplete){
        ScaleMsgState scaleMsgState = new ScaleMsgState(curScale,curFocusX,curFocusY,isComplete,true);
        if(mHelperListener!=null)
            mHelperListener.requestPaint(new MsgEntity(scaleMsgState));
    }
}
