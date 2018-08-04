package com.kedacom.touchdata.whiteboard.msg.entity;

import com.kedacom.touchdata.whiteboard.graph.Graph;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class AreaEraseMsgState extends MsgState{

    private Graph mGraph;

    private boolean isComplete;

    public AreaEraseMsgState(){
       this(null,false);
    }

    public AreaEraseMsgState(Graph mGraph,boolean isComplete){
        setType(MsgState.MsgType.TYPE_AREA_ERASE);
        setmGraph(mGraph);
        setComplete(isComplete);
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
}
