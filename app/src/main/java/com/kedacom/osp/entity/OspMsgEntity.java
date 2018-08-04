package com.kedacom.osp.entity;

/**
 * Created by zhanglei on 2016/4/7.
 */
public class OspMsgEntity {

    private OspMsgHeadEntity msgHead;

    private byte[] content;

    public OspMsgHeadEntity getMsgHead() {
        return msgHead;
    }

    public void setMsgHead(OspMsgHeadEntity msgHead) {
        this.msgHead = msgHead;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
