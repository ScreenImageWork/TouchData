package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

import java.util.ArrayList;

public class Pen_bk extends Graph{

	private ArrayList<PointF> pointList = new ArrayList<PointF>();

	private Path curPath;

	public Pen_bk(){
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

	@Override
	public void addPoint(float x, float y) {
		if(curPath==null){
			curPath = new Path();
			curPath.moveTo(x, y);
		}else{
			curPath.lineTo(x, y);
		}
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
