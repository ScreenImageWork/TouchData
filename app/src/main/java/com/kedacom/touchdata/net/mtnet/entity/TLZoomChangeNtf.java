package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/5/30.
 */

public class TLZoomChangeNtf {
    private  String achTabId;
    private float dwZoom;

    public TLZoomChangeNtf(String achImgId,float dwZoom){
        setAchTabId(achImgId);
        setDwZoom(dwZoom);
    }

    public String getAchTabId() {
        return achTabId;
    }

    public void setAchTabId(String achImgId) {
        this.achTabId = achImgId;
    }

    public float getDwZoom() {
        return dwZoom;
    }

    public void setDwZoom(float dwZoom) {
        this.dwZoom = dwZoom;
    }
}
