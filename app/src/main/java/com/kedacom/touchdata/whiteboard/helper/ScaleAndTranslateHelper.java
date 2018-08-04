package com.kedacom.touchdata.whiteboard.helper;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.Scroller;

import com.kedacom.app.TouchDataApp;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleAndTranslateMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.TranslateMsgState;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2017/5/24.
 */
public class ScaleAndTranslateHelper implements IGestrueHelper{

    private ScaleGestureDetector mScaleDetector;

    private GestureDetector mGestureDetector ;

    private IHelperListener mHelperListener;

    private float curScale = 1.0f;    //未使用

    private float curScaleFactor = 1f;

    private float curFocusX = 0;
    private float curFocusY = 0;

    private float offsetX = 0;
    private float offsetY = 0;

    private float oldX = Integer.MIN_VALUE;
    private float oldY =  Integer.MIN_VALUE;

    private float centerX  = Integer.MIN_VALUE;
    private float centerY = Integer.MIN_VALUE;

    private boolean lock = false;

    private boolean scaleExtremity = false;

    private Context mContext;

    private  FlingRunnable flingRunnable;

    private AutoScaleRunnable mAutoScaleRunnable;

    public ScaleAndTranslateHelper(Context context, IHelperListener listener){
        mContext = context;
        mHelperListener = listener;
        mScaleDetector = new ScaleGestureDetector(context, mScaleGestureListener);
        mGestureDetector = new GestureDetector(context,mSimpleOnGestureListener);
    }

    public void setCurTranslate(float x,float y){
        offsetX = x;
        offsetY = y;
    }

    public void setCurScale(float scale){
        curScale = scale;
    }

