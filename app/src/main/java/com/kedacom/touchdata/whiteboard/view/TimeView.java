package com.kedacom.touchdata.whiteboard.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by zhanglei on 2018/6/1.
 */

@SuppressLint("AppCompatCustomView")
public class TimeView extends TextView {

    public static int MODE_COUNT_DOWN = 1; //倒计时

    public static int MODE_TIMEING = 2;  //顺计时

    private int timeMode = MODE_COUNT_DOWN;//默认倒计时

    private int startTime = 10;  //起始时间

    private int endTime = 0; //结束时间

    private int curTime = 0;

    private onTimeOutListener timeOutListener;

    private Handler timeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what!=100){
                return;
            }
            if(curTime!=endTime){
                if(timeMode == MODE_COUNT_DOWN){
                    curTime--;
                }else{
                    curTime++;
                }
                this.sendEmptyMessageDelayed(100,1000);
                if(timeOutListener!=null){
                    timeOutListener.onTimeing(curTime);
                }
            }else{
                if(timeOutListener!=null){
                    timeOutListener.onTimeOut();
                }
            }
        }
    };

    public TimeView(Context context) {
        super(context);
    }

    public TimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TimeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getTimeMode() {
        return timeMode;
    }

    public void setTimeMode(int timeMode) {
        this.timeMode = timeMode;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public void startTimer(){
        if(timeHandler.hasMessages(100)){
            return;
        }
        curTime = startTime;
        timeHandler.sendEmptyMessageDelayed(100,1000);
        if(timeOutListener!=null){
            timeOutListener.onTimeingStart(curTime);
        }
    }

    public void cancelTimer(){
        if(timeHandler.hasMessages(100)){
            timeHandler.removeMessages(100);
        }
        //做一次检查，如果没有取消成功，那么就继续取消，一般来说只会取消一次，下面代码不会执行
        if(timeHandler.hasMessages(100)){
            cancelTimer();
        }else{
            if(timeOutListener!=null){
                timeOutListener.onTimeingCancel();
            }
        }
    }

    public void reStartTimer(){
        cancelTimer();
        startTimer();
    }

    public void setOnTimeOutListener(onTimeOutListener listener){
        timeOutListener = listener;
    }

    public interface onTimeOutListener{
        void onTimeOut();
        void onTimeingStart(int time);
        void onTimeingCancel();
        void onTimeing(int time);
    }




}
