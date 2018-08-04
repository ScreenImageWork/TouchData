package com.kedacom.osp.entity;

/**
 * Created by zhanglei on 2016/4/7.
 *
 * osp消息头
 */
public class OspMsgHeadEntity {

    private long sourceNode;
    private long dstNode;
    private long dstIns;
    private long sourceIns;
    private int msgType;
    private int event;
    private int contentLength;
    private long msgPointer;
    private long aliasPointer;
    private byte aliaslength;

    public long getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(long sourceNode) {
        this.sourceNode = sourceNode;
    }

    public long getDstNode() {
        return dstNode;
    }

    public void setDstNode(long dstNode) {
        this.dstNode = dstNode;
    }

    public long getDstIns() {
        return dstIns;
    }

    public void setDstIns(long dstIns) {
        this.dstIns = dstIns;
    }

    public long getSourceIns() {
        return sourceIns;
    }

    public void setSourceIns(long sourceIns) {
        this.sourceIns = sourceIns;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public int getEvent() {
        return event;
    }

    public void setEvent(int event) {
        this.event = event;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public long getMsgPointer() {
        return msgPointer;
    }

    public void setMsgPointer(long msgPointer) {
        this.msgPointer = msgPointer;
    }

    public long getAliasPointer() {
        return aliasPointer;
    }

    public void setAliasPointer(long aliasPointer) {
        this.aliasPointer = aliasPointer;
    }

    public byte getAliaslength() {
        return aliaslength;
    }

    public void setAliaslength(byte aliaslength) {
        this.aliaslength = aliaslength;
    }

}
