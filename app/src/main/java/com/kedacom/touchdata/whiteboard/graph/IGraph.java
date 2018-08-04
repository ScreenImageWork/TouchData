package com.kedacom.touchdata.whiteboard.graph;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.kedacom.touchdata.whiteboard.data.Point;

public interface IGraph {
	
	void setStrokeWidth(float width);
	
	void setColor(int color);
	
	void setSelect(boolean select);
	
	void addPoint(float x, float y);
	
	void draw(Canvas canvas);
	
	void scale(float scale);
	
	void translateTo(float ox, float oy);
	
	void translateBy(float ox, float oy);
	
	void rotate(int angle);
	

	void changeCoordinate(Matrix matrix,float scale);

	
	int getGraphType();
	
	ArrayList<Point> getPoints();
	
	Rect getBounds();

	void destroy();
}
