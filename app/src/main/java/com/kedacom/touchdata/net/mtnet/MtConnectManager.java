package com.kedacom.touchdata.net.mtnet;

import android.os.Handler;
import android.os.Message;

import com.kedacom.service.BootService;
import com.kedacom.storagelibrary.unity.DataMeetingManger;
import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.net.mtnet.entity.ClearScreenNtf;
import com.kedacom.touchdata.net.mtnet.entity.CreateDcsConfRsp;
import com.kedacom.touchdata.net.mtnet.entity.DelSelectImgEntity;
import com.kedacom.touchdata.net.mtnet.entity.DownloadInfo;
import com.kedacom.touchdata.net.mtnet.entity.ElementOperFinalNtf;
import com.kedacom.touchdata.net.mtnet.entity.ImgUploadUrl;
import com.kedacom.touchdata.net.mtnet.entity.MtEntity;
import com.kedacom.touchdata.net.mtnet.entity.SelectImgCoordinateEntity;
import com.kedacom.touchdata.net.mtnet.entity.SynCoordinateMsg;
import com.kedacom.touchdata.net.mtnet.entity.TLScrollChangedNtf;
import com.kedacom.touchdata.net.mtnet.entity.TLZoomChangeNtf;
import com.kedacom.touchdata.net.mtnet.entity.UnDoOrReDoNtf;
import com.kedacom.touchdata.net.mtnet.utils.MtHandlerCommand;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Circle;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.Line;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.graph.Rectangle;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import org.apache.http.util.NetUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/3/20.
 */

public class MtConnectManager extends Handler implements MtHandlerCommand{

    private static final long RECONNECT_TIME_SPEED = 10*1000; //断开连接后重连时间间隔

    private static final long CONNECT_TIMEOUT = 10*1000; //连接超时时间

    private static MtConnectManager mInstance;

    private MtNetCallback mtCallBack;

    private MtNetReader mtNetReader;

    private MtNetSender mtNetSender;

    private BootService mService;

    private boolean isConnect = false;

    public boolean startCooperation = false;

    public boolean hasDisconnect = false; //是否有过断链，只有断链重连之后的DcsGetConfInfo_Ntf消息才生效其余的均不生效，防止多次DcsGetConfInfo_Ntf多次收到

    private MtConnectManager(){
        TPLog.printError("->MtConnectManager init...");
        mtNetReader = new MtNetReader();
        mtNetSender = new MtNetSender();
    }

    public synchronized static MtConnectManager getInstance(){
        if(mInstance == null){
            mInstance = new MtConnectManager();
        }
        return mInstance;
    }

    public void bindService(BootService mService){
        this.mService = mService;
    }

    public boolean isConnect(){
        return isConnect;
    }

    public MtNetSender getMtNetSender(){
        return mtNetSender;
    }

    public void connectMt(){
        TPLog.printError("开始连接终端。。。");
        MtNetUtils.achTerminalE164 = "";
        startConnectTimeoutTimer();
        mtNetSender.connectMt(MtNetUtils.MT_IP,MtNetUtils.MT_USERNAME,MtNetUtils.MT_PASSWORD,MtNetUtils.MT_PORT,mtNetReader);
    }

    public void disconnectMt(){
        TPLog.printError("开始断开终端连接。。。");
        mtNetSender.disConnectMt();
    }

    public void reconnectMt(){
        TPLog.printError("等待"+(RECONNECT_TIME_SPEED/1000)+"s 后开始重连终端。。。");
        if(hasMessages(MT_RECONNECT)){
            removeMessages(MT_RECONNECT);
        }
        sendEmptyMessageDelayed(MT_RECONNECT,RECONNECT_TIME_SPEED);
        if(mService!=null)
            DataMeetingManger.reFinderMt(mService);
    }

    public void stopReconnectMt(){
        TPLog.printError("停止终端重连功能。。。");
        if(hasMessages(MT_RECONNECT)){
            removeMessages(MT_RECONNECT);
        }
    }

    public void startConnectTimeoutTimer(){
        TPLog.printError("启动终端连接超时定时器。。。");
        if(hasMessages(MT_CONNECT_TIMEOUT)){
            removeMessages(MT_CONNECT_TIMEOUT);
        }
        sendEmptyMessageDelayed(MT_CONNECT_TIMEOUT,CONNECT_TIMEOUT);
    }

