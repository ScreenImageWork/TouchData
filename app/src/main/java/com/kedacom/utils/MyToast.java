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
public class MyToast {

    private final static int SPEED = 2*1000;

    private final static int DISMISS_WHAT = 1;

    private static MyToast mToast;

    private FloatViewManager mFloatManager;

    private WeakReference<Context> mContext;

    private TextView mTextView;

    private boolean isShowing;


    private MyToast(){

    }

    public synchronized static MyToast getInstance(){
        if(mToast==null){
            mToast = new MyToast();
        }
        return mToast;
    }

    public void init(Context context){
        mContext = new WeakReference<Context>(context);

        View view = LayoutInflater.from(context).inflate(R.layout.layout_mytoast,null);
        mTextView = (TextView) view.findViewById(R.id.myToastView);

        float bottombar_height = context.getResources().getDimension(R.dimen.touchdata_bottombar_height);

        mFloatManager = new FloatViewManager(context,view);
        mFloatManager.setFloatGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        mFloatManager.setAnimationStyle(R.style.toastAnimationStyle);
        mFloatManager.setPosition(0,(int)bottombar_height+50);
    }

   public void setText(String text){
        mTextView.setText(text);
        restartTimer();
    }

    public boolean isShowing(){
        return isShowing;
    }

    public void show(){
        if(isShowing)return;
        if(mFloatManager==null)
            return;
        mFloatManager.show();
        startTimer();
        isShowing = true;
    }

    public void dismiss(){
        if(mFloatManager==null){
            return;
        }
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
               if(mFloatManager!=null)
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
