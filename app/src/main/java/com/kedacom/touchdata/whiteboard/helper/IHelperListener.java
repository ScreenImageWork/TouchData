package com.kedacom.touchdata.whiteboard.helper;

import java.util.ArrayList;

import android.graphics.Canvas;

import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.page.IPage;

public interface IHelperListener {

	void setDrawEnable(boolean enable);

    /**
     * 获取当前绘图Canvas
     * @return
     */
	Canvas getCavans();

    /**
	 * 获取当前现实的Page
	 * @return IPage
	 */
	IPage getCurPage();
	
	/**
	 * 保存图元
	 * @param ge  需要保存的图元
	 */
	void saveGraphEntity(Graph ge, boolean isSyn);


	/**
	 * 获取当前页面的图元列表
	 * @return ArrayList<Graph>
	 */
	ArrayList<Graph> getCurGraphList();

	/**
	 * 获取当前页面的图片图元列表
	 * @return ArrayList<Graph>
	 */
	ArrayList<Graph> getCurImageGraphList();
	
	/**
	 * 重绘当前屏幕显示区域
	 */
	void requestRepaint();

    /**
	 * 刷新屏幕
	 */
	void refreshScreen();

	/**
	 * 重绘局部区域
	 * @param paintMsgEntity 需要重绘的区域
	 */
	void updateUI(MsgEntity paintMsgEntity);

	void requestPaint(MsgEntity paintMsgEntity);

	/**
	 * 请求绘制图元  前景绘制，该图元不会保存
	 * @param graph 需要绘制的图元
	 */
	void requestDrawGraph(Graph graph);
	
	/**
	 * 请求绘制图元，绘制到背景，并保存
	 * @param graph 需要绘制的图元
	 */
	void requestDrawGraphAndSave(Graph graph);
	
	/**
	 * 白板缩放 ，作用于当前白板tab页下所有的子页
	 * @param scale 缩放级别
	 * @param  isSyn 是否进行同步
	 */
	void onScale(float scale, boolean isSyn);
	
	/**
	 * 白板旋转，作用于当前白板tab页下所有的子页
	 * @param angle 旋转的角度
	 * @param isComplete 是否添加到可撤销集合，true 添加到，false不添加
	 * @param isSyn  是否进行同步
	 */
	void onRotate(int angle, boolean isComplete, boolean isSyn);
	
	/**
	 * 白板平移，作用与当前显示tab 子页
	 * @param ox 沿X轴移动距离
	 * @param oy 沿Y轴移动距离
	 * @param isSyn 是否同步给其他客户端
	 * @param isfinish 是否执行完毕
	 */
	void onTranslate(float ox, float oy, boolean isSyn, boolean isfinish);


	void onTransform(float scale, int angle, float offsetX, float offsetY);

	void onPostTransform(float scale,float spx,float spy, int angle,float offsetX, float offsetY);
	
	/**
	 * tab页切换
	 * @param pageIndex tab页的索引
	 */
	void onPageChanged(int pageIndex, boolean isSyn);
	
	/**
	 * 子页切换（文档翻页）
	 * @param index 需要切换到额子页索引
	 */
	void onSubPageChanged(int index, boolean isSyn);
	
    /**
     * 撤销  目前可撤销最多5步	
     * @return  当前撤销掉的操作
     */
	IOperation undo(boolean isSend);
	
	/**
	 * 还原，相对于撤销，还原撤销掉的擦做，最多可还原5步
	 * @return 当前还原的操作
	 */
	IOperation redo(boolean isSyn);
	
	/**
	 * 清除屏幕上的所有图元
	 */
	void clearScreen(boolean isSyn);
	
	/**
	 * 获取当前tab也的旋转角度
	 * @return
	 */
	int getCurAngle();

	/**
	 * 获取当前缩放级别
	 * @return
     */
	float getCurScale();

	int getOffsetX();

	int getOffsetY();

	/**
	 * 显示板擦控件
	 * @param width 板擦的宽度
	 * @param height 板擦的高度
	 * @param x 板擦显示x坐标
     * @param y 板擦显示的y坐标
     */
	void displayErasePanel(int width,int height,int x,int y);

	/**
	 * 设置板擦移动
	 * @param x 板擦移动的x坐标
	 * @param y 板擦移动的y坐标
     */
	void erasePanelMoveTo(int x,int y);

	/**
	 * 隐藏板擦
	 */
	void dismissErasePanelWindow();

	boolean erasePanelIsShowing();

	void widthSelf();

	void heightSelf();

	void selfAdaption();

	void oneToOne();

	void updateScaleUI(float scale);

	void cancelSelectImageTimer();

	void onDelSelectImg(int imageId);

	void onDestory();

	void onPaintDrawDown();

    //为了兼容终端区域擦除创建的
	void compatibilityMtAreaErase(AreaErase areaErase);

	void lock(boolean lock);

	boolean isLock();
    
}
