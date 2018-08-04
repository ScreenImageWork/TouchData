package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/5/28.
 */

public class SynCoordinateMsg {
    private String achImgId;
    private int subPageIndex;
    private float matrixValues[];

    public SynCoordinateMsg(String achImgId, int subPageIndex,float matrixValues[]){
        setAchImgId(achImgId);
        setSubPageIndex(subPageIndex);
        setMatrixValues(matrixValues);
    }

    public String getAchImgId() {
        return achImgId;
    }

    public void setAchImgId(String achImgId) {
        this.achImgId = achImgId;
    }

    public int getSubPageIndex() {
        return subPageIndex;
    }

    public void setSubPageIndex(int subPageIndex) {
        this.subPageIndex = subPageIndex;
    }

    public float[] getMatrixValues() {
        return matrixValues;
    }

    public void setMatrixValues(float[] matrixValues) {
        this.matrixValues = matrixValues;
    }
}
