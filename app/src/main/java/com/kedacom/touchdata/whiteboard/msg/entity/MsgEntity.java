package com.kedacom.touchdata.whiteboard.msg.entity;

/**
 * Created by zhanglei on 2016/10/31.
 */
public class MsgEntity {

    private MsgState curState;

    public MsgEntity(){

    }

    public MsgEntity(MsgState state){
        curState = state;
    }

    public MsgState getCurState() {
        return curState;
    }

    public void setCurState(MsgState curState) {
        this.curState = curState;
    }
}
