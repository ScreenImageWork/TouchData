package com.kedacom.touchdata.whiteboard.graph;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Parcel;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Scroller;

import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.helper.ScaleAndTranslateHelper;
import com.kedacom.touchdata.whiteboard.helper.SelectImgHelper;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.GraphUtils;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2017/6/20.
 */
public class SelectGraph extends Graph{

    private static final int SELECTING_PATH_COLOR = Color.parseColor("#00aff2");  //选择操作中Path 颜色

    private static final int SELECTED_BOUND_COLOR = Color.parseColor("#00aff2");  //选择完成后选择框线条颜色

    private static final int SELECTED_CIRCLE_BOUND_COLOR = Color.parseColor("#00aff2");  //选择完成后，选择框上圆形边框线颜色

    private static final int SELECTED_CIRCLE_SOLID_COLOR = Color.parseColor("#ffffff");  //选择完成后，选择框上圆形填充色

    private static final int CLIP_RECT_COLOR = Color.parseColor("#00aff2"); //剪裁边框上的矩形颜色

    private static final int CLIP_SHADOW_COLOR = Color.parseColor("#CC000000"); //剪裁边框上的矩形颜色

    private static final int SELECTING_PATH_FULL_LINE_WIDTH = 4;  //选择操作中实线长度

    private static final int SELECTING_PATH_HOLLOW_LINE_WIDTH = 4;  //选择操作实线间隔距离

    private static  int SELECTING_PATH_FULL_LINE_HEIGHT = 6;  //选择操作中线条的StrokeWidth

    private static final int SELECTED_BOUND_FULL_LINE_WIDTH = 4;  //选则完成后边框实线长度

    private static final int SELECTED_BOUND_HOLLOW_LINE_WIDTH = 4;  //选则完成后实线间隔长度

    private static  int SELECTED_BOUND_HOLLOW_LINE_HEIGHT = 4;  //选择完成后边框线条粗细

    private static  int SELECTED_CIRCLE_RADIUS =6;    //圆形半径，UCD提供的直径是18px，减去描边12px

    private static  int SELECTED_CIRCLE_STROKEWIDTH = 6; //圆形的边框宽度

    private static  int SELECTED_BOUNDS_AND_IMG_SPACING = 10; //选择后边框与图片之间的间隙

    private static final int CLIP_RECT_WIDTH = 30;  //剪裁框上矩形的宽度

    private static final int CLIP_RECT_HEIGHT = 9;  //剪裁框上矩形的高度

    private static final DashPathEffect mSelectingDashPathEffect = new DashPathEffect(new float[] { SELECTING_PATH_FULL_LINE_WIDTH, SELECTING_PATH_HOLLOW_LINE_WIDTH }, 0);

    private static final DashPathEffect mSelectedDashPathEffect = new DashPathEffect(new float[] { SELECTED_BOUND_FULL_LINE_WIDTH, SELECTED_BOUND_HOLLOW_LINE_WIDTH }, 0);

    private float x;

    private float y;

    private float width;

    private float height;

    private Paint paint;

    private Path path;

    private Rect originalBounds = new Rect();

    private ArrayList<Point> pointList = new ArrayList<Point>();

    private boolean selectComplete;

    private Circle circles[] = new Circle[8];//选择框上的8个圆形 左上顺时针排序

    private ClipBoundsRect clipBoundsRect[] = new ClipBoundsRect[12];

    private ManualScale mManualScale = new ManualScale();

    private ManualTranslate mManualTranslate = null;

    private ClipGesture mClipGesture;

    private ToolsBtnManager mToolsBtnManager = new ToolsBtnManager();

    private boolean showToolsBar = false;

    private boolean clipEnable = false;

    public SelectImgHelper selectImgHelper;

    private Rect curClipRect;

    public SelectGraph(Context context){
        super(WhiteBoardUtils.GRAPH_SELECT);
        mManualTranslate = new ManualTranslate(context);
        mClipGesture = new ClipGesture(context);
        init();
    }

    public void setSelectImgHelper(SelectImgHelper mHelper){
        selectImgHelper = mHelper;
    }

    @Override
    public Paint getPaint() {
        return null;
    }

    private void init(){

        if(VersionUtils.isImix()){
            SELECTED_CIRCLE_RADIUS = 4;
            SELECTED_CIRCLE_STROKEWIDTH = 3;
            SELECTED_BOUNDS_AND_IMG_SPACING = 6;
            SELECTING_PATH_FULL_LINE_HEIGHT = 3;
            SELECTED_BOUND_HOLLOW_LINE_HEIGHT = 2;
        }

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setDither(true);
        path = new Path();

        for(int i = 0;i<circles.length;i++){
            circles[i] = new Circle();
        }

        for(int i = 0;i<clipBoundsRect.length;i++){
            clipBoundsRect[i] = new ClipBoundsRect();
        }
    }

    public void computeBounds(){
        originalBounds.set((int)x,(int)y,(int)(x+width),(int)(y+height));
    }

    public void reset(){
        path.reset();
        x = 0;
        y = 0;
        width = 0;
        height = 0;
        selectComplete = false;
        showToolsBar = false;
        if(clipEnable){
            mListener.onClipImgCancel();
        }
        clipEnable = false;
        pointList.clear();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x - SELECTED_BOUNDS_AND_IMG_SPACING;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y - SELECTED_BOUNDS_AND_IMG_SPACING;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width + SELECTED_BOUNDS_AND_IMG_SPACING*2;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height + SELECTED_BOUNDS_AND_IMG_SPACING*2;
    }

    public void setSelectComplete(boolean complete){
        selectComplete = complete;
    }

    public boolean isSelectComplete(){
        return selectComplete;
    }

    public boolean isShowToolsBar() {
        return showToolsBar;
    }

    public void setShowToolsBar(boolean showToolsBar) {
        this.showToolsBar = showToolsBar;
    }

    @Override
    public void addPoint(float x, float y) {
        if(path.isEmpty()){
            path.moveTo(x,y);
        }else{
            path.lineTo(x,y);
        }
        pointList.add(new Point((int)x,(int)y));
    }

