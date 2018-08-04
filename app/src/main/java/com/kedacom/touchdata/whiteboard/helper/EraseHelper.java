package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.EraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2016/10/27.
 * 废弃了，现在被GestrueEraseHelper代替了
 *
 */
public class EraseHelper implements IGestrueHelper {

    private Graph mGraph;

    private IHelperListener mHelperListener;

    private boolean isComputeTouchArea = false;

    private int erasePanelWidth = 0;

    private int erasePanelHeight = 0;

    private int maxErasePanelWidth = 150;

    private int maxErasePanelHeight = 150;

    public EraseHelper(IHelperListener listener){
        mHelperListener = listener;
    }

    @Override
    public void complete() {
        isComputeTouchArea = false;
        if(mHelperListener!=null&&mGraph!=null) {
            requestPaint(true);
            mHelperListener.dismissErasePanelWindow();
        }
        mGraph = null;
    }



    public boolean onTouchEvent(MotionEvent event) {

        if(!isComputeTouchArea){
            computeTouchArea(event);
        }

        computeTouchErase(event);

        return true;
    }

    @Override
    public void onDestory() {
        mGraph = null;
    }


    private void computeTouchArea(MotionEvent event){

        int action = event.getActionMasked();

        boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP?true:false;

        int pointCount = event.getPointerCount();

        int skipIndex = pointerUp?event.getActionIndex():-1;

        int div = pointerUp?pointCount-1:pointCount;

        float sumX = 0;

        float sumY = 0;

        Path path = new Path();

        for(int i = 0;i<pointCount;i++){
            if(skipIndex==i)continue;
            float x = event.getX(i);
            float y = event.getY(i);
            if(path.isEmpty())path.moveTo(x,y);
            else path.lineTo(x,y);

            sumX += x;
            sumY += y;
        }

        RectF bounds = new RectF();
        path.computeBounds(bounds,true);

        float width = bounds.width();
        float height = bounds.height();

        if(width>height){
            erasePanelWidth = (int)width;
            erasePanelHeight = (int)width;
        }else{
            erasePanelWidth = (int)height;
            erasePanelHeight = (int)height;
        }

        if(erasePanelWidth>maxErasePanelWidth){
            erasePanelWidth = maxErasePanelWidth;
        }

        if(erasePanelHeight > maxErasePanelHeight){
            erasePanelHeight = maxErasePanelHeight;
        }

        //erasePanelWidth = (int)((float)erasePanelHeight*1.25f);

        //TPLog.printKeyStatus("擦除实际区域：width="+width+",height="+height+",\n板擦大小：erasePanelWidth="+erasePanelWidth+",erasePanelHeight="+erasePanelHeight);

        float focusX = sumX /div;
        float focusY = sumY /div;

        int erasePanelX = (int)(focusX - erasePanelWidth/2f);
        int erasePanelY = (int)(focusY - erasePanelHeight/2f);

        if(mHelperListener!=null)
        mHelperListener.displayErasePanel(erasePanelWidth,erasePanelHeight,erasePanelX,erasePanelY);

        mGraph = GraphFactory.makeGraph(WhiteBoardUtils.GRAPH_ERASE);
        mGraph.addPoint(focusX,focusY);
        mGraph.setStrokeWidth(erasePanelHeight);


        isComputeTouchArea = true;
    }

    public void computeTouchErase(MotionEvent event){

        int action = event.getActionMasked();

        boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP?true:false;

        int pointCount = event.getPointerCount();

        int div = pointerUp?pointCount-1:pointCount;

        int skipIndex = pointerUp?event.getActionIndex():-1;

        float sumX = 0;

        float sumY = 0;

        for(int i = 0;i<pointCount;i++){
            if(skipIndex == i)continue;
            sumX += event.getX(i);
            sumY += event.getY(i);
        }

        final float focusX = sumX/div;
        final float focusY = sumY/div;

        mGraph.addPoint(focusX, focusY);

        int erasePanelX = (int)(focusX - erasePanelWidth/2f);
        int erasePanelY = (int)(focusY - erasePanelHeight/2f);

        if(mHelperListener!=null) {
            mHelperListener.erasePanelMoveTo(erasePanelX, erasePanelY);
             requestPaint(false);
        }
    }

    private void requestPaint(boolean isComplete){
        if(mHelperListener==null)return;
        EraseMsgState eraseMsgState = new EraseMsgState(mGraph,isComplete,true);
        mHelperListener.requestPaint(new MsgEntity(eraseMsgState));
    }

}
