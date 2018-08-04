package com.kedacom.touchdata.whiteboard.helper;

import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.AreaEraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class DrawAreaEraseHelper implements IHelper{

    private int curDrawType = WhiteBoardUtils.GRAPH_ERASE_AREA;

    private Graph mGraph;

    private IHelperListener mHelperListener;

    public DrawAreaEraseHelper(IHelperListener listener){
        mHelperListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (0xFF & event.getAction()) {
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
        mGraph = GraphFactory.makeGraph(curDrawType);
        float x = event.getX();
        float y = event.getY();
        mGraph.addPoint(x, y);

    }

    private void touchMove(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        mGraph.addPoint(x, y);
        requestPaint(false);
    }

    private void touchUp(MotionEvent event){
        if(curDrawType == WhiteBoardUtils.GRAPH_ERASE_AREA){
            ((AreaErase)mGraph).commitErase();
        }
        requestPaint(true);
    }


    @Override
    public void onDestory() {

    }

    private void requestPaint(boolean isComplete){
        if(mHelperListener!=null){
            AreaEraseMsgState eraseState = new AreaEraseMsgState(mGraph,isComplete);
            mHelperListener.requestPaint(new MsgEntity(eraseState));
        }
    }
}
