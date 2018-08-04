package com.kedacom.service;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.kedacom.storagelibrary.model.MailboxInfo;
import com.kedacom.storagelibrary.model.WifiInfo;
import com.kedacom.touchdata.mail.MailUtil;
import com.kedacom.touchdata.net.ConnectManager;
import com.kedacom.touchdata.net.mtnet.MtConnectManager;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.StorageMangerJarUtils;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;


import junit.runner.Version;

/**
 * Created by zhanglei on 2016/12/22.
 */
public class BootService extends Service implements StorageMangerJarUtils.ISettingParamsChangeListener{

    private ConnectManager mConnectManager;

    public static BootService instance;

    private boolean selfIsRunning = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        TPLog.printKeyStatus("BootService -> onCreate");
//        if(!WhiteBoardUtils.isAPPShowing){
//            Utils.notificationTouchDataDisplayState(this,false);
//        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        TPLog.printKeyStatus("BootService -> onStart");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TPLog.printKeyStatus("BootService -> onStartCommand");

        if(selfIsRunning){
            return super.onStartCommand(intent, flags, startId);
        }

        TPLog.printKeyStatus("BootService -> onStartCommand  init...");

        selfIsRunning = true;

        instance = this;

        initLocalConnect();

        initRemoteConnect();

        initStorageMangerJarUtils();

        return super.onStartCommand(intent, flags, startId);
    }


    private void initLocalConnect(){
        if(VersionUtils.is55InchDevice()) {//55寸版本无协作功能
            return;
        }
        TPLog.printError("-->Local ConnectManager init...");
        mConnectManager = ConnectManager.getInstance();
        mConnectManager.bindService(this);
//SP2不再进行主动连接
//        if(VersionUtils.isImix()) {
//            mConnectManager.start(NetUtil.IP, NetUtil.PORT);
//        }
    }

    private void initRemoteConnect(){
        if(!VersionUtils.isImix()){//临时注释掉
            return;
        }
        TPLog.printError("-->Remote ConnectManager init...");
        MtConnectManager.getInstance().bindService(this);
//        MtConnectManager.getInstance().connectMt();
    }


    public void initStorageMangerJarUtils(){
        StorageMangerJarUtils.registerCallBack(this);
        StorageMangerJarUtils.init();
    }

    //启动协作
    public void startCooperation(){
        if(!WhiteBoardUtils.isAPPShowing){
            WhiteBoardUtils.isAPPShowing = true;
            Utils.doStartApplicationWithPackageName(this,"com.kedacom.touchdata://touchdata");
            sendAwakenBroadcast();
        }
    }

    public void startRemoteCooperation(){
        if(!WhiteBoardUtils.isAPPShowing){
            Utils.setRecScreenShareEnable(this,false,true);
            sendAwakenBroadcast();
        }
    }


    @Override
    public void onDestroy() {
        TPLog.printKeyStatus("BootService -> onDestroy");
        super.onDestroy();
    }


    /****************************************系统设置参数改变后回调*****************************************************/

    @Override
    public void onMailParamsChanged(MailboxInfo mailboxInfo) {
        TPLog.printKeyStatus("Mail信息改变，邮箱服务器："+mailboxInfo.mailboxServer+",用户名："+mailboxInfo.user+",密码:"+mailboxInfo.password);
        MailUtil.configMail(mailboxInfo.mailboxServer,mailboxInfo.user,mailboxInfo.password);
    }

    @Override
    public void onDataMeetingServerIpChanged(String ip) {
//SP2不再进行主动连接 20180524
//        if(VersionUtils.is55InchDevice()) {//55寸版本无协作功能
//            return;
//        }
//        TPLog.printKeyStatus("收到数据会议IP改变消息，当前设置IP为："+ip);
//        if(ip==null||ip.trim().isEmpty()){
//            return;
//        }
//
//        NetUtil.IP = ip;
//
//        //1、如果协作没有打开，就直接跳过
//        if(!StorageMangerJarUtils.PARAMS_COLLABORATION_ONOFF){
//            return;
//        }
//        //2、如果已经连接就需要断开当前链接
//        if(mConnectManager.isStart()){
//            mConnectManager.setCallback(null);
//            mConnectManager.stop();
//            mConnectManager.stopReconnect();
//        }
//        //3、建立新链接
//        //mConnectManager.setCallback(this);
//        mConnectManager.start(ip,NetUtil.PORT);
    }

    @Override
    public void onWifiParamsChanged(WifiInfo wifiInfo) {
        TPLog.printKeyStatus("Wifi热点信息改变，SSID:"+wifiInfo.wifiName+",密码:"+wifiInfo.wifiPwd);
    }

    @Override
    public void onCollaborationOnOffChanged(boolean onOrOff) {//
//SP2不再进行主动连接 20180524
//        if(VersionUtils.is55InchDevice()) {//55寸版本无协作功能
//            return;
//        }
//        TPLog.printKeyStatus("是否开启协作状态改变为:"+(onOrOff?"开启自动协作":"关闭自动协作"));
//        if(onOrOff) { //协作打开
//            if(!mConnectManager.isStart()) {
////                mConnectManager.setCallback(this);
//                mConnectManager.start(NetUtil.IP,NetUtil.PORT);
//            }
//        }else{ //协作关闭
//            if(mConnectManager.isStart()) {
//                mConnectManager.setCallback(null);
//                mConnectManager.stop();
//                mConnectManager.stopReconnect();
//            }
//        }
    }

    @Override
    public void onMtIpChanged(final String mtIp) {
        if(!VersionUtils.isImix()){
            return;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.e("123", "run: "+mtIp+"," );
                if( MtNetUtils.MT_IP .equals(mtIp)&&MtConnectManager.getInstance().isConnect()){//如果是同一个IP就不用做任何操作了
                    return;
                }
                MtNetUtils.MT_IP = mtIp==null?"0.0.0.0":mtIp;
                if(MtConnectManager.getInstance().isConnect()){
                    MtConnectManager.getInstance().disconnectMt();
                }
                MtConnectManager.getInstance().connectMt();
            }
        });
    }

    @Override
    public void onSettingParamsChangedError(StorageMangerJarUtils.ErrorCode errCode) {//
        TPLog.printError("获取配置参数时出现异常:"+errCode);
    }

    private void sendAwakenBroadcast(){
        TPLog.printError("发送唤醒广播。。。");
        Intent intent = new Intent();
        intent.setAction("com.kedacom.netmanage.wakeup");
        sendBroadcast(intent);
    }

    Handler handler  = new Handler(){
        @Override
        public void handleMessage(Message msg) {
//            if(msg.what == 100){
//                initRemoteConnect();
//            }
        }
    };
}
