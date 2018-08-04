package com.kedacom.osp.callback;

import com.kedacom.osp.entity.OspMsgEntity;

/**
 * Created by zhanglei on 2016/4/9.
 */
public interface OspCallback {
    void ospConnected();
    void ospDisconnect();
    void ospReceiveMsg(OspMsgEntity msgEntity);
    void ospException(Exception e);
}
