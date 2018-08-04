package com.kedacom.touchdata.whiteboard.msg.entity;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

import com.kedacom.touchdata.whiteboard.graph.Graph;

/**
 * Created by zhanglei on 2016/10/31.
 */
public class PaintMsgState extends MsgState{

    private Path path;

    private Paint paint;

    private Graph graph;   // isComplete = true 时才不为null

    private boolean isComplete;   //绘图是否完毕

    private boolean isSyn = false;

    private boolean isLastUp;//是否是最后一个手指抬起

    public PaintMsgState(){
        this(null,null,null,false,true,true);
    }

    public PaintMsgState(Path path,Paint paint,Graph graph,boolean isComplete, boolean isSyn,boolean isLastUp){
        setType(MsgType.TYPE_PAINT);
        setPath(path);
        setGraph(graph);
        setComplete(isComplete);
        setPaint(paint);
        setSyn(isSyn);
        setLastUp(isLastUp);
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
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

    public boolean isLastUp() {
        return isLastUp;
    }

    public void setLastUp(boolean lastUp) {
        isLastUp = lastUp;
    }
}
