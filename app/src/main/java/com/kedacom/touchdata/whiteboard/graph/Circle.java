package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2018/5/4.
 */

public class Circle extends Graph {

    private Point startPoint;

    private Point endPoint;

    private Rect rect = new Rect();

    public Circle() {
        super(WhiteBoardUtils.GRAPH_CIRCLE);
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
//        canvas.drawLine(startPoint.x,startPoint.y,endPoint.x,endPoint.y,paint);
        canvas.drawOval(new RectF(rect.left,rect.top,rect.right,rect.bottom),paint);
//        canvas.drawCircle(rect.centerX(),rect.centerY(),rect.width()/2,paint);
    }

    @Override
    public void changeCoordinate(Matrix matrix, float scale) {

    }

    @Override
    public ArrayList<Point> getPoints() {
        ArrayList<Point> list = new ArrayList<Point>();
        list.add(startPoint);
        list.add(endPoint);
        return list;
    }

    @Override
    public Rect getBounds() {
        return rect;
    }

    @Override
    public void destroy() {
        startPoint = null;
        endPoint = null;
        paint = null;
        rect = null;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }
}