    public void preSelectGraphs(ArrayList<Graph> sourceGraphs){
        RectF bounds = new RectF();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int)bounds.left, (int)bounds.top,(int)bounds.right, (int)bounds.bottom));
        int graphCount = sourceGraphs.size();
        for(int i = 0;i<graphCount;i++){
            Graph graph = sourceGraphs.get(i);
            if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
                if(!region.quickReject(graph.getBounds())){
                    ((ImageGraph)graph).setSelecting(true);
                }
            }
        }
    }

    public void getSelectGraphs(ArrayList<Graph> dstGraphs,ArrayList<Graph> sourceGraphs){
        RectF bounds = new RectF();
        path.close();
        path.computeBounds(bounds, true);
        Region region = new Region();
        region.setPath(path, new Region((int)bounds.left, (int)bounds.top,(int)bounds.right, (int)bounds.bottom));
        TPLog.printKeyStatus("选择Path的Region信息："+region);

        int graphCount = sourceGraphs.size();
        for(int i = graphCount-1;i>=0;i--){
            Graph graph = sourceGraphs.get(i);
            if(graph.getGraphType() == WhiteBoardUtils.GRAPH_IMAGE){
               if(!region.quickReject(((ImageGraph)graph).getClipRectBounds())){
                   dstGraphs.add(graph);
               }
            }
        }
    }


    public void draw(Canvas canvas){
        canvas.save();
        canvas.setMatrix(new Matrix());
        if(selectComplete) {
            if(!clipEnable) {//非剪裁状态
                drawSelectComplete(canvas);
            }else{//剪裁状态
                drawClip(canvas);
            }

            if (showToolsBar) {//绘制工具栏
                drawToolsBar(canvas);
            }
        }else{
            drawSelecting(canvas);
        }
        canvas.restore();
    }

    private void drawSelecting(Canvas canvas){
        paint.setColor(SELECTING_PATH_COLOR);
        paint.setPathEffect(mSelectingDashPathEffect);
        paint.setStrokeWidth(SELECTING_PATH_FULL_LINE_HEIGHT);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path,paint);
    }

    private void drawSelectComplete(Canvas canvas){
        RectF bounds = new RectF(x, y, x + width, y + height);

        float bdCx = bounds.centerX();
        float bdCy = bounds.centerY();

        float rbx = bounds.right;
        float rby = bounds.bottom;

        circles[0].set(x,y,SELECTED_CIRCLE_RADIUS);
        circles[1].set(bdCx,y,SELECTED_CIRCLE_RADIUS);
        circles[2].set(rbx,y,SELECTED_CIRCLE_RADIUS);
        circles[3].set(rbx,bdCy,SELECTED_CIRCLE_RADIUS);
        circles[4].set(rbx,rby,SELECTED_CIRCLE_RADIUS);
        circles[5].set(bdCx,rby,SELECTED_CIRCLE_RADIUS);
        circles[6].set(x,rby,SELECTED_CIRCLE_RADIUS);
        circles[7].set(x,bdCy,SELECTED_CIRCLE_RADIUS);

        paint.setPathEffect(mSelectedDashPathEffect);
        paint.setStrokeWidth(SELECTED_BOUND_HOLLOW_LINE_HEIGHT);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(SELECTED_BOUND_COLOR);
        canvas.drawRect(bounds,paint);

        paint.setPathEffect(null);
        for(Circle c:circles){
            c.draw(canvas,paint);
        }
    }

    private void drawClip(Canvas canvas){
        Rect bounds = curClipRect;
        if(bounds == null){
            clipEnable = false;
            return;
        }

        float hwcx = bounds.left + (bounds.width() - CLIP_RECT_WIDTH)/2f;//水平居中起始X
        float vhcy = bounds.top + (bounds.height() - CLIP_RECT_WIDTH)/2f;//垂直平居中起始Y

        //左上
        clipBoundsRect[0].set(bounds.left,bounds.top,bounds.left+CLIP_RECT_HEIGHT,bounds.top + CLIP_RECT_WIDTH);
        clipBoundsRect[0].draw(canvas);
        //上左
        clipBoundsRect[1].set(bounds.left,bounds.top,bounds.left+CLIP_RECT_WIDTH,bounds.top + CLIP_RECT_HEIGHT);
        clipBoundsRect[1].draw(canvas);
        //上中
        clipBoundsRect[2].set(hwcx,bounds.top,hwcx+CLIP_RECT_WIDTH,bounds.top + CLIP_RECT_HEIGHT);
        clipBoundsRect[2].draw(canvas);
        //上右
        clipBoundsRect[3].set(bounds.right - CLIP_RECT_WIDTH,bounds.top,bounds.right,bounds.top + CLIP_RECT_HEIGHT);
        clipBoundsRect[3].draw(canvas);
        //右上
        clipBoundsRect[4].set(bounds.right - CLIP_RECT_HEIGHT,bounds.top,bounds.right,bounds.top + CLIP_RECT_WIDTH);
        clipBoundsRect[4].draw(canvas);
        //右中
        clipBoundsRect[5].set(bounds.right - CLIP_RECT_HEIGHT,vhcy,bounds.right,vhcy + CLIP_RECT_WIDTH);
        clipBoundsRect[5].draw(canvas);
        //右下
        clipBoundsRect[6].set(bounds.right - CLIP_RECT_HEIGHT,bounds.bottom - CLIP_RECT_WIDTH,bounds.right,bounds.bottom);
        clipBoundsRect[6].draw(canvas);
        //下右
        clipBoundsRect[7].set(bounds.right - CLIP_RECT_WIDTH,bounds.bottom - CLIP_RECT_HEIGHT,bounds.right,bounds.bottom);
        clipBoundsRect[7].draw(canvas);
        //下中
        clipBoundsRect[8].set(hwcx,bounds.bottom - CLIP_RECT_HEIGHT,hwcx+CLIP_RECT_WIDTH,bounds.bottom);
        clipBoundsRect[8].draw(canvas);
        //下左
        clipBoundsRect[9].set(bounds.left,bounds.bottom - CLIP_RECT_HEIGHT,bounds.left+CLIP_RECT_WIDTH,bounds.bottom);
        clipBoundsRect[9].draw(canvas);
        //左下
        clipBoundsRect[10].set(bounds.left,bounds.bottom - CLIP_RECT_WIDTH,bounds.left+CLIP_RECT_HEIGHT,bounds.bottom);
        clipBoundsRect[10].draw(canvas);
        //左中
        clipBoundsRect[11].set(bounds.left,vhcy,bounds.left+CLIP_RECT_HEIGHT,vhcy + CLIP_RECT_WIDTH);
        clipBoundsRect[11].draw(canvas);

        float width = bounds.width();
        float height = bounds.height();

        float width1_3 = width/3f;
        float height1_3 = height/3f;


        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);

        /*************************画线****************************/
        //画两条竖线
        canvas.drawLine(bounds.left+width1_3,bounds.top,bounds.left+width1_3,bounds.bottom,paint);
        canvas.drawLine(bounds.left+width1_3*2,bounds.top,bounds.left+width1_3*2,bounds.bottom,paint);
        //画两条横线
        canvas.drawLine(bounds.left,bounds.top+height1_3,bounds.right,bounds.top+height1_3,paint);
        canvas.drawLine(bounds.left,bounds.top+height1_3*2,bounds.right,bounds.top+height1_3*2,paint);

        /***********************画阴影*************************/
        /**
         * 阴影分为上下左右四个区域，因此该部分分为四个矩形来绘制
         */
        RectF selectBounds = new RectF(x+SELECTED_BOUNDS_AND_IMG_SPACING, y+SELECTED_BOUNDS_AND_IMG_SPACING, x + this.width-SELECTED_BOUNDS_AND_IMG_SPACING, y + this.height-SELECTED_BOUNDS_AND_IMG_SPACING);
        //上侧阴影部分
        RectF rect1 = new RectF(selectBounds.left,selectBounds.top,selectBounds.right,bounds.top);
        //右侧阴影部分
        RectF rect2 = new RectF(bounds.right,bounds.top,selectBounds.right,selectBounds.bottom);
        //下侧阴影部分
        RectF rect3 = new RectF(selectBounds.left,bounds.bottom,bounds.right,selectBounds.bottom);
        //左侧阴影部分
        RectF rect4 = new RectF(selectBounds.left,bounds.top,bounds.left,bounds.bottom);

        paint.setColor(CLIP_SHADOW_COLOR);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(rect1,paint);
        canvas.drawRect(rect2,paint);
        canvas.drawRect(rect3,paint);
        canvas.drawRect(rect4,paint);

    }


    public void drawToolsBar(Canvas canvas){
        mToolsBtnManager.draw(canvas);
    }

     @Override
    public void changeCoordinate(Matrix matrix, float scale) {

    }

    @Override
    public ArrayList<Point> getPoints() {
        //模拟两个点，要不然无法保存
        ArrayList<Point> points = new ArrayList<Point>();
        points.add(new Point());
        points.add(new Point());
        return points;
    }

    @Override
    public Rect getBounds() {
        return new Rect((int)x,(int)y,(int)(x+width),(int)(y+height));
    }

    @Override
    public void destroy() {

    }

   private boolean touchIn;
    private boolean click;
    public boolean onTouchEvent(MotionEvent event){
        int action = event.getActionMasked();

        float x = event.getX();
        float y = event.getY();

        TPLog.printKeyStatus("SelectGraph----->当前触摸的点为:("+x+","+y+")");

        if(action  == MotionEvent.ACTION_DOWN){
            touchIn = true;
            click = false;
            Rect bounds = getBounds();

            if(!clipEnable) {
                boolean check = mManualScale.check(event);
                if (!bounds.contains((int) x, (int) y) && !mToolsBtnManager.getBounds().contains(x, y) && !check) {
                    touchIn = false;
                }
            }else{
                boolean check = mClipGesture.check(event);
                if (!curClipRect.contains((int) x, (int) y) && !mToolsBtnManager.getBounds().contains(x, y)&&!check) {
                    touchIn = false;
                }
            }

            if(mToolsBtnManager.getBounds().contains(x,y)){
                click = true;
            }

            if(!touchIn){
                return false;
            }
        }

        /**
         * 如果是触摸在选择区域以内而且没有触发点击事件，那么要不是缩放要不是就是拖动
         */
        if(touchIn&&!click&&!clipEnable) {
            boolean boo = mManualScale.onTouchEvent(event);
            if (!boo) {
                mManualTranslate.onTouchEvent(event);
            }
        }else if(touchIn&&!click&&clipEnable){//如果当前是剪裁模式，而且触摸在了剪裁区域内，也不是点击，那么就是剪裁区域缩放或者剪裁区域拖动
            mClipGesture.onTouchEvent(event);
        }else if(touchIn&&click){
            mToolsBtnManager.click(event);
        }


        if(clipEnable){//如果当前是剪裁的话，就不用点击外面取消选择了
            return true;
        }

        return touchIn;
    }


    public class Circle{

        private float radius;
        private float cx;
        private float cy;

        public Circle(){

        }

        public void draw(Canvas canvas,Paint paint){
            //绘制圆形边框
            paint.setStrokeWidth(SELECTED_CIRCLE_STROKEWIDTH);
            paint.setColor(SELECTED_CIRCLE_BOUND_COLOR);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawCircle(cx,cy,radius,paint);
            //绘制实心圆形
            paint.setColor(SELECTED_CIRCLE_SOLID_COLOR);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(cx,cy,radius,paint);

//Test
//            paint.setColor(Color.RED);
//            int left = (int)(cx - radius) - SELECTED_CIRCLE_STROKEWIDTH;
//            int top = (int)(cy - radius) - SELECTED_CIRCLE_STROKEWIDTH;
//            int right = (int)(cx + radius) + SELECTED_CIRCLE_STROKEWIDTH;
//            int bottom = (int)(cy + radius) + SELECTED_CIRCLE_STROKEWIDTH;
//            Rect rect = new Rect(left,top,right,bottom);
//            canvas.drawRect(rect,paint);
        }

        public void set(float cx,float cy,float radius){
            setCx(cx);
            setCy(cy);
            setRadius(radius);
        }

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public float getCx() {
            return cx;
        }

        public void setCx(float cx) {
            this.cx = cx;
        }

        public float getCy() {
            return cy;
        }

        public void setCy(float cy) {
            this.cy = cy;
        }
    }

    class ClipBoundsRect{
        private float left;
        private float top;
        private float right;
        private float bottom;

        public void set(float left,float top,float right,float bottom){
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }


        public void draw(Canvas canvas){
            paint.setColor(CLIP_RECT_COLOR);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
            canvas.drawRect(getSelfBounts(),paint);
        }

        public boolean contains(float x,float y){
            RectF bounds = getSelfBounts();
            return bounds.contains(x,y);
        }

        public RectF getSelfBounts(){
            return new RectF(left,top,right,bottom);
        }

        public float getLeft() {
            return left;
        }

        public void setLeft(float left) {
            this.left = left;
        }

        public float getTop() {
            return top;
        }

        public void setTop(float top) {
            this.top = top;
        }

        public float getRight() {
            return right;
        }

        public void setRight(float right) {
            this.right = right;
        }

        public float getBottom() {
            return bottom;
        }

        public void setBottom(float bottom) {
            this.bottom = bottom;
        }
    }

    class ManualScale{
        public static final int SCALE_MODE_UNKNOWN = -1;
        public static final int SCALE_MODE_LEFT_TOP= 0;
        public static final int SCALE_MODE_TOP = 1;
        public static final int SCALE_MODE_RIGHT_TOP = 2;
        public static final int SCALE_MODE_RIGHT = 3;
        public static final int SCALE_MODE_RIGHT_BOTTOM = 4;
        public static final int SCALE_MODE_BOTTOM = 5;
        public static final int SCALE_MODE_LEFT_BOTTOM = 6;
        public static final int SCALE_MODE_LEFT= 7;

        private  int curScaleMode = SCALE_MODE_UNKNOWN;

        private float lastX;

        private float lastY;

        private float offsetX;

        private float offsetY;

        private float curScaleX = 1.0f;

        private float curScaleY = 1.0f;

        private float scalePx = 0;

        private float scalePy = 0;

        private float tempWidth = width;

        private float tempHeight = height;

        private Matrix mMatrix = new Matrix();

        public void setMode(int mode){
            curScaleMode = mode;
        }

        public boolean check(MotionEvent event){
            float x = event.getX();
            float y = event.getY();
            for(int i = 0;i<circles.length;i++){
                int left = (int)(circles[i].cx - circles[i].radius) - SELECTED_CIRCLE_STROKEWIDTH;
                int top = (int)(circles[i].cy - circles[i].radius) - SELECTED_CIRCLE_STROKEWIDTH;
                int right = (int)(circles[i].cx + circles[i].radius) + SELECTED_CIRCLE_STROKEWIDTH;
                int bottom = (int)(circles[i].cy + circles[i].radius) + SELECTED_CIRCLE_STROKEWIDTH;
                Rect rect = new Rect(left,top,right,bottom);

                if(rect.contains((int)x,(int)y)){
                    curScaleMode = i;
                    return true;
                }
            }
            return false;
        }

        public boolean isCheck(){
            return curScaleMode == SCALE_MODE_UNKNOWN?false:true;
        }

        public boolean onTouchEvent(MotionEvent event){

            if(curScaleMode==SCALE_MODE_UNKNOWN){
                return false;
            }

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                lastX = event.getX();
                lastY = event.getY();
                return true;
            }

            if(event.getAction() == MotionEvent.ACTION_UP||event.getAction() == MotionEvent.ACTION_CANCEL){
                scalePx = 0;
                scalePy = 0;
                curScaleX = 1.0f;
                curScaleY = 1.0f;
                curScaleMode = SCALE_MODE_UNKNOWN;
                return true;
            }

            float x = event.getX();
            float y = event.getY();

            offsetX = x - lastX;
            offsetY = y - lastY;

             tempWidth = width;
             tempHeight = height;

            switch(curScaleMode){
                case SCALE_MODE_LEFT_TOP:
                    scaleLeftTop(event);
                    break;
                case SCALE_MODE_TOP:
                    scaleTop(event);
                    break;
                case SCALE_MODE_RIGHT_TOP:
                    scaleTopRight(event);
                    break;
                case SCALE_MODE_RIGHT:
                    scaleRight(event);
                    break;
                case SCALE_MODE_RIGHT_BOTTOM:
                    scaleRightBottom(event);
                    break;
                case SCALE_MODE_BOTTOM:
                    scaleBottom(event);
                    break;
                case SCALE_MODE_LEFT_BOTTOM:
                    scaleLeftBottom(event);
                    break;
                case SCALE_MODE_LEFT:
                    scaleLeft(event);
                    break;
                default:
                    return true;
            }

//            curScaleX = width / tempWidth;
//            curScaleY = height / tempHeight;

            if(mListener!=null){
                mListener.onPostScale(curScaleX,curScaleY,scalePx,scalePy);
            }

            curScaleX = 1.0f;
            curScaleY = 1.0f;
            lastX = x;
            lastY = y;
          //  debug();
            return  true ;
        }

        private void reComputeBounds(){
            mMatrix.postScale(curScaleX,curScaleY,scalePx,scalePy);
            RectF dstRect = new RectF();
            RectF srcRect = new RectF(originalBounds.left,originalBounds.top,originalBounds.right,originalBounds.bottom);
            mMatrix.mapRect(dstRect,srcRect);

            x = dstRect.left;
            y = dstRect.top;
            width = dstRect.width();
            height = dstRect.height();
        }

        private void scaleLeftTop(MotionEvent event){
            offsetX = offsetX*-1;
            offsetY = offsetY*-1;

            float scaleWidth = width + offsetX;
            float scaleHeight = height + offsetY;

            float tempScale = 1.0f;
            if(offsetX>=0&&offsetY>=0){//放大
                if(offsetX<offsetY){
                     tempScale =scaleWidth /tempWidth;
                }else{
                    tempScale =scaleHeight /height;
                }
            }else{
                if(offsetX>offsetY){
                    tempScale =scaleWidth /tempWidth;
                }else{
                    tempScale =scaleHeight /height;
                }
            }

            width = width * tempScale;
            height = height * tempScale;

            offsetX = width - tempWidth;
            offsetY = height - tempHeight;

            x = x - offsetX;
            y = y - offsetY;

            Rect bounds = getBounds();
            scalePx = bounds.right;
            scalePy = bounds.bottom;

            curScaleX = tempScale;
            curScaleY = tempScale;
        }

        private void scaleTop(MotionEvent event){

            offsetY = offsetY*-1;

            height = height + offsetY;

            y = y - offsetY;

            Rect bounds = getBounds();
            scalePx = bounds.centerX();
            scalePy = bounds.bottom;

            curScaleX = 1.0f;
            curScaleY = height / tempHeight;
        }

        private void scaleTopRight(MotionEvent event){

            offsetY = offsetY*-1;

            float scaleWidth = width + offsetX;
            float scaleHeight = height + offsetY;

            float tempScale = 1.0f;
            if(offsetX>=0&&offsetY>=0){
                if(offsetX<offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }else{
                if(offsetX>offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }

            width = width * tempScale;
            height = height * tempScale;

            offsetY = height - tempHeight;

            y = y - offsetY;

            Rect bounds = getBounds();
            scalePx = bounds.left;
            scalePy = bounds.bottom;

            curScaleX = tempScale;
            curScaleY = tempScale;
        }

        private void scaleRight(MotionEvent event){
            width = width + offsetX;
            Rect bounds = getBounds();
            scalePx = bounds.left;
            scalePy = bounds.centerY();
            curScaleY = 1.0f;
            curScaleX = width / tempWidth;
        }

        private void scaleRightBottom(MotionEvent event){
            float scaleWidth = width + offsetX;
            float scaleHeight = height + offsetY;

            float tempScale = 1.0f;
            if(offsetX>=0&&offsetY>=0){
                if(offsetX<offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }else{
                if(offsetX>offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }

            width = width*tempScale;
            height = height*tempScale;

            Rect bounds = getBounds();
            scalePx = bounds.left;
            scalePy = bounds.top;

            curScaleX = tempScale;
            curScaleY = tempScale;
        }

        private void scaleBottom(MotionEvent event){
            height = height + offsetY;
            Rect bounds = getBounds();
            scalePx = bounds.centerX();
            scalePy = bounds.top;
            curScaleX = 1.0f;
            curScaleY = height / tempHeight;
        }

        private void scaleLeftBottom(MotionEvent event){

            offsetX = offsetX*-1;

            float scaleWidth = width + offsetX;
            float scaleHeight = height + offsetY;

            float tempScale = 1.0f;
            if(offsetX>=0&&offsetY>=0){
                if(offsetX<offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }else{
                if(offsetX>offsetY){
                    tempScale = scaleWidth/tempWidth;
                }else{
                    tempScale = scaleHeight/tempHeight;
                }
            }

            width = width*tempScale;
            height = height*tempScale;

            offsetX = width - tempWidth;

            x = x - offsetX;
            Rect bounds = getBounds();
            scalePx = bounds.right;
            scalePy = bounds.top;

            curScaleX = tempScale;
            curScaleY = tempScale;
        }

        private void scaleLeft(MotionEvent event){
            offsetX = offsetX*-1;

            width = width + offsetX;
            x = x - offsetX;
            Rect bounds = getBounds();
            scalePx = bounds.right;
            scalePy = bounds.centerY();
            curScaleY = 1.0f;
            curScaleX = width / tempWidth;
        }


        private void debug(){
            TPLog.printError("*********************************************");
            TPLog.printError("curScaleMode = "+curScaleMode);
            TPLog.printError("curScale = "+curScale);
            TPLog.printError("x = "+x);
            TPLog.printError("y = "+y);
            TPLog.printError("width = "+width);
            TPLog.printError("height = "+height);
            TPLog.printError("*********************************************");
        }

    }


    class ManualTranslate{

        private GestureDetector mGestureDetector;

        private FlingRunnable flingRunnable;

        private SoftReference<Context> context;

        public ManualTranslate(Context context){
            mGestureDetector = new GestureDetector(context,mSimpleOnGestureListener);
            this.context = new SoftReference<Context>(context);
        }

        public boolean onTouchEvent(MotionEvent event){
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                if(flingRunnable!=null){
                    flingRunnable.cancelFling();
                    flingRunnable = null;
                }
            }
            mGestureDetector.onTouchEvent(event);
            return true;
        }


        GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {

                distanceX = distanceX*-1;
                distanceY = distanceY*-1;

                SelectGraph.this.x += distanceX;
                SelectGraph.this.y += distanceY;

                if(mListener!=null){
                    mListener.onPostMove(distanceX,distanceY);
                }

                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                flingRunnable = new FlingRunnable(context.get());
                flingRunnable.fling((int)velocityX*-1,(int)velocityY*-1);
                new Thread(flingRunnable).start();
                return true;
            }
        };



    }


    class  FlingRunnable implements Runnable{
        private Scroller mScroller;

        private int mCurrentX , mCurrentY;

        public FlingRunnable(Context context){
            if(context == null)
                return;
            mScroller = new Scroller(context);
        }

        public void cancelFling(){
            mScroller.forceFinished(true);
        }

        /**
         * 这个方法主要是从onTouch中或得到当前滑动的水平和竖直方向的速度
         * 调用scroller.fling方法，这个方法内部能够自动计算惯性滑动
         * 的x和y的变化率，根据这个变化率我们就可以对图片进行平移了
         */
        public void fling(int velocityX , int velocityY){
            mScroller.fling(0,0,velocityX,velocityY,Integer.MIN_VALUE,Integer.MAX_VALUE,Integer.MIN_VALUE,Integer.MAX_VALUE);
        }

        @Override
        public void run() {
//            TPLog.printError("SimpleOnGestureListener -- > FlingRunnable running... ");
            if (mScroller.isFinished()){
//                TPLog.printError("SimpleOnGestureListener -- > mScroller Finished... ");
                return;
            }
            //如果返回true，说明当前的动画还没有结束，我们可以获得当前的x和y的值
            while (mScroller.computeScrollOffset()){

                if(!checkDisplayOnScreen()){
                    cancelFling();
                    return;
                }

                //获得当前的x坐标
                final int newX = mScroller.getCurrX();
                //获得当前的y坐标
                final int newY = mScroller.getCurrY();

                offsetX = mCurrentX-newX ;
                offsetY = mCurrentY-newY;
//                TPLog.printError("SimpleOnGestureListener -- > offsetX = "+offsetX+",offsetY = "+offsetY);
                //进行平移操作
//                checkBorderWhenTranslate();
//                setImageMatrix(mScaleMatrix);
                mCurrentX = newX;
                mCurrentY = newY;

//                TPLog.printError("FlingRunnable ---> offsetX = "+offsetX+",offsetY = "+offsetY);

                if(mListener!=null){
                    mListener.onPostMove(offsetX,offsetY);
                }

                //每16ms调用一次
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean checkDisplayOnScreen(){
        Rect rect = getBounds();
//        TPLog.printError("SimpleOnGestureListener -- >checkDisplayOnScreen  rect = "+rect.toString());
        Rect screenRect = new Rect(0,0,(int)WhiteBoardUtils.whiteBoardWidth,(int)WhiteBoardUtils.whiteBoardHeight);

        boolean boo = screenRect.intersect(rect);
//        TPLog.printError("SimpleOnGestureListener -- >checkDisplayOnScreen  boo = "+boo);
        return boo;
    }




    /*********************************剪裁操作**************************************/


    class ClipGesture{
        private ManualClipScale mManualClipScale;
        private ManualClipTranslate mManualClipTranslate;

        public ClipGesture(Context context){
            mManualClipScale = new ManualClipScale();
            mManualClipTranslate = new ManualClipTranslate(context);
        }

        public boolean check(MotionEvent event){
            return mManualClipScale.check(event);
        }

        public boolean onTouchEvent(MotionEvent event){
            if(!mManualClipScale.onTouchEvent(event)){
                mManualClipTranslate.onTouchEvent(event);
            }
            return true;
        }
    }


    class ManualClipScale{
        public static final int SCALE_MODE_UNKNOWN = -1;
        public static final int SCALE_MODE_LEFT_TOP= 0;
        public static final int SCALE_MODE_TOP = 1;
        public static final int SCALE_MODE_RIGHT_TOP = 2;
        public static final int SCALE_MODE_RIGHT = 3;
        public static final int SCALE_MODE_RIGHT_BOTTOM = 4;
        public static final int SCALE_MODE_BOTTOM = 5;
        public static final int SCALE_MODE_LEFT_BOTTOM = 6;
        public static final int SCALE_MODE_LEFT= 7;

        private  int curScaleMode = SCALE_MODE_UNKNOWN;

        private float lastX;

        private float lastY;

        private float offsetX;

        private float offsetY;

        private float curScaleX = 1.0f;

        private float curScaleY = 1.0f;

        private float scalePx = 0;

        private float scalePy = 0;

        private Matrix mMatrix = new Matrix();

        public void setMode(int mode){
            curScaleMode = mode;
        }

        public boolean check(MotionEvent event){
            float x = event.getX();
            float y = event.getY();
            for(int i = 0;i<clipBoundsRect.length;i++){
                if(i==1||i==4||i==7||i==10){//拐角结束位置不做处理
                  continue;
                }
                RectF rect = null;
                if(i==0||i==3||i==6||i==9) { //拐角起始位置
                    RectF  rect1 = clipBoundsRect[i].getSelfBounts();
                    RectF  rect2 = clipBoundsRect[i+1].getSelfBounts();
                    rect = GraphUtils.combineRect(new RectF[]{rect1,rect2});
                }else{
                    rect = clipBoundsRect[i].getSelfBounts();
                }
                if(rect.contains((int)x,(int)y)){
                    if(i == 0){
                        curScaleMode = i;
                    }else if(i==2){
                        curScaleMode = i-1;
                    }else if( i == 3){
                        curScaleMode = i-1;
                    }else if( i == 5){
                        curScaleMode = i-2;
                    }else if(i == 6){
                        curScaleMode = i-2;
                    }else if( i == 8){
                        curScaleMode = i-3;
                    }else if( i == 9){
                        curScaleMode = i-3;
                    }else if(i == 11){
                        curScaleMode = i-4;
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isCheck(){
            return curScaleMode == SCALE_MODE_UNKNOWN?false:true;
        }

        public boolean onTouchEvent(MotionEvent event){

            if(curScaleMode==SCALE_MODE_UNKNOWN){
                return false;
            }

            if(event.getAction() == MotionEvent.ACTION_DOWN){
                lastX = event.getX();
                lastY = event.getY();
                return true;
            }

            if(event.getAction() == MotionEvent.ACTION_UP||event.getAction() == MotionEvent.ACTION_CANCEL){
                scalePx = 0;
                scalePy = 0;
                curScaleX = 1.0f;
                curScaleY = 1.0f;
                curScaleMode = SCALE_MODE_UNKNOWN;
                return true;
            }

            float x = event.getX();
            float y = event.getY();

            offsetX = x - lastX;
            offsetY = y - lastY;

            switch(curScaleMode){
                case SCALE_MODE_LEFT_TOP:
                    scaleLeftTop(event);
                    break;
                case SCALE_MODE_TOP:
                    scaleTop(event);
                    break;
                case SCALE_MODE_RIGHT_TOP:
                    scaleTopRight(event);
                    break;
                case SCALE_MODE_RIGHT:
                    scaleRight(event);
                    break;
                case SCALE_MODE_RIGHT_BOTTOM:
                    scaleRightBottom(event);
                    break;
                case SCALE_MODE_BOTTOM:
                    scaleBottom(event);
                    break;
                case SCALE_MODE_LEFT_BOTTOM:
                    scaleLeftBottom(event);
                    break;
                case SCALE_MODE_LEFT:
                    scaleLeft(event);
                    break;
                default:
                    return true;
            }

            selectImgHelper.onRefreshUI();

            curScaleX = 1.0f;
            curScaleY = 1.0f;
            lastX = x;
            lastY = y;
            //  debug();
            return  true ;
        }

        private void scaleLeftTop(MotionEvent event){
            offsetX = offsetX*-1;
            offsetY = offsetY*-1;

            curClipRect.left =  (int)(curClipRect.left - offsetX);
            curClipRect.top = (int)(curClipRect.top - offsetY);

            if(curClipRect.left<(SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.left = (int)SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING;
            }

            if(curClipRect.top < (SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.top = (int)SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING;
            }

            //进行当前剪裁矩形大小校验
            int width = curClipRect.width();
            int height = curClipRect.height();

            if(width<WhiteBoardUtils.CLIP_RECT_MIN_WIDTH){
                int cWidth = WhiteBoardUtils.CLIP_RECT_MIN_WIDTH - width;
                curClipRect.left -= cWidth;
            }

            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.top -= cH;
            }
        }

        private void scaleTop(MotionEvent event){

            offsetY = offsetY*-1;

//           float  tempHeight = curClipRect.height() + offsetY;

            curClipRect.top = (int)(curClipRect.top - offsetY);

            if(curClipRect.top<(SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.top = (int)SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING;
            }

            int height = curClipRect.height();

            //进行当前剪裁矩形大小校验
            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                 int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.top -= cH;
            }
        }

        private void scaleTopRight(MotionEvent event){

            offsetY = offsetY*-1;

            float  tempWidth = curClipRect.width() + offsetX;
            float  tempHeight = curClipRect.height() + offsetY;

            curClipRect.top = (int)(curClipRect.top - offsetY);

            curClipRect.right = (int)(curClipRect.left + tempWidth);

            if(curClipRect.right>(SelectGraph.this.x+SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.right = (int)(SelectGraph.this.x+SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            if(curClipRect.top<(SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.top = (int)SelectGraph.this.y+SELECTED_BOUNDS_AND_IMG_SPACING;
            }

            //进行当前剪裁矩形大小校验
            int height = curClipRect.height();
            int width = curClipRect.width();

            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.top -= cH;
            }

            if(width<WhiteBoardUtils.CLIP_RECT_MIN_WIDTH){
                int cW = WhiteBoardUtils.CLIP_RECT_MIN_WIDTH - width;
                curClipRect.right += cW;
            }
        }

        private void scaleRight(MotionEvent event){
            float tempWidth = curClipRect.width() + offsetX;
            curClipRect.right = (int)(curClipRect.left + tempWidth);

            if( curClipRect.right > (SelectGraph.this.x +  SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.right = (int)(SelectGraph.this.x +  SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            //进行当前剪裁矩形大小校验
            int width = curClipRect.width();
            if(width<WhiteBoardUtils.CLIP_RECT_MIN_WIDTH){
                int cW = WhiteBoardUtils.CLIP_RECT_MIN_WIDTH - width;
                curClipRect.right += cW;
            }
        }

        private void scaleRightBottom(MotionEvent event){
            float tempWidth =  curClipRect.width() + offsetX;
            float tempHeight =  curClipRect.height() + offsetY;

            curClipRect.right = (int)(curClipRect.left + tempWidth);
            curClipRect.bottom = (int)(curClipRect.top + tempHeight);

            if( curClipRect.right > (SelectGraph.this.x +  SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.right = (int)(SelectGraph.this.x +  SelectGraph.this.width-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            if(curClipRect.bottom > (SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.bottom = (int)(SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            //进行当前剪裁矩形大小校验
            int width = curClipRect.width();
            int height = curClipRect.height();
            if(width<WhiteBoardUtils.CLIP_RECT_MIN_WIDTH){
                int cW = WhiteBoardUtils.CLIP_RECT_MIN_WIDTH - width;
                curClipRect.right += cW;
            }

            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.bottom += cH;
            }
        }

        private void scaleBottom(MotionEvent event){
            float tempHeight = curClipRect.height() + offsetY;
            curClipRect.bottom = (int)(curClipRect.top + tempHeight);

            if(curClipRect.bottom > (SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.bottom = (int)(SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            int height = curClipRect.height();
            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.bottom += cH;
            }
        }

        private void scaleLeftBottom(MotionEvent event){
            offsetX = offsetX*-1;

            float tempWidth =  curClipRect.width() + offsetX;
            float tempHeight = curClipRect.height()+ offsetY;
            curClipRect.left = (int)(curClipRect.left - offsetX);
            curClipRect.bottom = (int)(curClipRect.top + tempHeight);

            if(curClipRect.bottom > (SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING)){
                curClipRect.bottom = (int)(SelectGraph.this.y +  SelectGraph.this.height-SELECTED_BOUNDS_AND_IMG_SPACING);
            }

            if(curClipRect.left < (SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING )){
                curClipRect.left = (int)(SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING );
            }

            //进行当前剪裁矩形大小校验
            int width = curClipRect.width();
            int height = curClipRect.height();
            if(height<WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT){
                int cH = WhiteBoardUtils.CLIP_RECT_MIN_HEIGHT - height;
                curClipRect.bottom += cH;
            }
            if(width<WhiteBoardUtils.CLIP_RECT_MIN_WIDTH){
                int cW = WhiteBoardUtils.CLIP_RECT_MIN_WIDTH - width;
                curClipRect.left += cW;
            }
        }

        private void scaleLeft(MotionEvent event){
            offsetX = offsetX*-1;

            float tempWidth = curClipRect.width() + offsetX;
            curClipRect.left = (int)(curClipRect.left - offsetX);

            if(curClipRect.left < (SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING )){
                curClipRect.left = (int)(SelectGraph.this.x+SELECTED_BOUNDS_AND_IMG_SPACING );
            }
        }

    }


    class ManualClipTranslate{

        private GestureDetector mGestureDetector;

        public ManualClipTranslate(Context context){
            mGestureDetector = new GestureDetector(context,mSimpleOnGestureListener);
        }

        public boolean onTouchEvent(MotionEvent event){
            mGestureDetector.onTouchEvent(event);
            return true;
        }


        GestureDetector.SimpleOnGestureListener mSimpleOnGestureListener = new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                    float distanceY) {

                distanceX = distanceX*-1;
                distanceY = distanceY*-1;

                RectF rect = new RectF(x+SELECTED_BOUNDS_AND_IMG_SPACING,y+SELECTED_BOUNDS_AND_IMG_SPACING,x+width-SELECTED_BOUNDS_AND_IMG_SPACING,y+height-SELECTED_BOUNDS_AND_IMG_SPACING);

                curClipRect.left += distanceX;
                curClipRect.top += distanceY;
                curClipRect.right += distanceX;
                curClipRect.bottom += distanceY;

                if(curClipRect.left<rect.left){
                    distanceX = curClipRect.left - rect.left;
                    curClipRect.left -= distanceX;
                    curClipRect.right -= distanceX;
                }

                if(curClipRect.top<rect.top){
                    distanceY = curClipRect.top - rect.top;
                    curClipRect.top -= distanceY;
                    curClipRect.bottom -= distanceY;
                }

                if(curClipRect.right>rect.right){
                    distanceX = curClipRect.right - rect.right;
                    curClipRect.right -= distanceX;
                    curClipRect.left -= distanceX;
                }

                if(curClipRect.bottom>rect.bottom){
                    distanceY = curClipRect.bottom - rect.bottom;
                    curClipRect.bottom -= distanceY;
                    curClipRect.top -= distanceY;
                }

                selectImgHelper.onRefreshUI();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                   float velocityY) {
                return false;
            }
        };
    }


    /******************************************************************************/


    class ToolsBtnManager{
        //按钮属性
        private int TOOLSBAR_WIDTH = 160; //工具栏宽度

        private static final int TOOLSBAR_HEIGHT = 40; //工具栏高度

        private static final int BTN_WIDTH = 40;  //按钮宽度

        private static final int BTN_HEIGHT = 40; //按钮高度

        private static final int BTN_SPACING = 10; //工具栏两边及按钮之间间隙

        private static final int TOOLS_IMG_WIDTH = 14;

        private static final int TOOLS_IMG_HEIGHT = 8;

        private ToolsBtn normalToolsBtns[] =  new ToolsBtn[2];

        private ToolsBtn clipToolsBtns[] =  new ToolsBtn[2];

        private ToolsBtn toolsBtns[] = null;

        private ToolsImg mToolsImg = new ToolsImg();

        private RectF curToolBarBounds;

        public ToolsBtnManager(){
            for(int i = 0;i<normalToolsBtns.length;i++){
                normalToolsBtns[i] = new ToolsBtn();
                normalToolsBtns[i].setWidth(BTN_WIDTH);
                normalToolsBtns[i].setHeight(BTN_HEIGHT);
                normalToolsBtns[i].setId(i+1);
            }
//            normalToolsBtns[0].setIcon(BitmapManager.CLIP_BTN_ICON);
            normalToolsBtns[0].setIcon(BitmapManager.ROTATE_BTN_ICON);
            normalToolsBtns[1].setIcon(BitmapManager.DELETE_BTN_ICON);

            for(int i = 0;i<clipToolsBtns.length;i++){
                clipToolsBtns[i] = new ToolsBtn();
                clipToolsBtns[i].setWidth(BTN_WIDTH);
                clipToolsBtns[i].setHeight(BTN_HEIGHT);
                clipToolsBtns[i].setId(i);
            }
            clipToolsBtns[0].setIcon(BitmapManager.SURE_BTN_ICON);
            clipToolsBtns[1].setIcon(BitmapManager.CANCEL_BTN_ICON);

            mToolsImg.setIcon(BitmapManager.IMG_TOOLS_TOP_ICON);
            mToolsImg.setWidth(TOOLS_IMG_WIDTH);
            mToolsImg.setHeight(TOOLS_IMG_HEIGHT);
        }

        public RectF getBounds(){
            computePosition();
            return curToolBarBounds;
        }

        public void computePosition(){
            if(clipEnable){
                computePosition2();
            }else{
                computePosition1();
            }
        }

        public void computePosition1(){

            toolsBtns = normalToolsBtns;

            RectF curSelectBounds = new RectF(x, y, x + width, y + height);

            float x = SelectGraph.this.x + (width - TOOLS_IMG_WIDTH)/2f;

            float y = curSelectBounds.bottom + BTN_SPACING;

            mToolsImg.setX(x);
            mToolsImg.setY(y);

            TOOLSBAR_WIDTH = toolsBtns.length*BTN_WIDTH+(toolsBtns.length+1)*BTN_SPACING;;

             x = SelectGraph.this.x + (width - TOOLSBAR_WIDTH)/2f;

             y = y+TOOLS_IMG_HEIGHT ;

            curToolBarBounds = new RectF(x,y,TOOLSBAR_WIDTH+x,TOOLSBAR_HEIGHT+y);

            float clipBtnX = x + BTN_SPACING;
            float clipBntY = y ;

            float rotateBtnX = clipBtnX + BTN_SPACING + BTN_WIDTH;
            float rotateBtnY = y;

            float deleteBtnX = rotateBtnX + BTN_SPACING+ BTN_WIDTH;
            float deleteBtnY = y;

            toolsBtns[0].setX(clipBtnX);
            toolsBtns[0].setY(clipBntY);

            toolsBtns[1].setX(rotateBtnX);
            toolsBtns[1].setY(rotateBtnY);

//            toolsBtns[2].setX(deleteBtnX);
//            toolsBtns[2].setY(deleteBtnY);
        }

        public void computePosition2(){
            toolsBtns = clipToolsBtns;
            /**
             * 计算工具栏上方三角图标位置
             */
            RectF curSelectBounds = new RectF(curClipRect.left,curClipRect.top,curClipRect.right,curClipRect.bottom);

            float x = curSelectBounds.left + (curSelectBounds.width() - TOOLS_IMG_WIDTH)/2f;

            float y = curSelectBounds.bottom + BTN_SPACING;

            mToolsImg.setX(x);
            mToolsImg.setY(y);

            TOOLSBAR_WIDTH = toolsBtns.length*BTN_WIDTH+(toolsBtns.length+1)*BTN_SPACING;

           /**                   计算工具栏及按钮显示位置                              **/
            x = curSelectBounds.left + (curSelectBounds.width() - TOOLSBAR_WIDTH)/2f;

            y = y+TOOLS_IMG_HEIGHT ;

            curToolBarBounds = new RectF(x,y,TOOLSBAR_WIDTH+x,TOOLSBAR_HEIGHT+y);

            float clipBtnX = x + BTN_SPACING;
            float clipBntY = y ;

            float rotateBtnX = clipBtnX + BTN_SPACING + BTN_WIDTH;
            float rotateBtnY = y;

            float deleteBtnX = rotateBtnX + BTN_SPACING+ BTN_WIDTH;
            float deleteBtnY = y;

            toolsBtns[0].setX(clipBtnX);
            toolsBtns[0].setY(clipBntY);

            toolsBtns[1].setX(rotateBtnX);
            toolsBtns[1].setY(rotateBtnY);
        }


        public void draw(Canvas canvas){

            computePosition();

            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawRect(curToolBarBounds,paint);

            for(int i =0;i<toolsBtns.length;i++){
                toolsBtns[i].draw(canvas);
            }

            mToolsImg.draw(canvas);
        }

        public boolean click(MotionEvent event){

            int action = event.getActionMasked();

            boolean actionDown = (action == MotionEvent.ACTION_DOWN);
            boolean actionUp = (action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL);

            float x = event.getX();
            float y = event.getY();

            if(actionDown) {
                if (curToolBarBounds.contains(x, y)) {
                    int btnCount = toolsBtns.length;

                    for (int i = 0; i < btnCount; i++) {
                        if (toolsBtns[i].getBounds().contains(x, y)) {
                            toolsBtns[i].setPressed(true);
                            if(mListener!=null){
                                mListener.onRefreshUI();
                            }
                        }
                    }
                    return true;
                }
            }

            if(actionUp){
                if(mListener!=null) {
                    for (int i = 0; i < toolsBtns.length; i++) {
                        boolean pressed = toolsBtns[i].isPressed();
                        if (pressed) {
                            switch (toolsBtns[i].getId()) {
                                case 0:
                                    if(!clipEnable) {//剪裁
                                        curClipRect = selectImgHelper.getSelectGraphClipBounds();
                                        if(curClipRect!=null){
                                            clipEnable = true;
                                            mListener.onClipImgStart();
                                        }
                                    }else{//剪裁确认
                                        clipEnable = false;
                                        mListener.onClipImgComplete(curClipRect);
                                    }
                                    break;
                                case 1:
                                    if(!clipEnable) {//旋转
                                        Rect bounds = SelectGraph.this.getBounds();
                                        mListener.onPostRotate(90, bounds.centerX(), bounds.centerY());
                                    }else{//剪裁取消
                                        clipEnable = false;
                                        mListener.onClipImgCancel();
                                    }
                                    break;
                                case 2:
                                    mListener.onDeleteImg();
                                    break;
                            }
                        }
                    }
                }

                for(int i = 0;i<toolsBtns.length;i++){
                    toolsBtns[i].setPressed(false);
                }
            }
            return false;
        }

    }

    class ToolsBtn {
        private int id;
        private float x;
        private float y;
        private float width;
        private float height;
        private boolean pressed;
        private Bitmap icon[] ;

        public void draw(Canvas canvas){
            if(icon==null){
                return;
            }
            if(pressed){
                canvas.drawBitmap(icon[1],x,y,null);
            }else{
                canvas.drawBitmap(icon[0],x,y,null);
            }
        }

        public RectF getBounds(){
            return new RectF(x,y,x+width,y+height);
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public boolean isPressed() {
            return pressed;
        }

        public void setPressed(boolean pressed) {
            this.pressed = pressed;
        }

        public void setIcon(Bitmap icon[]){
            this.icon = icon;
        }
    }

    class ToolsImg {
        private float x;
        private float y;
        private float width;
        private float height;
        private Bitmap icon;

        public void draw(Canvas canvas){
            canvas.drawBitmap(icon,x,y,null);
        }

        public RectF getBounds(){
            return new RectF(x,y,x+width,y+height);
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getWidth() {
            return width;
        }

        public void setWidth(float width) {
            this.width = width;
        }

        public float getHeight() {
            return height;
        }

        public void setHeight(float height) {
            this.height = height;
        }

        public Bitmap getIcon() {
            return icon;
        }

        public void setIcon(Bitmap icon) {
            this.icon = icon;
        }
    }


    private OnSelectGraphOpListener mListener;
    public void setOnSelectGraphOpListener(OnSelectGraphOpListener listener){
        mListener = listener;
    }


    public interface OnSelectGraphOpListener{
        void onPostScale(float sx, float sy, float px, float py);
        void onPostMove(float ox, float oy);
        void onPostRotate(int angle, float px, float py);
        void onClipImgStart();
        void onClipImgComplete(Rect clipRect);
        void onClipImgCancel();
        void onDeleteImg();
        void onRefreshUI();
    }
}
