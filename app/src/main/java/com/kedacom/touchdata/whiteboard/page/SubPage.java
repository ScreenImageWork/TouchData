package com.kedacom.touchdata.whiteboard.page;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.op.ClearScreenOperation;
import com.kedacom.touchdata.whiteboard.op.GraphOperation;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.op.MtAreaEraseOperation;
import com.kedacom.touchdata.whiteboard.op.RotateOperation;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.IUndoAndRedoListener;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils;
import com.kedacom.touchdata.whiteboard.utils.UndoAndRedoManager;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils.ISavePageCallBack;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

import junit.runner.Version;

/**
 * 当前文档图片不再这里进行绘制
 * @author zhanglei
 *
 */
public class SubPage implements ISubPage{

	private int owerIndex = 0;
	
	private int progress;
	
    private int curAngle = 0;
    
    private int tempAngle = 0;
	
	private float offsetX;
	
	private float offsetY;
	
	private ArrayList<Graph> graphs = new ArrayList<Graph>();

	private ArrayList<Graph> imageList = new ArrayList<Graph>();
	
	private Image mImage; // 文档图片，每个子页只有一张
	
	private UndoAndRedoManager uarManager = new UndoAndRedoManager();
	
	private SavePageUtils mSavePageUtils = SavePageUtils.getInstence();
	
	private boolean isNeedSave = false;

	private int background;

	private float scaleFocusX;

	private float scaleFocusY;

	private Matrix matrix = new Matrix();

	private float matrixValues[] = new float[9];

	public SubPage(){

	}
	
	public SubPage(Image mImage){
		this.mImage = mImage;
	}
	
	public SubPage(int index){
		owerIndex = index;
	}
	

	public SubPage(Parcel parcel){
		owerIndex = parcel.readInt();
		progress = parcel.readInt();
		curAngle = parcel.readInt();
		tempAngle = parcel.readInt();
		float curZoom = parcel.readFloat();
		offsetX = parcel.readFloat();
		offsetY = parcel.readFloat();
		Parcelable graphArray[] = parcel.readParcelableArray(Graph.class.getClassLoader());
		for(int i = 0;i<graphArray.length;i++){
			graphs.add((Graph)graphArray[i]);
		}
		
	   // Log.e("error", "解析到的图元个数是："+graphs.size());
		
		Parcelable undoArray[] = parcel.readParcelableArray(IOperation.class.getClassLoader());
		for(int i = 0;i<undoArray.length;i++){
			uarManager.addToUndo((IOperation)undoArray[i]);
		}
		
		Parcelable redoArray[] = parcel.readParcelableArray(IOperation.class.getClassLoader());
		for(int i = 0;i<redoArray.length;i++){
			uarManager.addToRedo((IOperation)redoArray[i]);
		}
		
		boolean boo[] = new boolean[1];
		parcel.readBooleanArray(boo);
		
		isNeedSave = boo[0];
		mImage = (Image)parcel.readParcelable(Image.class.getClassLoader());
	}

	boolean hasGraphs(){
		if(graphs.isEmpty()&&imageList.isEmpty()){
			return false;
		}
		return true;
	}

	public void setIndex(int index){
		owerIndex = index;
	}
	

	@Override
	public boolean isNeedSave() {
		return isNeedSave;
	}

	@Override
	public boolean isEmpty() {
		if(graphs.isEmpty()&&imageList.isEmpty()){
			return true;
		}
		return false;
	}


	@Override
	@Deprecated
	public void setImage(Image image) {
		this.mImage = image;
		if(mImage!=null){
			isNeedSave = true;
		}
	}

	@Override
	public void setBackgroundColor(int color) {
		background = color;
	}

	@Override
	public int getBackgroundColor() {
		return background;
	}

	void scale(float scale){
		float scaleFactor = scale / getScale();
		postScale(scaleFactor,WhiteBoardUtils.whiteBoardCenterX,WhiteBoardUtils.whiteBoardCenterY);
	}
/*
	void postScale(float scale,float focusX,float focusY){
		float curZoom =  getScale();
		TPLog.printKeyStatus("SubPage-postScale","postScale->curZoom="+curZoom+",ScaleFactor="+scale);
		Matrix tempMatrix = new Matrix();
		tempMatrix.set(matrix);
		if(curZoom>=2.9997f&&scale>0.997f){
			matrixValues[0] = 3.0f;
			matrixValues[4] = 3.0f;
			matrix.setValues(matrixValues);
			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				((ImageGraph)imageList.get(i)).reset(tempMatrix,matrix);
			}
		}else if(curZoom<=0.5005f&&scale<1f){
			matrixValues[0] = 0.5f;
			matrixValues[4] = 0.5f;
			matrix.setValues(matrixValues);
			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				((ImageGraph)imageList.get(i)).reset(tempMatrix,matrix);
			}
		}else {
			curZoom = curZoom * scale;
			if (curZoom > 3.0f) {
				scale = 3.0f / curZoom;
			}

			if (curZoom < 0.5) {
				scale = 0.5f / curZoom;
			}

			TPLog.printKeyStatus("postScale->scale="+scale+",focusX="+focusX+",focusY="+focusY);
			matrix.postScale(scale, scale, focusX, focusY);

			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				//((ImageGraph)imageList.get(i)).overallSituationScale(scale,focusX,focusY);
				((ImageGraph)imageList.get(i)).postScale(scale, scale, focusX, focusY);
			}
		}

		tempMatrix = null;
	}*/

