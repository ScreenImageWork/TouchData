package com.kedacom.touchdata.whiteboard.colorpanel.entity;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.Log;

import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2016/11/22.
 */
public class HSVProgressEntity extends Entity {

    private float px;

    private float py;

    private float width;

    private float height;

    private float boudnsSize = 2;

    private int boundsColor = Color.parseColor("#333333");

    private Bitmap hsvBitmap;

    private Canvas hsvCanvas;

    private Paint paint;

    private RectF colorRect;

    private RectF colorCurRect ;

    private RectF bounds;

    private RectF touchBounds;//单纯的滚动条太窄了不方便触摸，因此在外面添加了一个新的触摸区

    private LinearGradient shader;

    private DragLumpEntity mDragLumpEntity;

    public HSVProgressEntity(Context context, float px, float py, float width, float height){
        this.px = px;
        this.py = py;
        this.width = width;
        this.height = height;
        init(context);
    }

    private void init(Context context){

        boudnsSize = boudnsSize * WhiteBoardUtils.density;

        mDragLumpEntity = new DragLumpEntity(context,width);
        mDragLumpEntity.setX(px);
        mDragLumpEntity.setY(py);

        float dragWidth = mDragLumpEntity.getWidth();
        float dragHeight = mDragLumpEntity.getHeight();

        float cpx = px - dragWidth;
        float cpy = py - dragHeight;
        float cprx = px + width + dragWidth;
        float cpry = cpy + height + dragHeight;
        touchBounds = new RectF(cpx,cpy,cprx,cpry);

//        startCurRect = new RectF(0,0,width,height/2f);
        colorCurRect = new RectF(0,0,width,height);

//        startRect = new RectF(px,py,px+width,py+height/2f);
        colorRect = new RectF(px,py,px+width,py+height);

        bounds = new RectF(px-boudnsSize,py-boudnsSize,px+width+boudnsSize,py+height+boudnsSize);


        hsvBitmap = Bitmap.createBitmap((int)width,(int)height,Bitmap.Config.ARGB_8888);
        hsvCanvas = new Canvas(hsvBitmap);

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
    }


    public void setColor(int color){
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);

        float hsv[] = new float[3];
        Color.RGBToHSV(red, green, blue, hsv);

        float startHsv[] = new float[3];
        float endHsv[] = new float[3];
        System.arraycopy(hsv, 0, endHsv, 0, endHsv.length);
        endHsv[2] = 0;
        System.arraycopy(hsv, 0, startHsv, 0, startHsv.length);
        startHsv[2] =1f;

        int startColor = Color.HSVToColor(startHsv);
        int endColor = Color.HSVToColor(endHsv);

        int colors[] = {Color.WHITE,startColor,endColor,};
        shader = new LinearGradient(0,0,0,height,colors,null,Shader.TileMode.CLAMP);

        paint.setStyle(Paint.Style.FILL);
        paint.setShader(shader);
        hsvCanvas.drawRect(colorCurRect,paint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(shader);
        canvas.drawRect(colorRect,paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setShader(null);
        paint.setColor(boundsColor);
        paint.setStrokeWidth(boudnsSize);
        canvas.drawRect(bounds,paint);

        mDragLumpEntity.onDraw(canvas);
    }

    public boolean isTouch(float x,float y){
        return touchBounds.contains(x,y);
    }

    @Override
    public boolean contains(int x, int y) {
        return bounds.contains(x,y);
    }

    public int getColor(float x,float y){
        x = x - px;
        y = y - py;

        if(y<3){
            return Color.parseColor("#ffffff");
        }

        if(y<0)y=0;
        if(y>hsvBitmap.getHeight())y = hsvBitmap.getHeight();
        return hsvBitmap.getPixel(hsvBitmap.getWidth()/2,(int)y);
    }

    public void dragToPosition(int px,int py){
        if(this.py>py)
            py=(int)this.py;

        if((this.py+height)<py)
            py=(int)(this.py+height);

        mDragLumpEntity.setY(py);
    }

    private OnHSVChangedListener hSVListener;
    public void setOnHSVChangedListener(OnHSVChangedListener mlistener){
        hSVListener = mlistener;
    }

    public interface OnHSVChangedListener{
        void onHSVChanged(int color);
    }
}
