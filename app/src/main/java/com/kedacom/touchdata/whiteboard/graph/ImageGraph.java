package com.kedacom.touchdata.whiteboard.graph;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Parcel;

import com.kedacom.touchdata.net.SynFileManager;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;

/**
 * Created by zhanglei on 2017/6/20.
 */
public class ImageGraph extends Graph{

    private static final int SELECTING_BOUND_COLOR = Color.parseColor("#1ab800");

    private static final float SELECTING_BOUND_STROKEWIDTH = 2;

    private float x;

    private float y;

    private int width;

    private int height;

    private float scaleX = 1.0f;

    private float scaleY = 1.0f;

    private String imgPath;

    private String fileName;

    private Bitmap bitmap;

    private Matrix matrix = new Matrix();

    private Rect originalRect = new Rect();

    private RectF clipRect =new RectF();

    private boolean selecting = false;

    private boolean cliping = false;

    public ImageGraph(){
        super(WhiteBoardUtils.GRAPH_IMAGE);
        init();
    }

    public ImageGraph(String imgPath) {
        super(WhiteBoardUtils.GRAPH_IMAGE);
        this.imgPath = imgPath;
        init();
    }

    public ImageGraph(Parcel arg0){
        super(WhiteBoardUtils.GRAPH_IMAGE);
        init();
    }

    private void init(){
        paint.setColor(SELECTING_BOUND_COLOR);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(SELECTING_BOUND_STROKEWIDTH);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
    }

    /**
     * 在全局操作的时候同步的是全局的Matrix
     * 而全局的Matrix中的数据需要同步给所有的图片图元
     * 因此需要做一些特殊的处理
     * @param oldMatrix
     * @param curMatrix
     */
    public void reset(Matrix oldMatrix,Matrix curMatrix){
        //获取之前矩阵的逆矩阵
        Matrix invert = new Matrix();
        oldMatrix.invert(invert);
        //乘以之间矩阵的逆矩阵就相当于除以之前的矩阵
        matrix.postConcat(invert);
        //再乘以当前最新的全局矩阵就成功计算出了当前的图元矩阵
        matrix.postConcat(curMatrix);

        invert = null;
        oldMatrix = null;
    }

