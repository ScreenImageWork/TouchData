package com.touch.touchsdk;

import android.util.Log;

public class TouchInterface {

    TouchPointData TouchData[];
    int nMaxPointNum;
    boolean[] touchState = new boolean[32];
    PenStruct penMsg;
    TouchStruct[] touchMsg;
    public TouchInterface( )
    {
        nMaxPointNum = 0;
        TouchData = new TouchPointData[32];
        for (int i = 0; i < 32; i++)
        {
            TouchData[i] = new TouchPointData();
        }
        penMsg = new PenStruct();
        touchMsg = new TouchStruct[32];
        for (int i = 0; i < 32; i++)
        {
            touchMsg[i] = new TouchStruct();
        }
        for (int i = 0; i < 32; i++)
        {
            touchState[i] = false;
        }

    }

    public void MultiTouchDataCallback()
    {
        nMaxPointNum = GetTouchData(TouchData, 32);

        for(int i = 0; i<nMaxPointNum; i++){
            TouchData[i].x = (int)(TouchData[i].x*0.05859375f);
            TouchData[i].y = (int)(TouchData[i].y*0.032958984f);
            if((TouchData[i].x != 0)||(TouchData[i].y != 0)) {
                Log.i("TouchSDK-MultiTouchData", "fusion data: " + "id= " + TouchData[i].c +
                        ", ts=" + TouchData[i].ts +
                        ", x=" + TouchData[i].x +
                        ", y=" + TouchData[i].y +
                        ", w=" + TouchData[i].w +
                        ", h=" + TouchData[i].h +
                        ", e=" + TouchData[i].e +
                        ", p=" + TouchData[i].p +
                        ", op=" + TouchData[i].op +
                        ", r=" + TouchData[i].r);


                if(touchCallBack!=null){
                    touchCallBack.touchCallBack(new TouchEvent(TouchData[i].ts,TouchData[i].x,TouchData[i].y,TouchData[i].w,TouchData[i].h,TouchData[i].p));
                }
            }
        }
    }

    synchronized public void TouchRawDataFormNative(int TouchRawDatas[], int nDataLen) {
        int id;
        int state;
        for(int j = 0; j< 32; j++) {
            touchMsg[j].id = 0;
            touchMsg[j].s = 0;
            touchMsg[j].x = 0;
            touchMsg[j].y = 0;
            touchMsg[j].w = 0;
            touchMsg[j].h = 0;
        }

        for (int i = 0; i < 6; i++) {
            touchMsg[i].id = (TouchRawDatas[2 + i * 10] & 0xff);
            if (touchMsg[i].id == 0xff) {
                touchMsg[i].id = 0;
            }
            touchMsg[i].s = (TouchRawDatas[1 + i * 10] & 0xff);

            id = touchMsg[i].id;
            state = touchMsg[i].s;

            if (state == 0x02) {
                if (touchState[id] == false) {
                    touchMsg[i].s = 1;  // 0:none;1:down;2:move;3:up;
                    touchState[id] = true;
                } else {
                    touchMsg[i].s = 3;
                    touchState[id] = false;
                }
            }
            if (state == 0x03) {
                if (touchState[id] == false) {
                    touchMsg[i].s = 1;
                    touchState[id] = true;
                }

                touchMsg[i].s = 2;
            }

            touchMsg[i].x = ( TouchRawDatas[4 + i * 10] & 0xff) * 256 + ( TouchRawDatas[3 + i * 10] & 0xff);
            touchMsg[i].y = ( TouchRawDatas[6 + i * 10] & 0xff) * 256 + ( TouchRawDatas[5 + i * 10] & 0xff);
            touchMsg[i].w = ( TouchRawDatas[8 + i * 10] & 0xff) * 256 + ( TouchRawDatas[7 + i * 10] & 0xff);
            touchMsg[i].h = ( TouchRawDatas[10 + i * 10] & 0xff) * 256 + ( TouchRawDatas[9 + i * 10] & 0xff);
            if((touchMsg[i].x !=0)||(touchMsg[i].y !=0))
            {
                touchMsg[i].x = (int)(touchMsg[i].x*0.05859375f);
                touchMsg[i].y = (int)(touchMsg[i].y*0.032958984f);
                Log.d("TouchSDK-TouchRawData", "touch data:" + " id: " + touchMsg[i].id +" s: " + touchMsg[i].s +" x: " + touchMsg[i].x +" y: " + touchMsg[i].y +" w: " + touchMsg[i].w +" h: " + touchMsg[i].h);
            }
        }
    }

    synchronized public void PenRawDataFromNative(int PenRawDatas[], int nDataLen)
    {
        penMsg.id = (PenRawDatas[2] & 0xff);
        penMsg.t  = ( PenRawDatas[1] & 0xff);
        penMsg.s  = (PenRawDatas[3] & 0xff);
        penMsg.p  = (PenRawDatas[5] & 0xff) * 256 + ((int) PenRawDatas[4] & 0xff);

        Log.d("TouchSDK-PenRawData", "pen data: "+" id: "+penMsg.id+" t: "+penMsg.t+" s: "+penMsg.s+" p: "+penMsg.p);
    }

    public void HotplugCallback(TouchDeviceInfo DevInfo, boolean attached)
    {
        Log.i("TouchSDK", "attached="+attached+", vid is "+DevInfo.nVendorID);
    }

    public native int GetTouchData(TouchPointData[] pData, int nMaxCount);
    public native void InitTouch(TouchDeviceInfo[] pDevInfoList, int nMaxCount);
    public native void ExitTouch();
    public native int GetDeviceCount();
    public native int GetTouchDevInfo(TouchDeviceInfo[] pDevInfoList, int nMaxCount);

    public native boolean SendData(TouchDeviceInfo DevInfo, byte[] pSrcData, int nSrcLen, byte[] pRecvData, int nRetLen);

    public native boolean EnableRawData(TouchDeviceInfo DevInfo);
    public native boolean DisableRawData(TouchDeviceInfo DevInfo);

    // set if device send touch width data, used for IRTouch
    public native boolean EnableTouchWidthData(TouchDeviceInfo DevInfo);
    public native boolean DisableTouchWidthData(TouchDeviceInfo DevInfo);


    private OnTouchCallBack touchCallBack;
    public void setOnTouchCallBack(OnTouchCallBack touchCallBack){
        this.touchCallBack = touchCallBack;
    }

    public interface OnTouchCallBack{
        void touchCallBack(TouchEvent event);
    }


    static {
        System.loadLibrary("TouchSDK");
    }



}