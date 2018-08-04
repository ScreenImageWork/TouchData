package com.kedacom.touchdata.whiteboard.op;

import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class GraphOperation implements IOperation {

	private Graph mGraph;
	
	public GraphOperation(){
		
	}

	@Override
	public int getType() {
		return IOperation.OPT_GRAPH;
	}
	
	
	public void setGraph(Graph graph){
		mGraph = graph;
	}
	
	public Graph getGraph(){
		return mGraph;
	}

}
