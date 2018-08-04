package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/7/20.
 */

public class DelSelectImgEntity {
    private String achTabId ;
    private int dwSubPageId;
    private String delGraphId[];

    public DelSelectImgEntity(String achTabId, int dwSubPageId,String delGraphId[]){
        this.achTabId = achTabId;
        this.dwSubPageId = dwSubPageId;
        this.delGraphId = delGraphId;
    }

    public String getAchTabId() {
        return achTabId;
    }

    public int getDwSubPageId() {
        return dwSubPageId;
    }

    public String[] getDelGraphId() {
        return delGraphId;
    }
}
