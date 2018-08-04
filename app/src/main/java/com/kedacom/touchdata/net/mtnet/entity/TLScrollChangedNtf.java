package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/5/30.
 */

public class TLScrollChangedNtf {
    private  String achTabId;
    private int subPageIndex;
    private int scrollX;
    private int scrollY;

    public TLScrollChangedNtf(String achTabId,int subPageIndex,int scrollX,int scrollY){
        setAchTabId(achTabId);
        setSubPageIndex(subPageIndex);
        setScrollX(scrollX);
        setScrollY(scrollY);
    }

    public String getAchTabId() {
        return achTabId;
    }

    public void setAchTabId(String achImgId) {
        this.achTabId = achImgId;
    }

    public int getSubPageIndex() {
        return subPageIndex;
    }

    public void setSubPageIndex(int subPageIndex) {
        this.subPageIndex = subPageIndex;
    }

    public int getScrollX() {
        return scrollX;
    }

    public void setScrollX(int scrollX) {
        this.scrollX = scrollX;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }
}
