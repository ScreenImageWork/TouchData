package com.kedacom.touchdata.whiteboard.helper;

import android.annotation.TargetApi;
import android.graphics.Path;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintMsgState;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.utils.ArithUtil;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by wt on 2018/7/30.
 * 绘制
 */
public class BaseDrawGraphHelper {
    private final float twoPointInterval = 3; //Touch Move时前后两点之间的距离大于该距离时才进行图形绘制

    //private final float extendInterval = 5;

    protected float mLastPointX;
    protected float mLastPointY;

    protected float mPreviousX;
    protected float mPreviousY;

    private float mPX;
    private float mPY;

    public   float mCurX;
    public   float mCurY;


    private int curDrawType = WhiteBoardUtils.GRAPH_PEN;

    private Graph mGraph;

    private IHelperListener mHelperListener;




    public BaseDrawGraphHelper(IHelperListener listener) {
        mHelperListener = listener;
    }



    public void setDrawType(int drawType) {
        curDrawType = drawType;
    }


    //MotionEvent event
    public void touchDown(float x, float y) {
        this.mPX = x;
        this.mPY = y;
        mGraph = GraphFactory.makeGraph(curDrawType);
        mGraph.addPoint(mPX, mPY);

        float strokeWidth = ArithUtil.mul(WhiteBoardUtils.curStrokeWidth, mHelperListener.getCurScale());
        mGraph.getPaint().setStrokeWidth(strokeWidth);
        drawStart(null, mPX, mPY);

    }

    public void touchMove(float x, float y) {
        Path curPath = new Path();
        this.mPX = x;
        this.mPY = y;

        mGraph.addPoint(mPX, mPY);
        drawIng(curPath, mPX, mPY);
        requestPaint(curPath, false,false);
    }

    /**
     *
     * @param x
     * @param y
     * @param isLastUp 是否是最后一个手指抬起
     */
    public void touchUp(float x,float y,boolean isLastUp) {
        this.mPX = x;
        this.mPY = y;
        mGraph.addPoint(mPX, mPY);
        drawEnd(null, mPX, mPY,isLastUp);
    }

    public void drawStart(Path curPath, float x, float y) {
        if (curPath == null) {
            curPath = new Path();
        }
        mLastPointX = x;
        mLastPointY = y;
        mPreviousX = x + 1;
        mPreviousY = y + 1;

        //2017.08.19添加，手指点击也会绘图
        mGraph.addPoint(mPreviousX, mPreviousY);
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        curPath.moveTo(mLastPointX, mLastPointY);
        curPath.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
        this.mLastPointX = f1;
        this.mLastPointY = f2;
        requestPaint(curPath, false,false);
    }

    public void drawIng(Path path, float x, float y) {
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        if (path == null) {
            path = new Path();
        }
        if (path.isEmpty()) {
            path.moveTo(mLastPointX, mLastPointY);
        }

        path.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);

//		TPLog.printRepeat("x="+x);
//		TPLog.printRepeat("y="+y);
//		TPLog.printRepeat("mLastPointX="+mLastPointX);
//		TPLog.printRepeat("mLastPointY="+mLastPointY);
//		TPLog.printRepeat("mPreviousX="+mPreviousX);
//		TPLog.printRepeat("mPreviousY="+mPreviousY);
//		TPLog.printRepeat("f1="+f1);
//		TPLog.printRepeat("f2="+f2);
//		TPLog.printRepeat("-------------------------------------------------------------------------------------");

        this.mPreviousX = x;
        this.mPreviousY = y;
        this.mLastPointX = f1;
        this.mLastPointY = f2;

    }

    public void drawEnd(Path curPath, float x, float y, boolean isLastUp) {
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        if (curPath == null)
            curPath = new Path();
//	    this.mCurrentPath.reset();
        if (curPath.isEmpty()) {
            curPath.moveTo(this.mLastPointX, this.mLastPointY);
        }
        this.mLastPointX = f1;
        this.mLastPointY = f2;
        curPath.lineTo(x, y);
        this.mPreviousX = x;
        this.mPreviousY = y;

        // drawing(mCurrentPath);
        requestPaint(curPath, true,isLastUp);
        //saveGraphEntity(mGraph);
    }

    private boolean containsPoint(float px, float py, ArrayList<Point> points) {
        if (points == null || points.size() == 0) {
            return false;
        }
        int x = (int) px;
        int y = (int) py;

        Iterator<Point> iterator = points.iterator();
        while (iterator.hasNext()) {
            Point point = iterator.next();
            if (point.equals(x, y)) {
                return true;
            }
        }

        return false;
    }

    private void requestPaint(Path curPath, boolean isComplete,boolean isLastUp) {
        if (mHelperListener != null) {
            PaintMsgState paintMsgState = null;
            paintMsgState = new PaintMsgState(curPath, mGraph.getPaint(), mGraph, isComplete, true,isLastUp);
            mHelperListener.requestPaint(new MsgEntity(paintMsgState));

        }

    }

    private Graph cloneGraph() {
        Graph graph = GraphFactory.makeGraph(curDrawType);
        graph.setColor(mGraph.getColor());
        graph.setStrokeWidth(mGraph.getStrokeWidth());
        graph.setGraphIndex(mGraph.getGraphIndex());
        graph.setId(mGraph.getId());
        graph.setPageIndex(mGraph.getPageIndex());
        graph.setTabId(mGraph.getTabId());
        ArrayList<Point> pointList = mGraph.getPoints();
        for (int i = 0; i < pointList.size(); i++) {
            Point point = pointList.get(i);
            graph.addPoint(point.x, point.y);
        }
        return graph;
    }

    public void onDestory() {
        mGraph = null;
    }


}
