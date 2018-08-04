package com.kedacom.touchdata.net.entity;

/**
 * Created by zhanglei on 2017/7/4.
 */
public class CoordinateChangedMsg {
    private long tabId;
    private long subPageIndex;
    private float matrixValues[];

    public long getTabId() {
        return tabId;
    }

    public void setTabId(long tabId) {
        this.tabId = tabId;
    }

    public long getSubPageIndex() {
        return subPageIndex;
    }

    public void setSubPageIndex(long subPageIndex) {
        this.subPageIndex = subPageIndex;
    }

    public float[] getMatrixValues() {
        return matrixValues;
    }

    public void setMatrixValues(float[] matrixValues) {
        this.matrixValues = matrixValues;
    }
}
