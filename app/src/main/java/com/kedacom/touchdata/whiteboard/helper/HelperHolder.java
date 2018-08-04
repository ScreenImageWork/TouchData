package com.kedacom.touchdata.whiteboard.helper;

import java.util.ArrayList;

import android.content.Context;
import android.view.MotionEvent;

import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintAndSaveGraphState;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleAndTranslateMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.ScaleMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.TranslateMsgState;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.op.RotateOperation;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.utils.CheckGestureUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.view.DisplayTouchView;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;
import com.touch.touchsdk.HuaXinSdkMng;
import com.touch.touchsdk.TouchEvent;
import com.touch.touchsdk.TouchInterface;

public class HelperHolder implements IHelperHolder,TouchInterface.OnTouchCallBack{

	public static final int TYPE_TOUCH_GESTURE = 0;

	public static final int TYPE_TOUCH_PAINT = 1;

	public static final int TYPE_TOUCH_UNKNOWN = -1;

	private int curTouchType = TYPE_TOUCH_UNKNOWN;

	private IHelperListener mIHelperListener;
	private IHelper curHelper;
	private DrawHelper mDrawHelper;
	private NewGestureHelper mGestureHelper;
	private SelectImgHelper mSelectImgHelper;

	private DisplayTouchView mDisplayTouchView;

	private boolean gestureErase = false;

	//触摸事件触发后锁定时间  防止误操作
	private long touchLockTime = 100;

	private boolean touchIsLock = false;

	public HelperHolder(Context context,IHelperListener listener){
		mIHelperListener = listener;
		initHelper(context);
//		HuaXinSdkMng.setCallback(this);
	}

	private void initHelper(Context context){
		mDrawHelper = new DrawHelper(mIHelperListener);
		mGestureHelper = new NewGestureHelper(context,mIHelperListener);
		mSelectImgHelper = new SelectImgHelper(context,mIHelperListener);
	}

	@Override
	public void init(float scale, int angle, float offsetX, float offsetY) {
		if(mGestureHelper==null)return;
		mGestureHelper.setCurAngle(angle);
		mGestureHelper.setCurScale(scale);
		mGestureHelper.setCurTranslate(offsetX,offsetY);
	}

	@Override
	public void setDisplayTouchView(DisplayTouchView dtv) {
		mDisplayTouchView = dtv;
	}

	public void lockWb(boolean lock){
		if(mIHelperListener!=null){
			mIHelperListener.lock(lock);
		}
	}

	public boolean isLockWb(){
		if(mIHelperListener==null){
			return false;
		}
		return mIHelperListener.isLock();
	}

	@Override
	public void setOpType(int type) {
		if(WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_SELECT_IMG){
			if(WhiteBoardUtils.OP_SELECT_IMG!=type){
				mSelectImgHelper.reset();
				mSelectImgHelper.onRefreshUI();
			}
		}
		WhiteBoardUtils.curOpType = type;
		int tempType = WhiteBoardUtils.GRAPH_PEN;
		switch(type){
			case WhiteBoardUtils.OP_PAINT:
				tempType = WhiteBoardUtils.GRAPH_PEN;
				break;
			case WhiteBoardUtils.OP_ERASE:
				tempType = WhiteBoardUtils.GRAPH_ERASE;
				break;
			case WhiteBoardUtils.OP_ERASE_AREA:
				tempType = WhiteBoardUtils.GRAPH_ERASE_AREA;
				break;
			default:
				tempType = WhiteBoardUtils.GRAPH_UNKNOWN;
				break;
		}
		mDrawHelper.setDrawType(tempType);
	}

	@Override
	public int getCurOpType() {
		return WhiteBoardUtils.curOpType;
	}

	@Override
	public void setPaintColor(int color) {
	    WhiteBoardUtils.curColor = color;
	}

	@Override
	public void setPaintStrokeWidth(float width) {
		WhiteBoardUtils.curStrokeWidth = width;
	}

	@Override
	public int getPaintColor() {
		return  WhiteBoardUtils.curColor;
	}

	@Override
	public float getPaintStrokeWidth() {
		return WhiteBoardUtils.curStrokeWidth;
	}

	@Override
	public void setDrawEnable(boolean enable) {
		mIHelperListener.setDrawEnable(enable);
	}

	@Override
	public void undo() {
		if(mIHelperListener!=null){
			IOperation op = mIHelperListener.undo(false);
			if(op!=null&&op.getType() == IOperation.OPT_ROTATE){
				RotateOperation rop= (RotateOperation)op;
				int curAngle = rop.getCurAngle();
				mGestureHelper.setCurAngle(curAngle);
			}
		}
	}

