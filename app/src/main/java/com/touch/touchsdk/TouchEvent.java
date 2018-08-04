package com.touch.touchsdk;

/**
 * Created by zhanglei on 2018/2/5.
 */

public class TouchEvent {
    public static final int ACTION_DOWN = 1;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_UP = 3;

    private int action;
    private float x;
    private float y;
    private float width;
    private float height;
    private float pressure;

    public TouchEvent(){

    }

    public TouchEvent(int action,float x,float y,float width,float height,float pressure){
        setAction(action);
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setPressure(pressure);
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getPressure() {
        return pressure;
    }

    public void setPressure(float pressure) {
        this.pressure = pressure;
    }


    public static String actionToString(int action){
        switch(action){
            case ACTION_DOWN:

                return "ACTION_DOWN";
            case ACTION_MOVE:

                return "ACTION_MOVE";
            case ACTION_UP:

                return "ACTION_UP";
        }


        return "Non";
    }
}
