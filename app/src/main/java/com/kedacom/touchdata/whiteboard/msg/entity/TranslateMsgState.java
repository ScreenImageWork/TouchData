package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2016/10/31.
 */
public class TranslateMsgState extends MsgState{

    private float offsetX;

    private float offsetY;

    private boolean isSyn = false;

    private boolean isComplete = false;

    public TranslateMsgState(){
        this(0,0,false,true);
    }

    public TranslateMsgState(float offsetX,float offsetY,boolean isComplete,boolean isSyn){
        setType(MsgType.TYPE_TRANSLATE);
        setSyn(isSyn);
        setComplete(isComplete);
        setOffsetX(offsetX);
        setOffsetY(offsetY);
    }


    public float getOffsetX() {
        return offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
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
