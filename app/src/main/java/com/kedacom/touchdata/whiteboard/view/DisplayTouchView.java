package com.kedacom.touchdata.whiteboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kedacom.touchdata.R;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2017/11/17.
 *
 * 触摸点显示控件
 * 触摸点>=2时才会显示触摸
 */
public class DisplayTouchView extends View{

    public static  int DISPLAY_TOUCH_ICON_WIDTH = 50;

    public static  int DISPLAY_TOUCH_ICON_HEIGHT = 50;

    private ArrayList<Point> pointList = new ArrayList<Point>();

    private Bitmap displayTouchIcon;


    public DisplayTouchView(Context context) {
        super(context);
        init();
    }

    public DisplayTouchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DisplayTouchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        displayTouchIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.touch_icon);

        DISPLAY_TOUCH_ICON_WIDTH = displayTouchIcon.getWidth();
        DISPLAY_TOUCH_ICON_HEIGHT = displayTouchIcon.getHeight();

        TPLog.printKeyStatus("DISPLAY_TOUCH_ICON_WIDTH = "+DISPLAY_TOUCH_ICON_WIDTH);
        TPLog.printKeyStatus("DISPLAY_TOUCH_ICON_HEIGHT = "+DISPLAY_TOUCH_ICON_HEIGHT);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        synchronized (pointList){

            if(pointList.isEmpty()){
                return;
            }

            int count = pointList.size();

            for(int i = 0;i<count;i++){

                int x  = pointList.get(i).x - (int) (DISPLAY_TOUCH_ICON_WIDTH/2f*0.5f);
                int y  = pointList.get(i).y - (int) (DISPLAY_TOUCH_ICON_HEIGHT/2f*0.5f);
                canvas.save();
                canvas.scale(0.5f,0.5f,x,y);
                canvas.drawBitmap(displayTouchIcon,x,y,null);
                canvas.restore();
            }

            pointList.clear();
        }
    }


    private long lastTouchTime = 0;
    public void touch(MotionEvent event){
        long curTouchTime = System.currentTimeMillis();
        TPLog.printKeyStatus("touch time = "+(curTouchTime - lastTouchTime));
        lastTouchTime = curTouchTime;

        int action = event.getActionMasked();

        synchronized (pointList) {

            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                pointList.clear();
                invalidate();
                return;
            }

            int count = event.getPointerCount();

            if (count == 1) {
                pointList.clear();
                invalidate();
                return;
            }

            for (int i = 0; i < count; i++) {
                int x = (int)event.getX(i);
                int y = (int)event.getY(i);
                Point point = new Point(x,y);
                pointList.add(point);
            }
            invalidate();
        }
    }
}
