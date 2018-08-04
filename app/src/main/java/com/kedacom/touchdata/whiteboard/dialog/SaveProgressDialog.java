package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2017/11/20.
 */
public class SaveProgressDialog implements IControler {

    private Context mContext;

    private TPPopupWindow mWindow;

    private View contentView;

    private ImageView animView;

    private TextView saveHintTv;

    public SaveProgressDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }

    private void initView(){
        contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_save_progress,null);
        saveHintTv = (TextView) contentView.findViewById(R.id.saveHintTv);
        animView = (ImageView) contentView.findViewById(R.id.animView);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setContentView(contentView);
        mWindow.setOnDismissListener((BaseActivity)mContext);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_PHONE);
    }

    public void setText(String text){
        saveHintTv.setText(text);
    }

    @Override
    public boolean isShow() {
        if(mWindow == null){
            return false;
        }
        return mWindow.isShowing();
    }

    private void startAnim(){
        AnimationDrawable anim = (AnimationDrawable)animView.getBackground();
        anim.start();
    }

    private void stopAnim(){
//        if (animView!=null) {
//            animView.clearAnimation();
//        }
    }

    @Override
    public void show() {
        if(mWindow==null){
            return;
        }
        saveHintTv.setText("保存中...");
        TPLog.printError("SaveProgressWindow show...");
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
        startAnim();
    }

    public void show(String text) {
        if(mWindow==null){
            return;
        }
        saveHintTv.setText(text);
        TPLog.printError("SaveProgressWindow show...");
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
        startAnim();
    }

    @Override
    public void dismiss() {
        TPLog.printError("SaveProgressWindow dismiss...");
        stopAnim();
        if(mWindow==null){
            return;
        }
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        TPLog.printError("SaveProgressWindow destory...");
        if(isShow()){
            dismiss();
        }
        mWindow = null;
        contentView = null;
        animView = null;
    }
}