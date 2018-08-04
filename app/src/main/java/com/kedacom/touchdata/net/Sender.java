package com.kedacom.touchdata.net;

import com.kedacom.osp.Osp;
import com.kedacom.osp.OspUtils;
import com.kedacom.touchdata.net.utils.Command;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2016/12/16.
 */
public class Sender {

    private Connecter mConnecter;

    public Sender(Connecter connect){
        this.mConnecter = connect;
    }

    /**
     * 加入会议 会议消息体 = 会议名称长度 + 会议名称 + 密码长度 + 密码 + 0x0D
     * @param meetingName 会议名称
     * @param password 会议密码
     * @param isJoin 是否是加入会议 true 加入会议，false 创建会议
     */
    public void joinOrCreateMeeting(String meetingName,String password,boolean isJoin){
        if(meetingName==null||meetingName.equals("")) {
            meetingName = "";
        };

        char even = 0;

        if(isJoin) {
            even = NetUtil.EVEN_JOIN_CONFERENCE_REQ;
        }else{
            even =NetUtil.EVEN_CREATE_CONFERENCE_REQ;
        }

        byte byteMeetingName[] = meetingName.getBytes();
        if("".equals(meetingName)){
            byteMeetingName = new byte[0];
        }
        int meetingNameLength = byteMeetingName.length;

        int passwordLength = 0;

        byte bytePassword[] = null;

        if(password!=null&&!password.equals("")){
            //密码进行MD5加密
            //password = Md5Util.getMd5(password);

            bytePassword = password.getBytes();

            passwordLength = bytePassword.length;
            //将密码最后一位置为0
            bytePassword[passwordLength-1] = 0;
        }


        byte data[] = new byte[4+meetingNameLength+4+passwordLength+1];

        int index = 0;

        byte byteMeetingNameLength[] = OspUtils.uintToByte(meetingNameLength);

        byte bytePasswordLength[] = OspUtils.uintToByte(passwordLength);

        //会议名称长度
        for(int i=0;i<byteMeetingNameLength.length;i++){
            data[index++] = byteMeetingNameLength[i];
        }

        //会议名称
        for(int i=0;i<byteMeetingName.length;i++){
            data[index++] = byteMeetingName[i];
        }

        //密码长度
        for(int i=0;i<bytePasswordLength.length;i++){
            data[index++] = bytePasswordLength[i];
        }

        //密码
        if(passwordLength>0){
            for(int i=0;i<bytePassword.length;i++){
                data[index++] = bytePassword[i];
            }
        }

        //消息结束位
        data[index++] = 0x0D;

       NetUtil.displayArray(data);

        sendMsg(data, even);
    }

    public void sendQuitMeetingMsg(){
        sendMsg(new byte[0], NetUtil.EVEN_QUIT_CONFERENCE_REQ);
    }

    /**
     * 发送清屏命令给其他客户端
     */
    public void sendClearScreenMsg(){
        //该命令为固定格式
        //前四位是数据长度
        //0x11是清屏消息号
        //0x0D是消息结束位
        //中间四个0无意义
        byte msg[] = {0x00,0x00,0x00,0x06,0x11,0x00,0x00,0x00,0x00,0x0D};
        sendMsg(msg, NetUtil.EVEN_SYNCHRONOUS_REQ);
    }


    /**
     * 发送缩放同步命令
     */
    public void sendZoomMsg(long tabId,float zoom){
        byte zooms[] = OspUtils.uFloatToByte(zoom);
        byte zooms2[] = new byte[4];
        zooms2[3] = zooms[0];
        zooms2[2] = zooms[1];
        zooms2[1] = zooms[2];
        zooms2[0] = zooms[3];

        byte content[] = new byte[8];
        int index = 0;
        index += NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index += NetUtil.memcpy(content,zooms2,index);
        sendEntityPacket(content, Command.CC_ZOOM);
    }

    public void sendGestureZoomMsg(long tabId,float zoomFactor,int focusX,int focusY){
        byte zooms[] = OspUtils.uFloatToByte(zoomFactor*100f);
        byte zooms2[] = new byte[4];
        zooms2[3] = zooms[0];
        zooms2[2] = zooms[1];
        zooms2[1] = zooms[2];
        zooms2[0] = zooms[3];

        byte content[] = new byte[16];
        int index = 0;
        index += NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index += NetUtil.memcpy(content,zooms2,index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(focusX),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(focusY),index);
        sendEntityPacket(content, Command.CC_ZOOM_GESTURE);
    }

    /**
     * 发送旋转数据
     */
    public void sendRotateMsg(boolean isLeft,long tabId){


        byte event = 0;

        if(isLeft){
            event = Command.CC_ROTATELEFT;
        }else{
            event = Command.CC_ROTATERIGHT;
        }

        byte content[] = OspUtils.uintToByte(tabId);

        sendEntityPacket(content, event);

    }


