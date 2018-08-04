package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2016/10/31.
 */
public class ScaleMsgState extends MsgState{

    private float scale;

    private boolean isComplete;

    private boolean isSyn = false;

    private float curFocusX = 0;
    private float curFocusY = 0;

    public ScaleMsgState(){
        this(1.0f,false,true);
    }

    public ScaleMsgState(float scale,boolean isComplete,boolean isSyn){
        setType(MsgType.TYPE_SCALE);
        setScale(scale);
        setComplete(isComplete);
        setSyn(isSyn);
    }

    public ScaleMsgState(float scale,float focusX,float focusY,boolean isComplete,boolean isSyn){
        setType(MsgType.TYPE_SCALE);
        setScale(scale);
        setComplete(isComplete);
        setSyn(isSyn);
        setCurFocusX(focusX);
        setCurFocusY(focusY);
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
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

    public float getCurFocusX() {
        return curFocusX;
    }

    public void setCurFocusX(float curFocusX) {
        this.curFocusX = curFocusX;
    }

    public float getCurFocusY() {
        return curFocusY;
    }

    public void setCurFocusY(float curFocusY) {
        this.curFocusY = curFocusY;
    }
}
