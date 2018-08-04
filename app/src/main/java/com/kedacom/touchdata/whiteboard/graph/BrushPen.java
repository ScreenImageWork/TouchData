package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Canvas;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.helper.DrawBrushPenHelper;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2018/4/17.
 * 带笔锋图元
 */

public class BrushPen extends Graph{

    private final float STROKEWIDTH = 5;

    private ArrayList<DrawBrushPenHelper.BrushPenSegment> segmentList = new ArrayList<DrawBrushPenHelper.BrushPenSegment>();

    private Rect bounds ;

    public BrushPen() {
        super(WhiteBoardUtils.GRAPH_BRUSHPEN);
        init();
    }

    private void init(){
        paint.setPathEffect(new CornerPathEffect(5));
        paint.setColor(color);
        paint.setStrokeWidth(strokeWidth);
        paint.setStrokeWidth(STROKEWIDTH);
    }

    @Override
    @Deprecated
    public void addPoint(float x, float y) {
    }

    public void addSegment(DrawBrushPenHelper.BrushPenSegment segment) {
        segmentList.add(segment);
    }


    @Override
    public void draw(Canvas canvas) {
            TPLog.printKeyStatus("qwqwqwqqwqwqq---------------segmentList.size = "+segmentList.size());
            if (!segmentList.isEmpty()) {
                for (int i = 0; i < segmentList.size(); i++) {
                    segmentList.get(i).draw(canvas,paint);
                }
            }
    }

    @Override
    public void changeCoordinate(Matrix matrix, float scale) {

      //1.获取当前矩阵的逆向矩阵
        Matrix inverse = new Matrix();
        matrix.invert(inverse);

        int pointCount = segmentList.size();

        float points[] = new float[pointCount*2];

        int index = 0;

        //2.取出所有的坐标点
        for(int i = 0;i<pointCount;i++){
            PointF point = new PointF(segmentList.get(i).getCurX(),segmentList.get(i).getCurY());
            points[index++] = point.x;
            points[index++] = point.y;
        }

        float dstPoints[] = new float[pointCount*2];
        //3.将所有的坐标点进行逆向转换
        inverse.mapPoints(dstPoints,points);


        String msg = "";
        for(int i = 0;i<dstPoints.length;i++){
            msg = msg + dstPoints[i]+",";
        }

        TPLog.printKeyStatus("lalalalalal1---------------------->"+msg);

        msg = "";
        for(int i = 0;i<points.length;i++){
            msg = msg + points[i]+",";
        }

        TPLog.printKeyStatus("lalalalalal2---------------------->"+msg);


        index = 0;

         float mLastPointX = 0;
         float mLastPointY = 0;

         float mPreviousX = 0;
         float mPreviousY = 0;

        TPLog.printKeyStatus("Test changeCoordinate points.length = "+points.length+",dstPoints.length = "+dstPoints.length);
        TPLog.printKeyStatus("Test changeCoordinate pointCount = "+pointCount);
        //4.重新保存转换后的坐标点
        for(int i = 0;i<pointCount;i++){
            float x = dstPoints[index++];
            float y = dstPoints[index++];
            float f1 = 0;
            float f2 = 0;
            if(i == 0){
                mLastPointX = x;
                mLastPointY = y;
                mPreviousX = x+1;
                mPreviousY = y+1;
                f1 = (x + mPreviousX) / 2.0F;
                f2 = (y + mPreviousY) / 2.0F;
                segmentList.get(i).setCurX(x);
                segmentList.get(i).setCurY(y);
                segmentList.get(i).moveTo(mLastPointX, mLastPointY);
                segmentList.get(i).cubicTo(mLastPointX, mLastPointY, mPreviousX, mPreviousY, f1, f2);
                mLastPointX = f1;
                mLastPointY = f2;
            }else if(i == pointCount-1){//最后一个点
                f1 = (x + mPreviousX) / 2.0F;
                f2 = (y + mPreviousY) / 2.0F;
                segmentList.get(i).moveTo(mLastPointX, mLastPointY);
                mLastPointX = f1;
                mLastPointY = f2;
                segmentList.get(i).lineTo(x, y);
                mPreviousX = x;
                mPreviousY = y;
            }else{
                f1 = (x + mPreviousX) / 2.0F;
                f2 = (y + mPreviousY) / 2.0F;
                segmentList.get(i).moveTo(mLastPointX, mLastPointY);
                segmentList.get(i).cubicTo(mLastPointX, mLastPointY, mPreviousX, mPreviousY, f1, f2);
                mPreviousX = x;
                mPreviousY = y;
                mLastPointX = f1;
                mLastPointY = f2;
            }
        }
        inverse = null;
        points = null;
        dstPoints = null;
    }

    @Override
    @Deprecated
    public ArrayList<Point> getPoints() {
        return new ArrayList<>();
    }
    public ArrayList<DrawBrushPenHelper.BrushPenSegment> getSegmentes() {
        return segmentList;
    }

    @Override
    public Rect getBounds() {
        if(bounds != null){
            return bounds;
        }

        bounds = new Rect(Integer.MAX_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MIN_VALUE);

        for(int i = 0;i<segmentList.size();i++){

            Rect rect = segmentList.get(i).getBounds();

            if(bounds.left>rect.left){
                bounds.left = rect.left;
            }

            if(bounds.top>rect.top){
                bounds.top = rect.top;
            }

            if(bounds.right < rect.right){
                bounds.right = rect.right;
            }

            if(bounds.bottom < rect.bottom){
                bounds.bottom = rect.bottom;
            }
        }

        bounds.sort();

        return bounds;
    }

    @Override
    public void destroy() {
        segmentList.clear();
        segmentList = null;
        paint = null;
    }

    @Override
    public Paint getPaint() {
        return paint;
    }
}