    /**
     * 发送撤销或者反撤销同步消息
     */
    public void sendUnDoOrReDoMsg(boolean isUnDo,long tabId,long pageId){

        byte event = Command.CC_UNDO;

        if(isUnDo){
            event = Command.CC_UNDO;
        }else{
            event = Command.CC_REDO;
        }

        byte content[] = new byte[8];
        int index = 0;
        index += NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(pageId),index);
        sendEntityPacket(content, event);
    }

    /**
     * 发送图源信息到服务端，主要实现共享标注功能
     * @param graphData
     */
    public void sendGraphMsg(byte graphData[]){
        if(graphData==null||graphData.length==0)return;

        //content(10) = contentLength(4) + even(1) + user_index(4)+ data(n) + end(1)

        TPLog.printError("SyncGraph","compress graph data....");
        //压缩数据
        byte cGraphData[] = NetUtil.CompressBuffer(graphData);

        TPLog.printError("SyncGraph","compress graph data end ,dataLength="+cGraphData.length);

        TPLog.printError("SyncGraph","Send Pen Graph Data...");
        sendEntityPacket(cGraphData, Command.CC_DRAWENTITY);
    }

    /**
     * 发送新建tab页消息
     * @param
     */
    public void sendNewTabMsg(int m_nTotalDocPage,String tabName,long tabId,int nSubPageCount,int mode,int width,int height,int owerIndex,int inConvert,int isAnoymous){

        int time = m_nTotalDocPage;

        byte byteTabName[] = null;
        if(isAnoymous==0) {
            try {
                byteTabName = tabName.getBytes("GBK");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int contentLength = 4*10 +128;

        byte content[] = new byte[contentLength];
        int index = 0;
        index += NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(nSubPageCount),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(m_nTotalDocPage),index);
        if(isAnoymous==0) {
            index += NetUtil.memcpy(content, byteTabName, index);
            index +=(128-byteTabName.length);
        }else{
            index +=128;
        }

        index += NetUtil.memcpy(content,OspUtils.uintToByte(mode),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(width),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(height),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(owerIndex),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(time),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(inConvert),index);
        index += NetUtil.memcpy(content,OspUtils.uintToByte(isAnoymous),index);

//        Log.e("msg","创建选项卡数据：");
//        Utils.displayArray(content);

        sendEntityPacket(content, Command.CC_NEWPAGE);
    }


    /**
     * 发送删除Tab页消息
     * @param removeTabId 需要删除的白板ID
     * @param lastTabId 下个需要显示的白板ID
     */
    public void sendRemoveTableMsg(long removeTabId,long lastTabId){

        byte content[] = new byte[8];
        int index = 0;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(removeTabId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(lastTabId),index);

        sendEntityPacket(content, Command.CC_REMOVEPAGE);
    }

    public void sendRemoveAllTableMsg(long newTabId){
        byte content[] = new byte[4];
        int index = 0;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(newTabId),index);
        sendEntityPacket(content, Command.CC_REMOVEALLPAGE);
    }

    /**
     * 切换Tab或者SubPage页
     * @param pageIndex
     */
    public void sendTabPageChangedMsg(final int pageIndex,long tabId){

        long needRequestImage = 0;
        byte content[] = new byte[12];
        int index = 0;
        index += NetUtil.memcpy(content, OspUtils.uintToByte(tabId), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(pageIndex), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(needRequestImage), index);

        sendEntityPacket(content, Command.CC_TABPAGE);

    }

    /**
     * 发送屏幕滚动消息
     * @param x
     * @param y
     */
    public void sendScrollChangedMsg(long tabId,long pageIndex,int x,int y,boolean isFinish){
        byte[] content = new byte[20];

        int index = 0;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(pageIndex),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(x),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(y),index);
        int finish = isFinish?0:1;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(finish),index);

        sendEntityPacket(content, Command.CC_SCROLL);
    }

    /**
     * 向服务器请求翻页
     * @param pageIndex
     * @param checkNeedRequestImage 是否需要检测下载图片
     */
    public void sendPageChangeMsg(int pageIndex,boolean checkNeedRequestImage){
//        //先进形页面切换
//        sendTabPageChangedMsg(pageIndex);
//        if(checkNeedRequestImage) {  //需要请求图片
//
//            if(ie.isDlSuccess()&&!ie.isExistOnServer()){//如果已经下载完成且服务器上面没有该文件，那么就发送该文件
//                requestServerRecFlow();//主动发送文件给服务器
//                return;
//            }
//
//            if(ie.isExistOnServer()){//如果服务器上面存在该图片就直接下载
//                SynFileManager.getInstance().requestFileInfo(ie);
//                return;
//            }
//
//            sendToServerReqPageTurn(ie,pageIndex);
//        }
    }

    /**
     * 请求服务器进行翻页
     * @param pageIndex
     */
    public void sendToServerReqPageTurn(int pageIndex, int imageId ,long tabId,int ownerIndex){
        byte content[] = new byte[16];
        int index = 0;
        index += NetUtil.memcpy(content, OspUtils.uintToByte(imageId), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(tabId), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(pageIndex), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(ownerIndex), index);
        sendEntityPacket(content, Command.CS_REQUESTPAGETURN);
    }

    /**
     * 发送文档打开成功命令
     */
    public void sendOpenOfficeComplete(long tabId){
        byte content[] = OspUtils.uintToByte(tabId);
        sendEntityPacket(content, Command.CC_DOCPAGECONVERTCOMPLETE);
    }

    /**
     * 发送子页总数
     * @param count
     */
    public void sendSubpageCountMsg(long tabId,int count){
        byte content[] = new byte[8];
        int index = 0;
        index = NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index = NetUtil.memcpy(content,OspUtils.uintToByte(count),index);
        sendEntityPacket(content, Command.CC_ADDSUBPAGE);
    }

    /**
     * 发送文件传输结束命令
     */
    public void sendFileEndMsg(){
        Osp osp = mConnecter.getOsp();
        boolean isConnected = mConnecter.isConnect();
        if(osp==null||!isConnected)return;
        osp.writeEmptyMsg(NetUtil.EV_SV_CL_FILEEND);
    }


    /**
     * 当客户端 加入会议成功后 请求同步数据
     */
    public void requestSynchronous(){
        //固定格式 消息体 = 4字节消息体长度 + 1字节0x32 + 1字节0x09
        byte data[] = new byte[6];

        byte length[] = OspUtils.uintToByte(2);

        int index = 0;

        for(int i = 0;i<length.length;i++){
            data[index++] = length[i];
        }

        data[index++] = Command.CS_SYNCHRONOUS;

        data[index++] = Command.MSG_C2S;

        sendMsg(data, NetUtil.EVEN_SYNCHRONOUS_REQ);
        TPLog.printError("请求同步消息已经发出。。。");
    }

    /**
     * 请求服务器当前接收流量大小
     */
    public void requestServerRecFlow(){
        sendMsg(new byte[0],NetUtil.EV_SV_CL_BUF_SIZE_REQ);
    }


    //同步会议数据发送待定。。。。。。。。。。。。。。。。。。。。。。。。。。。。
    public void sendSynchronous(long dwRequestId){

    }


    /**
     * 将需要发送内容进行封包发送
     */
    public void sendEntityPacket(byte content[],byte event){

        if(content==null||content.length==0)return;

        int packLength = 1 + 4 + content.length + 1;
        int contentLength = content.length;

        byte packet[] = new byte[packLength+4];

        int index = 0;
        index += NetUtil.memcpy(packet,OspUtils.uintToByte(packLength),index);
        packet[index++] = event;
        index += NetUtil.memcpy(packet,OspUtils.uintToByte(contentLength),index);
        index += NetUtil.memcpy(packet,content,index);
        packet[index] = Command.MSG_TERM;
        sendMsg(packet, NetUtil.EVEN_SYNCHRONOUS_REQ);
    }


    /**
     * 发送消息到服务器
     * @param packet 需要发送的消息
     * @param even 消息事件
     */
    public void sendMsg(final byte packet[],final char even){
//        new Thread(){//部分机型上面在主线程内发送数据会出现异常，因此这里改为在子线程内发送数据
//            @Override
//            public void run() {
                Osp osp = mConnecter.getOsp();
                boolean isConnected = mConnecter.isConnect();
                if(osp==null||!isConnected)return;
                //Utils.displayArray(content);
//                TPLog.printKeyStatus("OSP准备写出消息。。。");
                osp.writeContentMsg(packet,even);
//            }
//        }.start();
    }

    /**
     * 建立连接后发送的第一条消息  相当于登录
     */
    public void writeConnectMsg(){
        OspUtils.DST_CURRENT_INSID = OspUtils.DST_INSID;

        //创建消息体
        byte msg[] ={
                0,0,0,1,49,46,49,46,49,46,49,46,48,46,50,48,49,54,48,49,49,57
        };

        byte content[] = new byte[68];

        for(int i=0;i<content.length;i++){
            if(i<msg.length){
                content[i] = msg[i];
            }
        }

        sendMsg(content,NetUtil.EVEN_CONNECT_REQ);
    }

}
