package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2018/4/23.
 */

public class CenterHintToast implements IControler {

    private final static int KEEP_IIME = 5*1000;  //提示保存时间

    private Context mContext;

    private View contentView;

    private TextView mMsgTv;

    private TPPopupWindow mWindow;

    public CenterHintToast(Context context){
        mContext = context;
        initView();
        initWindow();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        contentView = inflater.inflate(R.layout.toast_centerhint,null);
        mMsgTv = (TextView) contentView.findViewById(R.id.hintView);
    }

    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setContentView(contentView);
        mWindow.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        mWindow.setFocusable(false);
        mWindow.setOutsideTouchable(true);
        mWindow.setOnDismissListener((BaseActivity)mContext);
    }

    public void setHintText(String text){
        mMsgTv.setText(text);
    }

    public void startTimer(){
        if(handler.hasMessages(100)){
            return;
        }
        handler.sendEmptyMessageDelayed(100,KEEP_IIME);
    }

    public void cancleTimer(){
        if(handler.hasMessages(100)){
            handler.removeMessages(100);
        }
    }

    public void restartTimer(){
        cancleTimer();
        startTimer();
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    @Deprecated
    public void show() {
        if(isShow()){
            restartTimer();
            return;
        }
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
        startTimer();
    }

    public void show(String hintText){
        mMsgTv.setText(hintText);
        show();
    }

    @Override
    public void dismiss() {
        if(mWindow!=null)
            mWindow.dismiss();
    }

    @Override
    public void destory() {
        dismiss();
        mWindow = null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                dismiss();
            }
        }
    };
}
