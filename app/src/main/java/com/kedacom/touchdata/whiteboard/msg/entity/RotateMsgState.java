package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2016/10/31.
 */
public class RotateMsgState extends MsgState{

    private int angle;

    private boolean isSyn;

    private boolean isComplete;

    public RotateMsgState(){
       this(0,false,true);
    }

    public RotateMsgState(int angle,boolean isComplete,boolean isSyn){
        setType(MsgType.TYPE_ROTATE);
        setComplete(isComplete);
        setSyn(isSyn);
        setAngle(angle);
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isSyn() {
        return isSyn;
    }

    public void setSyn(boolean syn) {
        isSyn = syn;
    }
}
