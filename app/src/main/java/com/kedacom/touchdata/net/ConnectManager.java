package com.kedacom.touchdata.net;

import android.os.Message;

import com.kedacom.service.BootService;
import com.kedacom.osp.callback.OspCallback;
import com.kedacom.osp.entity.OspMsgEntity;
import com.kedacom.touchdata.net.callback.NetCallback;
import com.kedacom.touchdata.net.entity.CoordinateChangedMsg;
import com.kedacom.touchdata.net.entity.DeleteGraphMsg;
import com.kedacom.touchdata.net.entity.GestureScaleEntity;
import com.kedacom.touchdata.net.entity.GraphCoordinateChangedMsg;
import com.kedacom.touchdata.net.utils.NetError;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanglei on 2016/12/16.
 */
public class ConnectManager extends NetHandler implements OspCallback {

    private final static long RECONNECT_TIME = 10*1000;

    private static ConnectManager mInstance;

    private Reader mReader;

    private Sender mSender;

    private Connecter mConnecter;

    private NetCallback callback;

    private boolean isStart;

    private boolean reconnectEnable = true;

    private BootService service;

    private ConnectManager(){
        mConnecter = new Connecter();
        mReader = new Reader(this);
        mSender = new Sender(mConnecter);
        SynFileManager.getInstance().setNetHandler(this);
        SynFileManager.getInstance().setSender(mSender);
        SendHelper.getInstance().init(mSender);
    }

    public void bindService(BootService service){
        this.service = service;
    }

    public static synchronized ConnectManager getInstance(){
        if(mInstance==null){
            mInstance = new ConnectManager();
        }
        return mInstance;
    }

    public void start(String ip,int port){
        if(isStart)return;
        isStart = true;
        mConnecter.connect(ip,port,this);
    }

    public void stop(){
        mConnecter.disConnect();
        isStart = false;
    }

    public boolean isStart(){
        return isStart;
    }


    public Reader getReader() {
        return mReader;
    }

    public Sender getSender() {
        return mSender;
    }

    public Connecter getConnecter() {
        return mConnecter;
    }

    public void setCallback(NetCallback callback){
        this.callback = callback;
    }

    @Override
    public void ospConnected() {
        TPLog.printError("数据会议服务器已连接");
        SendHelper.getInstance().loginServer();
        sendMessage(OSP_CONNECTED,null);
        stopReconnect();
        SendHelper.getInstance().sendEnable(true);
    }

    @Override
    public void ospDisconnect() {
        TPLog.printError("数据会议服务器已断开链接");
        SendHelper.getInstance().sendEnable(false);
        sendMessage(OSP_DISCONNECT,null);
        startReconnect();
    }

    @Override
    public void ospReceiveMsg(OspMsgEntity msgEntity) {
        mReader.readMsd(msgEntity);
    }

    @Override
    public void ospException(Exception e) {
        TPLog.printError("OSP连接出现异常：");
        TPLog.printError(e+":"+e.getMessage());

        SendHelper.getInstance().sendEnable(false);
        sendMessage(OSP_EXCEPTION,e);
        startReconnect();
    }

    public void stopReconnect(){
        TPLog.printError("关闭重连功能");
        reconnectEnable = false;
        removeMsg(RECONNECT);
    }

    public void startReconnect(){
        TPLog.printError("启动重连功能");
        reconnectEnable = true;
        TPLog.printError("当前是否存在重连消息："+hasMessages(RECONNECT));
        if(!hasMessages(RECONNECT)) {
            TPLog.printError((RECONNECT_TIME/1000)+"秒后发送重连消息");
            mInstance.sendEmptyMessageDelayed(RECONNECT,RECONNECT_TIME);
        }
    }

    private void removeMsg(int msg){
        TPLog.printError("移除Handler消息");
        if(hasMessages(msg)){
            removeMessages(msg);
        }
        if(hasMessages(msg)){
            removeMsg(msg);
        }
    }

