package com.kedacom.touchdata.whiteboard.helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintMsgState;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.ArithUtil;

/**
 * 该类主要实现Path类图元的绘制，目前只有两个 分别是Pen 和 Erase
 * implements IDrawHelper
 */
public class DrawPathGraphHelper implements IDrawHelper {

//    private final float twoPointInterval = 3; //Touch Move时前后两点之间的距离大于该距离时才进行图形绘制
//
//    //private final float extendInterval = 5;
//
//    protected float mLastPointX;
//    protected float mLastPointY;
//
//    protected float mPreviousX;
//    protected float mPreviousY;
//
//    private float mPX;
//    private float mPY;
//

    //
//    private Graph mGraph;
//
    private IHelperListener mHelperListener;
    private int curDrawType = WhiteBoardUtils.GRAPH_PEN;
    //用于指定当前是哪个对象
    private SparseArray<BaseDrawGraphHelper> baseDrawGraphHelperMap = new SparseArray();
    List<BaseDrawGraphHelper> completedPolylines = new ArrayList<>();


    //最大触摸手指量
    private static final int MAX_TOUCHPOINTS = 2;

    private BaseDrawGraphHelper baseDrawGraphHelper;


    private final static int INVALID_ID = -1;
    private int activePointerId = INVALID_ID;
    private boolean isCanUpadate=false;


    public DrawPathGraphHelper(IHelperListener listener) {
        mHelperListener = listener;
    }

    @Override
    public void setDrawType(int drawType) {
        if (baseDrawGraphHelperMap == null || baseDrawGraphHelperMap.size() == 0) return;
        curDrawType = drawType;
        for (int i = 0; i < baseDrawGraphHelperMap.size(); i++) {
            baseDrawGraphHelperMap.get(i).setDrawType(curDrawType);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //当前DOWN或者UP的是手指的index

        int actionIndex = event.getActionIndex();
        //通过index获得当前手指的id
        activePointerId = event.getPointerId(0);
        // 获得屏幕触点数量
        int pointerCount = event.getPointerCount();
        int action = event.getActionMasked();
        if (activePointerId == -1 && action != MotionEvent.ACTION_DOWN) {
            return false;
        }
        if (pointerCount > MAX_TOUCHPOINTS) {
            pointerCount = MAX_TOUCHPOINTS;
        }
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                BaseDrawGraphHelper baseDrawGraphHelperOne = new BaseDrawGraphHelper(mHelperListener);
                baseDrawGraphHelperMap.put(activePointerId, baseDrawGraphHelperOne);
                baseDrawGraphHelperOne.touchDown(event.getX(0), event.getY(0));
                break;
            case MotionEvent.ACTION_POINTER_DOWN://第一个之后的触控点按下
                // 将新落下来那根手指作为活动手指
                if (event.getPointerId(actionIndex) == (pointerCount - 1)) {
                    activePointerId = event.getPointerId(actionIndex);
                    BaseDrawGraphHelper baseDrawGraphHelperTwo = new BaseDrawGraphHelper(mHelperListener);
                    baseDrawGraphHelperMap.put(activePointerId, baseDrawGraphHelperTwo);
                    baseDrawGraphHelperTwo.touchDown(event.getX(pointerCount - 1), event.getY(pointerCount - 1));
                }
//                touchDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_ID) {
                    return false;
                }
                for (int i = 0; i < pointerCount; i++) {
                    baseDrawGraphHelper = baseDrawGraphHelperMap.get(event.getPointerId(i));
                    if (baseDrawGraphHelper != null) {
                        baseDrawGraphHelper.touchMove(event.getX(i),
                                event.getY(i));
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //非最后一个点被释放
                //获得释放的点的索引和ID
                TPLog.printError("DrawPathGraphHelper:onTouchEvent,UP | ACTION_CANCEL | ACTION_POINTER_UP " +
                        "  =   " + MotionEvent.actionToString(event.getAction()));
//                if (baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)) != null) {
//                    int newPointerIndex = event.findPointerIndex(event.getPointerId(actionIndex));
//                    baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)).touchUp(event.
//                            getX(newPointerIndex), event.getY(newPointerIndex), false);
//                    baseDrawGraphHelperMap.remove(event.getPointerId(actionIndex));
//
//                }


                    // pointerIndex都是像0, 1, 2这样连续的
//                    final int newPointerIndex = actionIndex == 0 ? 1 : 0;
//                    activePointerId = event.getPointerId(newPointerIndex);
                    if (baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)) != null) {
                        int newPointerIndex = event.findPointerIndex(event.getPointerId(actionIndex));
                        if (!isCanUpadate) {
                            isCanUpadate = true;
//                            baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)).touchUp(event.
//                                    getX(newPointerIndex), event.getY(newPointerIndex), false);
                        } else {
                            isCanUpadate = false;
//                            baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)).touchUp(event.
//                                    getX(newPointerIndex), event.getY(newPointerIndex), true);
                        }
                        baseDrawGraphHelperMap.remove(event.getPointerId(actionIndex));
                        Log.e("dd", "onTouchEvent: 松开的是活动手指");
                    }
                    Log.e("dd", "onTouchEvent:ACTION_POINTER_UP " + event.getPointerId(actionIndex));

                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.e("dd", "onTouchEvent:ACTION_UP " + event.getPointerId(actionIndex));
                if (baseDrawGraphHelperMap.get(event.getPointerId(actionIndex)) != null) {
                    baseDrawGraphHelper = baseDrawGraphHelperMap.get(event.getPointerId(actionIndex));
                    if (baseDrawGraphHelper != null) {
                        isCanUpadate=false;
                        int index=event.findPointerIndex(event.getPointerId(actionIndex));
//                        baseDrawGraphHelper.touchUp(event.getX(index),event.getY(index), true);
                    }
                    activePointerId = INVALID_ID;
                    baseDrawGraphHelperMap.clear();
                }
                break;

        }
        return true;
    }

