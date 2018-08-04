package com.kedacom.osp.msg;

/**
 * Created by zhanglei on 2018/3/16.
 */

public class SendMsg {

    private byte[] msg;

    public SendMsg(){

    }

    public SendMsg(byte msg[]){
        this.msg = msg;
    }


    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }
}
