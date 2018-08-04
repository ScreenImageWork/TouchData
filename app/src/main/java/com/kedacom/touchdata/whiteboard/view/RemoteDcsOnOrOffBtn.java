package com.kedacom.touchdata.whiteboard.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.tplog.TPLog;
import com.touch.touchsdk.TouchInterface;

/**
 * Created by zhanglei on 2018/4/25.
 */

public class RemoteDcsOnOrOffBtn extends View implements View.OnTouchListener{

    private final static int BK_COLOR_SELECT = Color.parseColor("#e500aff2");
    private final static int BK_COLOR_NORMAL = Color.parseColor("#e54e4e4e");
    private final static int CONTOUR_COLOR = Color.parseColor("#9f9f9f");
    private final static int TEXT_COLOR= Color.parseColor("#FFFFFF");

    private RectF viewRect = new RectF(); //边框
    private RectF viewRect2 = new RectF(); //背景

    private DragHandle mDragHandle;

    private Paint paint;

    private boolean open = false;


    private Handler hand = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                if(mDragHandle.isAutoMove()){
                    invalidate();
                    sendEmptyMessageDelayed(100,16);
                }
            }
        }
    };

    public RemoteDcsOnOrOffBtn(Context context) {
        super(context);
        init();
    }

    public RemoteDcsOnOrOffBtn(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RemoteDcsOnOrOffBtn(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){

        setOnTouchListener(this);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(BK_COLOR_NORMAL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        TPLog.printError("onDraw------------------------onLayout changed = " +changed);
        if(changed) {
            viewRect.set(1, 1, getWidth() - 1, getHeight() - 1);
            viewRect2.set(3, 3, getWidth() - 3, getHeight() - 3);
            mDragHandle = new DragHandle();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return  mDragHandle.onTouch(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        paint.setColor(CONTOUR_COLOR);
        paint.setStrokeWidth(1.5f);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(viewRect,viewRect.height()/2f+2,viewRect.height()/2f,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(BK_COLOR_NORMAL);
        canvas.drawRoundRect(viewRect2,viewRect2.height()/2f+2,viewRect2.height()/2f,paint);
        mDragHandle.draw(canvas);
    }


    public void setSelect(boolean select){
        TPLog.printError("  setSelect   select == "+select+"-----open = "+open);
        if(open == select){
            return;
        }
        open = select;
        if(mDragHandle!=null) {
            mDragHandle.select(select);
        }
    }

    public boolean isSelect(){
        return open;
    }


    class DragHandle{

        private float x = 2;

        private float y = 2;

        private int width;

        private int height;

        private float lastX;

        private float lastY;

        private boolean canDrag = false;

        private int autoMoveSpeed = 4;

        private boolean autoMove = false;

        private int dragHandleBmpIndex = 0;


        public DragHandle(){
            width = BitmapManager.REMOTE_DCCONF_ON_OFF_BTN_ICON[0].getWidth();
            height = BitmapManager.REMOTE_DCCONF_ON_OFF_BTN_ICON[0].getHeight();
           if(open){
               x = viewRect2.right - width;
               dragHandleBmpIndex = 2;
           }else{
               x = viewRect2.left;
               dragHandleBmpIndex = 0;
           }
            y = (viewRect.height() - height)/2f+1;
        }

        public boolean isAutoMove(){
            return autoMove;
        }

        public void select(boolean isSelect){
            if(isSelect){
                x = viewRect2.right - width;
                dragHandleBmpIndex = 2;
            }else{
                x = viewRect2.left;
                dragHandleBmpIndex = 0;
            }

            autoMove = false;
            invalidate();
        }

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public void draw(Canvas canvas){
            if(autoMove){
                x = x + autoMoveSpeed;
                if(x<=viewRect2.left){
                    x = viewRect2.left;
                    autoMove = false;
                    dragHandleBmpIndex = 0;
                }else if((x+width)>=viewRect2.right){
                    x = viewRect2.right - width;
                    autoMove = false;
                    dragHandleBmpIndex = 2;
                }
            }

            if(open||canDrag||(autoMove&&autoMoveSpeed<0)) {
                paint.setColor(BK_COLOR_SELECT);
                canvas.drawRoundRect(viewRect2.left, viewRect2.top, x + width, viewRect2.bottom, viewRect2.height() / 2+2, viewRect2.height() / 2, paint);
            }

            if(!canDrag&&!autoMove){
                paint.setTextSize(15);
                paint.setStrokeWidth(1);
//                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(TEXT_COLOR);
                if(x==viewRect2.left){//显示打开文字
                    canvas.drawText("数据协作",x+width+7,viewRect2.top+22,paint);
                }else if(x == (viewRect2.right - width)){ //显示关闭文字
                    canvas.drawText("退出协作",13,viewRect2.top+22,paint);
                }
            }

            canvas.drawBitmap(BitmapManager.REMOTE_DCCONF_ON_OFF_BTN_ICON[dragHandleBmpIndex],x,y,null);

            TPLog.printError("onDraw------------------------open = "+open + ",autoMove = "+mDragHandle.autoMove);
            TPLog.printError("onDraw------------------------x = "+mDragHandle.x);
        }

        public RectF getBounds(){
            return new RectF(x,y,x+width,y+height);
        }

        public boolean onTouch(MotionEvent event){
            if(autoMove){
                return false;
            }
            int action = event.getActionMasked();
            float curX = event.getX();
            float curY = event.getY();
            if(action == MotionEvent.ACTION_DOWN){
                if(!getBounds().contains(curX,curY)){
                    canDrag = false;
                    return false;
                }
                lastX = curX;
                lastY = curY;
                canDrag = true;
                if(open){
                    dragHandleBmpIndex = 3;
                }else{
                    dragHandleBmpIndex = 1;
                }
                invalidate();
                return true;
            }

            if(!canDrag){
                return false;
            }

            float cx = curX - lastX;

            x = x + cx;

            if(x<viewRect2.left){
                x = viewRect2.left;
            }else if((x+width)>viewRect2.right){
                x = viewRect2.right - width;
            }

            lastX = curX;
            lastY = curY;

            if(action == MotionEvent.ACTION_UP||action == MotionEvent.ACTION_CANCEL){
                canDrag = false;
                boolean callback = true;
                if(x!=viewRect2.left||x!=(viewRect2.right - width)){//没有在两端
                    if((x+width/2)<=viewRect2.centerX()){//靠近左边，执行关闭动画
                        autoMoveSpeed = Math.abs(autoMoveSpeed)*-1;
                        if(!open){
                            callback = false;
                        }
                        open = false;
                    }else{ //执行打开动画
                        autoMoveSpeed = Math.abs(autoMoveSpeed);
                        if(open){
                            callback = false;
                        }
                        open = true;
                    }
                    autoMove = true;
                    hand.sendEmptyMessageDelayed(100,16);
                }else{
                    if(x==viewRect2.left){
                        dragHandleBmpIndex = 0;
                        if(!open){
                            callback = false;
                        }
                        open = false;
                    }else{
                        if(open){
                            callback = false;
                        }
                        dragHandleBmpIndex = 2;
                        open = true;
                    }

                }

                if(listener!=null&&callback){
                    listener.onSelect(open);
                }
            }else{
                invalidate();
            }
            return true;
        }
    }


    private OnSelectListener listener;
    public void setOnSelectListener(OnSelectListener listener){
        this.listener = listener;
    }

    public interface OnSelectListener{
        void onSelect(boolean select);
    }


}