	void postScale(float scale,float focusX,float focusY){
		/*
		float curZoom =  getScale();
		//已经放到最大了，就不用再放大了
		if(curZoom>3.0f&&scale>1){
			return;
		}
//		TPLog.printKeyStatus("SubPage-postScale","postScale->curZoom="+curZoom+",ScaleFactor="+scale);
		Matrix tempMatrix = new Matrix();
		tempMatrix.set(matrix);
		curZoom = curZoom*scale;
		TPLog.printKeyStatus("SubPage-postScale","postScale->curZoom="+curZoom+",ScaleFactor="+scale);
		float yuzhi = 2.999f;
		if(VersionUtils.isImix()){
			yuzhi = 2.995f;
		}
		if(curZoom>2.999f){
			matrixValues[0] = 3.0f;
			matrixValues[4] = 3.0f;
			matrix.setValues(matrixValues);
			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				((ImageGraph)imageList.get(i)).reset(tempMatrix,matrix);
			}
		}else if(curZoom<0.5f){
			matrixValues[0] = 0.5f;
			matrixValues[4] = 0.5f;
			matrix.setValues(matrixValues);
			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				((ImageGraph)imageList.get(i)).reset(tempMatrix,matrix);
			}
		}else {
//			curZoom = curZoom * scale;
//			if (curZoom > 3.0f) {
//				scale = 3.0f / curZoom;
//			}
//
//			if (curZoom < 0.5) {
//				scale = 0.5f / curZoom;
//			}

			TPLog.printKeyStatus("postScale->scale="+scale+",focusX="+focusX+",focusY="+focusY);
			matrix.postScale(scale, scale, focusX, focusY);

			int imgCount = imageList.size();
			for(int i = 0;i<imgCount;i++){
				//((ImageGraph)imageList.get(i)).overallSituationScale(scale,focusX,focusY);
				((ImageGraph)imageList.get(i)).postScale(scale, scale, focusX, focusY);
			}
		}

		tempMatrix = null;
		*/
		matrix.postScale(scale, scale, focusX, focusY);
		int imgCount = imageList.size();
		for(int i = 0;i<imgCount;i++){
			//((ImageGraph)imageList.get(i)).overallSituationScale(scale,focusX,focusY);
			((ImageGraph)imageList.get(i)).postScale(scale, scale, focusX, focusY);
		}

//		TPLog.printError("postScale==="+matrix.toString());
	}

	void postRotate(int angle,boolean isComplete){
		if(isComplete){
			RotateOperation rop = new RotateOperation();
			rop.setAngle(curAngle+angle, curAngle);
			uarManager.addToUndo(rop);
		}
		curAngle = curAngle+angle;

		float centerX = WhiteBoardUtils.whiteBoardCenterX;
		float centerY = WhiteBoardUtils.whiteBoardCenterY;
		matrix.postRotate(angle,centerX,centerY);

		int imgCount = imageList.size();
		for(int i = 0;i<imgCount;i++){
//			((ImageGraph)imageList.get(i)).overallSituationRotate(angle,centerX,centerY);
			((ImageGraph)imageList.get(i)).postRotate(angle,centerX,centerY);
		}
	}
	
	void rotate(int angle,boolean isComplete){
		postRotate(angle-curAngle,true);

		if(isComplete){
		RotateOperation rop = new RotateOperation();
		rop.setAngle(this.tempAngle, angle);
		uarManager.addToUndo(rop);
		tempAngle = angle;
		}
		curAngle = angle;
	}
	
	void setRotate(int angle){
		postRotate(angle-curAngle,true);

		RotateOperation rop = new RotateOperation();
		rop.setAngle(this.tempAngle, angle);
		uarManager.addToUndo(rop);
		tempAngle = angle;
		curAngle = angle;
	}


	public void translate(float ox,float oy){
		postTranslate(ox-offsetX,oy-offsetY);
	}

