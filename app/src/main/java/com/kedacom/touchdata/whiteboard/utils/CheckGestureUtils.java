package com.kedacom.touchdata.whiteboard.utils;

import android.graphics.Point;
import android.view.MotionEvent;

/**
 * Created by zhanglei on 2017/11/21.
 */
public class CheckGestureUtils {

    private final static int MIN_LENGTH = 10;

    private static Point prePoint;

    public static void preCheckGesture(MotionEvent event){
        prePoint = getCenterPoint(event);
    }

    public static boolean checkGesture(MotionEvent event){

        if(event.getPointerCount()<2){
            return false;
        }

        Point point = getCenterPoint(event);

        try {
            double length = GraphUtils.computeSegmentLength(prePoint.x,prePoint.y,point.x,point.y);

            if(MIN_LENGTH>=length){
                return true;
            }else{
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

    }

    private static Point getCenterPoint(MotionEvent event){
        float x = 0;
        float y = 0;

        int pointerCount = event.getPointerCount();

        for(int i = 0;i<pointerCount;i++){
            x = x + event.getX(i);
            y = y + event.getY(i);
        }


        x = x/pointerCount;
        y = y/pointerCount;

        Point point = new Point((int)x,(int)y);

        return point;
    }
}