    @Override
    public void handleMessage(Message msg) {

        if(msg.what == SERVER_CONNECT_NUM_NTF){
            int connectNum = (Integer) msg.obj;
            NetUtil.curServerConnectNum = connectNum;
        }

        if(msg.what == CONF_MEMBER_NUM_UPDATE){
            int memberNum = (Integer) msg.obj;
            NetUtil.curJoinLocalConfMemberNum = memberNum;
        }

        if(msg.what != MEETING_NAME&&callback==null&&RECONNECT!=msg.what&&LOGIN!=msg.what){
            TPLog.printKeyStatus("msg.what != MEETING_NAME&&callback==null");
            return;
        }

        switch(msg.what){
            case OSP_CONNECTED:
                callback.onServerConnected();
                break;
            case OSP_DISCONNECT:
                callback.onServerDisconnected();
                break;
            case LOGIN:
                /*有一种可能，当其他人发起协作时当前客户端不在线，无法成功启动，因此当该客户端重新登录成功后
                 * 检测到当前有会议而且客户端没有显示时，直接打开客户端  （这里的回调函数为空就可以判断为当前客户端没有启动）
                */
                //stopReconnect();
                TPLog.printKeyStatus("->LOGIN");
                String meetingName = "";
                if(msg.obj!=null){
                    meetingName = (String)msg.obj;
                }
                TPLog.printKeyStatus("->meetingName:"+meetingName);

                NetUtil.curMeetingName = meetingName;

                //如果当前有会议而且是PAD那么就直接入会
                if(callback==null&&meetingName!=null&&!meetingName.equals("")&&!meetingName.equals("null")&& !VersionUtils.isImix()){
                    service.startCooperation();
                }else if(callback!=null){
//                    callback.onRecMeetingName(meetingName);
                    callback.onLoginServerSuccess();
                }

                break;
            case OSP_EXCEPTION:
                callback.onServerConnectException((Exception) msg.obj);
                break;
            case MEETING_NAME://获取到的会议名称
               //这里不再进行加入会议操作，只进行APP的启动操作
                if(msg.obj==null){
                    meetingName = "";
                 }else{
                    meetingName = (String)msg.obj;
                }

                NetUtil.curMeetingName = meetingName;

                if(callback!=null){
                    callback.onRecMeetingName(meetingName);
                }
                if(service!=null&&!meetingName.trim().isEmpty()&&!"null".equals(meetingName))
                service.startCooperation();
                break;
            case CREATE_MEETING:
                if(msg.obj == null){
                    return;
                }
                long createRstCode = (Long)msg.obj;
                callback.onRecCreateMeeting(createRstCode);
                break;
            case JOIN_MEETING://加入/创建会议 响应结果
                if(msg.obj == null){
                    return;
                }
                long rstCode = (Long)msg.obj;
                callback.onRecJoinMeeting(rstCode);
                break;
            case BUF_SIZE://服务器返回的当前的流量大小   流量大小暂时没有使用  感觉没有什么意义  主要是为了响应服务请求的翻页操作
                int bufSize = 0;
                if(msg.obj!=null){
                    bufSize = (Integer)msg.obj;
                }
                callback.onRecServerCurBufferSzie(bufSize);
                break;
            case SC_SYNCHRONOUS:  //服务器请求 同步数据
                callback.onRecServerReqSyn((Long)msg.obj);
                break;
            case SYNCHRONOUSE_FAILED: //同步数据失败
                callback.onRecSynFailed();
                break;
            case SYNCHRONOUSE:
                if(msg.obj==null){
                    return;
                }
                Map<String ,Object> synData = (Map<String,Object>)msg.obj;
                long curPageId = (Long)synData.get("curPageId");
                List<Page> pageList = (List<Page>)synData.get("PageList");
                callback.onRecSynData(curPageId,pageList);
                break;
            case GRAPH_UPDATE:
                callback.onRecGraphData((Graph)msg.obj);
                break;
            case ADD_IMAGE://接收服务器发送过来的图片信息
                if(msg.obj==null){
                    return;
                }
                callback.onRecImageData((SubPage)msg.obj);
                break;
            case SCALE_CHANGED: //接收到服务器发送过来的缩放数据
                if(msg.obj==null){
                    return;
                }
                callback.onRecZoomData((Float)msg.obj);
                break;
            case SCALE_CHANGED_FROM_GESTURE: //收到其他客户端发送过来的手势缩放消息
                if(msg.obj==null){
                    return;
                }
                GestureScaleEntity gse = (GestureScaleEntity)msg.obj;
                callback.onRecGestureZoomData(gse.getScaleFactor(),gse.getFocusX(),gse.getFocusY());
                gse = null;
                break;
            case SCROLL_CHANGED: //滚动条
                if(msg.obj==null){
                    return;
                }
                callback.onRecScrollData((android.graphics.Point) msg.obj);
                break;
            case CLEAR_SCREEN: //清屏
                callback.onRecClearScreen();
                break;
            case CREATE_TAB: //创建白板
                if(msg.obj==null){
                    return;
                }
                callback.onRecCreateWbData((Page)msg.obj);
                break;
            case DEL_TAB://删除白板
                if(msg.obj==null){
                    return;
                }
                String delData = (String)msg.obj;
                String data[] = delData.split("_");
                long delWbId = Long.parseLong(data[0]);
                long nextWbId = Long.parseLong(data[1]);
                callback.onRecDelWbData(delWbId,nextWbId);
                break;
            case DEL_ALL_TAB:
                callback.onRecDelAllWbData((Long)msg.obj);
                break;
            case CHANGE_PAGE:  //翻页
                if(msg.obj==null){
                    return;
                }
                Map<String,Long> changeData = (Map<String,Long>)msg.obj;
                callback.onRecChangePage(changeData.get("tabId"),changeData.get("pageIndex"));
                break;
            case SR_CHANGE_PAGE:  //服务器向客户端请求翻页
                if(msg.obj==null){
                    return;
                }
                changeData = (Map<String,Long>)msg.obj;
                callback.onRecServerReqChangePage(changeData.get("tabId"),changeData.get("pageIndex"));
                break;
            case IMAGE_DOWNLOAD://图片已经下载完毕
                if(msg.obj==null){
                    return;
                }
                callback.onRecImageDownloaded((Long)msg.obj);
                break;
            case REDO: //恢复
                changeData = (Map<String,Long>)msg.obj;
                callback.onRecRedoData(changeData.get("tabId"),changeData.get("pageIndex"));
                break;
            case UNDO: //撤销
                changeData = (Map<String,Long>)msg.obj;
                callback.onRecUndoData(changeData.get("tabId"),changeData.get("pageIndex"));
                break;
            case ROTATE_LEFT:
                callback.onRecLeftRotate();
                break;
            case ROTATE_RIGHT:
                callback.onRecRightRotate();
                break;
            case INSERT_IMG://2017.07.04
                callback.onRecInsertImg(((ImageGraph) msg.obj));
                break;
            case COORDINATE_CHANGED:
                callback.onRecCoordinateChanged(((CoordinateChangedMsg) msg.obj));
                break;
            case GRAPH_COORDINATE_CHANGED:
                callback.onRecSelectGrpahCoordinateChanged((ArrayList<GraphCoordinateChangedMsg>) msg.obj);
                break;
            case DELETE_GRAPH:
                callback.onRecDeleteGraph((DeleteGraphMsg) msg.obj);
                break;
            case ROTATE_CHANGED:
                if(msg.obj==null){
                    return;
                }
                changeData = (Map<String,Long>)msg.obj;
                callback.onRecRotate((Long)changeData.get("angle"),changeData.get("isFinish"));
                break;
            case RECONNECT:
                TPLog.printError("重连开关："+reconnectEnable);
                if(reconnectEnable)
                mConnecter.reConnect();
                break;
            case CONF_MEMBER_NUM_UPDATE:
                int memberNum = (Integer) msg.obj;
                if(callback!=null){
                    callback.onRecConfMemberListUpdate(memberNum);
                }
                break;
            case SERVER_CONNECT_NUM_NTF:
                int connectNum = (Integer) msg.obj;
                if(callback!=null){
                    callback.onRecServerConnectNumNtf(connectNum);
                }
                break;
        }
    }


}
