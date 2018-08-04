package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

import java.util.ArrayList;

public abstract class Graph implements IGraph{
	
	protected int id;

	protected String remoteId;

	protected long tabId;

	protected String remotePageId;
	
	protected long pageIndex;
	
	protected float strokeWidth = 1.0f;
	
	protected int color;
	
	protected int graphType;
	
	protected float curScale = 1.0f;
	
	protected int curAngle = 0;
	
	protected float offsetX = 0;
	
	protected float offsetY = 0;
	
	protected boolean isSelected = false;
	
	protected Paint paint;

	private String curEraseId;
	// TODO: 2018/7/30  
	private ArrayList<Graph> pointLists = new ArrayList<>();

	 

	//当前图元索引，主要是为了防止同时发送图元信息时出现图元交换现象，如：擦除和铅笔绘图
	protected int graphIndex = -1;//当前图元索引
	
	public Graph(int type){
		setGraphType(type);
		init();
//		id = (int)WhiteBoardUtils.getId();
		setRemoteId(WhiteBoardUtils.getRemoteId());
		setId(getRemoteId().hashCode());
	}

	protected void setGraphType(int type){
		graphType = type;
	}
	
	public void setId(int id){
		this.id = id;
	}

	public void setRemoteId(String id){
		this.remoteId = id;
	}

	public int getId(){
		return id;
	}

	public String getRemoteId(){
		return remoteId;
	}

	public int getGraphIndex() {
		return graphIndex;
	}

	public void setGraphIndex(int graphIndex) {
		this.graphIndex = graphIndex;
	}

	public long getTabId() {
		return tabId;
	}

	public String getRemotePageId(){
		return remotePageId;
	}

	public void setTabId(long tabId) {
		this.tabId = tabId;
	}

	public void setRemotePageId(String remotePageId){
		this.remotePageId = remotePageId;
	}

	public long getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(long pageIndex) {
		this.pageIndex = pageIndex;
	}

	public String getCurEraseId() {
		return curEraseId;
	}

	public void setCurEraseId(String curEraseId) {
		this.curEraseId = curEraseId;
	}

	@Override
	public void setStrokeWidth(float width) {
		strokeWidth = width;
		if(graphType!=WhiteBoardUtils.GRAPH_ERASE_AREA)
		    paint.setStrokeWidth(strokeWidth);
	}

	@Override
	public void setColor(int color) {
		this.color = color;
		paint.setColor(color);
	}
	
	public int getColor(){
		return color;
    }

	@Override
	public void setSelect(boolean select) {
		isSelected = select;
	}
	
	@Override
	public void scale(float scale) {
		curScale = scale;
	}

	@Override
	public void translateTo(float ox, float oy) {
		offsetX = ox;
		offsetY = oy;
	}
	
	@Override
	public void translateBy(float ox, float oy) {
		offsetX += ox;
		offsetY += oy;
	}

	@Override
	public void rotate(int angle) {
		curAngle = angle;
		if(curAngle>360){
			curAngle = curAngle%360;
		}
	}

	@Override
	public int getGraphType() {
		return graphType;
	}

	public float getStrokeWidth(){
		return strokeWidth;
	}
	
	public abstract Paint getPaint();
	
	private void init(){
		paint = new Paint();
		paint.setAntiAlias(true); //设置抗锯齿
		paint.setDither(true);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);
	}


	public Point changeCoordinate(Rect bounds,Point point ,Point cPoint ,int angle, float scale, float offsetX,
			float offsetY){
		
		int x1 = point.x;
		int y1 = point.y;
		
		//平移 
		x1 = (int)(x1) + (int)(offsetX);
		y1 = (int)(y1) + (int)(offsetY);
		
		//缩放
		float cx = x1 - cPoint.x;
		float cy = y1 - cPoint.y;
		
		x1 = (int)(cPoint.x + (cx/scale));
		y1 = (int)(cPoint.y + (cy/scale));

		//旋转
		Point p = GraphUtils.computeRotatePoint(new Point(x1, y1),cPoint, angle);
		
		//Point point1 = new Point(x1, y1);
		return p;
	}


}
