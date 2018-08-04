package com.kedacom.touchdata.whiteboard.page;

import java.io.File;
import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.utils.IUndoAndRedoListener;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils.ISavePageCallBack;
import com.kedacom.tplog.TPLog;

public class Page implements IPage{
	
	private int ownerIndex;
	
	private int m_bInConvert;
	
	private int m_nTime;
	
	private int pageMode = MODE_NORMAL;
	
	private boolean isAnoymous = true;

	private int isDocOwner = 1;
	
	private String pageName;
	
	private long pageId;

	private String remotePageId;

	private int curSuPageIndex = 0;

	private ArrayList<SubPage> subPages = new ArrayList<SubPage>();
	
	private SubPage curSubPage;
	
	private boolean isLock = false;

	private int backGroundColor = Color.parseColor("#959595");

	private String achElementUrl = "";


	private int dwWbSerialNumber = 0;

	public int getCurTLScX() {
		return curTLScX;
	}

	public void setCurTLScX(int curTLScX) {
		this.curTLScX = curTLScX;
	}

	public int getCurTLScY() {
		return curTLScY;
	}

	public void setCurTLScY(int curTLScY) {
		this.curTLScY = curTLScY;
	}

	private int curTLScX = 0;

	private int curTLScY = 0;


	public Page(){
		 setId(System.currentTimeMillis());
		 remotePageId = WhiteBoardUtils.getRemoteId();
		 pageId = remotePageId.hashCode();

	}

	public int getOwnerIndex() {
		return ownerIndex;
	}

	public void setOwnerIndex(int ownerIndex) {
		this.ownerIndex = ownerIndex;
	}


	public int getM_bInConvert() {
		return m_bInConvert;
	}



	public void setM_bInConvert(int m_bInConvert) {
		this.m_bInConvert = m_bInConvert;
	}



	public int getM_nTime() {
		return m_nTime;
	}



	public void setM_nTime(int m_nTime) {
		this.m_nTime = m_nTime;
	}



	public int getPageMode() {
		return pageMode;
	}



	public void setPageMode(int pageMode) {
		this.pageMode = pageMode;
	}
	

	public int getIsDocOwner() {
		return isDocOwner;
	}

	public void setIsDocOwner(int isDocOwner) {
		this.isDocOwner = isDocOwner;
	}

	public int getIsAnoymous() {
		if(isAnoymous){
			return 1;
		}else{
			return 0;
		}
	}

	public void setAnoymous(boolean isAnoymous) {
		this.isAnoymous = isAnoymous;
	}

	@Override
	public void setId(long id) {
		pageId = id;
	}

	public void setRemotePageId(String remotePageId){
		this.remotePageId = remotePageId;
	}

	@Override
	public long getId() {
		return pageId;
	}

	public String getRemotePageId(){
		return remotePageId;
	}

	@Override
	public void setName(String name) {
		pageName = name;
	}

	@Override
	public String getName() {
		return pageName;
	}

	public String getAchElementUrl() {
		return achElementUrl;
	}

	public void setAchElementUrl(String achElementUrl) {
		this.achElementUrl = achElementUrl;
	}

	@Override
	public Matrix getMatrix() {
		return curSubPage.getMatrix();
	}

	@Override
	public void setBackGroundColor(int color) {
		this.backGroundColor = color;
		int count = getDocPageCount();
		for(int i = 0;i<count;i++){
			subPages.get(i).setBackgroundColor(color);
		}
	}

	@Override
	public int getBackGroundColor() {
		return backGroundColor;
	}

	public boolean isLock() {
		return isLock;
	}

	public void lock(boolean isLock) {
		this.isLock = isLock;
	}

//	public void setDocPageCount(int count){
//		subPageMaxIndex = count;
//	}

	public int getDocPageCount(){
		return subPages.size();
	}
	
