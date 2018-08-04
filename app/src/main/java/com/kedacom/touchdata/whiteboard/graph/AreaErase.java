package com.kedacom.touchdata.whiteboard.graph;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class AreaErase extends Graph {
	private Point startPoint;
	
	private Point endPoint;
	
	private Rect rect = new Rect();
	
	private  DashPathEffect mDashPathEffect = new DashPathEffect(new float[] { 6.0F, 6.0F }, 1.0F);

	private boolean bNexvision = true;

	public AreaErase(){
		super(WhiteBoardUtils.GRAPH_ERASE_AREA);
		setGraphType(WhiteBoardUtils.GRAPH_ERASE_AREA);
		init();
	}
	
	public AreaErase(Parcel arg0){
		super(WhiteBoardUtils.GRAPH_ERASE_AREA);
		id = arg0.readInt();
		tabId = arg0.readLong();
		pageIndex = arg0.readLong();
		strokeWidth = arg0.readFloat();
		color = arg0.readInt();
		graphType = arg0.readInt();
		curScale = arg0.readFloat();
		curAngle =  arg0.readInt();
		offsetX = arg0.readFloat();
		offsetY = arg0.readFloat();
		
		boolean boo[] = new boolean[1];
		arg0.readBooleanArray(boo);
		isSelected = boo[0];
		
		startPoint = arg0.readParcelable(Point.class.getClassLoader());
		endPoint = arg0.readParcelable(Point.class.getClassLoader());
		init();
	}
	
	private void init(){
		paint.setStyle(Paint.Style.STROKE);
		paint.setColor(Color.parseColor("#00aff2"));
		paint.setPathEffect(mDashPathEffect);
	}

	@Override
	public void addPoint(float x, float y) {
		if(startPoint==null){
			startPoint = new Point((int)x, (int)y);
		}else{
			if(endPoint==null){
				endPoint = new Point();
			}
			endPoint.set((int)x, (int)y);
		}
		if(endPoint==null){
		rect.set(startPoint.x, startPoint.y, startPoint.x, startPoint.y);
		}else{
		rect.set(startPoint.x, startPoint.y, endPoint.x, endPoint.y);
		}
		rect.sort();
	}

	@Override
	public void draw(Canvas canvas) {
//		if(canvas==null)return;
//		canvas.save();
//		canvas.translate(offsetX, offsetY);
//		canvas.scale(curScale, curScale);
//		canvas.rotate(curAngle);
		canvas.drawRect(rect, paint);
//		canvas.restore();
	}
	
	public void commitErase(){
		paint.setPathEffect(null);
//		paint.setColor(Color.RED);
		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		paint.setStyle(Paint.Style.FILL);
	}

	@Override
	public ArrayList<Point> getPoints() {
		ArrayList<Point> list = new ArrayList<Point>();
		list.add(startPoint);
		list.add(endPoint);
		return list;
	}
	

	@Override
	public Paint getPaint() {
		return paint;
	}


	@Override
	public Rect getBounds() {
		return rect;
	}

	public boolean isbNexvision() {
		return bNexvision;
	}

	public void setbNexvision(boolean bNexvision) {
		this.bNexvision = bNexvision;
	}

	@Override
	public void changeCoordinate(Matrix matrix,float scale) {

		//1.获取当前矩阵的逆向矩阵
		Matrix inverse = new Matrix();
		matrix.invert(inverse);

		int pointCount = 2;

		float points[] = new float[pointCount*2];

		int index = 0;

		//2.取出所有的坐标点
		points[index++] = startPoint.x;
		points[index++] = startPoint.y;
		points[index++] = endPoint.x;
		points[index++] = endPoint.y;

		float dstPoints[] = new float[pointCount*2];
		//3.将所有的坐标点进行逆向转换
		inverse.mapPoints(dstPoints,points);

		index = 0;
		//4.重新保存转换后的坐标点
		startPoint =null;
		endPoint = null;
		addPoint(dstPoints[index++],dstPoints[index++]);
		addPoint(dstPoints[index++],dstPoints[index++]);

		inverse = null;
		points = null;
		dstPoints = null;
	}


	public Rect getCurAbsolutePath(Matrix matrix,float scale){
		if(endPoint == null||startPoint==null){
			return new Rect();
		}

		//1.获取当前矩阵的逆向矩阵
		Matrix inverse = new Matrix();
		matrix.invert(inverse);

		int pointCount = 2;

		float points[] = new float[pointCount*2];

		int index = 0;

		Point startPoint = new Point(this.startPoint.x,this.startPoint.y);
		Point endPoint = new Point(this.endPoint.x,this.endPoint.y);

		//2.取出所有的坐标点
		points[index++] = startPoint.x;
		points[index++] = startPoint.y;
		points[index++] = endPoint.x;
		points[index++] = endPoint.y;

		float dstPoints[] = new float[pointCount*2];
		//3.将所有的坐标点进行逆向转换
		inverse.mapPoints(dstPoints,points);

		index = 0;
		//4.重新保存转换后的坐标点

		startPoint.set((int)dstPoints[index++],(int)dstPoints[index++]);
		endPoint.set((int)dstPoints[index++],(int)dstPoints[index++]);

		inverse = null;
		points = null;
		dstPoints = null;

		return new Rect(startPoint.x,startPoint.y,endPoint.x,endPoint.y);
	}


	@Override
	public void destroy() {
		startPoint = null;
		endPoint = null;
		rect = null;
		mDashPathEffect = null;
		paint = null;
	}

}
