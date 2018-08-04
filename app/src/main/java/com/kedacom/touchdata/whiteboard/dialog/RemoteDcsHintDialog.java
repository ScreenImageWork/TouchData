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
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.BaseActivity;
import com.kedacom.touchdata.whiteboard.dialog.controler.IControler;
import com.kedacom.touchdata.whiteboard.dialog.view.TPPopupWindow;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;

/**
 * Created by zhanglei on 2018/4/28.
 */

public class RemoteDcsHintDialog implements IControler {

    private final static int KEEP_IIME = 5*1000;  //提示保存时间

    private Context mContext;

    private View contentView;

    private TextView mMsgTv;

    private TPPopupWindow mWindow;

    public RemoteDcsHintDialog(Context context){
        mContext = context;
        initView();
        initWindow();
    }

    private void initView(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        contentView = inflater.inflate(R.layout.hint_remote_dcs,null);
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
    public void show() {}

    private float offsetX = Integer.MIN_VALUE;
    private float offsetY = Integer.MIN_VALUE;

    public void show(View anchor) {
        if(!WhiteBoardUtils.isAPPShowing){
            return;
        }
        if(isShow()){
            restartTimer();
            return;
        }
        if(offsetX == Integer.MIN_VALUE||offsetY == Integer.MIN_VALUE) {

            float bottomIvWidth = mContext.getResources().getDimension(R.dimen.touchdata_bottom_btn_width);

            float bottomIvMarginRight = mContext.getResources().getDimension(R.dimen.new_menu_bottom_iv_marginright);

            int anchorPoints[] = new int[2];
            anchor.getLocationInWindow(anchorPoints);

            offsetY = WhiteBoardUtils.bottomBarHeight-10;

            offsetX = bottomIvMarginRight + bottomIvWidth + 10;
        }
        mWindow.showAtLocation(contentView,Gravity.BOTTOM|Gravity.LEFT,(int)offsetX,(int)offsetY);
        startTimer();
    }

    public void show(String hintText){
        mMsgTv.setText(hintText);
        show(new View(mContext));
    }

    @Override
    public void dismiss() {
        mWindow.dismiss();
        cancleTimer();
    }

    @Override
    public void destory() {
        if(isShow()){
            mWindow.dismiss();
        }
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
