package com.kedacom.touchdata.whiteboard.helper;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

import com.kedacom.touchdata.whiteboard.graph.BrushPen;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.msg.entity.BrushPenPaintMsgState;
import com.kedacom.touchdata.whiteboard.msg.entity.MsgEntity;
import com.kedacom.touchdata.whiteboard.msg.entity.PaintMsgState;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.touch.touchsdk.TouchEvent;

/**
 * Created by zhanglei on 2018/4/17.
 */

public class DrawBrushPenHelper implements IDrawHelper{

    private final float STROKE_THRESHOLD = 0.3f;
    private final float STROKE_SPEED = 0.5f;
    private final float STROKE_MIN = 2;

    protected float mLastPointX;
    protected float mLastPointY;

    protected float mPreviousX;
    protected float mPreviousY;

    private float mPX;
    private float mPY;

    private Paint paint;

    private final float STROKEWIDTH = 5;

    private BrushPen curPen;

    private IHelperListener mHelperListener;

    public DrawBrushPenHelper(IHelperListener listener){
        mHelperListener = listener;
    }

    @Override
    public void setDrawType(int drawType) {
    }

    @Override
    @Deprecated
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(TouchEvent event) {
        int action = event.getAction();
        TPLog.printError("HuaXinSdk Action = "+action);

        this.mPX = event.getX();
        this.mPY = event.getY();

        float strokeWidth = computeStrokeWidth(event);
        BrushPenSegment segment = new BrushPenSegment();
        segment.setPressure(event.getPressure());
        segment.setStrokeWidth(strokeWidth);
        segment.setCurX(mPX);
        segment.setCurY(mPY);

        boolean isComplete = false;
        switch (action) {
            case TouchEvent.ACTION_DOWN:
                touchDown(event,segment);
                break;
            case TouchEvent.ACTION_MOVE:
                touchMove(event,segment);
                break;
            case TouchEvent.ACTION_UP:
                isComplete = true;
                touchUp(event,segment);
                break;
        }
        requestPaint(segment,isComplete);
        return true;
    }

    public float computeStrokeWidth(TouchEvent event){

        float curStrokeWidth = STROKEWIDTH * event.getPressure()/10f;

        if(Float.isNaN(curStrokeWidth)){
            curStrokeWidth = STROKE_MIN;
        }

        if (curStrokeWidth < STROKE_MIN) {
            curStrokeWidth = STROKE_MIN;
        }

        return curStrokeWidth;
    }

    private void touchDown(TouchEvent event, BrushPenSegment segment){
        curPen = (BrushPen) GraphFactory.makeGraph(WhiteBoardUtils.GRAPH_BRUSHPEN);
        drawStart(mPX,mPY,segment);
    }

    private void touchMove(TouchEvent event, BrushPenSegment segment){
        drawIng(mPX,mPY,segment);
    }

    private void touchUp(TouchEvent event, BrushPenSegment segment){
        drawEnd(mPX,mPY,segment);
    }

    public void drawStart(float x,float y,BrushPenSegment segment) {
        mLastPointX = x;
        mLastPointY = y;
        mPreviousX = x+1;
        mPreviousY = y+1;

        //2017.08.19添加，手指点击也会绘图
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        segment.moveTo(mLastPointX, mLastPointY);
        segment.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
        this.mLastPointX = f1;
        this.mLastPointY = f2;
        curPen.addSegment(segment);
    }

    public void drawIng(float x,float y,BrushPenSegment segment) {
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        segment.moveTo(mLastPointX, mLastPointY);
        segment.cubicTo(this.mLastPointX, this.mLastPointY, this.mPreviousX, this.mPreviousY, f1, f2);
        this.mPreviousX = x;
        this.mPreviousY = y;
        this.mLastPointX = f1;
        this.mLastPointY = f2;
        curPen.addSegment(segment);
    }

    public void drawEnd(float x,float y,BrushPenSegment segment) {
        float f1 = (x + this.mPreviousX) / 2.0F;
        float f2 = (y + this.mPreviousY) / 2.0F;
        segment.moveTo(this.mLastPointX, this.mLastPointY);
        this.mLastPointX = f1;
        this.mLastPointY = f2;
        segment.lineTo(x, y);
        this.mPreviousX = x;
        this.mPreviousY = y;
        curPen.addSegment(segment);
    }

    @Override
    public void onDestory() {

    }

    private void requestPaint(BrushPenSegment curPath , boolean isComplete){
        TPLog.printKeyStatus("touchCallBack-------------->requestPaint = isComplete = "+isComplete);
        if(mHelperListener!=null){
            BrushPenPaintMsgState paintMsgState = null;
            paintMsgState = new BrushPenPaintMsgState(curPath,curPen,isComplete,false);
            mHelperListener.requestPaint(new MsgEntity(paintMsgState));
        }
    }


  public  class BrushPenSegment{

        private float curX;

        private float curY;

        private Path path = new Path();

        private float pressure;

        private float strokeWidth;

        private Rect bounds;

        public BrushPenSegment(){

        }

        public void moveTo(float x,float y){
            path.reset();
            path.moveTo(x,y);
        }

        public void lineTo(float x,float y){
            path.lineTo(x,y);
        }

        public void cubicTo(float mLastPointX, float mLastPointY, float mPreviousX, float mPreviousY, float f1, float f2){
            path.cubicTo(mLastPointX,mLastPointY,mPreviousX,mPreviousY,f1,f2);
        }

        public void draw(Canvas canvas, Paint paint){
            TPLog.printKeyStatus("Brush strokeWidth = "+strokeWidth);
            paint.setStrokeWidth(strokeWidth);
            canvas.drawPath(path,paint);
        }

        public float getCurX() {
            return curX;
        }

        public void setCurX(float curX) {
            this.curX = curX;
        }

        public float getCurY() {
            return curY;
        }

        public void setCurY(float curY) {
            this.curY = curY;
        }

        public Path getPath() {
            return path;
        }

        public float getStrokeWidth() {
            return strokeWidth;
        }

        public void setStrokeWidth(float strokeWidth) {
            this.strokeWidth = strokeWidth;
        }

        public float getPressure() {
            return pressure;
        }

        public void setPressure(float pressure) {
            this.pressure = pressure;
        }

        public Rect getBounds(){
            if(bounds== null){
                bounds = new Rect();
                RectF rf = new RectF();
                path.computeBounds(rf,true);
                bounds.set((int)(rf.left - 10),(int)(rf.top-10),(int)(rf.right + 10),(int)(rf.bottom + 10));
            }

            return bounds;
        }
    }
}