	@Override
	public void redo() {
		if(mIHelperListener!=null){
			IOperation op = mIHelperListener.redo(false);
			if(op!=null&&op.getType() == IOperation.OPT_ROTATE){
				RotateOperation rop= (RotateOperation)op;
				int curAngle = rop.getOldAngle();
				mGestureHelper.setCurAngle(curAngle);
			}
		}
	}


	@Override
	public void rotate(int angle,boolean isFinish) {
		mGestureHelper.setCurAngle(angle);
		if(mIHelperListener!=null){
			mIHelperListener.onRotate(angle, isFinish,false);
		}
	}

	@Override
	public void scale(float scale) {
		mGestureHelper.setCurScale(scale);
		if(mIHelperListener!=null){
			mIHelperListener.onScale(scale,false);
		}
	}

	@Override
	public void postScale(float scaleFactor, int focusX, int focusY) {
		if(mIHelperListener!=null){
			ScaleMsgState scaleMsgState = new ScaleMsgState(scaleFactor,focusX,focusY,false,false);
			TranslateMsgState translateMsgEntity = new TranslateMsgState(0,0,false,false);
			ScaleAndTranslateMsgState sah = new ScaleAndTranslateMsgState(scaleMsgState,translateMsgEntity);
			sah.setScalExtremity(false);
			mIHelperListener.requestPaint(new MsgEntity(sah));
		}
	}

	@Override
	public void translate(float ox, float oy) {
		mGestureHelper.setCurTranslate(ox, oy);
		if(mIHelperListener!=null){
			mIHelperListener.onTranslate(ox, oy,false,false);
		}
	}

	@Override
	public void postTranslate(float ox, float oy) {
		if(mIHelperListener!=null)
			if(mIHelperListener!=null){
				ScaleMsgState scaleMsgState = new ScaleMsgState(1.0f,0,0,false,false);
				TranslateMsgState translateMsgEntity = new TranslateMsgState(ox,oy,false,false);
				ScaleAndTranslateMsgState sah = new ScaleAndTranslateMsgState(scaleMsgState,translateMsgEntity);
				sah.setScalExtremity(false);
				mIHelperListener.requestPaint(new MsgEntity(sah));
			}
	}

	public void transform(float scale,int angle,float ox, float oy){
//		mGestureHelper.setCurAngle(angle);
//		mGestureHelper.setCurScale(scale);
//		mGestureHelper.setCurTranslate(ox, oy);
		if(mIHelperListener!=null)
			mIHelperListener.onTransform(scale,angle,ox, oy);
	}

	@Override
	public void postTransform(float scale, float spx, float spy, int angle, float ox, float oy) {
		if(mIHelperListener!=null)
			mIHelperListener.onPostTransform(scale,spx,spy,angle,ox, oy);
	}

	@Override
	public void clearScreen() {
		if(mIHelperListener!=null){
			mIHelperListener.clearScreen(false);
		}
	}

	@Override
	public void requestDrawGraph(Graph graph) {
		if(mIHelperListener!=null){
			long graphId = graph.getId();
			ArrayList<Graph> list = mIHelperListener.getCurGraphList();
			if(list==null){
				TPLog.printError("Mt requestDrawGraph--->list = "+list);
				return;
			}
			int count = list.size();
			TPLog.printError("Mt requestDrawGraph--->count = "+count);
			TPLog.printError("Mt requestDrawGraph--->NetUtil.isRemoteConf = "+NetUtil.isRemoteConf);
			for(int i = 0; i<count;i++){
				if(NetUtil.isRemoteConf){
					if(graph.getRemoteId().equals(list.get(i).getRemoteId())){
						TPLog.printError("Mt requestDrawGraph---> 图元已经存在！");
						return;
					}
				}else{
					if(list.get(i).getId() == graphId){
						TPLog.printError("Mt requestDrawGraph---> 图元已经存在！");
						return;
					}
				}
			}
			TPLog.printError("Mt requestDrawGraph---> 开始保存和渲染图元。。。");
			mIHelperListener.requestDrawGraphAndSave(graph);
		}
	}

