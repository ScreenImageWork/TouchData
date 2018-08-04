package com.kedacom.touchdata.net.entity;

/**
 * Created by zhanglei on 2017/6/7.
 */
public class GestureScaleEntity {

    private float scaleFactor;

    private int focusX;

    private int focusY;

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getFocusX() {
        return focusX;
    }

    public void setFocusX(int focusX) {
        this.focusX = focusX;
    }

    public int getFocusY() {
        return focusY;
    }

    public void setFocusY(int focusY) {
        this.focusY = focusY;
    }
}
