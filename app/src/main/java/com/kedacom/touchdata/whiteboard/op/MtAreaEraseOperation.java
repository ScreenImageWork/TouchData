package com.kedacom.touchdata.whiteboard.op;

import com.kedacom.touchdata.whiteboard.graph.Graph;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2018/4/26.
 */

public class MtAreaEraseOperation implements IOperation {

    private ArrayList<Graph> grapList = new ArrayList<Graph>();

    @Override
    public int getType() {
        return MT_AREA_ERASE;
    }

    public MtAreaEraseOperation(){

    }

    public void addOldGraphList(ArrayList<Graph> list){
        this.grapList = list;
    }

    public ArrayList<Graph> getGraphList(){
        return grapList;
    }
}