    public void changeMode(){
        if(WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_ERASE||WhiteBoardUtils.curOpType == WhiteBoardUtils.OP_ERASE_AREA){
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OVER));
        }else{
            paint.setXfermode(null);
        }
    }

    //2017.07.08 临时添加，处理单个图元同步时，不协调问题
    public Matrix getMatrix(){
        return matrix;
    }

    public void setMatrix(Matrix matrix){
        this.matrix.set(matrix);
    }

    public void setMatrixValues(float values[]){
        this.matrix.reset();
        this.matrix.setValues(values);
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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public String getFileName(){ return this.fileName; }

    public void setFileName(String fileName){  this.fileName = fileName; }

    public boolean isSelecting() {
        return selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }

    public void setCliping(boolean cliping){
        this.cliping = cliping;
    }

    public boolean isCliping(){
        return cliping;
    }

    @Override
    public Paint getPaint() {
        return null;
    }

    @Override
    public void addPoint(float x, float y) {

    }

    @Override
    public void scale(float scale) {
        super.scale(scale);
        Rect bounds = getBounds();
        postScaleX(scale/scaleX,bounds.centerX(),bounds.centerY());
        postScaleY(scale/scaleY,bounds.centerX(),bounds.centerY());
    }

    public void scale(float scale,float cx,float cy) {
        super.scale(scale);
        postScaleX(scale/scaleX,cx,cy);
        postScaleY(scale/scaleY,cx,cy);
    }

    public void postsSale(float scale,float px,float py){
        postScaleX(scale,px,py);
        postScaleY(scale,px,py);
    }

    public void postScale(float sx,float sy,float px,float py){
        postScaleX(sx,px,py);
        postScaleY(sy,px,py);
    }

    public void postScaleX(float scale,float px,float py){
      //  scale = checkWidthScale(scale);
        matrix.postScale(scale,1.0f,px,py);
        scaleX = scaleX*scale;
    }

    public void postScaleY(float scale,float px,float py){
       // scale = checkHeightScale(scale);
        matrix.postScale(1.0f,scale,px,py);
        scaleY = scaleY*scale;
    }

    private float checkWidthScale(float sx){
        float tempWidth = scaleX*sx*width;
        if(tempWidth<WhiteBoardUtils.IMG_MIN_WIDTH){
            tempWidth = WhiteBoardUtils.IMG_MIN_WIDTH;
            sx = tempWidth/width/scaleX;
        }
        return sx;
    }

    private float checkHeightScale(float sy){
        float tempHeight = scaleY*sy*height;
        if(tempHeight<WhiteBoardUtils.IMG_MIN_HEIGHT){
            tempHeight = WhiteBoardUtils.IMG_MIN_HEIGHT;
            sy = tempHeight/height/scaleY;
        }

        return sy;
    }

    @Override
    public void rotate(int angle) {
        super.rotate(angle);
        Rect rect = getBounds();
        postRotate((angle - curAngle),rect.centerX(),rect.centerY());
    }

    public void postRotate(int angle,float px,float py){
        super.rotate(angle+curAngle);
        matrix.postRotate(angle,px,py);
    }

    @Override
    public void translateBy(float ox, float oy) {
        super.translateBy(ox, oy);
        matrix.postTranslate(ox,oy);
    }

    @Override
    public void translateTo(float ox, float oy) {
        super.translateTo(ox, oy);
        translateBy(ox-offsetX,oy-offsetY);
    }

    public void postTranslate(float ox,float oy){
        translateBy(ox,oy);
    }

    public void overallSituationScale(float scale,float px,float py){
        matrix.postScale(scale,scale,px,py);
    }

    public void  overallSituationRotate(int angle ,float px,float py){
        matrix.postRotate(angle,px,py);
    }

    public void overallSituationTranslate(float ox,float oy){
        matrix.postTranslate(ox,oy);
    }

    @Override
    public void draw(Canvas canvas) {
        if(canvas==null){
            return;
        }
        if(bitmap==null||bitmap.isRecycled()){
            load();
            if(bitmap==null||bitmap.isRecycled()){
                return;
            }
        }

        canvas.save();
        canvas.setMatrix(matrix);
        if(!cliping) {
            canvas.clipRect(clipRect.left, clipRect.top, clipRect.right, clipRect.bottom);
        }
//        canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(bitmap,x,y,paint);
        if(isSelecting()) {
            canvas.drawRect(originalRect,paint);
        }
        canvas.restore();
    }

    public void draw(Canvas canvas,Matrix pageMatrix,Matrix matrix) {
        if(canvas==null){
            return;
        }
        if(bitmap==null||bitmap.isRecycled()){
            load();
            if(bitmap==null||bitmap.isRecycled()){
                return;
            }
       }

        Matrix matrix1 = new Matrix();
        matrix1.set(this.matrix);

        Matrix pageInvert= new Matrix();
        pageMatrix.invert(pageInvert);

        matrix1.postConcat(pageInvert);
        matrix1.postConcat(matrix);

        canvas.save();
        canvas.setMatrix(matrix1);
        canvas.drawBitmap(bitmap,x,y,paint);
        if(isSelecting()){
            canvas.drawRect(originalRect,paint);
        }
        canvas.restore();
    }

    public boolean load(){
        if(SynFileManager.getInstance().isDownloading(id)){
            return false;
        }
        if(bitmap==null||bitmap.isRecycled()){
            bitmap = BitmapManager.getInstence().loadBitmap(imgPath);
        }
        if(bitmap!=null){
            width = bitmap.getWidth();
            height = bitmap.getHeight();

            if(width>WhiteBoardUtils.IMG_MAX_WIDTH||height>WhiteBoardUtils.IMG_MAX_HEIGHT){
                float wScale = 1.0f;
                float hScale = 1.0f;
               if(width>WhiteBoardUtils.IMG_MAX_WIDTH){
                   wScale = (float)WhiteBoardUtils.IMG_MAX_WIDTH / (float)width;
               }

                if(height>WhiteBoardUtils.IMG_MAX_HEIGHT){
                    hScale = (float)WhiteBoardUtils.IMG_MAX_HEIGHT / (float)height;
                }

                if(wScale<hScale){
                    width = (int)(wScale * (float)width);
                    height = (int)(wScale * (float)height);
                }else{
                    width = (int)(hScale * (float)width);
                    height = (int)(hScale * (float)height);
                }

               Bitmap tempBitmap =  BitmapManager.zoomImg(bitmap,width,height);

                bitmap.recycle();
                bitmap = null;
                bitmap = tempBitmap;
            }

            //这里也添加下吧，否则无法显示
            originalRect.set((int)x,(int)y,(int)(x+width),(int)(y+height));
            clipRect.set((int)x,(int)y,(int)(x+width),(int)(y+height));
            TPLog.printKeyStatus("当前图片宽度："+width);
            TPLog.printKeyStatus("当前图片高度: "+height);
            return true;
        }else{
            if(NetUtil.isRemoteConf){
                return false;
            }
            TPLog.printError("加载图片失败，图片ID:"+id+",图片路径:"+imgPath+"\n重新下载。。。");
            SynFileManager.getInstance().requestDownload(id);
            return false;
        }
    }

    @Override
    public void changeCoordinate(Matrix matrix, float scale) {
        this.matrix.reset();
        this.matrix.set(matrix);

        RectF rf = new RectF(x,y,width+x,height+y);

        float values[] = new float[9];
        matrix.getValues(values);

        float tempScale = 1.0f/values[0];
        this.matrix.postScale(tempScale,tempScale,rf.centerX(),rf.centerY());

                //保存一份初始矩阵信息，还原矩阵信息时使用
        //matrix.getValues(originalMatrixValues);

        //获取当前矩阵的逆向矩阵
        Matrix inverse = new Matrix();
        this.matrix.invert(inverse);

        inverse.mapRect(rf);

        setX(rf.left);
        setY(rf.top);

        originalRect.set((int)rf.left,(int)rf.top,(int)rf.right,(int)rf.bottom);

        clipRect.set((int)rf.left,(int)rf.top,(int)rf.right,(int)rf.bottom);

        width = (int)clipRect.width();
        height = (int)clipRect.height();

//        Bitmap tempBmp = BitmapManager.zoomImg(bitmap,originalRect.width(),originalRect.height());
//        if(tempBmp!=null&&!tempBmp.isRecycled()){
//            bitmap = tempBmp;
//        }
//
//        width = bitmap.getWidth();
//        height = bitmap.getHeight();

        inverse = null;
    }

    @Override
    public ArrayList<Point> getPoints() {
        ArrayList<Point> ponits = new ArrayList<Point>();
        ponits.add(new Point((int)x,(int)y));
        ponits.add(new Point((int)x,(int)y));
        return ponits;
    }

    /**
     * 剪裁操作下返回源图区域大小，非剪裁操作下返回剪裁区域大小
     * @return
     */
    @Override
    public Rect getBounds() {
        if(cliping) {
            RectF rf = new RectF((int) x, (int) y, (int) (x + width), (int) (y + height));
            RectF dstRect = new RectF();
            matrix.mapRect(dstRect,rf);
            Rect rect = new Rect();
            dstRect.roundOut(rect);
            dstRect = null;
            return rect;
        }else{
            return getClipRectBounds();
    }
    }


    public void setClipRect(Rect rect){
        if(rect==null){
            TPLog.printWarning("设置的剪裁区域为null");
            return;
        }
        Matrix invert = new Matrix();
        matrix.invert(invert);
        invert.mapRect(clipRect,new RectF(rect.left,rect.top,rect.right,rect.bottom));
        invert = null;
    }

    public Rect getClipRectBounds() {
        RectF dstRect = new RectF();
        matrix.mapRect(dstRect,clipRect);

        Rect rt = new Rect();
        dstRect.roundOut(rt);
        dstRect = null;

        return rt;
    }

    @Override
    public void destroy() {
        if(bitmap!=null&&!bitmap.isRecycled()){
            bitmap.recycle();
        }
        bitmap = null;
        matrix = null;
        originalRect = null;
        clipRect = null;
        paint = null;
    }

    private void  debug(){
        float values[] = new float[9];
        matrix.getValues(values);
        String msg = "";
        for(int i = 0;i<9;i++){
            msg += values[i]+",";
        }
        TPLog.printKeyStatus("msg="+msg);
    }
}