	@Override
	public void scale(float scale) {
	//	curZoom = scale;
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			subPages.get(i).scale(scale);
		}
	}

	@Override
	public void postScale(float scale, float focusX, float focusY) {
//		if(scale>1.2f){
//			return;
//		}
//		curZoom = curZoom*scale;

		int count = subPages.size();
		for(int i = 0;i<count;i++){
			subPages.get(i).postScale(scale,focusX,focusY);
		}
	}

	@Override
	public void rotate(int angle,boolean isComplete) {
//		curAngle = angle;
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			if(curSuPageIndex == i){
		        subPages.get(i).rotate(angle, isComplete);
			}else{
				subPages.get(i).rotate(angle, false);
			}
		}
	}

	@Override
	public void postRotate(int angle, boolean isComplete) {
//		curAngle += angle;
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			if(curSuPageIndex == i){
				subPages.get(i).postRotate(angle, isComplete);
			}else{
				subPages.get(i).postRotate(angle, false);
			}
		}
	}

	@Override
	public boolean translate(float ox, float oy) {
		
//		if(ox == offsetX&&oy == offsetY){
//			return false;
//		}
//
//		offsetX = ox;
//		offsetY = oy;
		
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			subPages.get(i).translate(ox, oy);
		}
		
		return true;
	}

	@Override
	public boolean postTranslate(float ox, float oy) {
		if(ox == 0&&oy == 0){
			return false;
		}

//		offsetX = offsetX+ox;
//		offsetY = offsetY+oy;

		int count = subPages.size();
		for(int i = 0;i<count;i++){
			subPages.get(i).postTranslate(ox, oy);
		}

		return true;
	}


	@Override
	public void selectSubPage(int index) {
		curSuPageIndex = index-1;
		curSubPage = getCurSubPage();
	}
	
	@Override
	public void selectSubPage(long imageId) {
		int count = subPages.size();
		
		int index = -1;
		
		for(int i= 0;i<count;i++){
			Image img = subPages.get(i).getImage();
			if(img!=null&&img.getId() == imageId){
				index = i;
			}
		}
		
		if(index==-1){
			return;
		}
		
		selectSubPage((index + 1));
	}

	@Override
	public void addSubPage(SubPage sp) {
		if(curSubPage==null)
			curSubPage = sp;
		//这里是不能创建多个空的子页，打开文件时保存用户在打开文件之前在当前白板上面绘制的内容
		if(subPages.size()==1){
			if(subPages.get(0).getImage()==null){
				subPages.get(0).setImage(sp.getImage());
				return;
			}
		}
		sp.setBackgroundColor(backGroundColor);
		subPages.add(sp);
	}

	@Override
	public SubPage getCurSubPage() {
		if(curSuPageIndex<0){
			return null;
		}
		TPLog.printError("getCurSubPage --- > curSuPageIndex = "+curSuPageIndex);
		return subPages.get(curSuPageIndex);
	}
	

	@Override
	public ArrayList<SubPage> getSubPageList() {
		return subPages;
	}
	
	@Override
	public boolean nextSubPage() {
		if(hasNextSubpage()){
			curSuPageIndex++;
		}

		int subPageMaxIndex = getDocPageCount();
		if(curSuPageIndex>=subPageMaxIndex){
			curSuPageIndex =  subPageMaxIndex - 1;
		}
		
		
		curSubPage = getCurSubPage();
		
		return true;
	}

	@Override
	public boolean PreviousSubPage() {
		curSuPageIndex--;
		
		if(curSuPageIndex<0){
			curSuPageIndex = 0;
			return false;
		}
		
		curSubPage = getCurSubPage();
		
		return true;
	}

	@Override
	public boolean hasNextSubpage() {
		if(curSuPageIndex>=subPages.size()-1){
			return false;
		}
		return true;
	}
	

	@Override
	public boolean hasPreSubPage() {
		if(curSuPageIndex<=0){
			return false;
		}
		return true;
	}
	

	@Override
	public int getSubPageCount() {
		return subPages.size();
	}


	@Override
	public int getCurSubPageIndex() {
		return curSuPageIndex+1;
	}
	
	@Override
	public float getCurScale() {
		return curSubPage.getScale();
	}

	@Override
	public int getCurAngle() {
		return curSubPage.getAngle();
	}

	@Override
	public float getOffsetX() {
		if(curSubPage!=null){
			return curSubPage.getOffsetX();
		}
		return 0;
	}

	@Override
	public float getOffsetY() {
		if(curSubPage!=null){
			return curSubPage.getOffsetY();
		}
		return 0;
	}

	@Override
	public void draw(Canvas canvas) {
		if(curSubPage!=null){
			curSubPage.draw(canvas);
		}
	}
	
	public void drawImage(Canvas canvas){
		if(canvas!=null){
			curSubPage.drawImage(canvas);
		}
	}
	
	@Override
	public ISubPage getSubPage(int index) {
		return subPages.get(index);
	}


	@Override
	public IOperation undo() {
		IOperation op = curSubPage.undo();
//		if(op==null)return null;
//		if(op.getType() == IOperation.OPT_ROTATE){
//			RotateOperation rop= (RotateOperation)op;
//			//curAngle = rop.getCurAngle();
//		}
		return op;
	}

	@Override
	public IOperation redo() {
		IOperation op = curSubPage.redo();
//		if(op==null)return null;
//		if(op.getType() == IOperation.OPT_ROTATE){
//			RotateOperation rop= (RotateOperation)op;
//			//curAngle = rop.getOldAngle();
//		}
		return op;
	}

	@Override
	public boolean undoEnable() {
		return curSubPage.undoEnable();
	}

	@Override
	public boolean redoEnable() {
		return curSubPage.redoEnable();
	}

	@Override
	public void clearAll() {
		if(curSubPage!=null){
			curSubPage.clearAll();
		}
	}

	@Override
	public void setUndoAndRedoListener(IUndoAndRedoListener listener) {
		curSubPage.setUndoAndRedoListener(listener);
	}

	@Override
	public void imgWidthToScreenWidth() {
//		if(curSubPage!=null)
//			curSubPage.imgWidthToScreenWidth();
		int count = getSubPageCount();

		for(int i = 0;i<count;i++){
			ISubPage subPage = getSubPage(i);
			subPage.imgWidthToScreenWidth();
		}
	}

	@Override
	public void imgHeightToScreenHeight() {
//		if(curSubPage!=null)
//			curSubPage.imgHeightToScreenHeight();
		int count = getSubPageCount();
		for(int i = 0;i<count;i++){
			ISubPage subPage = getSubPage(i);
			subPage.imgHeightToScreenHeight();
		}
	}

	@Override
	public void selfAdaption() {
		curSubPage.selfAdaption();
	}

	@Override
	public void oneToOne(){
		curSubPage.oneToOne();
	}

	@Override
	public boolean containsImage(int dwId) {
		int count = subPages.size();
		for(int i=0;i<count;i++){
			Image image = subPages.get(i).getImage();
			if(image!=null&&image.getId() == dwId){
				return true;
			}
		}
		return false;
	}
	

	@Override
	public Image getImage(int dwId) {
		int count = subPages.size();
		for(int i=0;i<count;i++){
			Image image = subPages.get(i).getImage();
			if(image!=null&&image.getId() == dwId){
				return subPages.get(i).getImage();
			}
		}
		return null;
	}

	@Override
	public void saveCurSubPageToImage(String saveDir,String fileName ,ISavePageCallBack callBack) {
//		String tempDir = saveDir +"";
//		String name = fileName;
//		if(name!=null){
//			if(tempDir.endsWith("/")){
//				tempDir = tempDir + name;
//			}else{
//				tempDir = tempDir + File.separator + name;
//			}
//		}
		File file = new File(saveDir);
		if(!file.exists()){
			file.mkdirs();
			file.mkdir();
		}
//		String fn = getCurSubPageIndex() + ".jpg";
		fileName = fileName + ".png";
		if(curSubPage==null){
			if(callBack!=null){
				callBack.onSaveFailed();
			}
			return;
		}
		curSubPage.saveToImage(saveDir,fileName, callBack);
	}

	//中断保存操作
	public void stopSave(){
		SavePageUtils.getInstence().stopSave();
	}

	@Override
	public void saveToImage(String saveDir,String fileName, ISavePageCallBack callBack) {
		TPLog.printError("saveToImage begin");
		String tempDir = saveDir + "";//屏蔽掉空指针异常
//		String name = fileName;
		//拼接上子目录
//		if(name!=null){
//			if(tempDir.endsWith("/")){
//				tempDir = tempDir + name;
//			}else{
//				tempDir = tempDir + File.separator + name;
//			}
//		}
		File file = new File(tempDir);
		if(!file.exists()){
			file.mkdirs();
			file.mkdir();
		}
		
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			String n = fileName + ".png";
			TPLog.printError("save fileName = "+n);
			subPages.get(i).saveToImage(tempDir,n, callBack);
		}

		TPLog.printError("saveToImage end");
	}

	@Override
	public void saveImageToCache(String saveDir,String fileName, ISavePageCallBack callBack) {
		TPLog.printError("saveImageToCache begin");
		String tempDir = saveDir + "";//屏蔽掉空指针异常
//		String name = fileName;
		//拼接上子目录
//		if(name!=null){
//			if(tempDir.endsWith("/")){
//				tempDir = tempDir + name;
//			}else{
//				tempDir = tempDir + File.separator + name;
//			}
//		}
		File file = new File(tempDir);
		if(!file.exists()){
			file.mkdirs();
			file.mkdir();
		}

		int count = subPages.size();
		for(int i = 0;i<count;i++){
			String n = fileName + ".png";
			TPLog.printError("save fileName = "+n);
			subPages.get(i).saveImageToCache(tempDir,n, callBack);
		}

		TPLog.printError("saveImageToCache end");
	}
	
	@Override
	public boolean isNeedSave() {
		if(subPages==null)return false;
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			if(subPages.get(i).isNeedSave()){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isEmpty() {
		int count = subPages.size();
		for(int i = 0;i<count;i++){
			if(!subPages.get(i).isEmpty()){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean hasGraphs() {
		return curSubPage.hasGraphs();
	}

	@Override
	public Bitmap getPageThumbnail() {
		if(curSubPage==null){
			return null;
		}
		return curSubPage.getPageThumbnail(backGroundColor);
	}

	@Override
	public void destory() {
		if(subPages!=null) {
			int count = subPages.size();
			for (int i = 0; i < count; i++) {
				SubPage sp = subPages.get(i);
				sp.destroy();
				sp = null;
			}
			subPages.clear();
			subPages = null;
		}
		curSubPage = null;
	}

	@Override
	public void compatibilityMtAreaErase(AreaErase areaErase) {
		curSubPage.compatibilityMtAreaErase(areaErase);
	}

	public int getDwWbSerialNumber() {
		return dwWbSerialNumber;
	}

	public void setDwWbSerialNumber(int dwWbSerialNumber) {
		this.dwWbSerialNumber = dwWbSerialNumber;
	}
}
