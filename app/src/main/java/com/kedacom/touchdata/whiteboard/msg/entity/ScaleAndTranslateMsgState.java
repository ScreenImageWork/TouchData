package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2017/5/24.
 */
public class ScaleAndTranslateMsgState extends MsgState{

    private ScaleMsgState mScaleMsgState;

    private TranslateMsgState mTranslateMsgState;

    private boolean scalExtremity;

    public ScaleAndTranslateMsgState(ScaleMsgState mScaleMsgState,TranslateMsgState mTranslateMsgState){
        setType(MsgType.TYPE_SCALE_TRANSLATE);
        setmScaleMsgState(mScaleMsgState);
        setmTranslateMsgState(mTranslateMsgState);
    }

    public ScaleMsgState getmScaleMsgState() {
        return mScaleMsgState;
    }

    public void setmScaleMsgState(ScaleMsgState mScaleMsgState) {
        this.mScaleMsgState = mScaleMsgState;
    }

    public TranslateMsgState getmTranslateMsgState() {
        return mTranslateMsgState;
    }

    public void setmTranslateMsgState(TranslateMsgState mTranslateMsgState) {
        this.mTranslateMsgState = mTranslateMsgState;
    }

    public boolean isScalExtremity() {
        return scalExtremity;
    }

    public void setScalExtremity(boolean scalExtremity) {
        this.scalExtremity = scalExtremity;
    }
}
