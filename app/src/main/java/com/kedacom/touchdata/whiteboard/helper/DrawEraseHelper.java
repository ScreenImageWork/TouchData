package com.kedacom.touchdata.whiteboard.helper;

import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.EraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

public class DrawEraseHelper implements IDrawHelper {

	private IHelperListener mHelperListener;

	private Graph mGraph;

	public  DrawEraseHelper(IHelperListener listener){
		mHelperListener = listener;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch(event.getAction()){
			case MotionEvent.ACTION_DOWN:
				touchDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				touchMove(event);
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				touchUp(event);
				break;
		}
		return true;
	}

	private void touchDown(MotionEvent event){
		float x = event.getX();
		float y = event.getY();
		mGraph = GraphFactory.makeGraph(WhiteBoardUtils.GRAPH_ERASE);
		computeEraseSize();
		mGraph.addPoint(x, y);
		long time = event.getEventTime() - event.getDownTime();
		if(time>100){
			displayErasePanel(x,y);
		}
	}

	private void touchMove(MotionEvent event){
		if(mGraph == null){
			touchDown(event);
			return;
		}
		float x = event.getX();
		float y = event.getY();
		mGraph.addPoint(x, y);
		requestPaint(false);
		long time = event.getEventTime() - event.getDownTime();
		if(time>100){
			displayErasePanel(x,y);
		}
	}

	private void touchUp(MotionEvent event){
		if(mGraph==null)
			return;
		float x = event.getX();
		float y = event.getY();
		mGraph.addPoint(x, y);
		requestPaint(true);

		mGraph = null;
		if(mHelperListener!=null){
			mHelperListener.dismissErasePanelWindow();
		}
	}

	private void displayErasePanel(float x,float y){
		if(mGraph==null)return;
		//添加擦除
		if(mHelperListener!=null){
			int erasePanelX = (int) (x - ((Erase)mGraph).getEraseWidth() / 2f);
			int erasePanelY = (int) (y - ((Erase)mGraph).getEraseHeight() / 2f);
			if(!mHelperListener.erasePanelIsShowing()) {
				float erasePanelWidth = ((Erase)mGraph).getEraseWidth();
				float erasePanelHeight = ((Erase)mGraph).getEraseHeight();
				erasePanelX = (int) (x - erasePanelWidth / 2f);
				erasePanelY = (int) (y - erasePanelHeight / 2f);
				mHelperListener.displayErasePanel((int)erasePanelWidth, (int)erasePanelHeight, erasePanelX, erasePanelY);
			}else{
				mHelperListener.erasePanelMoveTo(erasePanelX,erasePanelY);
			}
		}
	}

//	public void displayErasePanel(){
//		if(mGraph==null)return;
//		if(mHelperListener!=null){
//			int pointCount = mGraph.getPoints().size();
//			if(pointCount==0){
//				return;
//			}
//			Point point = mGraph.getPoints().get(pointCount - 1);
//			int erasePanelX = (int) (point.x - mGraph.getStrokeWidth() / 2f);
//			int erasePanelY = (int) (point.y - mGraph.getStrokeWidth() / 2f);
//
//			if(!mHelperListener.erasePanelIsShowing()) {
//				int curPanelSize = WhiteBoardUtils.getCurEraseSize((int) WhiteBoardUtils.curStrokeWidth);
//				//mGraph.setStrokeWidth(curPanelSize);
//				erasePanelX = (int) (point.x - mGraph.getStrokeWidth() / 2f);
//				erasePanelY = (int) (point.y - mGraph.getStrokeWidth() / 2f);
//				mHelperListener.displayErasePanel(curPanelSize, curPanelSize, erasePanelX, erasePanelY);
//			}else{
//				mHelperListener.erasePanelMoveTo(erasePanelX,erasePanelY);
//			}
//		}
//		requestPaint(false);
//	}

	private void computeEraseSize(){
		int curPanelSize = WhiteBoardUtils.getCurEraseSize((int) WhiteBoardUtils.curStrokeWidth);
		float k = curPanelSize/16f;
		float erasePanelWidth = k*7f;
		float erasePanelHeight = k*9f;
		//mGraph.setStrokeWidth(curPanelSize);
		((Erase)mGraph).setEraseWidth((int)erasePanelWidth);
		((Erase)mGraph).setEraseHeight((int)erasePanelHeight);
	}


	@Override
	public void onDestory() {

	}

	@Override
	public void setDrawType(int drawType) {

	}

	private void requestPaint(boolean isComplete){
		if(mHelperListener!=null){
			EraseMsgState eraseMsgState = new EraseMsgState(mGraph,isComplete,true);
			mHelperListener.requestPaint(new MsgEntity(eraseMsgState));
		}
	}

}
