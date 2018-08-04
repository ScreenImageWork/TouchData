package com.kedacom.touchdata.whiteboard.helper;

import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.TranslateMsgState;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/10/27.
 *
 * 拖动的过程中不会因为触摸点的增加或者减少而对拖动造成影响
 * 当拖动触摸点数量发生变化时，就以当前改变的位置开始重新计算拖动距离
 *
 */
public class TranslateHelper implements IGestrueHelper {

    private float oldX = Integer.MIN_VALUE;
    private float oldY =  Integer.MIN_VALUE;

    private float centerX  = Integer.MIN_VALUE;
    private float centerY = Integer.MIN_VALUE;

    private float offsetX = 0;

    private float offsetY = 0;

    private IHelperListener mIHelperListener;

    public TranslateHelper(IHelperListener listener){
        mIHelperListener = listener;
    }

    public void setCurTranslate(float x,float y){
        offsetX = x;
        offsetY = y;
    }

    @Override
    public void complete() {
        oldX=Integer.MIN_VALUE;
        oldY = Integer.MIN_VALUE;
        centerX = Integer.MIN_VALUE;
        centerY = Integer.MIN_VALUE;

        if(mIHelperListener!=null) {
            requestPaint(true);
        }
    }

    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        int pointerCount = event.getPointerCount();

        boolean isConfigChanged = action == MotionEvent.ACTION_POINTER_UP || action == MotionEvent.ACTION_POINTER_DOWN;

        int div = action == MotionEvent.ACTION_POINTER_UP?pointerCount-1:pointerCount;

        int skipIndex = div == pointerCount?-1:event.getActionIndex();

        float sumX = 0;
        float sumY = 0;

        for(int i = 0;i<pointerCount;i++){
            if(skipIndex == i) continue;
            sumX += event.getX(i);
            sumY +=event.getY(i);
        }

        float focusX = sumX / div;
        float focusY = sumY / div;


        /*因为旋转时改变的是坐标系，而触摸的坐标系是恒定的左上角是(0,0)点
         因此这里需要对触摸的坐标点进行旋转*/
        if(centerX == Integer.MIN_VALUE ||centerY == Integer.MIN_VALUE){
            centerX = (WhiteBoardUtils.whiteBoardWidth)/2f - offsetX;
            centerY = (WhiteBoardUtils.whiteBoardHeight)/2f - offsetY;
        }

       int curAngle = mIHelperListener.getCurAngle();

        Point point = new Point((int)focusX, (int)focusY);
        Point centerPoint = new Point((int)centerX, (int)centerY);
        Point rPoint = GraphUtils.computeRotatePoint(point, centerPoint, curAngle);

        focusX = rPoint.x;
        focusY = rPoint.y;

        if(isConfigChanged||oldX==Integer.MIN_VALUE||oldY == Integer.MIN_VALUE){
            oldX = focusX;
            oldY = focusY;
            return true;
        }

        float cx =  focusX -  oldX;
        float cy = focusY - oldY;

        oldX = focusX;
        oldY = focusY;

        offsetX += cx;
        offsetY += cy;


        requestPaint(false);

        return true;
    }

    private void requestPaint(boolean isFinish){
        if(mIHelperListener!=null){
            TranslateMsgState translateMsgEntity = new TranslateMsgState(offsetX,offsetY,isFinish,true);
            mIHelperListener.requestPaint(new MsgEntity(translateMsgEntity));
        }
    }

    @Override
    public void onDestory() {

    }
}
