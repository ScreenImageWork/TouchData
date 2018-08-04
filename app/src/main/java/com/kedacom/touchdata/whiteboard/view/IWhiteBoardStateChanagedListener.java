package com.kedacom.touchdata.whiteboard.view;

import com.kedacom.touchdata.whiteboard.graph.Graph;

/**
 * 主要提供白板状态改变信息
 * @author zhanglei
 *
 */
public interface IWhiteBoardStateChanagedListener {
    /**
     * 白板缩放
     * @param curScale 缩放级别
     */
	void onScaleChanged(float curScale);

	void onScaleChangedFromGesture(float scaleFactor,float focusX,float focusY);

	void onUpdateScaleUI(float curScale);
	
	/**
	 * 白板旋转
	 * @param angle 旋转角度
	 */
	void onRotateChanged(int angle, boolean isFinish);
	
	/**
	 * 撤销
	 */
	void onUndo();
	
	/**
	 * 还原
	 */
	void onRedo();
	
	/**
	 * 是否还可以撤销
	 * @param enable
	 */
	void onUndoEnable(boolean enable);
	
	/**
	 * 是否还可以还原
	 * @param enable
	 */
	void onRedoEnable(boolean enable);
	
	/**
	 * 有图元更新
	 * @param graph 更新的图元
	 */
	void onGraphUpdate(Graph graph);
	
	/**
	 * 白板平移
	 * @param ox X轴平移距离
	 * @param oy Y轴平移距离
	 */
	void onTranslateChanged(float ox, float oy, boolean isFinish);

	/**
	 * 当前坐标系改变时回调，替换之前的缩放和平移回调接口
	 */
	void onCoordinateChanged();
	
	/**
	 * 白板页面改变时的状态信息  
	 * @param pageIndex 当前的tab页索引
	 * @param curSubPageIndex 当前的tab子页索引
	 * @param subPageNum 当前的子页总数
	 * @param nextSubPageEnable 是否有下个子页
	 * @param lastSubPageEnable 是否可以切换到上一个子页
	 */
	void onPageChanged(int pageIndex, int curSubPageIndex, int subPageNum, boolean nextSubPageEnable, boolean lastSubPageEnable);


	void cancelSelecImageTimer();

	void onDelSelectImg(int imgId);

	void onPaintDrawDown();

}