    void postTranslate(float ox,float oy){
//		if(true){
//			return;
//		}
		offsetX = offsetX+ox;
		offsetY = offsetY+oy;

		TPLog.printKeyStatus("postTranslate->ox="+ox+",oy="+oy);

		matrix.postTranslate(ox,oy);

		int imgCount = imageList.size();
		for(int i = 0;i<imgCount;i++){
			((ImageGraph)imageList.get(i)).postTranslate(ox,oy);
		}
//		TPLog.printError("postTranslate==="+matrix.toString());
	}


	void clearAll(){//删除所有图元，清屏
		isNeedSave = true;
		ClearScreenOperation cso = new ClearScreenOperation();
		ArrayList<Graph> list = new ArrayList<Graph>();
		ArrayList<Graph> imgList = new ArrayList<Graph>();
		int count = graphs.size();
		for(int i = 0;i < count;i++){
			list.add(graphs.get(i));
		}
		for(int i = 0;i < this.imageList.size();i++){
			imgList.add(imageList.get(i));
		}
		graphs.clear();
		imageList.clear();
		cso.addOldGraphList(list);
		cso.addOldImgGraphList(imgList);
		uarManager.addToUndo(cso);
	}

	public float getOffsetX(){
		matrix.getValues(matrixValues);
		return matrixValues[2];
	}
	
	public float getOffsetY(){
		matrix.getValues(matrixValues);
		return matrixValues[5];
	}
	
	public int getAngle(){
		return curAngle;
	}
	
	public float getScale(){
		matrix.getValues(matrixValues);
		return matrixValues[Matrix.MSCALE_X];
	};
	
	
	public int getProgress() {
		return progress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public synchronized void draw(Canvas canvas){
		//绘制图元
		int saveCount = canvas.save();
		canvas.setMatrix(matrix);
		for(int i = 0;i<graphs.size();i++){
			Graph graph = graphs.get(i);
			if(checkGraphInScreen(graph)) {
				graphs.get(i).draw(canvas);
			}
		}
		canvas.restoreToCount(saveCount);
	}

	@Override
	public synchronized void drawImage(Canvas canvas) {
//		TPLog.printError("drawImag  begin...");
		canvas.save();
		canvas.setMatrix(matrix);
		int  imageCount = imageList.size();
		for(int i = imageCount -1;i>=0;i--){
			Graph graph = imageList.get(i);
			if(checkGraphInScreen(graph)) {
//				TPLog.printError("IMG IN SCREEN...");
				graph.draw(canvas);
			}else{
//				TPLog.printError("IMG NOT IN SCREEN...");
			}
		}
		canvas.restore();
	}


	private boolean checkGraphInScreen(Graph graph){
		if(graph.getGraphType() == WhiteBoardUtils.GRAPH_SELECT){
			return true;
		}
		Rect bounds = graph.getBounds();
		RectF dst = new RectF();
		if(graph.getGraphType() != WhiteBoardUtils.GRAPH_IMAGE) {
			matrix.mapRect(dst, new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom));
		}else{
			dst.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
		}
		RectF screenBounds = new RectF(0,0,WhiteBoardUtils.whiteBoardWidth,WhiteBoardUtils.whiteBoardHeight);

		return screenBounds.intersect(dst);
	}

	@Override
	public void addGraph(Graph graph) {
		//如果图元里面的坐标点只有一个的话，就视其无效
//		if(graph.getPoints()==null||graph.getPoints().size()<2){
		if((graph.getPoints()==null||graph.getPoints().size()==0)&&graph.getGraphType() != WhiteBoardUtils.GRAPH_BRUSHPEN){
			return;
		}

		//如果没有擦掉任何图元的话就不用保存了
		if(graph.getGraphType() == WhiteBoardUtils.GRAPH_ERASE||graph.getGraphType() == WhiteBoardUtils.GRAPH_ERASE_AREA){
			if(graphs.size() == 0){//第一笔不能是擦除
				return;
			}
		}

		if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
			imageList.add(graph);
		}else {
			if(graph.getGraphIndex() == -1){
				graph.setGraphIndex(graphs.size());
				if(graphs.isEmpty()||graphs.get(graphs.size()-1).getGraphType()!=WhiteBoardUtils.GRAPH_SELECT) {
					graphs.add(graph);
				}else{
					graphs.add(graphs.size()-1,graph);
				}
			}else{
//				int index = graph.getGraphIndex();
//				if(index<graphs.size()){
//					graphs.add(index,graph);
//				}else{
//					graph.setGraphIndex(graphs.size());
//					graphs.add(graph);
//				}
				if(graphs.isEmpty()||graphs.get(graphs.size()-1).getGraphType()!=WhiteBoardUtils.GRAPH_SELECT) {
					graphs.add(graph);
				}else{
					graphs.add(graphs.size()-1,graph);
				}
			}

			GraphOperation gp = new GraphOperation();
			gp.setGraph(graph);
			uarManager.addToUndo(gp);
		}


