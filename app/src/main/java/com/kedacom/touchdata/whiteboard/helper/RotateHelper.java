package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Point;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.RotateMsgState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2016/9/27.
 */
public class RotateHelper implements IGestrueHelper {

    private final int ROTATE_ANGLE_SPEED = 1;

    public int curAngle;

    private float centerX = Integer.MIN_VALUE;
    private float centerY = Integer.MIN_VALUE;

    private float oldX;
    private float oldY;

    private IHelperListener mIHelperListener;

    private boolean isConfigChanged;

    private List<Integer> idList1 = new ArrayList<Integer>();
    private List<Integer> idList2 = new ArrayList<Integer>();


    public RotateHelper(IHelperListener listener){
        mIHelperListener = listener;
    }


    public void setCurAngle(int angle){
        curAngle = angle;
    }

    public void setDoubleHandId(List<Integer> idList1,List<Integer> idList2){
        this.idList1 = idList1;
        this.idList2 = idList2;
    }

    public void init(){
        centerX = Integer.MIN_VALUE;
        centerY = Integer.MIN_VALUE;
        oldX = 0;
        oldY = 0;
    }

    @Override
    public void complete() {
        isConfigChanged = false;
        idList1.clear();
        idList2.clear();
        requestPaint(true);
        init();
    }

    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP?true:false;

        if(!isConfigChanged)
            isConfigChanged = action == MotionEvent.ACTION_POINTER_DOWN ||  action == MotionEvent.ACTION_POINTER_UP;

        if(isConfigChanged){
            return false;
        }

        onGestureRotate(event);

        return true;
    }

    @Override
    public void onDestory() {
        idList1.clear();
        idList2.clear();
        idList1 = null;
        idList2 = null;
    }

    public int computeAngle(float x1,float y1,float x2,float y2,MotionEvent event){

        if(centerX == Integer.MIN_VALUE||centerY == Integer.MIN_VALUE){
            centerX = (x1 + x2)/2f;
            centerY = (y1 + y2)/2f;

            oldX = x1;
            oldY = y1;
            return curAngle;
        }

        int angle = computeRotateAngle((int)x1,(int)y1,(int)oldX,(int)oldY,(int)centerX,(int)centerY);

        if(angle<ROTATE_ANGLE_SPEED){
            return curAngle;
        }

        boolean boo = checkDirection((int)x1,(int)y1,(int)oldX,(int)oldY,(int)centerX,(int)centerY);

        oldX = x1;
        oldY = y1;


        if(!boo){
            curAngle = curAngle + angle;
        }else{
            curAngle = curAngle - angle;
        }

        if(curAngle>360||curAngle<-360){
            curAngle = curAngle%360;
        }

        requestPaint(false);

        return curAngle;
    }

    private void onGestureRotate(MotionEvent event){

        int pointerCount = event.getPointerCount();

        if(pointerCount<2){
            return;
        }

        //对当前屏幕上面所有的点进行分类
        List<Point> point1 = new ArrayList<Point>();
        List<Point> point2 = new ArrayList<Point>();

        for(int i = 0; i<pointerCount; i++){
            int id = event.getPointerId(i);
            int x = (int)event.getX(i);
            int y = (int)event.getY(i);
            Point p = new Point(x,y);

            int idListSize1 = idList1.size();
            for(int j=0;j<idListSize1;j++){
                if(id == idList1.get(j)){
                    point1.add(p);
                }
            }

            int idListSize2 = idList2.size();
            for(int k=0;k<idListSize2;k++){
                if(id == idList2.get(k)){
                    point2.add(p);
                }
            }
        }

        int x1 = 0;
        int y1 = 0;

        int x2 = 0;
        int y2 = 0;

        int pointSize = point1.size();

        for(int i = 0;i<pointSize;i++){
            x1 = x1 + point1.get(i).x;
            y1 = y1 + point1.get(i).y;
        }

        int pointSize2 = point2.size();
        for(int i = 0;i<pointSize2;i++){
            x2 = x2 + point2.get(i).x;
            y2 = y2 + point2.get(i).y;
        }

        if(pointSize==0||pointSize2==0){
            return;
        }

        x1 = x1 / pointSize;
        y1 = y1 / pointSize;

        x2 = x2 / pointSize2;
        y2 = y2 / pointSize2;

        computeAngle(x1, y1, x2, y2,event);
    }


    /**
     * 计算旋转角度
     * @param x 旋转后的x坐标
     * @param y 旋转后的y坐标
     * @param oldx 旋转前的x坐标
     * @param oldy 旋转前的y坐标
     * @param centerX 旋转中心点x坐标
     * @param centerY 旋转中心点y坐标
     * @return 旋转后的角度
     */
    private int computeRotateAngle(int x,int y,int oldx,int oldy,int centerX,int centerY){

        double line1 = Math.sqrt((((centerX - (double)oldx) * (centerX - (double)oldx)) + ((centerY - (double)oldy) * (centerY - (double)oldy))));
        double line2 = Math.sqrt((((centerX - (double) x) * (centerX - (double) x)) + ((centerY - (double) y) * (centerY - (double)y))));
        double line3 = Math.sqrt(((((double)oldx-(double)x)*((double)oldx-(double)x)) + (((double)oldy - (double)y)*((double)oldy - (double)y))));

        line1 = Math.abs(line1);
        line2 = Math.abs(line2);
        line3 = Math.abs(line3);

        double cos = (line1*line1 + line2*line2 - line3*line3)/(2*line1*line2);
        double radian = Math.acos(cos);
        double angle = (radian*180d)/Math.PI;

        return (int)angle;
    }

    /**
     * 检测旋转方向
     * @param x 旋转后的x坐标
     * @param y 旋转后的y坐标
     * @return  boolean true 顺时针旋转，false 逆时针旋转
     */
    private boolean checkDirection(int x,int y,int oldx,int oldy,int oldx2,int oldy2){
        int c = (oldx-oldx2)*(y-oldy)-(oldy - oldy2)*(y - oldy) - (oldy - oldy2)*(x - oldx);
        if(c>0){
            return false;
        }else{
            return true;
        }
    }

    private void requestPaint(boolean isFinish){
        RotateMsgState rotateMsgState = new RotateMsgState(curAngle,isFinish,true);
        if(mIHelperListener!=null)
            mIHelperListener.requestPaint(new MsgEntity(rotateMsgState));
    }
}