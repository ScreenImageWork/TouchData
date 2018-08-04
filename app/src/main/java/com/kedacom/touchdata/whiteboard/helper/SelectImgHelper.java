package com.kedacom.touchdata.whiteboard.helper;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.kedacom.touchdata.net.SendHelper;
import com.kedacom.touchdata.net.mtnet.MtConnectManager;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.SelectGraph;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.SelectGraphMsgState;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2017/6/21.
 *
 * 图片选择助手，图片选择分为两种模式，点击和圈选，
 *
 * 点击模式，一次最多只选择一张图片
 * 圈选模式，一次可选择多张图片
 *
 */
public class SelectImgHelper implements IHelper,SelectGraph.OnSelectGraphOpListener{

    private final static long CLICK_TIME_SPEED = 100;//如果一次触摸时间不超过该值，那么就可以判定为点击

    private SelectGraph mSelectGraph;

    private IHelperListener mIHelperListener;

    private  ArrayList<Graph> selectGraphs = new ArrayList<Graph>();

    private boolean hasTimer = false;

    public SelectImgHelper(Context context, IHelperListener listener){
        mIHelperListener = listener;
        mSelectGraph = new SelectGraph(context);
        mSelectGraph.setSelectImgHelper(this);
        mSelectGraph.setOnSelectGraphOpListener(this);
    }

    public void reset(){
        mSelectGraph.reset();
        selectGraphs.clear();
        mIHelperListener.getCurGraphList().remove(mSelectGraph);
    }

    public ArrayList<Graph> getSelectGraphList(){
        return selectGraphs;
    }

    public Rect getSelectGraphClipBounds(){
        if(selectGraphs.isEmpty()||selectGraphs.size()>1){
            return null;
        }
        if(selectGraphs.get(0).getGraphType()!=WhiteBoardUtils.GRAPH_IMAGE){
            return null;
        }
        return ((ImageGraph)selectGraphs.get(0)).getClipRectBounds();
    }

    /**
     * 指定选择的图片，单选
     * @param graph
     */
    public void selectImage(ImageGraph graph ){
        selectGraphs.clear();
        selectGraphs.add(graph);
        computeSeletGraphsBounds();
        mSelectGraph.setSelectComplete(true);
        mSelectGraph.setShowToolsBar(true);
        if(!mIHelperListener.getCurGraphList().contains(mSelectGraph)){
            mIHelperListener.getCurGraphList().add(mSelectGraph);
        }
        hasTimer = true;
    }

    private boolean continuePerform = true;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        boolean actionUp = (action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL);

        float x = event.getX();
        float y = event.getY();

        TPLog.printKeyStatus("SelectImgHelper->当前触摸的点为:("+x+","+y+")");

        if(mSelectGraph.isSelectComplete()){

            if(hasTimer){
                mIHelperListener.cancelSelectImageTimer();
            }

            continuePerform = mSelectGraph.onTouchEvent(event);

            if(!continuePerform){
                reset();
                requestPaint(true);
            }
            return continuePerform;
        }

        if(!continuePerform){
            if(actionUp){
                continuePerform = true;
            }
            return false;
        }

        if(action == MotionEvent.ACTION_DOWN){
            if(!mIHelperListener.getCurGraphList().contains(mSelectGraph)){
                mIHelperListener.getCurGraphList().add(mSelectGraph);
            }
        }

        mSelectGraph.addPoint(x,y);
        mSelectGraph.preSelectGraphs(mIHelperListener.getCurImageGraphList());

