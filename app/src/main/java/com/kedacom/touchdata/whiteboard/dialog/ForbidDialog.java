package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPDialogButton;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;

/**
 * Created by zhanglei on 2018/5/16.
 */

public class ForbidDialog  implements IControler {

    private Context context;

    private TPPopupWindow mWindow;

    private View contentView;

    private TextView msgView;

    private TPDialogButton sureBtn;

    @TargetApi(Build.VERSION_CODES.M)
    public ForbidDialog(Context context){

        this.context = context;

        LayoutInflater inflater = LayoutInflater.from(context);

        contentView = inflater.inflate(R.layout.layout_forbid, null);

        mWindow = new TPPopupWindow(context);
        mWindow.setContentView(contentView);
        mWindow.setFocusable(true);
        mWindow.setTouchable(true);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_APPLICATION);
        mWindow.setOnDismissListener((BaseActivity)context);
    }

    @Override
    public boolean isShow() {
        if(mWindow==null){
            return false;
        }
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        if(mWindow!=null){
            mWindow.showAtLocation(contentView, Gravity.CENTER,0,0);
        }
    }

    @Override
    public void dismiss() {
        if(mWindow!=null){
            mWindow.dismiss();
        }
    }

    @Override
    public void destory() {
        if(isShow()){
           dismiss();
        }
        mWindow = null;
    }
}