    public void cancelConnectTimeoutTimer(){
        TPLog.printError("取消终端连接超时定时器。。。");
        if(hasMessages(MT_CONNECT_TIMEOUT)){
            removeMessages(MT_CONNECT_TIMEOUT);
        }
    }

    public void setMtNetCallback( MtNetCallback callback){
        mtCallBack = callback;
    }

    public void handMsg(int msgWhat,Object obj){
        Message msg = obtainMessage();
        if(msg==null){
            msg = new Message();
        }
        msg.what = msgWhat;
        msg.obj = obj;
        sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {
        TPLog.printWarning("NewTouchData   handleMessage --- > begin...");
//         if(mtCallBack==null){
             if(msg.what == JOIN_DCONF_NTF){
                 if(!WhiteBoardUtils.isAPPShowing) {
                     MtEntity mt = msg.obj == null ? null : (MtEntity) msg.obj;
                     if (mt.getE164().equals(MtNetUtils.achTerminalE164)) {//被动加入会议，启动白板
                         NetUtil.isRemoteConf = true;
                         MtNetUtils.synConfData = true;//被动入会后，直接同步入会数据
                         startCooperation = true;
                         mService.startRemoteCooperation();
//                         mService.startCooperation();
                     }
                 }
             }
//             else if(msg.what == START_CONF_NTF){ //视频会议开始
//                 NetUtil.hasVideoConf = true;
//             }else if(msg.what == OVER_CONF_NTF){ //视频会议结束
//                 NetUtil.hasVideoConf = false;
//             }else if(msg.what == QUIT_DCS_CONF_NTF){
//                 startCooperation = false;
//                 NetUtil.isRemoteConf = false;
//             }else if(DCS_RELEASE_CONF == msg.what){
//                 startCooperation = false;
//                 NetUtil.isRemoteConf = false;
//             }
//             return;
//         }
        switch(msg.what){
            case CONNECT_MT://连接终端反馈结果
                TPLog.printError("接收到终端连接返回结果。。。");
                cancelConnectTimeoutTimer();
                stopReconnectMt();
                boolean success = msg.obj == null?false:(Boolean)msg.obj;
                TPLog.printError("终端连接："+success);
                isConnect = success;

                if(success) {
                    hasDisconnect = true;
                    mtNetSender.getApsLoginParamCfgReq();
                }else{
                    sendEmptyMessage(MT_CONNECT_TIMEOUT);
                }
//                mtCallBack.onMtConnect(success);
                break;
            case MT_DISCONNECT_NTF: //终端断开连接
                isConnect = false;
                if(mtCallBack!=null) {
                    mtCallBack.onMtDisconnect();
                }
                //准备重连终端
                reconnectMt();
                break;
            case MT_RECONNECT:  //重连终端
                connectMt();
                break;
            case MT_CONNECT_TIMEOUT:
                TPLog.printError("终端连接超时。。。");
                cancelConnectTimeoutTimer();
                reconnectMt();
                break;
            case CONF_INFO: //视频会议信息
                if(mtCallBack!=null)
                mtCallBack.onMtConfInfo();
                break;
            case CONF_DETAIL: //视频会议详细信息
                if(mtCallBack!=null)
                mtCallBack.onMtConfDatil();
                break;
            case CONF_MT_MEMBER: //当前所有的与会者
                if(!WhiteBoardUtils.isAPPShowing){
                    MtNetUtils.checkConfManager();
                }
                if(mtCallBack!=null)
                     mtCallBack.onMtConfMemberList();
                break;
            case CREATE_DCONF: //创建数据会议反馈结果
                CreateDcsConfRsp  rsp = msg.obj == null?null:(CreateDcsConfRsp)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtCreateWbConf(rsp);
                break;
            case JOIN_DCONF_NTF://加入白板会议通知
                MtEntity mt = msg.obj == null?null:(MtEntity)msg.obj;
                 if(mtCallBack!=null)
                     mtCallBack.onMtJoinWbConfNtf(mt);
                break;
            case CREATE_WB_RSP://请求同步创建白板消息反馈结果
                success = msg.obj==null?false:(Boolean)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynCreateWbRsp(success);
                break;
            case CREATE_WB_NTF://创建白板同步消息
                Page page = msg.obj==null?null:(Page)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtCreateWhiteBoardNtf(page);
                break;
            case SYN_PENGRAPH_RSP://请求同步铅笔图元响应消息
                success = msg.obj==null?false:(Boolean)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynPenGraphRsp(success);
                break;
            case SYN_PENGRAPH_NTF://同步图元消息
                Pen pen = msg.obj==null?null:(Pen)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynPenGraphNtf(pen);
                break;
            case START_CONF_NTF://视频会议开始
                if(mtCallBack!=null)
                mtCallBack.onMtStartConfNtf();
                else{
                    NetUtil.hasVideoConf = true;
                }
                break;
            case OVER_CONF_NTF://视频会议结束
                if(mtCallBack!=null)
                    mtCallBack.onMtOverConfNtf();
                else{
                    MtNetUtils.isConfManager = false;
                    NetUtil.hasVideoConf = false;
                    NetUtil.isRemoteConf = false;
                }
                break;
            case LEAVE_CONF_NTF://用户离开会议
                if(mtCallBack!=null)
                mtCallBack.onMtMemberLeaveConfNtf();
                break;
            case IS_ALREADY_IN_CONF://用户是否已经在数据会议中
                success = msg.obj==null?false:(Boolean)msg.obj;
                NetUtil.hasVideoConf = success;
                if(mtCallBack!=null) {
                    mtCallBack.onMtIsAlreadyInConf(success);
                }
                break;
            case GET_TERMINAL_E164://获取终端的E164号
                String e164 = msg.obj==null?"":(String)msg.obj;
                mtNetSender.reqMtConfState();//连接终端成功后
//                mtCallBack.onMtTerminalE164Rsp(e164);
                break;
            case GET_ALL_WB://获取所有的白板
                ArrayList<Page> pages =  msg.obj==null?null:(ArrayList)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtGetAllWbRsp(pages);
                break;
            case SWITCH_TAB_PAGE:  //白板翻页
                String tabId = msg.obj == null?"":(String)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSwitchTabPage(tabId);
                break;
            case AREA_ERASE:
                AreaErase ae = msg.obj == null?null:(AreaErase)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynAreaEraseGraphNtf(ae);
                break;
            case DEL_WB_PAGE:
                tabId = msg.obj == null?"":(String)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtDelWbPageNtf(tabId);
                break;
            case CLEAR_SCREN:
                ClearScreenNtf csn = msg.obj == null?null:(ClearScreenNtf)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtClearScreenNtf(csn);
                break;
            case SYN_UNDO:
                UnDoOrReDoNtf undo = msg.obj == null?null:(UnDoOrReDoNtf)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtUndoNtf(undo);
                break;
            case SYN_REDO:
                UnDoOrReDoNtf redo = msg.obj == null?null:(UnDoOrReDoNtf)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtRedoNtf(redo);
                break;
            case JOIN_CONF_SYN_END:
                ElementOperFinalNtf eofn = msg.obj == null?null:(ElementOperFinalNtf)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtJoinConfSynEnd(eofn);
                break;
            case ADD_OPERATOR_NTF:
                ArrayList<String> addOperUserList = msg.obj == null?null:(ArrayList<String>)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtAddOperator(addOperUserList);
                break;
            case QUIT_DCS_CONF_NTF:
                String quitDcsConfUserE164 = msg.obj==null?null:(String)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtQuitDcsConfNtf(quitDcsConfUserE164);
                break;
            case QUIT_DCS_CONF_RSP:
                success = msg.obj==null?false:(Boolean)msg.obj;
                if(mtCallBack!=null)
                     mtCallBack.onMtQuitDcsConfRsp(success);
                else
                    NetUtil.isRemoteConf = false;
                break;
            case OPER_IMG_INFO_NTF:
                ImageGraph imageGraph = msg.obj==null?null:(ImageGraph)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtOperImgInfoNtf(imageGraph);
                break;
            case DOWNLOAD_IMAGE_NTF:
                DownloadInfo downloadInfo = msg.obj==null?null:(DownloadInfo)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtDownloadImageNtf(downloadInfo);
                break;
            case DOWNLOAD_FILE_RSP:
                downloadInfo = msg.obj==null?null:(DownloadInfo)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtDownloadImageRsp(downloadInfo);
                break;
            case SYN_LINE_GRAPH:
                Line line = msg.obj==null?null:(Line)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynLineNtf(line);
                break;
            case SYN_CIRCLE_GRAPH:
                Circle circle = msg.obj==null?null:(Circle)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynCircleNtf(circle);
                break;
            case SYN_RECTANGLE_GRAPH:
                Rectangle rectangle = msg.obj==null?null:(Rectangle)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynRectangleNtf(rectangle);
                break;
            case SYN_ERASE:
                Erase erase = msg.obj==null?null:(Erase)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtSynEraseNtf(erase);
                break;
            case UPLOAD_IMG_URL_RSP:
                ImgUploadUrl mImgUploadUrl = msg.obj==null?null:(ImgUploadUrl)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtUploadUrlRsp(mImgUploadUrl);
                break;
            case GET_CUR_DISPLAY_WB_RSP:
                tabId = msg.obj==null?null:(String)msg.obj;
                if(mtCallBack!=null)
                mtCallBack.onMtDisplayCurWbRsp(tabId);
                break;
            case DCS_RELEASE_CONF:
                String confE164 = msg.obj==null?null:(String)msg.obj;
                if(!WhiteBoardUtils.isAPPShowing){
                    NetUtil.isRemoteConf = false;
                }
                if(mtCallBack!=null)
                mtCallBack.onMtReleaseDcsConf(confE164);
                break;
            case DCS_CONF_INFO:
                if(!hasDisconnect){
                    return;
                }
                MtNetUtils.synConfData = true;
                hasDisconnect = false;
                if(!WhiteBoardUtils.isAPPShowing){
                    if(NetUtil.isRemoteConf) {
                        startCooperation = true;
                        mService.startRemoteCooperation();
                    }
                }
                if(mtCallBack!=null)
                    mtCallBack.onMtDcsConfInfoNtf();
                break;
            case DCS_DEL_OPERATOR:
                if(mtCallBack!=null)
                    mtCallBack.onMtDelOperatorNtf();
                break;
            case CHAIR_TOKEN_GET_NTF:
                boolean chairToken = msg.obj == null?false:(Boolean)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtChairTokenGetNtf(chairToken);
                break;
            case DCS_USER_APPLY_OPER_NTF:
                MtEntity me = msg.obj == null?null:(MtEntity)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtReqOperNtf(me);
                break;
            case DCS_REJECT_OPER_NTF:
                me = msg.obj == null?null:(MtEntity)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtRejectOperNtf(me);
                break;
            case DCS_SYN_COORDINATE_MSG_NTF:
                SynCoordinateMsg scm = msg.obj == null?null:(SynCoordinateMsg)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtSynCoordinateMsgNtf(scm);
                break;
            case DCS_SYN_TL_SCROLL_NTF:
                TLScrollChangedNtf tlScn = msg.obj == null?null:(TLScrollChangedNtf)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtSynTLScrollNtf(tlScn);
                break;
            case DCS_SYN_TL_ZOOM_NTF:
                TLZoomChangeNtf tlzcn = msg.obj == null?null:(TLZoomChangeNtf)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtSynTLZoomNtf(tlzcn);
                break;
            case DCS_GET_USER_LIST:
                List<MtEntity> mtList = msg.obj == null?null:(ArrayList<MtEntity>)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtDCSGetUserList(mtList);
                break;
            case DCS_DEL_ALL_WB:
                boolean bSuccess =  msg.obj == null?false:(Boolean)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtDelAllWhiteBoard(bSuccess);
                break;
            case DCS_CONF_INFO_UPDATE:
                if(mtCallBack!=null)
                    mtCallBack.onMtDcsConfInfoUpdateNtf();
                break;
            case APPLY_CHAIR_NTF:
                ApplyChairNtf acn = msg.obj == null?null:(ApplyChairNtf)msg.obj;
                if(!WhiteBoardUtils.isAPPShowing){
                    if(mtNetSender!=null)
                        mtNetSender.confChairSpecNewChair(acn);
                    break;
                }
                if(mtCallBack!=null)
                    mtCallBack.onMtApplyChair(acn);
                break;
            case APPLY_OPER_FAILED:
                int errorCode = msg.obj == null?null:(Integer)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtApplyOperFailed(errorCode);
                break;
            case IMAGE_COORDINATE_CHANGED:
                SelectImgCoordinateEntity selectImgCoordinateEntity = msg.obj == null?null:(SelectImgCoordinateEntity)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtSelectImgCoordinateChanged(selectImgCoordinateEntity);
                break;
            case DEL_SELECT_IMG:
                DelSelectImgEntity delSelectImgEntity = msg.obj == null?null:(DelSelectImgEntity)msg.obj;
                if(mtCallBack!=null)
                    mtCallBack.onMtDelSelectImg(delSelectImgEntity);
                break;
        }

        TPLog.printWarning("NewTouchData   handleMessage --- > end...");
    }
}
