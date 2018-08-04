package com.kedacom.utils;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.kedacom.tplog.TPLog;

public class NetworkUtil {

    public static final int NET_MOBILE = 666;
    public static final int NET_WIFI = 777;
    public static final int TYPE_ETHERNET = 888;
    public static final int NET_NONE = 444;


    /**
     * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
     <uses-permission android:name="android.permission.INTERNET" />
     */



    /**
     * 获取当前网络状态
     * @return  boolean true 当前有网络，false 当前没有网络连接
     */
    public static boolean getNetworkState(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        NetworkInfo netWorkInfo = info[i];
                        if (netWorkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                            return true;
                        }else if(netWorkInfo.getType() == ConnectivityManager.TYPE_ETHERNET ){
                            return true;
                        }else if(netWorkInfo.getType() == ConnectivityManager.TYPE_MOBILE){
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    /**
     * 判断wifi是否打开
     * @return
     */
    public static boolean isWifiConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mWiFiNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWiFiNetworkInfo != null) {
                return mWiFiNetworkInfo.isAvailable();
            }
        }
        return false;
    }


    /**
     * 判断手机网络有没有打开
     * @return
     */
    public static boolean isMobileConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mMobileNetworkInfo = mConnectivityManager
                    .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (mMobileNetworkInfo != null) {
                return mMobileNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 检测在wifi情况下GPRS有没有打开
     * @return
     */
    public static boolean issMobileOpen(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Class cmClass         = cm.getClass();
        Class[] argClasses     = null;
        Object[] argObject     = null;
        Boolean isOpen = false;
        try
        {
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);
            isOpen = (Boolean) method.invoke(cm, argObject);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return isOpen;

    }
}
