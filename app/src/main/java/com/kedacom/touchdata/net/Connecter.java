package com.kedacom.touchdata.net;

import com.kedacom.osp.Osp;
import com.kedacom.osp.callback.OspCallback;
import com.kedacom.osp.entity.OspMsgEntity;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2016/12/16.
 * 与数据会议器的链接工作由该类负责，包含断链重连
 */
public class Connecter{

    private Osp mOsp;

    private String serverIp;

    private int serverPort;

    private OspCallback callback;

    public Connecter(){

    }

    public void connect(String serverIp,int port,OspCallback callback){
        TPLog.printKeyStatus("开始连接数据会议服务器");
        TPLog.printKeyStatus("连接信息：serverIp="+serverIp+",serverPort="+port);
        this.serverIp = serverIp;
        this.serverPort = port;
        this.callback = callback;
        mOsp = new Osp(serverIp, port, callback);
        mOsp.connect();
    }

    public boolean isConnect(){
        if(mOsp==null){
            return false;
        }else {
            return mOsp.isConnect();
        }
    }

    public void reConnect(){
        TPLog.printKeyStatus("开始重连数据会议服务器");

        if(mOsp!=null){
//            mOsp.removeCallback();
            mOsp.closeConnect();
            mOsp = null;
        }
        TPLog.printKeyStatus("重连信息：serverIp="+serverIp+",serverPort="+serverPort);
        TPLog.printKeyStatus("回调函数检测：callback="+callback);
        mOsp = new Osp(serverIp, serverPort, callback);
        mOsp.connect();
    }

    public void disConnect(){
        TPLog.printKeyStatus("断开与数据会议服务器连接");
        if(mOsp!=null&&mOsp.isConnect()){
            mOsp.removeCallback();
            mOsp.closeConnect();
            mOsp = null;
        }
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public OspCallback getCallback() {
        return callback;
    }

    public void setCallback(OspCallback callback) {
        this.callback = callback;
    }

    public Osp getOsp(){
        return mOsp;
    }
}
