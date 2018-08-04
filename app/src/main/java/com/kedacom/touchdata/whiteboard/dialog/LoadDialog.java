package com.kedacom.touchdata.whiteboard.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2016/12/15.
 */
public class LoadDialog implements IControler{

    private Context mContext;

    private View contentView;

    private TextView msgTv;

    private TPPopupWindow mWindow;

    public LoadDialog(Context context){
        mContext = context;
        contentView = LayoutInflater.from(context).inflate(R.layout.view_load, null);
        msgTv = (TextView) contentView.findViewById(R.id.mTv);
        initWindow();
    }

    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setOnDismissListener((BaseActivity)mContext);
    }

    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    public void show(String msg){
        msgTv.setText(msg);
        show();
    }

    @Override
    @Deprecated
    public void show() {
        mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        mContext = null;
        contentView = null;
        msgTv = null;
        mWindow = null;
    }

}
