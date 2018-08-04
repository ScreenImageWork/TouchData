package com.kedacom.touchdata.whiteboard.graph;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

public class Pen extends Graph{
	
	private ArrayList<PointF> pointList = new ArrayList<PointF>();

	private Path curPath;

	protected float mLastPointX;
	protected float mLastPointY;

	protected float mPreviousX;
	protected float mPreviousY;


	public Pen(){
		super(WhiteBoardUtils.GRAPH_PEN);
		//设置图元类型
		init();
	}

	private void init(){
		paint.setPathEffect(new CornerPathEffect(0));
		paint.setColor(color);
		paint.setStrokeWidth(strokeWidth);
	}
	
	@Override
	public void setStrokeWidth(float width) {
		super.setStrokeWidth(width);
		paint.setStrokeWidth(width);
	}

	@Override
	public void setColor(int color) {
		super.setColor(color);
		paint.setColor(color);
	}

	boolean isLineTo = true;
	@Override
	public void addPoint(float x, float y) {
		float f1 = 0;
		float f2 = 0;
		if(curPath==null){
			mLastPointX = x;
			mLastPointY = y;
			mPreviousX = x+1;
			mPreviousY = y+1;
			curPath = new Path();
			curPath.moveTo(mLastPointX,mLastPointY);
//			curPath.lineTo(mPreviousX,mPreviousY);
			f1 = (x + this.mPreviousX) / 2.0F;
			f2 = (y + this.mPreviousY) / 2.0F;
			curPath.lineTo(x,y);
		}else if(isLineTo){
			curPath.lineTo(x,y);
			f1 = (x + this.mPreviousX) / 2.0F;
			f2 = (y + this.mPreviousY) / 2.0F;
			isLineTo = false;
		} else{
			f1 = (x + this.mPreviousX) / 2.0F;
			f2 = (y + this.mPreviousY) / 2.0F;
			curPath.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
		}
		this.mPreviousX = x;
		this.mPreviousY = y;
		this.mLastPointX = f1;
		this.mLastPointY = f2;
		pointList.add(new PointF(x, y));
	}

	@Override
	public void draw(Canvas canvas) {
		if(canvas==null)return;
//		canvas.save();
//		canvas.translate(offsetX, offsetY);
//		canvas.scale(curScale, curScale);
//		canvas.rotate(curAngle);
		canvas.drawPath(curPath, paint);
//		canvas.restore();
	}
	

	@Override
	public ArrayList<Point> getPoints() {
		ArrayList<Point> points = new ArrayList<>();

		for(PointF pf : pointList){
			points.add(new Point((int)pf.x,(int)pf.y));
		}

		return points;
	}
	

	@Override
	public Paint getPaint() {
		return paint;
	}
	

	@Override
	public Rect getBounds() {
		RectF bounds = new RectF();
		curPath.computeBounds(bounds, true);
		Rect dst = new Rect();
		bounds.roundOut(dst);
		dst.sort();
		dst.right = dst.right+(int)getStrokeWidth()*3;
		dst.bottom = dst.bottom+(int)getStrokeWidth()*3;
		return dst;
	}


	public void changeCoordinate(Matrix matrix,float scale){
		//1.获取当前矩阵的逆向矩阵
		Matrix inverse = new Matrix();
		matrix.invert(inverse);

		int pointCount = pointList.size();

		float points[] = new float[pointCount*2];

		int index = 0;

		//2.取出所有的坐标点
		for(int i = 0;i<pointCount;i++){
			PointF point = pointList.get(i);
			points[index++] = point.x;
			points[index++] = point.y;
		}

		float dstPoints[] = new float[pointCount*2];
        //3.将所有的坐标点进行逆向转换
		inverse.mapPoints(dstPoints,points);

		pointList.clear();
		curPath.reset();
		curPath = null;
		index = 0;
		//4.重新保存转换后的坐标点
		for(int i = 0;i<pointCount;i++){
			if(pointCount-1 == i){//最后一个点设置为lineTo
				isLineTo = true;
			}
			addPoint(dstPoints[index++],dstPoints[index++]);
		}

		inverse = null;
		points = null;
		dstPoints = null;
	}


	@Override
	public void destroy() {
		pointList.clear();
		pointList = null;
		curPath = null;
		paint = null;
	}

}
