package com.kedacom.touchdata.whiteboard.dialog.view;

import android.os.Handler;
import android.os.Message;
import android.view.View;

/**
 * Created by zhanglei on 2017/7/24.
 *
 */
public class TPDialogBtnHelper {

    private static final long CLICK_DELAYED = 200; //点击事件执行间隔

    private static final int CLICK_MSG_WHATE = 110;

    private static TPDialogBtnHelper instance;


    private Handler hand = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == CLICK_MSG_WHATE){
                ((ITPButton)msg.obj).click();
            }
        }
    };

    private TPDialogBtnHelper(){
    }

    public synchronized static TPDialogBtnHelper getInstance(){
        if (instance == null) {
            instance = new TPDialogBtnHelper();
        }
        return instance;
    }

    public void executeBtnClick(ITPButton btn){
        btn.preClick();
        Message msg = hand.obtainMessage();
        msg.what = CLICK_MSG_WHATE;
        msg.obj = btn;
        hand.sendMessageDelayed(msg,CLICK_DELAYED);
    }



}
