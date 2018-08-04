package com.kedacom.touchdata.whiteboard.msg.entity;

import com.kedacom.touchdata.whiteboard.graph.SelectGraph;

/**
 * Created by zhanglei on 2017/6/22.
 */
public class SelectGraphMsgState extends MsgState{

    private SelectGraph mSelectGraph;

    private boolean complete;

    public SelectGraphMsgState(){
        setType(MsgType.TYPE_SELECT_GRAPH);
    }

    public SelectGraph getmSelectGraph() {
        return mSelectGraph;
    }

    public void setmSelectGraph(SelectGraph mSelectGraph) {
        this.mSelectGraph = mSelectGraph;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
