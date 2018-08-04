package com.kedacom.touchdata.whiteboard.msg.entity;

import com.kedacom.touchdata.whiteboard.graph.Graph;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class EraseMsgState extends MsgState{

    private Graph mGraph;

    private boolean isComplete = false;

    private boolean isSyn = false;

    public EraseMsgState(){
        this(null,false,true);
    }

    public  EraseMsgState(Graph graph, boolean isComplete, boolean isSyn){
        setType(MsgState.MsgType.TYPE_ERASE);
        setmGraph(graph);
        setComplete(isComplete);
        setSyn(isSyn);
    }

    public Graph getmGraph() {
        return mGraph;
    }

    public void setmGraph(Graph mGraph) {
        this.mGraph = mGraph;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isSyn() {
        return isSyn;
    }

    public void setSyn(boolean syn) {
        isSyn = syn;
    }
}
