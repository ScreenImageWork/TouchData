package com.kedacom.touchdata.whiteboard.msg.entity;

import android.graphics.Bitmap;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class FrameMsgState extends MsgState{

    private Bitmap frame;

    public FrameMsgState(){
        this(null);
    }

    public FrameMsgState(Bitmap frame){
        setType(MsgType.TYPE_FRAME);
        setFrame(frame);
    }

    public Bitmap getFrame() {
        return frame;
    }

    public void setFrame(Bitmap frame) {
        this.frame = frame;
    }

    public void destory(){
        if(frame!=null&&frame.isRecycled()){
            frame.recycle();
            frame = null;
        }
    }
}
