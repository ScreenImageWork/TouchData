package com.kedacom.touchdata.whiteboard.page;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.utils.IUndoAndRedoListener;
import com.kedacom.touchdata.whiteboard.utils.SavePageUtils.ISavePageCallBack;
import com.kedacom.touchdata.whiteboard.view.BaseImageView;

public interface ISubPage{

	boolean isNeedSave();

	boolean isEmpty();

	void addGraph(Graph graph);
	
	void setImage(Image image);

	Image getImage();

	Matrix getMatrix();

	void setMatrix(Matrix matrix);

	void setMatrixValues(float values[]);

	void setBackgroundColor(int color);

	int getBackgroundColor();
	
	ArrayList<Graph> getGraphList();

	ArrayList<Graph> getImageGraphList();

	ImageGraph getImgGraphFromRemoteId(String remoteId);
	
	void draw(Canvas canvas);
	
	//void drawImage(BaseImageView imageView);

	void drawImage(Canvas canvas);
	
	IOperation undo();
	
	IOperation redo();
	
	boolean undoEnable();
	
	boolean redoEnable();
	
	List<IOperation> getUndoList();
	
	List<IOperation> getRedoList();
	
	void saveToImage(String saveDir,String fileName);
	
	void save(Canvas canvas);
	
	void saveToImage(String saveDir,String fileName, ISavePageCallBack callBack);

	Bitmap getPageThumbnail(int backgroundColor);
	
	void setUndoAndRedoListener(IUndoAndRedoListener listener);

	void imgWidthToScreenWidth();

	void imgHeightToScreenHeight();

	void destroy();

	void compatibilityMtAreaErase(AreaErase erase);
}