	/**
	 * 入会同步时调用，主要是为了约束消息顺序，全部放到同一个线程内处理，已达到同步的效果
	 * @param graph
	 * @param pageManager
	 */
	@Override
	public void requestDrawGraphForSyn(Graph graph, PageManager pageManager) {
		TPLog.printKeyStatus("NewTouchData  requestDrawGraphForSyn  begin...");
			long graphId = graph.getId();
			ArrayList<Graph> list = mIHelperListener.getCurGraphList();
			if(list==null){
				TPLog.printError("NewTouchData requestDrawGraph--->list = "+list);
				return;
			}
			int count = list.size();
			TPLog.printError("NewTouchData requestDrawGraph--->count = "+count);
			TPLog.printError("NewTouchData requestDrawGraph--->NetUtil.isRemoteConf = "+NetUtil.isRemoteConf);
			for(int i = 0; i<count;i++){
				if(NetUtil.isRemoteConf){
					if(graph.getRemoteId().equals(list.get(i).getRemoteId())){
						TPLog.printError("NewTouchData requestDrawGraph---> 图元已经存在！");
						return;
					}
				}else{
					if(list.get(i).getId() == graphId){
						TPLog.printError("NewTouchData requestDrawGraph---> 图元已经存在！");
						return;
					}
				}
			}

		TPLog.printKeyStatus("处理同步到的图元~~");

		IPage page = null;
		TPLog.printError("NewTouchData NetUtil.isRemoteConf = "+NetUtil.isRemoteConf+",RemotePageId="+graph.getRemotePageId());
		if(NetUtil.isRemoteConf){
			page = pageManager.getPageFromRemotePageId(graph.getRemotePageId());
		}else{
//				page = pageManager.getPageFromId(graph.getTabId());
			page = pageManager.getSelectPage();
		}

		if(page!=null){
			TPLog.printError("NewTouchData page != null,1graphs count:"+page.getCurSubPage().getGraphList().size());
			page.getCurSubPage().addGraph(graph);
			TPLog.printError("NewTouchData page != null,2graphs count:"+page.getCurSubPage().getGraphList().size());
		}else{
			TPLog.printError("NewTouchData page == null");
		}
		TPLog.printKeyStatus("NewTouchData  requestDrawGraphForSyn  end...");
	}

	/**
	 * 兼容终端区域擦除
	 * @param areaErase
	 */
	public void compatibilityMtAreaErase(AreaErase areaErase){
		mIHelperListener.compatibilityMtAreaErase(areaErase);
	}

	@Override
	public void refreshScreen() {
		if(mIHelperListener!=null){
			mIHelperListener.refreshScreen();
		}
	}

	@Override
	public void setBackgroundColor(int color) {
		WhiteBoardUtils.curBackground = color;
	}

	@Override
	public void widthSelf() {
		if(mIHelperListener!=null){
			mIHelperListener.widthSelf();
		}
	}

	@Override
	public void heightSelf() {
		if(mIHelperListener!=null){
			mIHelperListener.heightSelf();
		}
	}

	@Override
	public void selfAdaption() {
		if(mIHelperListener!=null){
			mIHelperListener.selfAdaption();
		}
	}

	@Override
	public void oneToOne() {
		if(mIHelperListener!=null){
			mIHelperListener.oneToOne();
		}
	}

	@Override
	public void selectImage(ImageGraph graph) {
		mSelectImgHelper.selectImage(graph);
	}

	public void resetSelectImage(){
		mSelectImgHelper.reset();
	}

	@Override
	public void updateGestureCurScale(float scale) {
		mGestureHelper.setCurScale(scale);
	}

	private boolean paintBrushPen = false;

	@Override
	public void touchCallBack(TouchEvent event) {
//		TPLog.printError("触控区域检测------touchCallBack--"+TouchEvent.actionToString(event.getAction())+"------event.getSize()="+event.getWidth()*event.getHeight()+",paintBrushPen = "+paintBrushPen);
//         if(event.getAction() == TouchEvent.ACTION_DOWN) {
//			 if (mDrawHelper != null && (event.getHeight() * event.getWidth()) < 500000.0) {
//				 paintBrushPen = true;
//			 }
//		 }
//		 if(paintBrushPen)
//			mDrawHelper.onTouchEvent(event);
//
//
//		if(event.getAction() == TouchEvent.ACTION_UP){
//			paintBrushPen = false;
//		}
	}

	private boolean paintBrushPen2 = false;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
//		TPLog.printError("触控区域检测------onTouchEvent----"+MotionEvent.actionToString(event.getAction())+"----event.getSize()="+event.getSize()+",paintBrushPen2 = "+paintBrushPen2);

//		if(paintBrushPen2){
//			if(event.getAction() == MotionEvent.ACTION_UP){
//				paintBrushPen2 = false;
//				mDrawHelper.setDrawType(WhiteBoardUtils.GRAPH_PEN);
//			}
//			return true;
//		}

