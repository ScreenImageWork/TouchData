package com.kedacom.touchdata.whiteboard.msg.entity;

import com.kedacom.touchdata.whiteboard.graph.Graph;

/**
 * Created by zhanglei on 2017/9/11.
 */
public class PaintAndSaveGraphState extends MsgState{

    private Graph graph;

    public PaintAndSaveGraphState(Graph graph){
        setType(MsgType.TYPE_SAVE_PAINT);
        setGraph(graph);
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }
}
