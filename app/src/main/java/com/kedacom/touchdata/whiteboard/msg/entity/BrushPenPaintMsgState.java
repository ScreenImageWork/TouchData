package com.kedacom.touchdata.whiteboard.msg.entity;


import android.graphics.Paint;
import android.graphics.Path;

import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.helper.DrawBrushPenHelper;

import static com.kedacom.touchdata.whiteboard.msg.entity.MsgState.MsgType.TYPE_BRUSH_PEN;

/**
 * Created by zhanglei on 2018/4/18.
 */

public class BrushPenPaintMsgState extends MsgState {

    private Paint paint;

    private Graph graph;   // isComplete = true 时才不为null

    private DrawBrushPenHelper.BrushPenSegment segment;

    private boolean isComplete;   //绘图是否完毕

    private boolean isSyn = false;

    public BrushPenPaintMsgState(){
        setType(TYPE_BRUSH_PEN);
    }

    public BrushPenPaintMsgState(DrawBrushPenHelper.BrushPenSegment path,Graph graph,boolean isComplete, boolean isSyn){
        setType(MsgType.TYPE_BRUSH_PEN);
        setSegment(path);
        setGraph(graph);
        setComplete(isComplete);
        setPaint(paint);
        setSyn(isSyn);
    }

    public DrawBrushPenHelper.BrushPenSegment getSegment() {
        return segment;
    }

    public void setSegment(DrawBrushPenHelper.BrushPenSegment segment) {
        this.segment = segment;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public boolean isSyn() {
        return isSyn;
    }

    public void setSyn(boolean syn) {
        isSyn = syn;
    }
}
