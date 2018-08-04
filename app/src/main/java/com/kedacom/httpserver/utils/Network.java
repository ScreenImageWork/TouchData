package com.kedacom.httpserver.utils;


import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Network
{
    /*
    public static String getLocalIp(Context context)
    {
        WifiManager wifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);//获取WifiManager

        //检查wifi是否开启
        if (!wifiManager.isWifiEnabled())
        {
            return null;
        }

        WifiInfo wifiinfo = wifiManager.getConnectionInfo();

        String ip = intToIp(wifiinfo.getIpAddress());

        return ip;
    }*/

    /**
     * 获取ip地址
     * @return
     */
    public static String getLocalIp() {

        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue;// skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("Network", "SocketException");
            e.printStackTrace();
        }
        return hostIp;

    }

    private static String intToIp(int paramInt)
    {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "."
            + (0xFF & paramInt >> 16) + "." + (0xFF & paramInt >> 24);
    }
}
