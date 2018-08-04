package com.kedacom.touchdata.whiteboard.helper;

import android.content.Context;
import android.view.MotionEvent;

import com.kedacom.storagelibrary.utils.Util;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;

/**
 * Created by zhanglei on 2016/9/27.
 */
public class NewGestureHelper implements IHelper {
    //手势类型
    public final int GESTURE_PAINT = 9;  //绘制

    public final int GESTURE_MOVE = 10;  //画布拖动

    public final int GESTURE_ERASER = 11;  //擦除

    public final int GESTURE_SCALE = 12;  //缩放

    public final int GESTURE_ROTATE = 13;  //旋转

    public final int GESTURE_UNKUOW = -1;


    private int curGesture = GESTURE_UNKUOW;

    private IHelperListener mIHelperListener;

    private ScaleAndTranslateHelper mScaleAndTranslateHelper;

    private GestrueEraseHelper mGestureEraseHelper;


    public NewGestureHelper(Context context, IHelperListener listener){
        mIHelperListener = listener;
        init(context);
    }

    private void init(Context context){
        mScaleAndTranslateHelper = new ScaleAndTranslateHelper(context,mIHelperListener);
        mGestureEraseHelper = new GestrueEraseHelper(mIHelperListener);
    }

    public void lock(){
         mScaleAndTranslateHelper.lock();
    }

    public void unlock(){
        mScaleAndTranslateHelper.unLock();
    }



    //特殊手势 拖动 1个触摸点也可以
    public void drag(MotionEvent event){
        if(event.getActionMasked() == MotionEvent.ACTION_UP||event.getActionMasked() == MotionEvent.ACTION_CANCEL){
            mScaleAndTranslateHelper.translate(event,true);
        }else{
            mScaleAndTranslateHelper.translate(event,false);
        }
    }

    public boolean checkGestureErase(MotionEvent event){
        boolean boo = mGestureEraseHelper.checkPerform(event);
        if(boo)
            curGesture = GESTURE_ERASER;
        return boo;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

       // TPLog.printKeyStatus("NewGestureHelper="+Utils.touchActionToString(action));

        if(curGesture == GESTURE_ERASER){
            mGestureEraseHelper.onTouchEvent(event);
        }else{
            mScaleAndTranslateHelper.onTouchEvent(event);
        }

        if((action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL)){

            if(curGesture != GESTURE_ERASER){
                mScaleAndTranslateHelper.onTouchEvent(event);
                mScaleAndTranslateHelper.complete();
            }

            curGesture = GESTURE_UNKUOW;
            return true;
        }

        return true;
    }


    public void setCurAngle(int angle){
       // mRotateHelper.setCurAngle(angle);
    }

    public void setCurScale(float scale){
        mScaleAndTranslateHelper.setCurScale(scale);
    }

    public void setCurTranslate(float ox,float oy){
        mScaleAndTranslateHelper.setCurTranslate(ox,oy);
    }


    @Override
    public void onDestory() {
        mGestureEraseHelper.onDestory();
       // mRotateHelper.onDestory();
        mScaleAndTranslateHelper.onDestory();

        mGestureEraseHelper = null;
      //  mRotateHelper = null;
        mScaleAndTranslateHelper = null;
    }


    private String getGestrueText(int gestrue){
        String text = "";
        switch(gestrue){
            case GESTURE_PAINT:
                text = "GESTURE_PAINT";
                break;
            case GESTURE_ERASER:
                text = "GESTURE_ERASER";
                break;
            case GESTURE_MOVE:
                text = "GESTURE_MOVE";
                break;
            case GESTURE_ROTATE:
                text = "GESTURE_ROTATE";
                break;
            case GESTURE_SCALE:
                text = "GESTURE_SCALE";
                break;
            case GESTURE_UNKUOW:
                text = "GESTURE_UNKUOW";
                break;
        }

        return text;
    }

}