    @Override
    public void complete() {
        oldX=Integer.MIN_VALUE;
        oldY = Integer.MIN_VALUE;
        centerX = Integer.MIN_VALUE;
        centerY = Integer.MIN_VALUE;
        requestPaint(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action  = event.getActionMasked();
        if(action == MotionEvent.ACTION_DOWN){
            if(flingRunnable!=null){
                flingRunnable.cancelFling();
            }

        }
        //忘记了，这里为什么UP要return...,注释掉先
//        if(action == MotionEvent.ACTION_UP){
//            return true;
//        }
        //onTranslate(event);
        //mGestureDetector.onTouchEvent(event);
        mGestureDetector.onTouchEvent(event);
        mScaleDetector.onTouchEvent(event);
        requestPaint(false);

        if(action == MotionEvent.ACTION_UP){
            checkNeedAutoScale();
        }

        curScaleFactor = 1.0f;
        offsetY = 0;
        offsetX = 0;
        scaleExtremity = false;

        return true;
    }

    @Override
    public void onDestory() {
        mScaleDetector = null;
        mScaleGestureListener = null;
    }

    private void requestPaint(boolean isComplete){
           if(lock){
               return;
            }
            if(mHelperListener!=null) {
            ScaleMsgState scaleMsgState = new ScaleMsgState(curScaleFactor,curFocusX,curFocusY,isComplete,true);
            TranslateMsgState translateMsgEntity = new TranslateMsgState(offsetX,offsetY,isComplete,true);
            ScaleAndTranslateMsgState sah = new ScaleAndTranslateMsgState(scaleMsgState,translateMsgEntity);
                sah.setScalExtremity(scaleExtremity);
            mHelperListener.requestPaint(new MsgEntity(sah));
        }
    }

    private void requestPaint(float offsetX,float offsetY,boolean isComplete){
        if(lock){
            return;
        }
        if(mHelperListener!=null) {
            ScaleMsgState scaleMsgState = new ScaleMsgState(curScaleFactor,curFocusX,curFocusY,isComplete,true);
            TranslateMsgState translateMsgEntity = new TranslateMsgState(offsetX,offsetY,isComplete,true);
            ScaleAndTranslateMsgState sah = new ScaleAndTranslateMsgState(scaleMsgState,translateMsgEntity);
            sah.setScalExtremity(scaleExtremity);
            mHelperListener.requestPaint(new MsgEntity(sah));
        }
    }


    private void requestPaint(float curScaleFactor,float curFocusX,float curFocusY,boolean isComplete){
        if(lock){
            return;
        }
        if(mHelperListener!=null) {
            ScaleMsgState scaleMsgState = new ScaleMsgState(curScaleFactor,curFocusX,curFocusY,isComplete,true);
            TranslateMsgState translateMsgEntity = new TranslateMsgState(offsetX,offsetY,isComplete,true);
            ScaleAndTranslateMsgState sah = new ScaleAndTranslateMsgState(scaleMsgState,translateMsgEntity);
            sah.setScalExtremity(scaleExtremity);
            mHelperListener.requestPaint(new MsgEntity(sah));
        }
    }



    ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener(){

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {

            if(lock){
                return true;
            }

            curScaleFactor = scaleGestureDetector.getScaleFactor();

//            if(curScaleFactor>1.0f){
//                if(curScale==3.0f){
//                    curScaleFactor = 1.0f;
//                    return true;
//                }
//            }else if(curScaleFactor<1.0f){
//                if(curScale==0.5f){
//                    curScaleFactor = 1.0f;
//                    return true;
//                }
//            }

//            curScale = curScale*curScaleFactor;
//
//                if (curScale > 3.0f) {
//                    curScale = 3.0f;
//                    curScaleFactor = curScale / mHelperListener.getCurScale();
//                    scaleExtremity = true;
//                }if (curScale < 0.5f) {
//                    curScale = 0.5f;
//                    curScaleFactor =curScale / mHelperListener.getCurScale();
//                    scaleExtremity = true;
//                }else{
//                     scaleExtremity = false;
//                }

//            TPLog.printKeyStatus("curScale->"+curScale);

            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();

            curFocusX = focusX;
            curFocusY = focusY;

            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            curScale = mHelperListener.getCurScale();
            TPLog.printKeyStatus("onScaleBegin--->curScale->"+curScale);
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }
    };

    private void checkNeedAutoScale(){
        float targetScale = 0.5f;
        if (getCurScale() < 0.5f){
            //自动放大至初始化大小
            targetScale = 0.5f;
        }else if (getCurScale() > 3.0f){//如果当前图片大小大于最大值
//                //自动缩小至最大值
            targetScale = 3.0f;
        }else{
            return ;
        }
        if(mAutoScaleRunnable==null||!mAutoScaleRunnable.isAutoScale) {
            mAutoScaleRunnable = new AutoScaleRunnable(targetScale, curFocusX, curFocusY);
            new Thread(mAutoScaleRunnable).start();
        }
    }

    private boolean onTranslate(MotionEvent event){
        if(lock){
            return true;
        }

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

        int curAngle = mHelperListener.getCurAngle();

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

        return true;
    }

   public void translate(MotionEvent event,boolean complete){
       unLock();
       curScaleFactor = 1.0f;
       curFocusX = 0;
       curFocusY = 0;
       mGestureDetector.onTouchEvent(event);
       requestPaint(complete);
       offsetY = 0;
       offsetX = 0;
       scaleExtremity = false;
       if(complete){
           oldX=Integer.MIN_VALUE;
           oldY = Integer.MIN_VALUE;
           centerX = Integer.MIN_VALUE;
           centerY = Integer.MIN_VALUE;
           lock();
       }
   }



    GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
//            TPLog.printError("SimpleOnGestureListener -- > onScroll distanceX = "+distanceX+",distanceY = "+distanceY);
            if(lock){
                return true;
            }

            offsetX = distanceX*-1;
            offsetY = distanceY*-1;

            return false;
        }



        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            flingRunnable = new FlingRunnable(mContext);
            flingRunnable.fling((int)velocityX*-1,(int)velocityY*-1);
            new Thread(flingRunnable).start();
            return true;
        }
    };

    public void lock(){
        lock = true;
    }

    public void unLock(){
        lock = false;
    }


    /**
     * 自动放大缩小，自动缩放的原理是使用View.postDelay()方法，每隔16ms调用一次
     * run方法，给人视觉上形成一种动画的效果
     */
    private class AutoScaleRunnable implements Runnable{
        //放大或者缩小的目标比例
        private float mTargetScale;
        //可能是BIGGER,也可能是SMALLER
        private float tempScale;
        //放大缩小的中心点
        private float x;
        private float y;
        //比1稍微大一点，用于放大
        private final float BIGGER = 1.1f;
        //比1稍微小一点，用于缩小
        private final float SMALLER = 0.93f;

        private boolean isAutoScale = true;

        private  Matrix matrixTemp;
        //构造方法，将目标比例，缩放中心点传入，并且判断是要放大还是缩小
        public AutoScaleRunnable(float targetScale , float x , float y){
            this.mTargetScale = targetScale;
            this.x = x;
            this.y = y;
            //由于是这里的自动缩放线程和绘图处理线程，是异步的不好数据同步上存在问题，因此这里克隆一份当前的坐标矩阵，以便数据计算
            matrixTemp = new Matrix();
            Matrix matrix = mHelperListener.getCurPage().getMatrix();
            float values[] = new float[9];
            matrix.getValues(values);
            matrixTemp.setValues(values);

            //如果当前缩放比例小于目标比例，说明要自动放大
            if (getCurScale() < mTargetScale){
                //设置为Bigger
                tempScale = BIGGER;
            }
            //如果当前缩放比例大于目标比例，说明要自动缩小
            if (getCurScale() > mTargetScale){
                //设置为Smaller
                tempScale = SMALLER;
            }
        }
        @Override
        public void run() {
            while(isAutoScale){
                float values[] = new float[9];
                matrixTemp.getValues(values);
                //得到当前图片的缩放值
                float currentScale1 = values[Matrix.MSCALE_X];

                //局部缩放，计算过于麻烦，因此这里直接通过Matrix来处理
                matrixTemp.postScale(tempScale,tempScale,x,y);
                matrixTemp.getValues(values);
                float currentScale = values[Matrix.MSCALE_X];

                if ((tempScale > 1.0f) && currentScale < mTargetScale
                        ||(tempScale < 1.0f) && currentScale > mTargetScale){

                }else {
                    currentScale = currentScale1;
//                    TPLog.printError("run   end.....");
                    //保证图片最终的缩放值和目标缩放值一致
                    tempScale = mTargetScale / currentScale;
                    //自动缩放结束，置为false
                    isAutoScale = false;
                }


                curFocusX = x;
                curFocusY = y;
                curScaleFactor = tempScale;
                requestPaint(0,0,false);

                if(isAutoScale){
                    try {
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }


    class  FlingRunnable implements Runnable{
        private Scroller mScroller;

        private int mCurrentX , mCurrentY;

        public FlingRunnable(Context context){
            mScroller = new Scroller(context);
//            curScaleFactor = 1.0f;
//            curFocusX = 0;
//            curFocusY = 0;
        }

        public void cancelFling(){
            mScroller.forceFinished(true);
        }

        /**
         * 这个方法主要是从onTouch中或得到当前滑动的水平和竖直方向的速度
         * 调用scroller.fling方法，这个方法内部能够自动计算惯性滑动
         * 的x和y的变化率，根据这个变化率我们就可以对图片进行平移了
         */
        public void fling(int velocityX , int velocityY){
            mScroller.fling(0,0,velocityX,velocityY,Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE);
        }

        @Override
        public void run() {
//            TPLog.printError("SimpleOnGestureListener -- > FlingRunnable running... ");
            if (mScroller.isFinished()){
//                TPLog.printError("SimpleOnGestureListener -- > mScroller Finished... ");
                return;
            }
            //如果返回true，说明当前的动画还没有结束，我们可以获得当前的x和y的值
            while (mScroller.computeScrollOffset()){

                if(!checkDisplayOnScreen()){
                    cancelFling();
                    return;
                }

                //获得当前的x坐标
                final int newX = mScroller.getCurrX();
                //获得当前的y坐标
                final int newY = mScroller.getCurrY();

                offsetX = mCurrentX-newX ;
                offsetY = mCurrentY-newY;
//                TPLog.printError("SimpleOnGestureListener -- > offsetX = "+offsetX+",offsetY = "+offsetY);
                //进行平移操作
//                checkBorderWhenTranslate();
//                setImageMatrix(mScaleMatrix);
                mCurrentX = newX;
                mCurrentY = newY;

                requestPaint(1.0f,0,0,false);
                //每16ms调用一次
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private boolean checkDisplayOnScreen(){
        ISubPage subPage =  mHelperListener.getCurPage().getCurSubPage();
        Rect rect = computePageBounds(subPage);
//        TPLog.printError("SimpleOnGestureListener -- >checkDisplayOnScreen  rect = "+rect.toString());
        Rect screenRect = new Rect(0,0,(int)WhiteBoardUtils.whiteBoardWidth,(int)WhiteBoardUtils.whiteBoardHeight);

        boolean boo = screenRect.intersect(rect);
//        TPLog.printError("SimpleOnGestureListener -- >checkDisplayOnScreen  boo = "+boo);
        return boo;
    }

    private float getCurScale(){
        float values[] = new float[9];
       mHelperListener.getCurPage().getMatrix().getValues(values);
//        TPLog.printError("values[Matrix.MSCALE_X] = "+values[Matrix.MSCALE_X]);
        return values[Matrix.MSCALE_X];
    }


    private Rect computePageBounds(ISubPage subPage){
        ArrayList<Graph> graphList = subPage.getGraphList();
        ArrayList<Graph> imgList = subPage.getImageGraphList();

        ArrayList<Graph> list = new ArrayList<Graph>();

        int graphCount = graphList.size();
        int imgCount = imgList.size();
        for(int i = 0;i<graphCount;i++){
            list.add(graphList.get(i));
        }

        for(int i = 0;i<imgCount;i++){
            list.add(imgList.get(i));
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;

        for(int i = 0;i<list.size();i++){
            Rect bounds = list.get(i).getBounds();
            RectF dst = new RectF();
            if(list.get(i).getGraphType() != WhiteBoardUtils.GRAPH_IMAGE) {
                subPage.getMatrix().mapRect(dst, new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom));
            }else{
                dst.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
            }

            if(minX > dst.left){
                minX =  (int)dst.left;
            }

            if(minY > dst.top){
                minY = (int)dst.top;
            }

            if(maxX < dst.right){
                maxX = (int)dst.right;
            }

            if(maxY < dst.bottom){
                maxY = (int)dst.bottom;
            }
        }
        Rect bounds  = new Rect(minX,minY,maxX,maxY);
        return bounds;
    }
}
