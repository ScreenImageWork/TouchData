package com.kedacom.utils;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import com.kedacom.storagelibrary.model.MailboxInfo;
import com.kedacom.storagelibrary.model.WifiInfo;
import com.kedacom.storagelibrary.unity.AppUtil;
import com.kedacom.storagelibrary.unity.DataMeetingManger;
import com.kedacom.storagelibrary.unity.ICfgCallback;
import com.kedacom.storagelibrary.unity.IMailboxCallback;
import com.kedacom.storagelibrary.unity.IServerIpCallback;
import com.kedacom.storagelibrary.unity.IWhiteBoardCallback;
import com.kedacom.storagelibrary.unity.IWifiCallback;
import com.kedacom.touchdata.mail.MailUtil;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.tplog.TPLog;

import java.io.File;

/**
 * Created by zhanglei on 2017/3/31.
 */
public class StorageMangerJarUtils {
    /**
     * 错误码，设置参数回调时有可能出现错误，这里通过错误码来区分是什么类型的错误
     * 这个暂时只是定义了下，出现错误暂时未做处理
     */
    public enum ErrorCode {
        MAIL_ERROR,DATAMEETING_ERROR,WIFI_ERROR,COLLABORATION_ERROR,MT_IP_ERROR
    }

    private static final String APP_FILE_RETRIEVER_ACTION = "com.kedacom.fileexplorer.FileRetriever";

    public final static String PACKAGE_SYSTEM_SETTINGS = "com.kedacom.systemsetting";

    public final static String APP_SYSTEM_SETTINGS = "com.kedacom.systemsetting.activities.SystemSettingActivity";

    public static final String ACTION_SWTICH_FILE_ACTIVITY = "com.kedacom.fileexplorer.FileRetriever";//文件管理

    public static final String ACTION_SWTICH_FILE_ACTIVITY2 = "com.kedacom.fileshare.FileRetriever";//文件共享


    private static ISettingParamsChangeListener mParamsChangeListener;


//    public static String PARAMS_MAIL_SERVER = "";
//
//    public static String PARAMS_MAIL_USER = "";
//
//    public static String PARAMS_MAIL_PASSWORD = "";

    //public static String PARAMS_DATAMEETING_SERVER_IP = "";

    public static boolean PARAMS_COLLABORATION_ONOFF = false;

    public static String PARAMS_WIFI_SSID = "";

    public static String PARAMS_WIFI_PASSWORD = "";

 //   public static int PARAMS_DATAMEETING_SERVER_PORT = 5000;

    public static void toNetWorkSetting(Context context) throws ActivityNotFoundException{
        AppUtil.openSystemSettings(context,AppUtil.APP_NETWORK_VALUE);
    }

    public static void toPadNetWorkSetting(Context context){
        AppUtil.openConnectWifi(context);
    }

    public static void toFileManager(Context context,String path){
        AppUtil.openFileBroser(context,path);
    }

    public static boolean toSystemFileManager(Context context,String path){
        try {
            File file = new File(path);
            if(null==file || !file.exists()){
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(file), "file/*");

            context.startActivity(intent);
            //startActivity(Intent.createChooser(intent,"选择浏览工具"));
        } catch (ActivityNotFoundException e) {
            TPLog.printKeyStatus("打开系统文件管理器失败。。。。。");
            return false;
        }
        return true;
    }


    public static void toEmailSetting(Context context){
        AppUtil.openSystemSettings(context,AppUtil.APP_ADVANCED_VALUE);
    }

    public static boolean isInstallKDFileBroser(Context context){
        PackageManager packageManager = context.getPackageManager();
        Intent it = null;
        try {
             it = packageManager.getLaunchIntentForPackage("com.kedacom.fileexplorer");
        }catch(Exception e){
            TPLog.printKeyStatus("检测到科达文件管理未安装");
            return false;
        }
        if(it==null){
            TPLog.printKeyStatus("检测到科达文件管理未安装");
            return false;
        }

        TPLog.printKeyStatus("检测到科达文件管理已安装");
        return true;
    }


