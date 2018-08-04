package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/11/15.
 */
public class DragLumpEntity  extends Entity {

    private int dragRightRes  = R.mipmap.drag_left_icon;
    private int dragLeftRes = R.mipmap.drag_right_icon;

    private final float spacing = WhiteBoardUtils.density*3;

    private Bitmap dragLeftBitmap;

    private Bitmap dragRightBitmap;

    private float width;
    private float height;

    private float progressBarWidth = 20;

    private float x;

    private float y;

    public DragLumpEntity(Context context,float progressBarWidth){
        dragLeftBitmap =  BitmapFactory.decodeResource(context.getResources(),dragLeftRes);
        dragRightBitmap = BitmapFactory.decodeResource(context.getResources(),dragRightRes);
        this.progressBarWidth = progressBarWidth;
        width = progressBarWidth*0.8f;
        height = width * 1.25f;
        dragLeftBitmap = changeBitmapSize(dragLeftBitmap);
        dragRightBitmap = changeBitmapSize(dragRightBitmap);
    }

    private Bitmap changeBitmapSize(Bitmap bitmap ){
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();

        if(bitmapWidth==width||bitmapHeight==height){
            return bitmap;
        }

        float scale = 1.0f;

        scale = (float)height / (float)bitmapHeight;

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmapWidth, bitmapHeight, matrix, true);
        bitmap.recycle();
        bitmap = null;
        bitmap = tempBitmap;
        return bitmap;
    }

    @Override
    public void onDraw(Canvas canvas) {
        float y = this.y-dragLeftBitmap.getHeight()/2;
        canvas.drawBitmap(dragLeftBitmap,x-spacing,y,null);
        float x = dragLeftBitmap.getWidth() + this.x+ progressBarWidth + spacing;
        canvas.drawBitmap(dragRightBitmap,x,y,null);
    }

    @Override
    public boolean contains(int x, int y) {
        return false;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        x = x - dragLeftBitmap.getWidth();
        this.x = x;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }
}