//    //MotionEvent event
//    private void touchDown(MotionEvent event) {
//
//        int i = event.getActionIndex();
//        this.mPX = event.getX(i);
//        this.mPY = event.getY(i);
//
//        mGraph = GraphFactory.makeGraph(curDrawType);
//        mGraph.addPoint(mPX, mPY);
//
//        float strokeWidth = ArithUtil.mul(WhiteBoardUtils.curStrokeWidth, mHelperListener.getCurScale());
////		float strokeWidth = (float)WhiteBoardUtils.curStrokeWidth*mHelperListener.getCurScale();
////
////		TPLog.printKeyStatus("临时设置的strokeWidth="+strokeWidth);
//        mGraph.getPaint().setStrokeWidth(strokeWidth);
//        // mGraph.getPaint().setStrokeWidth((float)WhiteBoardUtils.curStrokeWidth);
//
//        drawStart(null, mPX, mPY);
//
//    }
//
//    private void touchMove(MotionEvent event, int pointerCount) {
//        int pi = event.getActionIndex();
//
//        Path curPath = new Path();
//        int historySize = event.getHistorySize();
//        for (int i = 0; i < historySize; i++) {
//            float x = event.getHistoricalX(pi, i);
//            float y = event.getHistoricalY(pi, i);
//            this.mPX = x;
//            this.mPY = y;
//
//            mGraph.addPoint(mPX, mPY);
//            drawIng(curPath, mPX, mPY);
//        }
//
//        requestPaint(curPath, false);
//    }
//
//    private void touchUp(MotionEvent event) {
//        int i = event.getActionIndex();
//        this.mPX = event.getX(i);
//        this.mPY = event.getY(i);
//        mGraph.addPoint(mPX, mPY);
//        drawEnd(null, mPX, mPY);
//    }
//
//    public void drawStart(Path curPath, float x, float y) {
//        if (curPath == null) {
//            curPath = new Path();
//        }
//        mLastPointX = x;
//        mLastPointY = y;
//        mPreviousX = x + 1;
//        mPreviousY = y + 1;
//
//        //2017.08.19添加，手指点击也会绘图
//        mGraph.addPoint(mPreviousX, mPreviousY);
//        float f1 = (x + this.mPreviousX) / 2.0F;
//        float f2 = (y + this.mPreviousY) / 2.0F;
//        curPath.moveTo(mLastPointX, mLastPointY);
//        curPath.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
//        this.mLastPointX = f1;
//        this.mLastPointY = f2;
//        requestPaint(curPath, false);
//    }
//
//    public void drawIng(Path path, float x, float y) {
//        float f1 = (x + this.mPreviousX) / 2.0F;
//        float f2 = (y + this.mPreviousY) / 2.0F;
//        if (path == null) {
//            path = new Path();
//        }
////		mCurrentPath.reset();
//        if (path.isEmpty()) {
//            path.moveTo(mLastPointX, mLastPointY);
//        }
//
//        path.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
//
////		TPLog.printRepeat("x="+x);
////		TPLog.printRepeat("y="+y);
////		TPLog.printRepeat("mLastPointX="+mLastPointX);
////		TPLog.printRepeat("mLastPointY="+mLastPointY);
////		TPLog.printRepeat("mPreviousX="+mPreviousX);
////		TPLog.printRepeat("mPreviousY="+mPreviousY);
////		TPLog.printRepeat("f1="+f1);
////		TPLog.printRepeat("f2="+f2);
////		TPLog.printRepeat("-------------------------------------------------------------------------------------");
//
//        this.mPreviousX = x;
//        this.mPreviousY = y;
//        this.mLastPointX = f1;
//        this.mLastPointY = f2;
//
////		requestPaint(false);
////		mCurrentPath = null;
//    }
//
//    public void drawEnd(Path curPath, float x, float y) {
//        float f1 = (x + this.mPreviousX) / 2.0F;
//        float f2 = (y + this.mPreviousY) / 2.0F;
//        if (curPath == null)
//            curPath = new Path();
////	    this.mCurrentPath.reset();
//        if (curPath.isEmpty()) {
//            curPath.moveTo(this.mLastPointX, this.mLastPointY);
//        }
//        this.mLastPointX = f1;
//        this.mLastPointY = f2;
//        curPath.lineTo(x, y);
//        this.mPreviousX = x;
//        this.mPreviousY = y;
//
//        // drawing(mCurrentPath);
//        requestPaint(curPath, true);
//        //saveGraphEntity(mGraph);
//    }
//
//    private boolean containsPoint(float px, float py, ArrayList<Point> points) {
//        if (points == null || points.size() == 0) {
//            return false;
//        }
//        int x = (int) px;
//        int y = (int) py;
//
//        Iterator<Point> iterator = points.iterator();
//        while (iterator.hasNext()) {
//            Point point = iterator.next();
//            if (point.equals(x, y)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
//
//    private void requestPaint(Path curPath, boolean isComplete) {
////		ArrayList<Point> list = mGraph.getPoints();
////		if(list.size()<4){
////			return;
////		}
//        if (mHelperListener != null) {
////			Path path = new Path();
////			path.set(mCurrentPath);
//
//
//            PaintMsgState paintMsgState = null;
////			if(isComplete){
////				Graph graph = cloneGraph();
////				mGraph = null;
////				paintMsgState = new PaintMsgState(path,graph.getPaint(),graph,isComplete,true);
////			}else{
////				paintMsgState = new PaintMsgState(path,mGraph.getPaint(),mGraph,isComplete,true);
////			}
//
//            paintMsgState = new PaintMsgState(curPath, mGraph.getPaint(), mGraph, isComplete, true);
//
//            mHelperListener.requestPaint(new MsgEntity(paintMsgState));
//
//        }
//
//    }
//
//    private Graph cloneGraph() {
//        Graph graph = GraphFactory.makeGraph(curDrawType);
//        graph.setColor(mGraph.getColor());
//        graph.setStrokeWidth(mGraph.getStrokeWidth());
//        graph.setGraphIndex(mGraph.getGraphIndex());
//        graph.setId(mGraph.getId());
//        graph.setPageIndex(mGraph.getPageIndex());
//        graph.setTabId(mGraph.getTabId());
//        ArrayList<Point> pointList = mGraph.getPoints();
//        for (int i = 0; i < pointList.size(); i++) {
//            Point point = pointList.get(i);
//            graph.addPoint(point.x, point.y);
//        }
//        return graph;
//    }
//

    public void onDestory() {
        for (int i = 0; i < baseDrawGraphHelperMap.size(); i++) {
            baseDrawGraphHelperMap.get(i).onDestory();
        }
    }

}
