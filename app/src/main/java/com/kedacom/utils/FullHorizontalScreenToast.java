package com.kedacom.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.kedacom.touchdata.R;

import java.lang.ref.WeakReference;

/**
 * Created by zhanglei on 2017/1/6.
 */
public class FullHorizontalScreenToast {

    private final static int SPEED = 3*1000;

    private final static int DISMISS_WHAT = 1;

    private static FullHorizontalScreenToast mToast;

    private FloatViewManager mFloatManager;

    private WeakReference<Context> mContext;

    private TextView mTextView;

    private boolean isShowing;


    private FullHorizontalScreenToast(){

    }

    public synchronized static FullHorizontalScreenToast getInstance(){
        if(mToast==null){
            mToast = new FullHorizontalScreenToast();
        }
        return mToast;
    }

    public void init(Context context){
        mContext = new WeakReference<Context>(context);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_mytoast,null);
        mTextView = (TextView) view.findViewById(R.id.myToastView);

        mFloatManager = new FloatViewManager(context,view);
        mFloatManager.setFloatGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        mFloatManager.setAnimationStyle(R.style.toastAnimationStyle);
    }

    public void setText(String text){
        if(mTextView==null){
            return;
        }
        mTextView.setText(text);
        restartTimer();
    }


    public boolean isShowing(){
        return isShowing;
    }

    public void show(){
        if(isShowing)return;
        mFloatManager.show();
        startTimer();
        isShowing = true;
    }

    public void dismiss(){
        mFloatManager.hide();
        removeTimer();
    }

    private void startTimer(){
        removeTimer();
        mHandler.sendEmptyMessageDelayed(DISMISS_WHAT,SPEED);
    }

    private void removeTimer(){
        if(mHandler.hasMessages(DISMISS_WHAT)){
            mHandler.removeMessages(DISMISS_WHAT);
        }
    }

    private void restartTimer(){
        removeTimer();
        startTimer();
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DISMISS_WHAT ){
                mFloatManager.hide();
                isShowing = false;
            }
        }
    };

    public void destroy(){
        if(mFloatManager!=null) {
            mFloatManager.hide();
            mFloatManager.release();
        }
        mFloatManager = null;
        if(mContext!=null)
            mContext.clear();
        mContext = null;
        mTextView = null;
    }

}
