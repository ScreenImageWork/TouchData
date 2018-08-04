package com.kedacom.touchdata.whiteboard.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2017/8/22.
 */
public class SoftKeyboardMonitor extends FrameLayout{

    private OnSoftKeyboardListener mKeyboardListener;

    public SoftKeyboardMonitor(Context context) {
        super(context);
    }

    public SoftKeyboardMonitor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SoftKeyboardMonitor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        TPLog.printKeyStatus("oldh------->"+oldh);
        if(h<oldh){//输入法弹出
            if(mKeyboardListener!=null){
                mKeyboardListener.onSoftKeyboardShowing();
            }
        }else{ //输入法隐藏
            if(mKeyboardListener!=null){
                mKeyboardListener.onSoftKeyboardHide();
            }
        }
    }

    public void setOnSoftKeyboardListener(OnSoftKeyboardListener listener){
        mKeyboardListener = listener;
    }

    public interface  OnSoftKeyboardListener{
        void onSoftKeyboardShowing();
        void onSoftKeyboardHide();
    }
}
