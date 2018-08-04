package com.kedacom.touchdata.whiteboard.msg.entity;

import android.graphics.Rect;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class DirtyRectMsgState extends MsgState{

    private Rect dirtyRect;

    public DirtyRectMsgState(){
        this(null);
    }

    public DirtyRectMsgState(Rect dRect){
        dirtyRect = dRect;
        setType(MsgType.TYPE_DIRTYRECT);
    }

    public Rect getDirtyRect() {
        return dirtyRect;
    }

    public void setDirtyRect(Rect dirtyRect) {
        this.dirtyRect = dirtyRect;
    }
}
