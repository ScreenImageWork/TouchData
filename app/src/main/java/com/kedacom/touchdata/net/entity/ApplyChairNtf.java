package com.kedacom.touchdata.net.entity;

/**
 * Created by zhanglei on 2018/6/23.
 */

public class ApplyChairNtf {
    private  int dwMcuId;
    private int dwTerId;

    public ApplyChairNtf(int dwMcuId,int dwTerId){
        this.dwMcuId = dwMcuId;
        this.dwTerId = dwTerId;
    }

    public int getDwMcuId() {
        return dwMcuId;
    }

    public void setDwMcuId(int dwMcuId) {
        this.dwMcuId = dwMcuId;
    }

    public int getDwTerId() {
        return dwTerId;
    }

    public void setDwTerId(int dwTerId) {
        this.dwTerId = dwTerId;
    }
}
