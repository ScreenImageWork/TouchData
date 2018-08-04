package com.kedacom.touchdata.whiteboard.graph;

import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class GraphFactory {
	
	private GraphFactory(){
		
	}
	
	
     public static Graph makeGraph(int type){
    	 Graph mGraph = null;
    	 
    	 switch(type){
			 case WhiteBoardUtils.GRAPH_PEN:
				 mGraph = new Pen();
				 mGraph.setColor(WhiteBoardUtils.curColor);
				 break;
			 case WhiteBoardUtils.GRAPH_ERASE:
				 mGraph = new Erase();
				 break;
			 case WhiteBoardUtils.GRAPH_ERASE_AREA:
				 mGraph = new AreaErase();
				 break;
    		 case WhiteBoardUtils.GRAPH_BRUSHPEN:
				 mGraph = new BrushPen();
				 mGraph.setColor(WhiteBoardUtils.curColor);
				 break;
    	 }
    	 
    	 mGraph.setStrokeWidth(WhiteBoardUtils.curStrokeWidth);
    	 return mGraph;
     }
}