		for(Graph g:graphs){
			if(g.getGraphType() == WhiteBoardUtils.GRAPH_PEN){
				isNeedSave = true;
				return;
			}
		}

		if(imageList.size()!=0){
			isNeedSave = true;
		}

	}

	@Override
	public ArrayList<Graph> getGraphList() {
		return graphs;
	}

	public ArrayList<Graph> getImageGraphList(){
		return imageList;
	}

	@Override
	public ImageGraph getImgGraphFromRemoteId(String remoteId){
		for(int i = 0;i<imageList.size();i++){
			TPLog.printError("getImgGraphFromRemoteId---->remoteId="+remoteId+",imageList.get(i).remoteId = "+imageList.get(i).getRemoteId());
			if(imageList.get(i).getRemoteId().equals(remoteId)){
				return (ImageGraph)imageList.get(i);
			}
		}
		return null;
	}


	@Override
	public IOperation undo() {
		IOperation op = uarManager.undo();
		if(op==null){
			return null;
		}
		int type = op.getType();
		
		TPLog.printKeyStatus("undo->type="+type);
		
		if(type == IOperation.OPT_GRAPH){
			graphs.remove(((GraphOperation)op).getGraph());
		}else if(type == IOperation.OPT_ROTATE){
			RotateOperation rop = (RotateOperation)op;
			int tempAngle = rop.getCurAngle();
			//curAngle = rop.getCurAngle();
			matrix.postRotate(tempAngle - curAngle,WhiteBoardUtils.whiteBoardCenterX,WhiteBoardUtils.whiteBoardCenterY );
			this.tempAngle = tempAngle;
			this.curAngle = tempAngle;
		}else if(type == IOperation.OPT_CLEAR_SCREEN){
			ClearScreenOperation csop = (ClearScreenOperation)op;
			ArrayList<Graph> list = csop.getGraphList();
			graphs.clear();
			int count = list.size();
			for(int i=0;i<count;i++){
				graphs.add(list.get(i));	
			}
			ArrayList<Graph> imgList = csop.getImgGraphList();
			imageList.clear();
			 count = imgList.size();
			for(int i=0;i<count;i++){
				imageList.add(imgList.get(i));
			}
		}else if(type == IOperation.MT_AREA_ERASE){
			MtAreaEraseOperation mtEO = (MtAreaEraseOperation)op;
			for(int i = 0;i<mtEO.getGraphList().size();i++){
				Graph graph = mtEO.getGraphList().get(i);
				int  index = getGraphIndexFromRemoteId(graph.getCurEraseId());
				graphs.add(index+1,graph);
			}
		}
		return op;
	}

	@Override
	public IOperation redo() {
		IOperation op = uarManager.redo();
		if(op==null){
			return null;
		}
		
		int type = op.getType();
		if(type == IOperation.OPT_GRAPH){
			graphs.add(((GraphOperation)op).getGraph());
		}else if(type == IOperation.OPT_ROTATE){
			RotateOperation rop = (RotateOperation)op;
			int tempAngle = rop.getOldAngle();
			//curAngle = rop.getCurAngle();
			matrix.postRotate(tempAngle - curAngle,WhiteBoardUtils.whiteBoardCenterX,WhiteBoardUtils.whiteBoardCenterY );
			curAngle = tempAngle;
			this.tempAngle = tempAngle;
		}else if(type == IOperation.OPT_CLEAR_SCREEN){
			graphs.clear();
			imageList.clear();
		}else if(type == IOperation.MT_AREA_ERASE){
			MtAreaEraseOperation mtEO = (MtAreaEraseOperation)op;
			for(int i = 0;i<mtEO.getGraphList().size();i++){
				graphs.remove(mtEO.getGraphList().get(i));
			}
		}
		
		return op;
	}

	@Override
	public boolean undoEnable() {
		return uarManager.undoEnable();
	}

	@Override
	public boolean redoEnable() {
		return uarManager.redoEnable();
	}
	
	public Image getImage(){
		return mImage;
	}

	@Override
	public Matrix getMatrix() {
		return matrix;
	}

	@Override
	public void setMatrix(Matrix matrix) {
		Matrix oldMatrix = new Matrix();
		oldMatrix.set(this.matrix);

		this.matrix.reset();
		this.matrix.set(matrix);

		int count = imageList.size();
		for(int i = 0;i<count;i++){
			((ImageGraph)imageList.get(i)).reset(oldMatrix,this.matrix);
		}
	}

	@Override
	public void setMatrixValues(float[] values) {
		Matrix oldMatrix = new Matrix();
		oldMatrix.set(this.matrix);

		this.matrix.reset();
		this.matrix.setValues(values);

		int count = imageList.size();
		for(int i = 0;i<count;i++){
			((ImageGraph)imageList.get(i)).reset(oldMatrix,this.matrix);
		}
	}

	public Graph getGraph(long graphId){
		int count = graphs.size();
		for(int i = 0;i<count;i++){
			if(graphs.get(i).getId() == graphId){
				return graphs.get(i);
			}
		}
		return null;
	}
	
	public void addUndo(IOperation op){
		uarManager.addToUndo(op);
	}
	
	public void addRedo(IOperation op){
		uarManager.addToRedo(op);
	}

	@Override
	public void setUndoAndRedoListener(IUndoAndRedoListener listener) {
		uarManager.setUndoAndRedoListener(listener);		
	}

	@Override
	public void imgWidthToScreenWidth() {
		if(mImage==null){
			return;
		}

		curAngle  = 0;
		float curZoom = 1.0f;
		offsetX = 0;
		offsetY = 0;

		float imgWidth = mImage.getWidth();
		float imgHeight = mImage.getHeight();

		float wbWidth = WhiteBoardUtils.whiteBoardWidth;
		float wbHeight = WhiteBoardUtils.whiteBoardHeight;

		float scale = wbWidth/imgWidth;

		float scaleImgHeight = imgHeight * scale;

		float imgX = mImage.getX();
		float imgY = mImage.getY();

		//将图片左上角移动到（0,0）点
		float curOffsetX = offsetX*-1+imgX*-1;
		float curOffsetY = offsetY*-1+imgY*-1;

		float spx = 0;
		float spy = 0;
		if(scaleImgHeight<wbHeight){
			spy = (wbHeight - scaleImgHeight)/2f;
			curOffsetY = curOffsetY + spy;
		}

		matrix.reset();

		postTranslate(curOffsetX,curOffsetY);
		postScale(scale,spx,spy);
	}

	@Override
	public void imgHeightToScreenHeight() {
		if(mImage==null){
			return;
		}

		curAngle = 0;
		float curZoom = 1.0f;
		offsetX = 0;
		offsetY = 0;

		float imgHeight = mImage.getHeight();
		float imgWidth = mImage.getWidth();
		float wbHeight = WhiteBoardUtils.whiteBoardHeight;
		float wbWidth = WhiteBoardUtils.whiteBoardWidth;

		float scale = wbHeight/imgHeight;

		float scaleWidth = scale * imgWidth;

		float imgX = mImage.getX();
		float imgY = mImage.getY();

		float curOffsetX = offsetX*-1+imgX*-1;
		float curOffsetY = offsetY*-1+imgY*-1;

		float spx = 0;
		float spy = 0;

		if(scaleWidth<wbWidth){
			spx = (wbWidth-scaleWidth)/2f;
			curOffsetX = curOffsetX + spx;
		}

		matrix.reset();
		postTranslate(curOffsetX,curOffsetY);
		postScale(scale,spx,spy);
	}

	/*
	void selfAdaption() {
		ArrayList<Graph> graphList = getGraphList();
		ArrayList<Graph> mImageGraph= getImageGraphList();
		ArrayList<Graph> graphs = new ArrayList<Graph>();

		int graphCount = graphList.size();
		for(int i = 0;i< graphCount;i++){
			graphs.add(graphList.get(i));
		}

		int imgCount = mImageGraph.size();
		for(int i = 0;i<imgCount;i++){
			graphs.add(mImageGraph.get(i));
		}

		Rect bounds = GraphUtils.computeGraphBounds(graphs);

		graphs.clear();
		graphs = null;
//		Matrix invert = new Matrix();
//		matrix.invert(invert);

		float graphWidth = bounds.width();
		float grapHeight = bounds.height();

		float tempOffsetX =  (WhiteBoardUtils.whiteBoardCenterX - bounds.centerX());// -  getOffsetX();
		float tempOffsetY =   (WhiteBoardUtils.whiteBoardCenterY - bounds.centerY());// - getOffsetY();

		//屏幕的Bounds
		RectF screenRect = new RectF(0, 0, WhiteBoardUtils.whiteBoardWidth, WhiteBoardUtils.whiteBoardHeight);

		if (!screenRect.contains(new RectF(bounds.left,bounds.top,bounds.right,bounds.bottom))) {//如果图元在当前屏幕内就不用做处理了
			matrix.reset();


			if (graphWidth > WhiteBoardUtils.whiteBoardWidth || grapHeight > WhiteBoardUtils.whiteBoardHeight) {
				float scaleWidth = (WhiteBoardUtils.whiteBoardWidth) / (graphWidth);
				float scaleHeight = (WhiteBoardUtils.whiteBoardHeight) / (grapHeight);
				curZoom = 1.0f;
				if(graphWidth>WhiteBoardUtils.whiteBoardWidth&&grapHeight<=WhiteBoardUtils.whiteBoardHeight){
					curZoom = scaleWidth/getScale();
				}else if(graphWidth<=WhiteBoardUtils.whiteBoardWidth&&grapHeight>WhiteBoardUtils.whiteBoardHeight){
					curZoom = scaleHeight/getScale();
				}else{
					if (scaleWidth > scaleHeight) {
						curZoom = scaleHeight/getScale();
					} else {
						curZoom = scaleWidth/getScale();
					}
				}

				matrix.postTranslate(tempOffsetX, tempOffsetY);
				matrix.postScale(curZoom,curZoom, (int)WhiteBoardUtils.whiteBoardCenterX, (int)WhiteBoardUtils.whiteBoardCenterY);

				 imgCount = imageList.size();
				for(int i = 0;i<imgCount;i++){
					if(imageList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
						((ImageGraph)imageList.get(i)).reset(matrix);
//						((ImageGraph)imageList.get(i)).overallSituationTranslate(offsetX,offsetY);
//						((ImageGraph)imageList.get(i)).overallSituationScale(curZoom,(int)WhiteBoardUtils.whiteBoardCenterX, (int)WhiteBoardUtils.whiteBoardCenterY);
					}
				}

			}else{
				matrix.postTranslate(tempOffsetX, tempOffsetY);
				imgCount = imageList.size();
				for(int i = 0;i<imgCount;i++){
					if(imageList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
						((ImageGraph)imageList.get(i)).reset(matrix);
//						((ImageGraph)imageList.get(i)).overallSituationTranslate(offsetX,offsetY);
						//((ImageGraph)imageList.get(i)).overallSituationScale(curZoom,(int)WhiteBoardUtils.whiteBoardCenterX, (int)WhiteBoardUtils.whiteBoardCenterY);
					}
				}
			}
		}
//		else{//屏幕内暂时就不做处理了
//			matrix.postTranslate(tempOffsetX, tempOffsetY);
//		}
	}*/

	void selfAdaption() {
		Matrix tempMatrix = new Matrix();
		tempMatrix.set(matrix);

		ArrayList<Graph> graphList = getGraphList();
		ArrayList<Graph> mImageGraph = getImageGraphList();
		ArrayList<Graph> graphs = new ArrayList<Graph>();

		int graphCount = graphList.size();
		for (int i = 0; i < graphCount; i++) {
			graphs.add(graphList.get(i));
		}

		int imgCount = mImageGraph.size();
		for (int i = 0; i < imgCount; i++) {
			graphs.add(mImageGraph.get(i));
		}

		Rect bounds = GraphUtils.computeGraphBounds(graphs,matrix);

		graphs.clear();
		graphs = null;

		float graphWidth = bounds.width();
		float grapHeight = bounds.height();

		//计算当前视窗大小及位置
		RectF screenRect = new RectF(0, 0, WhiteBoardUtils.whiteBoardWidth, WhiteBoardUtils.whiteBoardHeight);


		float tempOffsetX = (screenRect.centerX() - bounds.centerX());// -  getOffsetX();
		float tempOffsetY = (screenRect.centerY() - bounds.centerY());// - getOffsetY();

		if (!screenRect.contains(new RectF(bounds.left, bounds.top, bounds.right, bounds.bottom))) {//如果图元在当前屏幕内就不用做处理了
			matrix.reset();

			if (graphWidth > WhiteBoardUtils.whiteBoardWidth || grapHeight > WhiteBoardUtils.whiteBoardHeight) {
				float scaleWidth = (WhiteBoardUtils.whiteBoardWidth) / (graphWidth);
				float scaleHeight = (WhiteBoardUtils.whiteBoardHeight) / (grapHeight);
				float curZoom = 1.0f;
				if (graphWidth > WhiteBoardUtils.whiteBoardWidth && grapHeight <= WhiteBoardUtils.whiteBoardHeight) {
					curZoom = scaleWidth / getScale();
				} else if (graphWidth <= WhiteBoardUtils.whiteBoardWidth && grapHeight > WhiteBoardUtils.whiteBoardHeight) {
					curZoom = scaleHeight / getScale();
				} else {
					if (scaleWidth > scaleHeight) {
						curZoom = scaleHeight / getScale();
					} else {
						curZoom = scaleWidth / getScale();
					}
				}

				matrix.postTranslate(tempOffsetX, tempOffsetY);
				matrix.postScale(curZoom, curZoom, (int) WhiteBoardUtils.whiteBoardCenterX, (int) WhiteBoardUtils.whiteBoardCenterY);

				imgCount = imageList.size();
				for (int i = 0; i < imgCount; i++) {
					if (imageList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_IMAGE) {
						((ImageGraph) imageList.get(i)).reset(tempMatrix, matrix);
					}
				}

			} else {
				matrix.postTranslate(tempOffsetX, tempOffsetY);
				imgCount = imageList.size();
				for (int i = 0; i < imgCount; i++) {
					if (imageList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_IMAGE) {
						((ImageGraph) imageList.get(i)).reset(tempMatrix, matrix);
					}
				}
			}
		}
		else{//屏幕内暂时就不做处理了
			matrix.reset();
			matrix.postTranslate(tempOffsetX, tempOffsetY);
			imgCount = imageList.size();
			for (int i = 0; i < imgCount; i++) {
				if (imageList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_IMAGE) {
					((ImageGraph) imageList.get(i)).reset(tempMatrix, matrix);
				}
			}
	}
	}

	void oneToOne(){
		float temZoom = 1.0f / getScale();
		postScale(temZoom,WhiteBoardUtils.whiteBoardCenterX, WhiteBoardUtils.whiteBoardCenterY);
	}

	@Override
	public List<IOperation> getUndoList() {
		return uarManager.getUndoList();
	}

	@Override
	public List<IOperation> getRedoList() {
		return uarManager.getRedoList();
	}
	
	public void clearUndoList(){
		uarManager.clearUnoList();
	}
	
	public void clearRedoList(){
		uarManager.clearRedoList();
	}

	/**
	 * 获取缩略图
	 * @param canvas
     */
	@Override
	public void save(Canvas canvas) {
		if(graphs==null){
			return;
		}
		canvas.save();
		canvas.setMatrix(matrix);
		for(int i = 0;i<graphs.size();i++){
			if(graphs.get(i).getGraphType() == WhiteBoardUtils.GRAPH_SELECT){
				continue;
			}
			graphs.get(i).draw(canvas);
		}
		canvas.restore();

		int imgCount = imageList.size();
		for(int i = 0;i<imgCount;i++){
			ImageGraph ig = ((ImageGraph)imageList.get(i));
			ig.draw(canvas);
		}
	}

	/**
	 * 保存到本地
     */
	public void save(Canvas canvas,Matrix matrix) {
		canvas.save();
		canvas.setMatrix(matrix);
		for(int i = 0;i<graphs.size();i++){
			graphs.get(i).draw(canvas);
		}

		int imgCount = imageList.size();
		for(int i = 0;i<imgCount;i++){
			((ImageGraph)imageList.get(i)).draw(canvas,this.matrix,matrix);
		}
		canvas.restore();
	}

	@Override
	public void saveToImage(String saveDir,String fileName) {
		saveToImage(saveDir,fileName,null);
		isNeedSave = false;
	}
	
	@Override
	public void saveToImage(String saveDir,String fileName,ISavePageCallBack callBack) {
		TPLog.printError("saveToImage begin");
		mSavePageUtils.saveSubPage(this, saveDir,fileName, callBack);
		isNeedSave = false;
		TPLog.printError("saveToImage end");
	}

	public void saveImageToCache(String saveDir,String fileName,ISavePageCallBack callBack){
		TPLog.printError("saveImageToCache begin");
		mSavePageUtils.saveSubPage(this, saveDir,fileName, callBack);
		TPLog.printError("saveImageToCache end");
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	@Override
	public Bitmap getPageThumbnail(int backgroundColor) {
		int width = (int)WhiteBoardUtils.whiteBoardWidth;
		int height = (int)WhiteBoardUtils.whiteBoardHeight;

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		WhiteBoardUtils.drawWbBackground(canvas,backgroundColor);

		int saveLayerCount = canvas.saveLayer(0,0,width,height,null);
		save(canvas);
		canvas.restoreToCount(saveLayerCount);

		Bitmap bitmap2 = Bitmap.createBitmap((int)(((float)width/3f)), (int)(((float)height/3f)), Bitmap.Config.ARGB_8888);
		Canvas canvas2 = new Canvas(bitmap2);
		canvas2.scale((1f/3f), (1f/3f));
		canvas2.drawBitmap(bitmap, 0, 0, null);

		bitmap.recycle();
		bitmap = null;
		canvas = null;
		canvas2 = null;
		return bitmap2;
	}

	private Rect changeRect(Rect rect,float scale,int angle){
		Path path = new Path();
		path.moveTo(rect.left, rect.top);
		path.lineTo(rect.right, rect.top);
		path.lineTo(rect.right, rect.bottom);
		path.lineTo(rect.left, rect.bottom);
		path.close();
	
		float px = WhiteBoardUtils.whiteBoardWidth/2f;
		float py = WhiteBoardUtils.whiteBoardHeight/2f;
		
		//很诡异，Path进行坐标操作时，一次只能进行一种操作，如果进行组合操作时，将只会执行最后一种操作
		Matrix matrix = new Matrix();
		
		matrix.setTranslate(offsetX, offsetY);
		path.transform(matrix);
		
		if(scale!=1.0f){
		matrix.reset();
		matrix.setScale(scale, scale,px,py);
		path.transform(matrix);
		}
		
		if(angle!=0){//坐标旋转为左上角为0，0点的坐标系
			matrix.reset();
			matrix.setRotate(angle,px,py);
			path.transform(matrix);
			}
		
		RectF rectf = new RectF();
		path.computeBounds(rectf, true);
		Rect dstRect = new Rect();
		rectf.roundOut(dstRect);
		return dstRect;
	}
	
	public Rect getCurScreenRect(){
		Rect rect = new Rect(0, 0, (int)WhiteBoardUtils.whiteBoardWidth, (int)WhiteBoardUtils.whiteBoardHeight);
		return rect;
	}
	
	private boolean intersectRect(Rect r1,Rect r2){
		if(r2.right<r1.left){
			return false;
		}
		if(r2.left>r1.right){
			return false;
		}
		
		if(r2.bottom<r1.top){
			return false;
		}
		
		if(r2.top>r1.bottom){
			return false;
		}
		
		return true;
	}

	@Override
	public void destroy() {
		int count = graphs.size();
		for(int i = 0;i<count;i++){
			Graph graph = graphs.get(i);
			graph.destroy();
			graph = null;
		}
		if(imageList != null||!imageList.isEmpty()){
			count = imageList.size();
			for(int i = 0;i<count;i++){
				Graph graph = imageList.get(i);
				graph.destroy();
				graph = null;
			}
		}
		graphs.clear();
		imageList.clear();
		if(mImage!=null)
		mImage.destroy();
		graphs = null;
		imageList = null;
		mImage = null;
		uarManager= null;
		mSavePageUtils = null;
	}

	@Override
	public void compatibilityMtAreaErase(AreaErase erase) {
		TPLog.printError("compatibilityMtAreaErase,begin...");
		if(erase == null){
			return;
		}

		MtAreaEraseOperation mtEo = new MtAreaEraseOperation();

		int count = graphs.size();
		Graph curErase = null;
		for(int i = 0;i<count;i++){
			Graph graph = graphs.get(i);
			if(graph == null){
//				graphs.remove(i);
				continue;
			}
			if(graph.getGraphType() == WhiteBoardUtils.GRAPH_SELECT){
				continue;
			}
			Rect bounds = graph.getBounds();
			RectF dst = new RectF();
			dst.set(bounds.left, bounds.top, bounds.right, bounds.bottom);
			RectF screenBounds = new RectF(erase.getBounds());

			if(!screenBounds.intersect(dst)){
				continue;
			}
			if(graph.getGraphType() == WhiteBoardUtils.GRAPH_ERASE_AREA){
				curErase = graph;
				continue;
			}
			if(curErase!=null){
				String Id = curErase.getRemoteId();
				graph.setCurEraseId(Id);
			}
			mtEo.getGraphList().add(graph);
//			graphs.remove(i);
		}

		TPLog.printError("compatibilityMtAreaErase,mtEo.getGraphList()==="+mtEo.getGraphList().size());
		TPLog.printError("compatibilityMtAreaErase,1graphs.size==="+graphs.size());
		for(int i = 0;i<mtEo.getGraphList().size();i++){
			int index = getGraphIndexFromRemoteId(mtEo.getGraphList().get(i).getRemoteId());
			graphs.remove(index);
		}
		TPLog.printError("compatibilityMtAreaErase,2graphs.size==="+graphs.size());
		uarManager.addToUndo(mtEo);
	}

	public Graph getGraphFromRemoteId(String remoteId){
		for(int i = 0;i<graphs.size();i++){
			if(graphs.get(i).getRemoteId().equals(remoteId)){
				return graphs.get(i);
			}
		}
		return null;
	}

	public int getGraphIndexFromRemoteId(String remoteId){
		for(int i = 0;i<graphs.size();i++){
			if(graphs.get(i).getRemoteId().equals(remoteId)){
				return i;
			}
		}
		return -1;
	}


	public void debug(){
		TPLog.printError("*******************SubPage Debug*************************");
//		TPLog.printError("offsetX--------------->"+offsetX);
//		TPLog.printError("offsetY--------------->"+offsetY);
//		TPLog.printError("scale--------------->"+curZoom);
		float values[] = new float[9];
		matrix.getValues(values);
		String v = "";
		for(float f:values){
			v += f +",";
		}
		TPLog.printError("subPage---debug------------->"+v);
		TPLog.printError("*********************************************************");
	}

}
