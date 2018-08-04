package com.kedacom.utils;

import android.os.Handler;
import android.os.Message;

import com.kedacom.tplog.TPLog;

import java.util.logging.LogRecord;

/**
 * Created by zhanglei on 2017/9/13.
 */
public abstract class TPTimer extends Handler {

    private static final int WHAT_TIMER = 555;

    private long timeOut = 5*1000;

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public void startTimer(){
        TPLog.printKeyStatus("启动定时器。。。");
        if(!hasMessages(WHAT_TIMER)) {
            sendEmptyMessageDelayed(WHAT_TIMER, timeOut);
        }
    }

    public void cancelTimer(){
        TPLog.printKeyStatus("取消定时器。。。");
        removeMessages(WHAT_TIMER);
        if(hasMessages(WHAT_TIMER)){
            cancelTimer();
        }
    }

    public void reStartTimer(){
        TPLog.printKeyStatus("重新设置时定时器。。。");
        cancelTimer();
        startTimer();
    }

    public abstract void onTPTimerTask();

    @Override
    public void handleMessage(Message msg) {
       if(msg.what == WHAT_TIMER){
           onTPTimerTask();
       }
    }
}