        if((action == MotionEvent.ACTION_UP )|| (action == MotionEvent.ACTION_CANCEL)){
            long downTime = event.getDownTime();
            long eventTime = event.getEventTime();

            if(selectGraphs.size() == 0){
            if((eventTime - downTime)<=CLICK_TIME_SPEED){//点击事件最多选择一张图片
                getSelectGraph(mIHelperListener.getCurImageGraphList(),x,y);
                if(selectGraphs.size() == 0) {
                    TPLog.printKeyStatus("没有选择到图片，开始重置");
                    reset();
                }else{
                    TPLog.printKeyStatus("选择到了图片。。。。。。。。。。。。。");
                    mSelectGraph.setSelectComplete(true);
                    mSelectGraph.setShowToolsBar(true);
                    computeSeletGraphsBounds();
                }
            }else {
                getSelectGraphs();
                if(selectGraphs.size() == 0) {
                    reset();
                    TPLog.printKeyStatus("没有选择到图片，开始重置");
                }else{
                    TPLog.printKeyStatus("选择到了图片。。。。。。。。。。。。。");
                    mSelectGraph.setSelectComplete(true);
                    if(selectGraphs.size() == 1){
                        mSelectGraph.setShowToolsBar(true);
                    }
                    computeSeletGraphsBounds();
                }
            }
            }
            requestPaint(true);
        }else{
            requestPaint(mSelectGraph.isSelectComplete());
        }
     //  requestPaint(mSelectGraph.isSelectComplete());
        return true;
    }


    public void getSelectGraphs(){
        mSelectGraph.changeCoordinate(mIHelperListener.getCurPage().getMatrix(),mIHelperListener.getCurScale());
        selectGraphs.clear();
        mSelectGraph.getSelectGraphs(selectGraphs, mIHelperListener.getCurImageGraphList());

        int selectCount = selectGraphs.size();
        for(int i = selectCount-1;i>=0;i--){
            Graph graph = selectGraphs.get(i);
            ((ImageGraph)graph).setSelecting(false);
            mIHelperListener.getCurImageGraphList().remove(graph);
            mIHelperListener.getCurImageGraphList().add(graph);
        }
    }


    private void getSelectGraph(ArrayList<Graph> sourceGraphs,float x,float y){
        TPLog.printKeyStatus("选择图片点击点为:（"+x+","+y+"）");
//        Matrix matrix = mIHelperListener.getCurPage().getMatrix();
//        Matrix invert = new Matrix();
//        matrix.invert(invert);

        float sourcePoints[] = new float[2];
        sourcePoints[0] = x;
        sourcePoints[1] = y;

        float dstPoints[] = sourcePoints;

        TPLog.printKeyStatus("通过matrix对点:（"+x+","+y+"）处理后结果:（"+dstPoints[0]+","+dstPoints[1]+"）");

        int graphCount = sourceGraphs.size();
        TPLog.printKeyStatus("当前图元个数："+graphCount);

        for(int i = graphCount -1;i>=0;i--){
            Graph graph = sourceGraphs.get(i);
            if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
                Rect bounds = ((ImageGraph)graph).getBounds();
                TPLog.printKeyStatus("图片"+i+"的边界信息："+bounds);
                if(bounds.contains((int) dstPoints[0],(int) dstPoints[1])){
                    selectGraphs.add(graph);
                    //将该图元移动到底部，这样绘制的时候就会绘制在最上层
                    sourceGraphs.remove(graph);
                    sourceGraphs.add(graph);
                    return;
                }
            }
        }
    }

    public void  computeSeletGraphsBounds(){
        int selectCount = selectGraphs.size();
        if(selectCount == 0){
            return;
        }

        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;

        int maxX = Integer.MIN_VALUE;
        int maxY = Integer.MIN_VALUE;
        for(int i = 0;i<selectCount;i++){
            Graph graph = selectGraphs.get(i);
            Rect bounds = null;
            if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE) {
                bounds = ((ImageGraph)graph).getBounds();
//                RectF dstRect = new RectF();
//                ((ImageGraph)graph).getMatrix().mapRect(dstRect,new RectF(tempbounds.left,tempbounds.top,tempbounds.right,tempbounds.bottom));
//                bounds = new Rect();
//                dstRect.roundOut(bounds);
            }else{
                bounds = graph.getBounds();
            }
            if(minX>bounds.left){
                minX = bounds.left;
            }
            if(minY>bounds.top){
                minY = bounds.top;
            }
            if(maxX<bounds.right){
                maxX = bounds.right;
            }
            if(maxY<bounds.bottom){
                maxY = bounds.bottom;
            }
        }

        int width = Math.abs(maxX - minX);
        int height = Math.abs(maxY - minY);

        mSelectGraph.setX(minX);
        mSelectGraph.setY(minY);
        mSelectGraph.setWidth(width);
        mSelectGraph.setHeight(height);
        mSelectGraph.computeBounds();

        Rect rect = mSelectGraph.getBounds();
        TPLog.printKeyStatus("当前计算的操作区域："+rect);
    }

    private void requestPaint(boolean complete){
        TPLog.printKeyStatus("requestPaint------------》complete="+complete);
        MsgEntity msg = new MsgEntity();
        SelectGraphMsgState sgms = new SelectGraphMsgState();
        sgms.setmSelectGraph(mSelectGraph);
        sgms.setComplete(true);
        msg.setCurState(sgms);
        mIHelperListener.requestPaint(msg);
    }


    @Override
    public void onDestory() {

    }

    @Override
    public void onPostScale(float sx, float sy, float px, float py) {
        int selectCount = selectGraphs.size();
        for(int i = 0;i<selectCount;i++){
            ((ImageGraph)selectGraphs.get(i)).postScale(sx,sy,px,py);
        }
        computeSeletGraphsBounds();
        requestPaint(true);
        //发送同步消息
        long tabId = mIHelperListener.getCurPage().getId();
        int subPageIndex = mIHelperListener.getCurPage().getCurSubPageIndex() - 1;

        if(!NetUtil.isRemoteConf) {
            SendHelper.getInstance().sendSelectGraphChanged(selectGraphs, tabId, subPageIndex);
        }else{
            MtConnectManager.getInstance().getMtNetSender().synSelectImgCoordinateChanged(MtNetUtils.achConfE164,mIHelperListener.getCurPage().getRemotePageId(),(mIHelperListener.getCurPage().getCurSubPageIndex() - 1),selectGraphs);
        }
        }

    @Override
    public void onPostMove(float ox, float oy) {
        int selectCount = selectGraphs.size();
        for(int i = 0;i<selectCount;i++){
            ((ImageGraph)selectGraphs.get(i)).postTranslate(ox,oy);
        }
        computeSeletGraphsBounds();
        requestPaint(true);

        //发送同步消息
        long tabId = mIHelperListener.getCurPage().getId();
        int subPageIndex = mIHelperListener.getCurPage().getCurSubPageIndex() - 1;
        SendHelper.getInstance().sendSelectGraphChanged(selectGraphs,tabId,subPageIndex);

        if(!NetUtil.isRemoteConf){
            ((SubPage) mIHelperListener.getCurPage().getCurSubPage()).debug();
        }else{
            MtConnectManager.getInstance().getMtNetSender().synSelectImgCoordinateChanged(MtNetUtils.achConfE164,mIHelperListener.getCurPage().getRemotePageId(),(mIHelperListener.getCurPage().getCurSubPageIndex() - 1),selectGraphs);
        }

    }

    @Override
    public void onPostRotate(int angle, float px, float py) {
        int selectImgCount = selectGraphs.size();
        for(int i = 0;i<selectImgCount;i++){
            ((ImageGraph)selectGraphs.get(i)).postRotate(angle,px,py);
        }
        computeSeletGraphsBounds();
        requestPaint(true);

        //发送同步消息
        long tabId = mIHelperListener.getCurPage().getId();
        int subPageIndex = mIHelperListener.getCurPage().getCurSubPageIndex() - 1;

        if(!NetUtil.isRemoteConf) {
            SendHelper.getInstance().sendSelectGraphChanged(selectGraphs, tabId, subPageIndex);
        }else{
            MtConnectManager.getInstance().getMtNetSender().synSelectImgCoordinateChanged(MtNetUtils.achConfE164,mIHelperListener.getCurPage().getRemotePageId(),(mIHelperListener.getCurPage().getCurSubPageIndex() - 1),selectGraphs);
        }
    }

    @Override
    public void onClipImgStart() {
        Graph graph = selectGraphs.get(0);
        if(graph!=null){
            ((ImageGraph)graph).setCliping(true);
            computeSeletGraphsBounds();
        }
       onRefreshUI();
    }

    @Override
    public void onClipImgComplete(Rect clipRect) {
        Graph graph = selectGraphs.get(0);
        if(graph!=null){
            ((ImageGraph)graph).setCliping(false);
            ((ImageGraph)graph).setClipRect(clipRect);
        }
        computeSeletGraphsBounds();
        onRefreshUI();
    }

    @Override
    public void onClipImgCancel() {
        Graph graph = selectGraphs.get(0);
        if(graph!=null){
            ((ImageGraph)graph).setCliping(false);
        }
        computeSeletGraphsBounds();
        onRefreshUI();
    }

    @Override
    public void onDeleteImg() {
        int selectImgCount = selectGraphs.size();
        int id = selectGraphs.get(0).getId();
        String remoteId = selectGraphs.get(0).getRemoteId();
        for(int i = 0;i<selectImgCount;i++){
            mIHelperListener.getCurImageGraphList().remove(selectGraphs.get(i));
            selectGraphs.get(i).destroy();
        }
        reset();
        requestPaint(true);

        mIHelperListener.onDelSelectImg(id);

        //发送同步消息
        long tabId = mIHelperListener.getCurPage().getId();
        int subPageIndex = mIHelperListener.getCurPage().getCurSubPageIndex() - 1;
        if(!NetUtil.isRemoteConf) {
            SendHelper.getInstance().sendDeleteGraph(id, tabId, subPageIndex);
        }else{
            MtConnectManager.getInstance().getMtNetSender().synDelImg(MtNetUtils.achConfE164,mIHelperListener.getCurPage().getRemotePageId(),(mIHelperListener.getCurPage().getCurSubPageIndex() - 1),remoteId);
        }
    }

    @Override
    public void onRefreshUI() {
        requestPaint(true);
    }
}
