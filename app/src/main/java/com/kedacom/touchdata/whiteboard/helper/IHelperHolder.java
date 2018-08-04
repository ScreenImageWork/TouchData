package com.kedacom.touchdata.whiteboard.helper;

import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.view.DisplayTouchView;

public interface IHelperHolder extends IHelper {

	void init(float scale,int angle,float offsetX,float offsetY);

	void setDisplayTouchView(DisplayTouchView dtv);

	//设置当前擦除状态
	void setOpType(int type);

	int getCurOpType();
	
	void setPaintColor(int color);
	
	int getPaintColor();
	
	void setPaintStrokeWidth(float width);

	float getPaintStrokeWidth();

	void setDrawEnable(boolean enable);

	void rotate(int angle, boolean isFinish);
	
	void scale(float scale);

	void postScale(float scaleFactor,int focusX,int focusY);
	
	void translate(float ox, float oy);

	void postTranslate(float ox, float oy);

	void transform(float scale,int angle,float ox, float oy);

	void postTransform(float scale,float spx,float spy,int angle,float ox, float oy);
	
	void undo();
	
	void redo();

	//全屏擦除，清屏
	void clearScreen();
	
	void requestDrawGraph(Graph graph);

	void requestDrawGraphForSyn(Graph graph, PageManager pageManager);

	void refreshScreen();

	void setBackgroundColor(int color);

	void widthSelf();

	void heightSelf();

	void selfAdaption();

	void oneToOne();

	void selectImage(ImageGraph graph);

	void resetSelectImage();

	void updateGestureCurScale(float scale);

	void lockWb(boolean lock);

	boolean isLockWb();
}
