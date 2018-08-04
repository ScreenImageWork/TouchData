package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Path;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.EraseMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2017/6/15.
 */
public class GestrueEraseHelper implements IGestrueHelper{

    private static final float GESTRUEERASE_MIN_SIZE = 0.015f;

    private Graph mGraph;

    private IHelperListener mHelperListener;

    private int erasePanelWidth = 0;

    private int erasePanelHeight = 0;

    public GestrueEraseHelper(IHelperListener listener){
        mHelperListener = listener;
    }

    @Override
    public void complete() {
//        if(mHelperListener!=null&&mGraph!=null) {
//            requestPaint(true);
//            mHelperListener.dismissErasePanelWindow();
//        }
//        mGraph = null;
    }

    //检测是否执行手势擦除
    public boolean checkPerform(MotionEvent event){

        float size = event.getSize();
        TPLog.printKeyStatus("当前触摸区域大小："+size);
        if(size>=GESTRUEERASE_MIN_SIZE){
            TPLog.printKeyStatus("当前手势检测为手势擦除。。。");
            float screenWidth = WhiteBoardUtils.screenWidth;
            float screenHeight = WhiteBoardUtils.screenHeight;

            float touchArea = screenWidth*screenHeight*size;

            float touchAreaSideLength = (float)Math.sqrt(touchArea);

            float touchAreaSideLength16_1 = touchAreaSideLength/16f;

            erasePanelWidth = (int) (touchAreaSideLength16_1*7f);

            erasePanelHeight = (int) (touchAreaSideLength16_1*9f);

            float x = event.getX();
            float y = event.getY();

            initErase((int)x,(int)y);
            return true;
        }

        return false;
    }

    float focusX = Integer.MAX_VALUE;
    float focusY = Integer.MAX_VALUE;

    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP?true:false;

        int pointCount = event.getPointerCount();

        int div = (pointerUp?pointCount-1:pointCount);

        int skipIndex = pointerUp?event.getActionIndex():-1;

        float sumX = 0;

        float sumY = 0;

        for(int i = 0;i<pointCount;i++){
            if(skipIndex == i)continue;
            if(focusX!=Integer.MAX_VALUE&&focusY !=Integer.MAX_VALUE){
                double length = GraphUtils.computeSegmentLength(focusX,focusY, event.getX(i), event.getY(i));
                TPLog.printKeyStatus("length =============================="+length+",div="+div);
                if(length>110){
                    div--;
                    continue;
                }
            }

            sumX += event.getX(i);
            sumY += event.getY(i);
        }

        focusX = sumX/div;
        focusY = sumY/div;


        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL){
            completErase((int)focusX,(int)focusY);
            focusX = Integer.MAX_VALUE;
            focusY = Integer.MAX_VALUE;
        }else{
            continueErase((int)focusX,(int)focusY);
        }

        return true;
    }

    private void initErase(int x,int y){
        mGraph = GraphFactory.makeGraph(WhiteBoardUtils.GRAPH_ERASE);
        ((Erase)mGraph).setEraseWidth(erasePanelWidth);
        ((Erase)mGraph).setEraseHeight(erasePanelHeight);
        mGraph.addPoint(x,y);
        mGraph.setStrokeWidth(erasePanelWidth);
        x = x - (int)(erasePanelWidth/2f);
        y = y - (int)(erasePanelHeight/2f);
        displayErasePanel(erasePanelWidth, erasePanelHeight, x, y);
        requestPaint(false);
        TPLog.printKeyStatus("显示擦除面板。。。");
    }

    private void continueErase(int erasePanelX,int erasePanelY){
        mGraph.addPoint(erasePanelX,erasePanelY);
        erasePanelX = erasePanelX -(int) (erasePanelWidth/2f);
        erasePanelY = erasePanelY -(int)(erasePanelHeight/2f);
        moveErasePanel(erasePanelX,erasePanelY);
        requestPaint(false);
    }

    private void completErase(int erasePanelX,int erasePanelY){
        TPLog.printKeyStatus("手势擦除执行完毕。。。。");
        mGraph.addPoint(erasePanelX,erasePanelY);
        hiddenErasePanel();
        requestPaint(true);
    }

    private  void displayErasePanel(int erasePanelWidth,int erasePanelHeight,int erasePanelX,int erasePanelY){
        if(mHelperListener!=null)
            mHelperListener.displayErasePanel(erasePanelWidth,erasePanelHeight,erasePanelX,erasePanelY);
    }

    private void moveErasePanel(int erasePanelX,int erasePanelY){
        if(mHelperListener!=null)
            mHelperListener.erasePanelMoveTo(erasePanelX,erasePanelY);
    }

    private void hiddenErasePanel(){
        TPLog.printKeyStatus("隐藏擦除面板。。。");
        if(mHelperListener!=null)
            mHelperListener.dismissErasePanelWindow();
    }

    private void requestPaint(boolean isComplete){
        if(mHelperListener==null)return;
        EraseMsgState eraseMsgState = new EraseMsgState(mGraph,isComplete,true);
        mHelperListener.requestPaint(new MsgEntity(eraseMsgState));
    }

    @Override
    public void onDestory() {
        mGraph = null;
    }
}