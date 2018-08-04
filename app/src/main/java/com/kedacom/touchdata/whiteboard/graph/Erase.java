package com.kedacom.touchdata.whiteboard.graph;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

public class Erase extends Graph {

	private ArrayList<Point> pointList = new ArrayList<Point>();

	private ArrayList<RectF> rectfList = new ArrayList<RectF>();

	private Path curPath;

	private int eraseWidth;

	private int eraseHeight;

	public Erase() {
		super(WhiteBoardUtils.GRAPH_ERASE);
		init();
	}

	public Erase(Parcel arg0) {
		super(WhiteBoardUtils.GRAPH_ERASE);

		id = arg0.readInt();
		tabId = arg0.readLong();
		pageIndex = arg0.readLong();
		strokeWidth = arg0.readFloat();
		color = arg0.readInt();
		graphType = arg0.readInt();
		curScale = arg0.readFloat();
		curAngle = arg0.readInt();
		offsetX = arg0.readFloat();
		offsetY = arg0.readFloat();

		boolean boo[] = new boolean[1];
		arg0.readBooleanArray(boo);
		isSelected = boo[0];

		Parcelable points[] = arg0.readParcelableArray(Point.class.getClassLoader());


		for (int i = 0; i < points.length; i++) {
			Point p = (Point) points[i];
			addPoint(p.x, p.y);
		}

		init();
	}

	private void init() {

		paint.reset();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStrokeWidth(strokeWidth);
		paint.setStyle(Paint.Style.FILL);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeJoin(Paint.Join.ROUND);
//		paint.setPathEffect(new CornerPathEffect(50));
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

//		paint.setColor(Color.BLACK);

	}

	public int getEraseWidth() {
		return eraseWidth;
	}

	public void setEraseWidth(int eraseWidth) {
		this.eraseWidth = eraseWidth;
	}

	public int getEraseHeight() {
		return eraseHeight;
	}

	public void setEraseHeight(int eraseHeight) {
		this.eraseHeight = eraseHeight;
	}

	@Override
	public void setStrokeWidth(float width) {
		super.setStrokeWidth(width);
	}

	//这里改下，现在擦除暂时改为矩形擦除，Path内填充的不再是点了，而是矩形
	@Override
	public void addPoint(float x, float y) {
		float cx = x - eraseWidth / 2f;
		float cy = y - eraseHeight / 2f;
		RectF rect = new RectF();
		rect.set(cx, cy, cx + eraseWidth, cy + eraseHeight);
		if (curPath == null) {
			curPath = new Path();
		}

		if (rectfList.size() != 0) {
			RectF lastRectF = rectfList.get(rectfList.size() - 1);
			complementedPath(lastRectF, rect);
		}

		rectfList.add(rect);
		curPath.addRect(rect, Path.Direction.CCW);

		pointList.add(new Point((int) x, (int) y));
	}

	@Override
	public void draw(Canvas canvas) {
		if (canvas == null) return;
//		canvas.save();
//		canvas.translate(offsetX, offsetY);
//		canvas.scale(curScale, curScale);
//		canvas.rotate(curAngle);
		canvas.drawPath(curPath, paint);
//		canvas.restore();
	}


	@Override
	public ArrayList<Point> getPoints() {
		return pointList;
	}


	@Override
	public Paint getPaint() {
		//init();
		return paint;
	}


	@Override
	public Rect getBounds() {
		RectF bounds = new RectF();
		curPath.computeBounds(bounds, true);
		Rect dst = new Rect();
		bounds.roundOut(dst);
		dst.sort();
		int c = (int) (strokeWidth / 2f);
		dst.set(dst.left - c, dst.top - c, dst.right + c, dst.bottom + c);
		return dst;
	}