		//用触控面积来判断当前是智能笔还是手指或者是其他设备，目前没有找到其他可行的办法
//		if(event.getAction() == MotionEvent.ACTION_DOWN){
//			if(VersionUtils.isImix()){//当前是IMIX
//				if((event.getSize()<=0.002)){//如果触控面积小于该值那么就判定为智能笔\
//					if((WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_PAINT)){
//						mDrawHelper.setDrawType(WhiteBoardUtils.GRAPH_BRUSHPEN);
//						paintBrushPen2 = true;
//						return true;md
//					}
//				}
//			}
//		}

		if(mDisplayTouchView!=null&& VersionUtils.isImix()&&(curTouchType==TYPE_TOUCH_GESTURE)&&!gestureErase){
			mDisplayTouchView.touch(event);
		}

		//当前操作类型是拖动
		if(WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_DRAG){
			if(mGestureHelper!=null){
				mGestureHelper.drag(event);
			}
			return true;
		}

		if (WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_SELECT_IMG) {
				if (mSelectImgHelper != null)
					mSelectImgHelper.onTouchEvent(event);
				return true;
		}

		int action = event.getActionMasked();
//		String actionStr = Utils.touchActionToString(action);
		//TPLog.printKeyStatus("HelperHolder--->"+actionStr);

		if(action == MotionEvent.ACTION_DOWN){ //在刚按下屏幕的时候就检测下是否执行手势擦除
			if(VersionUtils.isImix()) {
				TPLog.printKeyStatus("当前设备为IMIX...");
				if (mGestureHelper.checkGestureErase(event)) {
					touchIsLock = true;
					gestureErase = true;
					curTouchType = TYPE_TOUCH_GESTURE;
					curHelper = mGestureHelper;
					return true;
				}
			}
			mGestureHelper.lock();
		}

		long touchRunMillis = event.getEventTime() - event.getDownTime() ;
		if(touchRunMillis>=touchLockTime&&!touchIsLock){
			TPLog.printKeyStatus("触摸执行时间："+touchRunMillis+">="+touchLockTime+"，触摸事件锁定！");
			touchIsLock = true;
			if(CheckGestureUtils.checkGesture(event)){
				TPLog.printKeyStatus("当前触摸操作选择手势操作。。。");
				curTouchType = TYPE_TOUCH_GESTURE;
				curHelper = mGestureHelper;
				mGestureHelper.unlock();
			}else if(curTouchType == TYPE_TOUCH_PAINT){
				mDrawHelper.setLockDraw(false);
			}
		}


		//单点时就是绘图操作，多点时就是手势
		if (action == MotionEvent.ACTION_DOWN&&curHelper==null) {
			TPLog.printKeyStatus("当前触摸操作选择铅笔绘图。。。");
			curTouchType = TYPE_TOUCH_PAINT;
			curHelper = mDrawHelper;
			mDrawHelper.setLockDraw(true);
		} else if (action == MotionEvent.ACTION_POINTER_DOWN&&!touchIsLock) {
			CheckGestureUtils.preCheckGesture(event);
		}

		//Android与华欣SDK衔接关键代码，华欣只用于IMIX上面，因此PAD依然只使用Android触摸事件
//		if(VersionUtils.isImix()) {
//			if (curTouchType == TYPE_TOUCH_PAINT && !touchIsLock) {
//				curHelper.onTouchEvent(event);
//			} else if (curTouchType != TYPE_TOUCH_PAINT) {
//				curHelper.onTouchEvent(event);
//			}
//		}else{//非IMIX设备直接忽略华欣SDK使用
//			curHelper.onTouchEvent(event);
//		}
        if(curHelper!=null)
			curHelper.onTouchEvent(event);

		if (action == MotionEvent.ACTION_UP
				|| action == MotionEvent.ACTION_CANCEL) {
			if (curHelper != null) {
				curHelper = null;
			}
			touchIsLock = false;
			gestureErase = false;
			curTouchType = TYPE_TOUCH_UNKNOWN;
		}

		if(curTouchType == TYPE_TOUCH_PAINT&&!touchIsLock){
			mGestureHelper.onTouchEvent(event);
		}

		return true;
	}

	public void dismissErasePanelWindow(){
		mIHelperListener.dismissErasePanelWindow();
	}

	@Override
	public void onDestory() {
		mDrawHelper.onDestory();
		mGestureHelper.onDestory();
		mDrawHelper = null;
		mGestureHelper = null;
	}
}
