package com.kedacom.utils;

import android.os.Build;

import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.tplog.TPLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Created by zhanglei on 2017/12/26.
 */

public class VersionUtils {
    //IMIX 目前已知版本
    public final static String DEVICE_MODEL_UNKNOWN = "unknown";

    public final static String DEVICE_MODEL_55_INCH_SINGLE = "IMIX-S0";

    public final static String DEVICE_MODEL_55_INCH_DOUBLE = "IMIX-D0";

    public final static String DEVICE_MODEL_65_INCH_SINGLE = "IMIX-S1";

    public final static String DEVICE_MODEL_65_INCH_DOUBLE = "IMIX-D1";

    public final static String DEVICE_MODEL_75_INCH_SINGLE = "";

    public final static String DEVICE_MODEL_75_INCH_DOUBLE = "";

    public static String curDeviceModel = DEVICE_MODEL_UNKNOWN;


    public static  void init(){
        if(!isImix()){
            return;
        }
        File file = new File(FileUtils.DEVICE_MODEL_PATH);

        if(!file.exists()){
            return;
        }

        try {
            InputStream in = new FileInputStream(file);
            byte buffer[] = new byte[1024];
            int len = in.read(buffer);
            curDeviceModel = new String(buffer,0,len);
            in.close();
            in = null;
            file = null;
        } catch (Exception e) {
            e.printStackTrace();
            TPLog.printError("读取版本信息时出现异常：");
            TPLog.printError(e);
        }
    }

    /**
     * 判断当前设备型号是否是IMIX
     * @return
     */
    public static boolean isImix(){
        if(Build.MODEL.equals("nex")){
            return true;
        }
        return false;
    }

    public static boolean is55InchSingleDevice(){
        if(DEVICE_MODEL_55_INCH_SINGLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is55InchDoubleDevice(){
        if(DEVICE_MODEL_55_INCH_DOUBLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is65InchSingleDevice(){
        if(DEVICE_MODEL_65_INCH_SINGLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is65InchDoubleDevice(){
        if(DEVICE_MODEL_65_INCH_DOUBLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is75InchSingleDevice(){
        if(DEVICE_MODEL_75_INCH_SINGLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is75InchDoubleDevice(){
        if(DEVICE_MODEL_75_INCH_DOUBLE.equals(curDeviceModel)){
            return true;
        }
        return false;
    }

    public static boolean is55InchDevice(){
        if(is55InchDoubleDevice()||is55InchSingleDevice()){
            return true;
        }
        return false;
    }

    public static boolean is65InchDevice(){
        if(is65InchDoubleDevice()||is65InchSingleDevice()){
            return true;
        }
        return false;
    }

    public static boolean is75InchDevice(){
        if(is75InchDoubleDevice()||is75InchSingleDevice()){
            return true;
        }
        return false;
    }

}
