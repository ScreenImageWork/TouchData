package com.kedacom.touchdata.whiteboard.dialog;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.kedacom.touchdata.R;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2018/3/23.
 */

public class PaintRefreshDialog implements IControler {

    private final int REFRESH_MAX_COUNT = 4;

    private int curRefreshCount = 0;

    private Context mContext;

    private View contentView;

    private TPPopupWindow mWindow;

    public PaintRefreshDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }

    private void initView(){
        contentView = new View(mContext);
//        contentView.getLayoutParams().width = (int)WhiteBoardUtils.screenWidth;
//        contentView.getLayoutParams().height = (int)WhiteBoardUtils.screenHeight;
        contentView.setBackgroundColor(Color.parseColor("#08000000"));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initWindow(){
        mWindow = new TPPopupWindow(mContext);
//        mWindow.setAnimationStyle(R.style.colorDialog_anim_style);
        mWindow.setAnimationStyle(0);
        mWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        mWindow.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
//        mWindow.setWidth(1);
//        mWindow.setHeight(1);
        mWindow.setFocusable(false);
        mWindow.setTouchable(false);
        //mWindow.setFocusable(true);
        //mWindow.setOutsideTouchable(true);
        mWindow.setContentView(contentView);
        mWindow.setWindowLayoutType(WindowManager.LayoutParams.TYPE_TOAST);
    }


    @Override
    public boolean isShow() {
        return mWindow.isShowing();
    }

    @Override
    public void show() {
        if(mWindow.isShowing()){
            return;
        }
        mWindow.showAtLocation(contentView, Gravity.LEFT|Gravity.BOTTOM,0,0);
        curRefreshCount = 0;

        if(handler.hasMessages(100)){
            handler.removeMessages(100);
        }
        handler.sendEmptyMessageDelayed(100,20);
    }

    public void stopRefresh(){
        if(handler.hasMessages(100)){
            handler.removeMessages(100);
        }
        dismiss();
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
    }

    @Override
    public void destory() {
        if(isShow()){
            mWindow.dismiss();
        }
        mWindow = null;
        contentView = null;
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 100){
                if(curRefreshCount<REFRESH_MAX_COUNT) {
                    curRefreshCount++;
                    if (curRefreshCount % 2 != 0) {
                        contentView.setBackgroundColor(Color.parseColor("#03000000"));
                       // contentView.setVisibility(View.GONE);
                    } else {
                        contentView.setBackgroundColor(Color.parseColor("#08000000"));
                       // contentView.setVisibility(View.VISIBLE);
                    }

                    contentView.invalidate();
                    handler.sendEmptyMessageDelayed(100,20);
                }else{
                    dismiss();
                }
            }
        }
    };
}