	@Override
	public void changeCoordinate(Matrix matrix, float scale) {
		//1.获取当前矩阵的逆向矩阵
		Matrix inverse = new Matrix();
		matrix.invert(inverse);

		eraseWidth = (int) ((float)eraseWidth/scale);
		eraseHeight = (int)((float)eraseHeight/scale);
//
//		curPath.transform(matrix);
//
//		setStrokeWidth(strokeWidth / scale);

		int pointCount = pointList.size();

		float points[] = new float[pointCount*2];

		float rectPoints[] = new float[pointCount*8];

		int index = 0;

		//2.取出所有的坐标点
		for(int i = 0;i<pointCount;i++){
			Point point = pointList.get(i);
			points[index++] = point.x;
			points[index++] = point.y;
		}

		float dstPoints[] = new float[pointCount*2];
		//3.将所有的坐标点进行逆向转换
		inverse.mapPoints(dstPoints,points);

		pointList.clear();
		rectfList.clear();
		curPath.reset();
		curPath = null;

		index = 0;
		//4.重新保存转换后的坐标点
		for(int i = 0;i<pointCount;i++){
			addPoint(dstPoints[index++], dstPoints[index++]);
		}

		inverse = null;
		points = null;
		dstPoints = null;

		//setStrokeWidth(strokeWidth/scale);
	}

	private void complementedPath(RectF lastRect, RectF curRect) {
		if (!lastRect.intersect(curRect)) {
			float lcx = lastRect.centerX();
			float lcy = lastRect.centerY();

			float ccx = curRect.centerX();
			float ccy = curRect.centerY();

			float ncx = ((lcx + ccx) / 2f) - (eraseWidth / 2f);
			float ncy = ((lcy + ccy) / 2f) - (eraseHeight / 2f);

			RectF rect2 = new RectF();
			rect2.set(ncx, ncy, (ncx + eraseWidth), (ncy + eraseHeight));


			curPath.addRect(rect2, Path.Direction.CCW);
			rectfList.add(rect2);

			boolean boo1 = RectF.intersects(rect2,lastRect);
			boolean boo2 = RectF.intersects(rect2,curRect);

			if(!boo1){
				complementedPath(lastRect, rect2);
			}

			if(!boo2){
				complementedPath(curRect, rect2);
			}
			}
}

	/**
	 * 这个函数只有绘图中，才有用，其他时候请勿用
	 * @param matrix
	 * @param scale
	 * @return
     */
	public Path getCurAbsolutePath(Matrix matrix, float scale){
		//1.获取当前矩阵的逆向矩阵
		Matrix inverse = new Matrix();
		matrix.invert(inverse);

		float eraseWidth = this.eraseWidth;
		float eraseHeight = this.eraseHeight;
		 eraseWidth = (int) (eraseWidth/scale);
		 eraseHeight = (int)(eraseHeight/scale);
//
//		curPath.transform(matrix);
//
//		setStrokeWidth(strokeWidth / scale);

		List<Point> pointList = new ArrayList<Point>();

		for(int i = 0;i<this.pointList.size();i++){
			pointList.add(this.pointList.get(i));
		}

		int pointCount = pointList.size();

		float points[] = new float[pointCount*2];

		int index = 0;

		//2.取出所有的坐标点
		for(int i = 0;i<pointCount;i++){
			Point point = pointList.get(i);
			points[index++] = point.x;
			points[index++] = point.y;
		}

		float dstPoints[] = new float[pointCount*2];
		//3.将所有的坐标点进行逆向转换
		inverse.mapPoints(dstPoints,points);

		Path curPath = new Path();

		index = 0;
		//4.重新保存转换后的坐标点
		for(int i = 0;i<pointCount;i++){
			float x = dstPoints[index++];
			float y = dstPoints[index++];
			float cx = x - eraseWidth / 2f;
			float cy = y - eraseHeight / 2f;
			RectF rect = new RectF();
			rect.set(cx, cy, cx + eraseWidth, cy + eraseHeight);
			if (curPath == null) {
				curPath = new Path();
			}
			curPath.addRect(rect, Path.Direction.CCW);
		}

		inverse = null;
		points = null;
		dstPoints = null;
		pointList.clear();
		pointList = null;

		return curPath;
	}

	@Override
	public void destroy() {
		pointList.clear();
		pointList = null;
		curPath = null;
		paint = null;
	}
	
}
