package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Path;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.MotionEvent;

import com.kedacom.frambuffer.SkiaPaint;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintMsgState;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.ArithUtil;
import com.kedacom.utils.VersionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/3/13.
 */

public class DrawPathGraphToFBHelper implements IDrawHelper {

    private int curDrawType = WhiteBoardUtils.GRAPH_PEN;

    private Graph mGraph;

    private Path curPath;

    private IHelperListener mHelperListener;

    private int curPointerId = -1;

    private float lastX;

    private float lastY;

    private float curStrokeWidth;

    private List<TouchEvent> pointCache = new ArrayList<TouchEvent>();


    public DrawPathGraphToFBHelper(IHelperListener listener) {
        mHelperListener = listener;
        if (VersionUtils.isImix())
            SkiaPaint.init(0);
    }

    Object synLock = new Object();

    @Override
    public void setDrawType(int drawType) {
        curDrawType = drawType;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        TPLog.printError("ACTION = "+MotionEvent.actionToString(event.getAction())+",,,,,,,,x = "+ event.getX()+",,,,,,,,,,,y = "+event.getY());
        synchronized (synLock) {
            int action = event.getActionMasked();

            int pointerId = event.getPointerId(0);

            int pointerCount = event.getPointerCount();

            if (curPointerId == -1 && action != MotionEvent.ACTION_DOWN) {
                return false;
            }

            float x = event.getX();
            float y = event.getY();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (mHelperListener != null)
                        mHelperListener.onPaintDrawDown();
                    curPointerId = pointerId;
                    lastX = x;
                    lastY = y;
                    mGraph = GraphFactory.makeGraph(curDrawType);
                    mGraph.addPoint(x, y);
                    curStrokeWidth = ArithUtil.mul(WhiteBoardUtils.curStrokeWidth, mHelperListener.getCurScale());
                    curPath = new Path();
                    curPath.moveTo(x, y);

                    if (!isLock) {
                        SkiaPaint.draw(x, y, curStrokeWidth, (int) mGraph.getColor(), MotionEvent.ACTION_DOWN);
                    } else {
                        pointCache.clear();
                        pointCache.add(new TouchEvent(x, y, MotionEvent.ACTION_DOWN));
                    }
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
//                    Log.e("123", "onTouchEvent: zzz");
//                    float x1 = event.getX(pointerCount - 1);
//                    float y1 = event.getY(pointerCount - 1);
//                    if (mHelperListener != null)
//                        mHelperListener.onPaintDrawDown();
//                    curPointerId = pointerId;
//                    lastX = x1;
//                    lastY = y1;
//                    mGraph = GraphFactory.makeGraph(curDrawType);
//                    mGraph.addPoint(x1, y1);
//                    curStrokeWidth = ArithUtil.mul(WhiteBoardUtils.curStrokeWidth, mHelperListener.getCurScale());
//                    curPath = new Path();
//                    curPath.moveTo(x1, y1);
//
//                    if (!isLock) {
//                        SkiaPaint.draw(x1, y1, curStrokeWidth, (int) mGraph.getColor(), MotionEvent.ACTION_DOWN);
//                    } else {
//                        pointCache.clear();
//                        pointCache.add(new TouchEvent(x1, y1, MotionEvent.ACTION_DOWN));
//                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float hx = event.getHistoricalX(i);
                        float hy = event.getHistoricalY(i);
                        mGraph.addPoint(hx, hy);
                        curPath.lineTo(hx, hy);
                        if (!isLock) {
                            SkiaPaint.draw(hx, hy, curStrokeWidth, (int) mGraph.getColor(), MotionEvent.ACTION_MOVE);
                        } else {
                            pointCache.add(new TouchEvent(hx, hy, MotionEvent.ACTION_MOVE));
                        }
                    }
//                    mGraph.addPoint(x, y);
//                    curPath.lineTo(x, y);
//                    if (!isLock) {
//                        SkiaPaint.draw(x, y, curStrokeWidth, (int) mGraph.getColor(), MotionEvent.ACTION_MOVE);
//                    } else {
//                        pointCache.add(new TouchEvent(x, y, MotionEvent.ACTION_MOVE));
//                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    if (curPointerId == event.getPointerId(event.getActionIndex())) {
                        curPointerId = -1;
                        mGraph.addPoint(x, y);
                        curPath.lineTo(x, y);
                        if (!isLock) {
                            SkiaPaint.draw(x, y, curStrokeWidth, (int) mGraph.getColor(), MotionEvent.ACTION_UP);
                        }
                        requestPaint(curPath, true,true);
                    }
                    break;
            }
            lastX = x;
            lastY = y;
        }
        return true;
    }


    private void requestPaint(Path curPath, boolean isComplete,boolean isLastUp) {
        if (mHelperListener != null) {
            PaintMsgState paintMsgState = null;
            paintMsgState = new PaintMsgState(curPath, mGraph.getPaint(), mGraph, isComplete, true,isLastUp);
            mHelperListener.requestPaint(new MsgEntity(paintMsgState));
        }
    }

    @Override
    public void onDestory() {
        mGraph = null;
        curPath = null;
    }


    private boolean isLock = true;

    public void lock() {
        synchronized (synLock) {
            isLock = true;
        }
    }

    public void unLock() {
        synchronized (synLock) {
            isLock = false;
            if (mGraph == null) {
                return;
            }
            for (int i = 0; i < pointCache.size(); i++) {
                TouchEvent event = pointCache.get(i);
                SkiaPaint.draw(event.x, event.y, curStrokeWidth, (int) mGraph.getColor(), event.action);
            }
        }
    }


    class TouchEvent {
        public float x;
        public float y;
        public int action;

        public TouchEvent(float x, float y, int action) {
            this.x = x;
            this.y = y;
            this.action = action;
        }
    }
}
