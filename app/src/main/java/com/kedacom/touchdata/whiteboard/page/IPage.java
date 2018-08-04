package com.kedacom.touchdata.whiteboard.page;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.utils.IUndoAndRedoListener;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils.ISavePageCallBack;
import com.kedacom.touchdata.whiteboard.view.BaseImageView;

public interface IPage {
	
	public static final int MODE_UNKNOWN = 0;
	
	public static final int MODE_NORMAL = 1;
	
	public static final int MODE_DOCUMENT = 2;
	
	boolean isLock();
	
	boolean isNeedSave();

	boolean isEmpty();

	void lock(boolean lock);

	boolean hasGraphs();

	void setId(long id);

	void setRemotePageId(String remotePageId);
	
	long getId();

	String getRemotePageId();
	
    void setName(String name);
	
	String getName();

	Matrix getMatrix();

	int getCurTLScX();

	void setCurTLScX(int curTLScX);

	int getCurTLScY();

	void setCurTLScY(int curTLScY);

	void setBackGroundColor(int color);

	int getBackGroundColor();
	
	boolean containsImage(int dwId);
	
	Image getImage(int dwId);
	
	void scale(float scale);

	void postScale(float scale,float focusX,float focusY);
	
	void rotate(int rotate, boolean isComplete);

	void postRotate(int rotate, boolean isComplete);

	boolean translate(float ox, float oy);

	boolean postTranslate(float ox, float oy);
	
	void selectSubPage(int index);
	
	void selectSubPage(long imageId);
	
	void addSubPage(SubPage sp);
	
	boolean nextSubPage(); 
	
	boolean PreviousSubPage();
	
	boolean hasNextSubpage();
	
	boolean hasPreSubPage();
	
	int getSubPageCount();
	
	ISubPage getCurSubPage();
	
	ISubPage getSubPage(int index);
	
	ArrayList<SubPage> getSubPageList();
	
	int getCurSubPageIndex();
	
	float getCurScale();
	
	int getCurAngle();
	
	float getOffsetX();
	
	float getOffsetY();
	
	void draw(Canvas canvas);
	
	void drawImage(Canvas canvas);
	
	IOperation undo();
	
	IOperation redo();
	
	boolean undoEnable();
	
	boolean redoEnable();
	
	void clearAll();
	
	Bitmap getPageThumbnail();
	
	/**
	 * 保存当前子页到sdcard
	 * @param saveDir 保存路径 不包含文件名称
	 * @param fileName 文件名称
	 * @param callBack 回掉接口
	 */
	void saveCurSubPageToImage(String saveDir,String fileName, ISavePageCallBack callBack);
	
	/**
	 * 保存所有子页到sdcard 
	 * @param saveDir 保存路径，保存时会创建以当前白板名称命名的文件夹，所有的子页都会保存到这个文件夹内。
	 * @param fileName 文件名称
	 * @param callBack  回掉接口
	 */
	void saveToImage(String saveDir,String fileName,ISavePageCallBack callBack);


	void saveImageToCache(String saveDir,String fileName,ISavePageCallBack callBack);
	
	void setUndoAndRedoListener(IUndoAndRedoListener listener);

	void imgWidthToScreenWidth();

	void imgHeightToScreenHeight();

	void selfAdaption();

	void oneToOne();

	void destory();


	void compatibilityMtAreaErase(AreaErase areaErase);

}