    public static void init(){
        DataMeetingManger.getInstance().getMailboxConfig(new IMailboxCallback() {
            @Override
            public void onFinish(MailboxInfo mailboxInfo) {
                if(mailboxInfo==null){
                    TPLog.printError("获取邮箱信息失败 ,mailboxInfo=null");
                    return;
                }
                TPLog.printError("Mail信息改变，邮箱服务器："+mailboxInfo.mailboxServer+",用户名："+mailboxInfo.user+",密码:"+mailboxInfo.password);
                //TPLog.printKeyStatus("success to get mail info,mailboxInfo = "+mailboxInfo.toString());
                if(mParamsChangeListener!=null){
                    mParamsChangeListener.onMailParamsChanged(mailboxInfo);
                }
            }

            @Override
            public void onError(Throwable throwable) {
                TPLog.printError("获取邮件服务信息时出现异常:");
                TPLog.printError((Exception) throwable);
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onSettingParamsChangedError(ErrorCode.MAIL_ERROR);
            }
        });

        DataMeetingManger.getInstance().getServerIp(new IServerIpCallback() {
            @Override
            public void onFinish(String s) {
                if(s==null||s.trim().isEmpty()){
                    TPLog.printError("获取数据协作IP失败 ,serverIp=null");
                    return;
                }

                String ips[] = s.split("\\.");

                boolean boo = true;

                for(String ip:ips){
                    int i = Integer.valueOf(ip);
                    if(i!=0){
                        boo = false;
                    }
                }

                if(boo){//0.0.0.0
                    s = "127.0.0.1";
                }

                TPLog.printError("收到数据会议IP改变消息，当前设置IP为："+s);
                if(NetUtil.IP.equals(s)){  //如果与当前IP一致就不用再次设置了
                    TPLog.printError("与当前IP一致不需要进行保存");
                    return;
                }
                //TPLog.printKeyStatus("success to get datameeting server ip ,serverIp = "+s);
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onDataMeetingServerIpChanged(s);
            }

            @Override
            public void onError(Throwable throwable) {
                TPLog.printError("获取数据会议服务器IP时出现异常:");
                TPLog.printError((Exception)throwable);
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onSettingParamsChangedError(ErrorCode.DATAMEETING_ERROR);
            }
        });

        DataMeetingManger.getInstance().getWifiInfo(new IWifiCallback() {
            @Override
            public void onFinish(WifiInfo wifiInfo) {
                if(wifiInfo==null){
                    TPLog.printError("获取WIFI信息失败 ,wifiInfo=null");
                    return;
                }
                TPLog.printError("Wifi热点信息改变，SSID:"+wifiInfo.wifiName+",密码:"+wifiInfo.wifiPwd);
                //TPLog.printKeyStatus("success to get wifi info ,wifiInfo="+wifiInfo.toString());

                PARAMS_WIFI_SSID = wifiInfo.wifiName;
                PARAMS_WIFI_PASSWORD = wifiInfo.wifiPwd;

                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onWifiParamsChanged(wifiInfo);
            }

            @Override
            public void onError(Throwable throwable) {
                TPLog.printError("获取wifi信息时出现异常:");
                TPLog.printError((Exception)throwable);
                if(mParamsChangeListener!=null)
                mParamsChangeListener.onSettingParamsChangedError(ErrorCode.WIFI_ERROR);
            }
        });

        DataMeetingManger.getInstance().enableWhiteBoard(new IWhiteBoardCallback() {
            @Override
            public void onFinish(boolean b) {
                TPLog.printError("是否开启协作状态改变为:"+(b?"开启自动协作":"关闭自动协作"));
                if( StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF == b){
                    TPLog.printError("与当前PARAMS_COLLABORATION_ONOFF一致不需要进行保存设置");
                    return;
                }
                StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF = b;
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onCollaborationOnOffChanged(b);
            }
            @Override
            public void onError(Throwable throwable) {
                TPLog.printError("获取白板协作开关状态时出现异常:");
                TPLog.printError((Exception)throwable);
                if(mParamsChangeListener!=null)
                mParamsChangeListener.onSettingParamsChangedError(ErrorCode.COLLABORATION_ERROR);
            }
        });

        DataMeetingManger.getInstance().getMtIp(new ICfgCallback<String>() {
            @Override
            public void onFinish(String s) {
                TPLog.printError("Mt Ip Changed Ip:" + s);
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onMtIpChanged(s);
            }

            @Override
            public void onError(Throwable throwable) {
                TPLog.printError("获取终端IP时出现异常:");
                TPLog.printError((Exception)throwable);
                if(mParamsChangeListener!=null)
                    mParamsChangeListener.onSettingParamsChangedError(ErrorCode.MT_IP_ERROR);
            }
        });
    }

    public static String getImixDevicesName(){
        String deviceName = DataMeetingManger.getInstance().getDeviceName();
        return deviceName;
    }

    public static void registerCallBack(ISettingParamsChangeListener iSettingParamsChangeListener){
        mParamsChangeListener = iSettingParamsChangeListener;
    }

    public interface ISettingParamsChangeListener{
        void onMailParamsChanged(MailboxInfo mailboxInfo);
        void onDataMeetingServerIpChanged(String ip);
        void onWifiParamsChanged(WifiInfo wifiInfo);
        void onCollaborationOnOffChanged(boolean onOrOff);
        void onSettingParamsChangedError(ErrorCode errCode);
        void onMtIpChanged(String mtIp);
    }




}
