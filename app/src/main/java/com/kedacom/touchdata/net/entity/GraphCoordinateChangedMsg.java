package com.kedacom.touchdata.net.entity;

/**
 * Created by zhanglei on 2017/7/5.
 */
public class GraphCoordinateChangedMsg {
    private int id;
    private float matrixValue[];
    private float drawMatrixValue[];

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float[] getMatrixValue() {
        return matrixValue;
    }

    public void setMatrixValue(float[] matrixValue) {
        this.matrixValue = matrixValue;
    }

    public float[] getDrawMatrixValue() {
        return drawMatrixValue;
    }

    public void setDrawMatrixValue(float[] drawMatrixValue) {
        this.drawMatrixValue = drawMatrixValue;
    }
}
