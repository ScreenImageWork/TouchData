package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/4/16.
 */

public class UnDoOrReDoNtf {
    public String tabId;
    public int subPageIndex;

    public UnDoOrReDoNtf(){

    }

    public UnDoOrReDoNtf(String tabId, int subPageIndex){
        this.tabId = tabId;
        this.subPageIndex = subPageIndex;
    }

}
