package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/7/20.
 */

public class ImgCoordinate{
    private float aachMatrixValue[];
    private String achGraphsId;

    public ImgCoordinate(String graphsId,float matrixValue[]){
        aachMatrixValue = matrixValue;
        achGraphsId = graphsId;
    }

    public float[] getAachMatrixValue() {
        return aachMatrixValue;
    }

    public String getAchGraphsId() {
        return achGraphsId;
    }
}