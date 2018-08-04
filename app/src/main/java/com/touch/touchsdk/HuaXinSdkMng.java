package com.touch.touchsdk;

import android.util.Log;

/**
 * Created by zhanglei on 2018/4/17.
 */

public class HuaXinSdkMng {

    private static  TouchInterface tIf;

    public static void start(){
//        TouchDeviceInfo pDevInfos[] = new TouchDeviceInfo[8];
//        for (int i = 0; i < 8; i++)
//            pDevInfos[i] = new TouchDeviceInfo();
//
//        tIf = new TouchInterface();
//        tIf.InitTouch(pDevInfos, 8);
////        tIf.setOnTouchCallBack(callback);
//
//        for(int i = 0; i < 8; i++) {
//            if(pDevInfos[i].nVendorID != 0)
//                Log.i("TouchSDK java", "vid:" + pDevInfos[i].nVendorID + ", pid is " + pDevInfos[i].nProductID + ", device name is " + pDevInfos[i].sDeviceName);
//        }
    }

    public static void setCallback(TouchInterface.OnTouchCallBack callback){
        if(tIf!=null){
            tIf.setOnTouchCallBack(callback);
        }
    }
}
