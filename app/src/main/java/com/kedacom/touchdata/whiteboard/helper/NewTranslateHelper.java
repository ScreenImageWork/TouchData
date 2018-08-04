package com.kedacom.touchdata.whiteboard.helper;
import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.helper.IGestrueHelper;
import com.kedacom.touchdata.whiteboard.helper.IHelperListener;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.TranslateMsgState;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class NewTranslateHelper extends GestureDetector.SimpleOnGestureListener implements IGestrueHelper {


    private float offsetX = 0;

    private float offsetY = 0;

    private float lastDistanceX;

    private float lastDistanceY;

    private IHelperListener mIHelperListener;

    //对于拖动和手势，这里采用Android原生的手势类
    private GestureDetector mGestureDetector;

    public NewTranslateHelper(Context context,IHelperListener listener){
        mIHelperListener = listener;
        mGestureDetector = new GestureDetector(context,this);
    }

    public void setCurTranslate(float x,float y){
        offsetX = x;
        offsetY = y;
    }

    @Override
    public void complete() {
            offsetX = lastDistanceX  + offsetX;
            offsetY = lastDistanceY + offsetY;
            requestPaint(offsetX,offsetY,true);
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        lastDistanceX = distanceX;
        lastDistanceY = distanceY;

        distanceX = offsetX + lastDistanceX;
        distanceY = offsetY + lastDistanceY;

        requestPaint(distanceX,distanceY,false);
        return true;
    }



    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        return false;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }


    private void requestPaint(float offsetX,float offsetY,boolean isFinish){
        if(mIHelperListener!=null){
            TranslateMsgState translateMsgEntity = new TranslateMsgState(offsetX,offsetY,isFinish,true);
            mIHelperListener.requestPaint(new MsgEntity(translateMsgEntity));
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onDestory() {

    }
}
