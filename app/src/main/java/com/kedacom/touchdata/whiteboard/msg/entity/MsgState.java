package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2016/11/1.
 */
public class MsgState {

    public static enum MsgType{
        TYPE_PAINT,
        TYPE_ERASE,
        TYPE_AREA_ERASE,
        TYPE_ROTATE,
        TYPE_SCALE,
        TYPE_TRANSLATE,
        TYPE_DIRTYRECT,
        TYPE_FRAME,
        TYPE_SCALE_TRANSLATE,
        TYPE_SELECT_GRAPH,
        TYPE_REFRESH,
        TYPE_SAVE_PAINT,
        TYPE_BRUSH_PEN
    };

    private  MsgType type = MsgType.TYPE_PAINT;

    public MsgType getType() {
        return type;
    }

    protected void setType(MsgType type) {
        this.type = type;
    }

}
