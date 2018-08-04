package com.kedacom.touchdata.net.entity;

/**
 * Created by zhanglei on 2017/7/5.
 */
public class DeleteGraphMsg {

    private long tabId;

    private int subPageIndex;

    private int graphId;


    public long getTabId() {
        return tabId;
    }

    public void setTabId(long tabId) {
        this.tabId = tabId;
    }

    public int getSubPageIndex() {
        return subPageIndex;
    }

    public void setSubPageIndex(int subPageIndex) {
        this.subPageIndex = subPageIndex;
    }

    public int getGraphId() {
        return graphId;
    }

    public void setGraphId(int graphId) {
        this.graphId = graphId;
    }
}
