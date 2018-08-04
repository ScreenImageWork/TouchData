package com.kedacom.touchdata.net;

import android.graphics.Point;
import android.util.Log;

import com.kedacom.osp.Osp;
import com.kedacom.osp.OspUtils;
import com.kedacom.osp.entity.OspMsgEntity;
import com.kedacom.touchdata.net.entity.CoordinateChangedMsg;
import com.kedacom.touchdata.net.entity.DeleteGraphMsg;
import com.kedacom.touchdata.net.entity.GestureScaleEntity;
import com.kedacom.touchdata.net.entity.GraphCoordinateChangedMsg;
import com.kedacom.touchdata.net.utils.Command;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.op.ClearScreenOperation;
import com.kedacom.touchdata.whiteboard.op.GraphOperation;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.op.RotateOperation;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanglei on 2016/12/16.
 */
public class Reader {

    private NetHandler mHandler;

    //分包重组
    private byte regroupSegment[] = null;
    private long segmentMaxSize = 0;
    private long firstSegmentId = 0;
    private long curRegroupLength;

    public Reader(NetHandler handler){
        mHandler = handler;
    }

    public void readMsd(OspMsgEntity msgEntity){
        try {
            int even = msgEntity.getMsgHead().getEvent();
            byte content[] = msgEntity.getContent();
            switch (even) {
                case NetUtil.EVEN_CONNECT_RSP://服务端响应连接消息 相当于登陆成功
                    //登陆成功系统会发送当前的会议列表  这里不进行处理
                    TPLog.printKeyStatus("EVEN_CONNECT_RSP");
                    //String msg = new String(content, "GBK");
                    //analysisConfList(msg);
                    String msg = new String(content, "GBK");
                    String meetingName = analysisConfList(msg);
                    sendHandlerMsg(NetHandler.LOGIN, meetingName);
                    break;
                case NetUtil.EVEN_CONFERENCE_LISG_RSP://会议列表更新
                    TPLog.printKeyStatus("会议列表更新。。。");
                     msg = "";
                    meetingName = "";
                    if (content != null) {
                        msg = new String(content, "GBK");
                         meetingName = analysisConfList(msg);
                    }
                    TPLog.printKeyStatus("当前会议名称:"+meetingName);
                    sendHandlerMsg(NetHandler.MEETING_NAME,meetingName);
                    break;
                case NetUtil.EVEN_CREATE_CONFERENCE_RSP://创建会议响应
                    TPLog.printKeyStatus("->收到加入会议消息");
                    if (content != null) {
                        //固定大小80
                        //content = 4位的错误码 + 4位的会议ID + 4位的用户ID + 4位的会议名称长度+N位的会议名称(最大64)
                        long err = OspUtils.getUintFromBuf(content, 0);
                        sendHandlerMsg(NetHandler.CREATE_MEETING, err);
                        TPLog.printKeyStatus("创建会议:errCode="+err);
                    }else{
                        TPLog.printKeyStatus("->加入会议失败");
                    }
                    break;
                case NetUtil.EVEN_JOIN_CONFERENCE_RSP://服务端响应加入会议
                    if (content != null) {
                        //content = 4位结果码 + 4位会议ID + 4用户ID
                        int index = 0;
                        long err = OspUtils.getUintFromBuf(content, index);
                        index += 4;
                        long meetId = OspUtils.getUintFromBuf(content, index);
                        index += 4;
                        long userId = OspUtils.getUintFromBuf(content, index);
                        index += 4;
                        NetUtil.curUserId = userId;
                        TPLog.printKeyStatus("加入会议返回信息：userId="+userId+",errCode="+err);
                        sendHandlerMsg(NetHandler.JOIN_MEETING, err);
                    }
                    break;
                case NetUtil.EVEN_SYNCHRONOUS_RSP://接收到服务端的数据同步消息请求
                    handleSynMsg(content);
                    break;
                case NetUtil.EV_SV_CL_BUF_SIZE_RSP: //服务器返回接收流量大小
                    //content 只有4个字节  目前没有使用
                    //发送文件给服务器
                    sendHandlerMsg(NetHandler.BUF_SIZE, 0);
                    break;
                case NetUtil.ev_User_Mail_Nty:// 接收邮件配置信息
                    TPLog.printKeyStatus("已接收到邮件配置信息！");
                    break;
                case NetUtil.EV_CL_SV_UPDATE_CONF_NUM:  //当前与会人员列表更新
                    TPLog.printKeyStatus("与会人员列表更新。。。");
                    NetUtil.displayArray(content);
                    handleUpdateConfMemberNum(content);
                    break;
                case NetUtil.EVEN_CONNECT_NUM_NTF:
                    TPLog.printKeyStatus("连接人数变更通知。。。");
//                    NetUtil.displayArray(content);
                    handleServerConnectNumNtf(content);
                    break;
            }
        }catch(Exception e){
            TPLog.printError("接收消息解析时出现异常:");
            TPLog.printError(e);
        }
    }


    private void handleServerConnectNumNtf(byte content[]){
        if(content == null){
            return;
        }
        int index = 0;
        long msgLen =  OspUtils.getUintFromBuf(content,index);
        index+=4;
        int connectNum =  content[index++];
        sendHandlerMsg(NetHandler.SERVER_CONNECT_NUM_NTF, connectNum);
    }


    private void handleUpdateConfMemberNum(byte content[]){
        if(content == null){
            return;
        }
        int index = 0;
        long contentLen = OspUtils.getUintFromBuf(content,index);
        index+=4;
        int confNum = content[index++];//会场个数
        long confNameLen = content[index++];
        String confName = new String(content,index,(int)confNameLen); //会议名称
        index+=confNameLen;
        int num = 0;
        while(index<content.length){
            int userIndex = content[index++];  //用户索引
            int userType = content[index++];  //用户类型
            int isConfCreater = content[index++]; //是否是创会者
            num++;
        }
        sendHandlerMsg(NetHandler.CONF_MEMBER_NUM_UPDATE, num);
    }


    /**
     * 处理同步通用消息
     */
    private void handleSynMsg(byte data[]) {
//        TPLog.printError("接收到消息。。。。。。。。。。。。。。。。。。。。。");
//        NetUtil.displayArray(data);
        /**                                           //length(4)
         * content(10) = contentLength(4) + even(1) + user_index(4)+ data(n) + end(1)
         * even：
         *0x42，请求同步
         *0x43,当前没有用户准备好
         *0x44,发送已存在文件通知(不懂)
         */
        if (data == null) {
            return;
        }
        long contentLength = OspUtils.getUintFromBuf(data, 0);
        if (contentLength < 5) {
            data = null;
            return;
        }
        byte syneven = data[4];

        //C++里面是这么处理的，这里照抄
        byte nHBit = (byte) (syneven & 0x80);        //取最高位 ,最高位为1，则nHBit = 0x80;
        boolean bSynchronousMsg = (nHBit == 0x80);
        syneven = (byte) (syneven & 0x7F);        //还原低7位

        switch (syneven) {
            case Command.SC_SYNCHRONOUS://请求同步消息
                TPLog.printError("SC_SYNCHRONOUS");
                parseRequestSynchronpousData(data);
                break;
            case Command.SC_SYNCHRONOUSFAILED://服务器回复当前没有用户准备好, 3秒后再请求一次
                //TPLog.printKeyStatus("SC_SYNCHRONOUSFAILED");
                 TPLog.printError("SC_SYNCHRONOUSFAILED");
                sendHandlerMsg(NetHandler.SYNCHRONOUSE_FAILED,null);
                //synchronouseBody(data);
                break;
            case Command.CC_SYNCHRONOUSEBODY://同步内容
//                TPLog.printKeyStatus("CC_SYNCHRONOUSEBODY");
                TPLog.printError("CC_SYNCHRONOUSEBODY");
                try{
                    synchronouseBody(data);
                }catch(Exception e){
                    TPLog.printError("解析同步数据时出现异常");
                    TPLog.printError(e);
                }
                break;
            case Command.CC_PAGEAVAILABLY:
                TPLog.printKeyStatus("CC_PAGEAVAILABLY");

                break;
            case Command.CC_DRAWENTITY: //接收其他客户端的图源信息
                TPLog.printKeyStatus("CC_DRAWENTITY");
                parseDrawEntityData(data);
                break;
            case Command.CC_IMAGE: //打开文件时进行文件同步
                TPLog.printKeyStatus("CC_IMAGE");
                SynFileManager.getInstance().parseImageInfo(data);
                break;
            case Command.CC_ZOOM:
                TPLog.printKeyStatus("CC_ZOOM");
                parseZoomData(data);
                break;
            case Command.CC_ZOOM_GESTURE:
                TPLog.printKeyStatus("CC_ZOOM_GESTURE");
                parseGestureZoomData(data);
                break;
            case Command.CC_SCROLL: //水平或者垂直滚动
                TPLog.printKeyStatus("CC_SCROLL");
                parseScrollData(data);
                break;
            case Command.CC_CLEAR: //清屏
                TPLog.printKeyStatus("CC_CLEAR");
                sendHandlerMsg(NetHandler.CLEAR_SCREEN, null);
                break;
            case Command.CC_NEWPAGE://创建选项卡
                TPLog.printKeyStatus("CC_NEWPAGE");
                parseNewTab(data);
                break;
            case Command.CC_REMOVEPAGE://删除选项卡
                TPLog.printKeyStatus("CC_REMOVEPAGE");
                parseDeleteTabMsg(data);
                break;
            case Command.CC_REMOVEALLPAGE:
                TPLog.printKeyStatus("CC_REMOVEALLPAGE");
                parseDeleteAllTabMsg(data);
                break;
            case Command.CC_TABPAGE://切换tab页或者子页
                TPLog.printKeyStatus("CC_TABPAGE");
                parseChangePage(data);
                break;
            case Command.SC_REQUESTPAGETURN://服务器向客户端请求翻页
                //TPLog.printKeyStatus("SC_REQUESTPAGETURN");
                TPLog.printError("SC_REQUESTPAGETURN");
                parseServerReqChangePage(data);
                break;
            case Command.SC_OWERLEAVE:
                TPLog.printKeyStatus("SC_OWERLEAVE");
                break;
            case Command.CC_ERASE: //擦除
                parseEraseData(data);
                break;
            case Command.SOCK_FILEINFO: //文件信息
                TPLog.printKeyStatus("SOCK_FILEINFO");
                SynFileManager.getInstance().parseFileInfo(data);
                break;
            case Command.SOCK_FILEINFOFAILED: //请求文件头信息失败
                TPLog.printError("SOCK_FILEINFOFAILED");
                SynFileManager.getInstance().reDownloadCurFile();
                break;
            case Command.CC_REQUESTFILEBODY_ACK: //文件
                TPLog.printKeyStatus("CC_REQUESTFILEBODY_ACK");
                SynFileManager.getInstance().parseFileBody(data);
                break;
            case Command.CC_REDO:
                TPLog.printKeyStatus("CC_REDO");
                parseRedoMsg(data);
                break;
            case Command.CC_UNDO:
                TPLog.printKeyStatus("CC_UNDO");
                parseUndoMsg(data);
                break;
            case Command.CC_SEGMENTMSG:  //拆包数据
                TPLog.printKeyStatus("CC_SEGMENTMSG");
                parseSegmentData(data);
                break;
            case Command.CC_ROTATELEFT://左旋转
                TPLog.printKeyStatus("CC_ROTATELEFT");
                sendHandlerMsg(NetHandler.ROTATE_LEFT, null);
                break;
            case Command.CC_ROTATERIGHT: //右旋转
                TPLog.printKeyStatus("CC_ROTATERIGHT");
                sendHandlerMsg(NetHandler.ROTATE_RIGHT, null);
                break;
            case Command.CC_ROTATE: //任意角度旋转
                TPLog.printKeyStatus("CC_ROTATE");
                parseRotateMsg(data);
                break;
            case Command.SC_FILEINVALID:
                TPLog.printKeyStatus("SC_FILEINVALID");
                break;
            case Command.SC_TABINVALID:
                TPLog.printKeyStatus("SC_TABINVALID");
                break;
            case Command.CC_ADDSUBPAGE: //文档总页数  Android端暂未使用
                TPLog.printKeyStatus("CC_ADDSUBPAGE");
                break;
            case Command.SC_EXIST_FILE_NOTIFY: //服务器告诉所有的客户端已经存在的文件
                TPLog.printKeyStatus("SC_EXIST_FILE_NOTIFY");
                parseExistFileNotifyData(data);
                break;
            case Command.CC_DOCPAGECONVERTCOMPLETE:  //文档打开完成  Android端暂未使用
                TPLog.printKeyStatus("CC_DOCPAGECONVERTCOMPLETE");

                break;
            case Command.CC_INSERT_IMG://2017.07.04添加 图片图元插入接口
                TPLog.printKeyStatus("CC_INSERT_IMG");
                parseInsertImgMsg(data);
                break;
            case  Command.CC_COORDINATE_CHANGED:
                TPLog.printKeyStatus("CC_COORDINATE_CHANGED");
                parseCoordinateChangedMsg(data);
                break;
            case Command.CC_GRAPH_COORDINATE_CHANGED:
                TPLog.printKeyStatus("CC_GRAPH_COORDINATE_CHANGED");
                parseGraphCoordinateChangedMsg(data);
                break;
            case Command.CC_DELETE_GRAPH:
                TPLog.printKeyStatus("CC_DELETE_GRAPH");
                parseDeleteGraphMsg(data);
                break;
        }
    }


    private int index = 0;
    //处理返回的同步会议信息
    private void synchronouseBody(byte data[]) {

        print("解析入会同步消息->开始");

        if(data==null||data.length <11){
            return;
        }

        List<Page> PageList = new ArrayList<Page>();

        long contentLength2 = OspUtils.getUintFromBuf(data, 0);//后续消息体长度
        long msgEvent = data[4]; //消息事件
        long contentLength3 = OspUtils.getUintFromBuf(data, 5);//后续消息体长度
        long contentLength4 = OspUtils.getUintFromBuf(data, 9);//后续消息体长度

        //C++里面是这么处理的，这里照抄
        byte nHBit = (byte) (msgEvent & 0x80);        //取最高位 ,最高位为1，则nHBit = 0x80;这句暂时没有用
        boolean bSynchronousMsg = (nHBit == 0x80);
        msgEvent = (byte) (msgEvent & 0x7F);        //还原低7位

        byte compressData[] = new byte[(int) contentLength4];
        //获取消息体
        NetUtil.memcpy(compressData, data, 0, 13, (int) contentLength4);
        //解压消息体
        byte unCompressData[] = NetUtil.UnCompressBuffer(compressData);
        //debug
       // NetUtil.displayArray(unCompressData);
        index = 0;
        long tabCount = OspUtils.getUintFromBuf(unCompressData, index);//tab页的个数
        index += 4;

        long curTabId = OspUtils.getUintFromBuf(unCompressData, index);//当前显示tabId
        index += 4;

        print("tabCount="+tabCount);

        for(int i = 0;i<tabCount;i++){

            print("tabIndex-->"+i);

            long tabId = OspUtils.getUintFromBuf(unCompressData, index); //当前Tab页Id
            index += 4;
            print("tabId="+tabId);

            long nameByteLength = OspUtils.getUintFromBuf(unCompressData, index); //名称字节长度
            index += 4;
            print("nameByteLength="+nameByteLength);

            byte nameBytes[] = new byte[(int)nameByteLength];
            for(int l = 0;l<nameByteLength;l++){
                nameBytes[l] = unCompressData[index++];
            }

            String tabName = ""; //tab名称
            try {
                tabName = new String(nameBytes, "Unicode");
            } catch (Exception e) {
                e.printStackTrace();
            }

            print("tabName="+tabName);


            long pageCount = OspUtils.getUintFromBuf(unCompressData, index); //当前Tab子页总数
            index += 4;
            print("pageCount="+pageCount);

            long curpageIndex = OspUtils.getUintFromBuf(unCompressData, index); //当前显示的子页页码 0开始
            index += 4;
            print("curpageIndex="+curpageIndex);

            long ownerIndex = OspUtils.getUintFromBuf(unCompressData, index); //该TAB页属主在服务器上的index
            index += 4;
            print("ownerIndex="+ownerIndex);

            long time = OspUtils.getUintFromBuf(unCompressData, index); //时间
            index += 4;
            print("time="+time);


            Page page = new Page();
            page.setId(tabId);
            page.setName(tabName);
            //page.setDocPageCount((int) pageCount);
            //page.selectSubPage((int) curpageIndex);
            page.setOwnerIndex((int) ownerIndex);
            page.setM_nTime((int) time);
            page.setBackGroundColor(WhiteBoardUtils.curBackground);

            //TPLog.printKeyStatus("subPageCount="+pageCount);
            //子页循环
            for(int p = 0;p<pageCount;p++) {

                float matrixValue[] = new float[9];

                for(int l = 0;l<matrixValue.length;l++){
                    matrixValue[l] = OspUtils.getUFloatFromBuf(unCompressData,index);
                    index += 4;
                }


                SubPage subPage = new SubPage();
                subPage.setMatrixValues(matrixValue);

                long graphCount = OspUtils.getUintFromBuf(unCompressData, index);//标注数量
                index += 4;

                long tempGraphCount = graphCount;
                print("graphCount = "+tempGraphCount);
                while (tempGraphCount > 0) {
                    long type = OspUtils.getUintFromBuf(unCompressData, index);//批注类型
                    byte buffer[] = synchronouseGraphEntity(unCompressData);

//                    NetUtil.displayArray(buffer);

                    if (buffer != null) {
                        if(type!= WhiteBoardUtils.GRAPH_IMAGE) {
                            Graph ge = parseGraphData(buffer, false);
                            if (ge != null) {
                                subPage.addGraph(ge);
                            }
                        }else{
                            ImageGraph image = parseSynchronouseImageEntity(buffer, p);
                            image.setPageIndex(p);
                            image.setTabId(tabId);
                            if(image!=null){
                                subPage.addGraph(image);
                            }
                        }
                        buffer = null;
                    }

                    tempGraphCount--;
                }


                //在图元添加完成之后必须进行撤销和还原列表的清空
                subPage.clearUndoList();
                subPage.clearRedoList();

                /**----------------unDoSet----------------------**/

                long undoSize = OspUtils.getUintFromBuf(unCompressData, index);
                index += 4;
                print("undoSize="+undoSize);
                for (int u = 0; u < undoSize; u++) {
                    /**
                     * eoAdd = 0,			//添加操作
                     * eoRotate =1,			//旋转
                     * eoClear = 2,			//清屏
                     * eoEarse = 3,			//橡皮擦
                     */
                    long operatprType = OspUtils.getUintFromBuf(unCompressData, index);
                    index += 4;

                    print("operatprType="+operatprType);
                    IOperation op = null;
                    if (operatprType == 0) {
                        long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                        index += 4;
                        byte buffer[] = synchronouseGraphEntity(unCompressData);

                        GraphOperation graphOp = new GraphOperation();
                        if (buffer != null) {
                            Graph ge = parseGraphData(buffer, false);
                            if(ge==null)continue;
                            Graph ge2 = subPage.getGraph(ge.getId());
                            if(ge2!=null){
                                graphOp.setGraph(ge2);
                            }else{
                                graphOp.setGraph(ge);
                            }
                            buffer = null;
                        }
                        op = graphOp;
                    } else {//if
                        switch ((int) operatprType) {
                            case 2:
                                long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long clearEntityCount =  OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                ClearScreenOperation cse = new ClearScreenOperation();
                                List<Graph> list = new ArrayList<Graph>();
                                for(int c=0;c<clearEntityCount;c++){
                                    byte buffer[] = synchronouseGraphEntity(unCompressData);
                                    if (buffer != null) {
                                        Graph ge = parseGraphData(buffer, false);
                                        if(ge!=null){
                                            list.add(0,ge);
                                        }
                                    }
                                    buffer = null;
                                }
                                op = cse;
                                break;
                            case 3:

                                break;
                            case 1:
                                m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long m_emRotateDirection  = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long curAngle = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long oldAngle = OspUtils.getUintFromBuf(unCompressData, index);

                                RotateOperation re = new RotateOperation();
                                re.setCurAngle((int)curAngle);
                                re.setOldAngle((int) oldAngle);
                                op = re;
                                break;
                        }
                    }
                    if(op!=null){
                        subPage.addUndo(op);
                    }
                }// for undo


                /***********************reDo*****************************/

                long redoSize = OspUtils.getUintFromBuf(unCompressData, index);
                index += 4;
                print("redoSize="+redoSize);
                for (int r = 0; r < redoSize; r++) {
                    long operatprType = OspUtils.getUintFromBuf(unCompressData, index);
                    index += 4;
                    IOperation op = null;
                    if (operatprType != 0) {
                        switch ((int) operatprType) {
                            case 2:
                                long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long clearEntityCount =  OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                ClearScreenOperation cse = new ClearScreenOperation();
                                List<Graph> list = new ArrayList<Graph>();
                                for(int c=0;c<clearEntityCount;c++){
                                    byte buffer[] = synchronouseGraphEntity(unCompressData);
                                    if (buffer != null) {
                                        Graph ge = parseGraphData(buffer, false);
                                        if(ge!=null){
                                            list.add(0,ge);
                                        }
                                    }
                                    buffer = null;
                                }
                                op = cse;
                                break;
                            case 3:

                                break;
                            case 1:  //旋转现在没有了，因此这里暂时先注释掉
//                                m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
//                                index += 4;
//                                long m_emRotateDirection  = OspUtils.getUintFromBuf(unCompressData, index);
//                                index += 4;
//                                long oldAngle = 0;
//                                if(m_emRotateDirection==0){
//                                    oldAngle = angle + 90;
//                                }else{
//                                    oldAngle = angle - 90;
//                                }
//                                RotateOperation re = new RotateOperation();
//                                re.setCurAngle((int)angle);
//                                re.setOldAngle((int) oldAngle);
//                                op = re;
                                break;
                        }
                    } else {
                        /**
                         * eoAdd = 0,			//添加操作
                         * eoRotate =1,			//旋转
                         * eoClear = 2,			//清屏
                         * eoEarse = 3,			//橡皮擦
                         */
                        long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                        index+=4;
                        long graphtype = OspUtils.getUintFromBuf(unCompressData, index);//图源类型
                        byte buffer[] = synchronouseGraphEntity(unCompressData);

                        if (buffer != null) {
                            Graph ge = parseGraphData(buffer, false);
                            if(ge==null)continue;
                            Graph ge2 =subPage.getGraph(ge.getId());

                            GraphOperation gop = new GraphOperation();
                            if(ge2!=null){
                                gop.setGraph(ge2);
                            }else{
                                gop.setGraph(ge);
                            }
                            op = gop;
                        }
                        buffer = null;
                    }

                    if (op != null) {
                        subPage.addRedo(op);
                    }
                } //redo
                page.addSubPage(subPage);
            } //page
            page.selectSubPage((int) curpageIndex+1);
            PageList.add(page);
        } //tab


        Map<String ,Object> map = new HashMap<String, Object>();
        map.put("curPageId", curTabId);
        map.put("PageList", PageList);
        sendHandlerMsg(NetHandler.SYNCHRONOUSE, map);
        unCompressData = null;
        TPLog.printKeyStatus("解析入会同步消息成功！");
    }



    private void parseDeleteGraphMsg(byte data[]){
        if (data == null) {
            return;
        }
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long tabId = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long subPageIndex = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long graphId = OspUtils.getUintFromBuf(data,index);
        index +=4;

        DeleteGraphMsg msg = new DeleteGraphMsg();
        msg.setGraphId((int) graphId);
        msg.setTabId(tabId);
        msg.setSubPageIndex((int)subPageIndex);
        sendHandlerMsg(NetHandler.DELETE_GRAPH,msg);
    }

    private void parseGraphCoordinateChangedMsg(byte data[]){
        if (data == null) {
            return;
        }

        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long tabId = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long subPageIndex = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long graphCount =  OspUtils.getUintFromBuf(data,index);
        index +=4;

       ArrayList<GraphCoordinateChangedMsg> list = new ArrayList<GraphCoordinateChangedMsg>();
        for(int i = 0;i<graphCount;i++){
            GraphCoordinateChangedMsg gccm = new GraphCoordinateChangedMsg();
            long graphId =  OspUtils.getUintFromBuf(data,index);
            index +=4;
            float matrixValues[]= new float[9];
            for(int j = 0;j<matrixValues.length;j++){
                matrixValues[j] =  OspUtils.getUFloatFromBuf(data,index);
                index +=4;
            }

//            float drawMatrixValues[]= new float[9];
//            for(int j = 0;j<drawMatrixValues.length;j++){
//                drawMatrixValues[j] =  OspUtils.getUFloatFromBuf(data,index);
//                index +=4;
//            }
            TPLog.printKeyStatus("parseGraphCoordinateChangedMsg--->graphId:"+graphId);
            gccm.setId((int)graphId);
            gccm.setMatrixValue(matrixValues);
//            gccm.setDrawMatrixValue(drawMatrixValues);
            list.add(gccm);
        }

//        print("parseGraphCoordinateChanged--------------------->");
//        NetUtil.displayArray(data);

        sendHandlerMsg(NetHandler.GRAPH_COORDINATE_CHANGED,list);
    }

    private void parseCoordinateChangedMsg(byte data[]){
        if (data == null) {
            return;
        }
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long tabId = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long subPageIndex = OspUtils.getUintFromBuf(data,index);
        index +=4;
        float matrixValues[] = new float[9];
        for(int i = 0;i<matrixValues.length;i++){
            matrixValues[i] = OspUtils.getUFloatFromBuf(data,index);
            index+=4;
        }
        CoordinateChangedMsg mCoordinateChangedMsg = new CoordinateChangedMsg();
        mCoordinateChangedMsg.setTabId(tabId);
        mCoordinateChangedMsg.setSubPageIndex(subPageIndex);
        mCoordinateChangedMsg.setMatrixValues(matrixValues);

//        print("parseCoordinateChangedMsg------------------------------->");
//        NetUtil.displayArray(data);

        sendHandlerMsg(NetHandler.COORDINATE_CHANGED,mCoordinateChangedMsg);
    }

    private void parseInsertImgMsg(byte data[]){
        if (data == null) {
            return;
        }
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long graphType = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long tabId = OspUtils.getUintFromBuf(data,index);
        index +=4;
        long subPageIndex =  OspUtils.getUintFromBuf(data,index);
        index+=4;
        long imgId =  OspUtils.getUintFromBuf(data,index);
        index+=4;
        long x =  OspUtils.getUintFromBuf(data,index);
        index+=4;
        long y =  OspUtils.getUintFromBuf(data,index);
        index+=4;
        long width =  OspUtils.getUintFromBuf(data,index);
        index+=4;
        long height =  OspUtils.getUintFromBuf(data,index);
        index+=4;

        float matrixValues[] = new float[9];
        for(int i = 0;i<matrixValues.length;i++){
            matrixValues[i] = OspUtils.getUFloatFromBuf(data,index);
            index+=4;
        }

        ImageGraph imgGraph = new ImageGraph();
        imgGraph.setTabId(tabId);
        imgGraph.setPageIndex(subPageIndex);
        imgGraph.setId((int)imgId);
        imgGraph.setX(x);
        imgGraph.setY(y);
        imgGraph.setWidth((int) width);
        imgGraph.setHeight((int)height);
        imgGraph.setMatrixValues(matrixValues);

//        print("tabId="+tabId);
//        print("subPageIndex="+subPageIndex);
//        print("imgId="+imgId);
//        print("x="+x);
//        print("y="+y);
//        print("width="+width);
//        print("height="+height);


        sendHandlerMsg(NetHandler.INSERT_IMG,imgGraph);
    }



    //其他客户端请求同步消息处理
    private void parseRequestSynchronpousData(byte data[]){
        if (data == null) {
            return;
        }
        int index = 0;
        long length = OspUtils.getUintFromBuf(data,index);
        index+=4;
        byte event = data[index++];
        long dwRequestId = OspUtils.getUintFromBuf(data,index);

        sendHandlerMsg(NetHandler.SC_SYNCHRONOUS, dwRequestId);
    }

    //处理返回的同步会议信息
    /*
    private int index = 0;
    private void synchronouseBody(byte data[]) {

        print("解析入会同步消息->开始");

        //NetUtil.displayArray(data);

        if(data==null||data.length <11){
            return;
        }

        List<Page> PageList = new ArrayList<Page>();

        long contentLength2 = OspUtils.getUintFromBuf(data, 0);//后续消息体长度
        long msgEvent = data[4]; //消息事件
        long contentLength3 = OspUtils.getUintFromBuf(data, 5);//后续消息体长度
        long contentLength4 = OspUtils.getUintFromBuf(data, 9);//后续消息体长度

        //C++里面是这么处理的，这里照抄
        byte nHBit = (byte) (msgEvent & 0x80);        //取最高位 ,最高位为1，则nHBit = 0x80;这句暂时没有用
        boolean bSynchronousMsg = (nHBit == 0x80);
        msgEvent = (byte) (msgEvent & 0x7F);        //还原低7位

        byte compressData[] = new byte[(int) contentLength4];
        //获取消息体
        NetUtil.memcpy(compressData, data, 0, 13, (int) contentLength4);
        //解压消息体
        byte unCompressData[] = NetUtil.UnCompressBuffer(compressData);
        index = 0;
        long tabCount = OspUtils.getUintFromBuf(unCompressData, index);//tab页的个数
        index += 4;

        print("tabCount="+tabCount);

        for(int i = 0;i<tabCount;i++){

            print("tabIndex-->"+i);

            long tabId = OspUtils.getUintFromBuf(unCompressData, index); //当前Tab页Id
            index += 4;
            print("tabId="+tabId);

            //这里的字符串比较难取，对端没有定义字符串的字节长度 只是设置末尾为两个零，表示这样很不靠谱
            int strEndIndex = 0;
            for (int a = index; a < unCompressData.length; a++) {
                if (unCompressData[a] == 0) {

                    if (a - strEndIndex == 1) {
                        break;
                    }

                    strEndIndex = a;
                }
            }

            int strByteLength = strEndIndex - index;

            if (strByteLength < 0) {
                return;
            }

            int tempIndex = index;

            index += strByteLength + 2;//跳过字符串末尾的两个0

            long mode = OspUtils.getUintFromBuf(unCompressData, index); //当前tab模式
            //字符串的结尾有可能也有一个0，这样的话mode和字符串之间就有3个零
            if(mode!=1&&mode!=2){
                index++;
                mode = OspUtils.getUintFromBuf(unCompressData, index); //当前tab模式
                strByteLength = strByteLength+1;
            }
            index += 4;

            byte strByte[] = new byte[strByteLength];
            NetUtil.memcpy(strByte, unCompressData, 0, tempIndex, strByteLength);

            String tabName = ""; //tab名称
            try {
                tabName = new String(strByte, "Unicode");
            } catch (Exception e) {
                e.printStackTrace();
            }

            print("tabName="+tabName);

            byte zooms[] = new byte[4];
            zooms[3] = unCompressData[index++];
            zooms[2] = unCompressData[index++];
            zooms[1] = unCompressData[index++];
            zooms[0] = unCompressData[index++];

            float zoom = OspUtils.getUFloatFromBuf(zooms, 0); //当前缩放级别

            print("zoom="+zoom);

            long angle = OspUtils.getUintFromBuf(unCompressData, index); //旋转角度
            index += 4;
            print("angle="+angle);

            long isDocOwner = OspUtils.getUintFromBuf(unCompressData, index); //该TAB页属主在服务器上的index
            index += 4;
            print("isDocOwner="+isDocOwner);

            long pageCount = OspUtils.getUintFromBuf(unCompressData, index); //当前Tab子页总数
            index += 4;
            print("pageCount="+pageCount);

            long curpageIndex = OspUtils.getUintFromBuf(unCompressData, index); //当前显示的子页页码 0开始
            index += 4;
            print("curpageIndex="+curpageIndex);

            long wbWidth = OspUtils.getUintFromBuf(unCompressData, index); //当前白板的宽度
            index += 4;
            print("wbWidth="+wbWidth);

            long wbHeight = OspUtils.getUintFromBuf(unCompressData, index); //当前白板的高度
            index += 4;
            print("wbHeight="+wbHeight);

            long ownerIndex = OspUtils.getUintFromBuf(unCompressData, index); //该TAB页属主在服务器上的index
            index += 4;
            print("ownerIndex="+ownerIndex);

            long time = OspUtils.getUintFromBuf(unCompressData, index); //时间
            index += 4;
            print("time="+time);

            long m_bInConvert = OspUtils.getUintFromBuf(unCompressData, index);//是否在进行文档转换
            index += 4;
            print("m_bInConvert="+m_bInConvert);


            Page page = new Page();
            page.setId(tabId);
            page.setName(tabName);
            page.setPageMode((int) mode);
            page.setIsDocOwner((int) isDocOwner);
            //page.setDocPageCount((int) pageCount);
            //page.selectSubPage((int) curpageIndex);
            page.setOwnerIndex((int) ownerIndex);
            page.setM_nTime((int) time);
            page.setM_bInConvert((int) m_bInConvert);
            page.setBackGroundColor(WhiteBoardUtils.curBackground);

            TPLog.printError("subPageCount="+pageCount);
            //子页循环
            for(int p = 0;p<pageCount;p++) {
                long progress = OspUtils.getUintFromBuf(unCompressData, index); //进度
                index += 4;
                long docX = OspUtils.getUintFromBuf(unCompressData, index);//文档显示的X坐标
                index += 4;
                long docY = OspUtils.getUintFromBuf(unCompressData, index);//文档显示的y坐标
                index += 4;

                SubPage subPage = new SubPage();
                subPage.setProgress((int) progress);
                subPage.translate((int) docX, (int) docY);

                long graphCount = OspUtils.getUintFromBuf(unCompressData, index);//标注数量
                index += 4;

                long tempGraphCount = graphCount;
                print("graphCount = "+tempGraphCount);
                while (tempGraphCount > 0) {
                    long type = OspUtils.getUintFromBuf(unCompressData, index);//批注类型
                    byte buffer[] = synchronouseGraphEntity(unCompressData);

                    //NetUtil.displayArray(buffer);

                    if (buffer != null) {
                        if(type!= WhiteBoardUtils.GRAPH_IMAGE) {
                            Graph ge = parseGraphData(buffer, false);
                            if (ge != null) {
                                subPage.addGraph(ge);
                            }
                        }else{
                            Image image = parseSynchronouseImageEntity(buffer, p);
                            image.setSubpageIndex(p);
                            if(image!=null){
                                subPage.setImage(image);
                            }
                        }
                        buffer = null;
                    }

                    tempGraphCount--;
                }


                //在图元添加完成之后必须进行撤销和还原列表的清空
                subPage.clearUndoList();
                subPage.clearRedoList();
                */

                /**----------------unDoSet----------------------

                long undoSize = OspUtils.getUintFromBuf(unCompressData, index);
                index += 4;
                print("undoSize="+undoSize);
                for (int u = 0; u < undoSize; u++) {**/
                    /**
                     * eoAdd = 0,			//添加操作
                     * eoRotate =1,			//旋转
                     * eoClear = 2,			//清屏
                     * eoEarse = 3,			//橡皮擦

                    long operatprType = OspUtils.getUintFromBuf(unCompressData, index);
                    index += 4;

                    print("operatprType="+operatprType);
                    IOperation op = null;
                    if (operatprType == 0) {
                        long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                        index += 4;
                        byte buffer[] = synchronouseGraphEntity(unCompressData);

                        GraphOperation graphOp = new GraphOperation();
                        if (buffer != null) {
                            Graph ge = parseGraphData(buffer, false);
                            if(ge==null)continue;
                            Graph ge2 = subPage.getGraph(ge.getId());
                            if(ge2!=null){
                                graphOp.setGraph(ge2);
                            }else{
                                graphOp.setGraph(ge);
                            }
                            buffer = null;
                        }
                        op = graphOp;
                    } else {//if
                        switch ((int) operatprType) {
                            case 2:
                                long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long clearEntityCount =  OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                ClearScreenOperation cse = new ClearScreenOperation();
                                List<Graph> list = new ArrayList<Graph>();
                                for(int c=0;c<clearEntityCount;c++){
                                    byte buffer[] = synchronouseGraphEntity(unCompressData);
                                    if (buffer != null) {
                                        Graph ge = parseGraphData(buffer, false);
                                        if(ge!=null){
                                            list.add(0,ge);
                                        }
                                    }
                                    buffer = null;
                                }
                                op = cse;
                                break;
                            case 3:

                                break;
                            case 1:
                                m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long m_emRotateDirection  = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long curAngle = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long oldAngle = OspUtils.getUintFromBuf(unCompressData, index);

                                RotateOperation re = new RotateOperation();
                                re.setCurAngle((int)curAngle);
                                re.setOldAngle((int) oldAngle);
                                op = re;
                                break;
                        }
                    }
                    if(op!=null){
                        subPage.addUndo(op);
                    }
                }// for undo
                     */

                /***********************reDo****************************

                long redoSize = OspUtils.getUintFromBuf(unCompressData, index);
                index += 4;
                print("redoSize="+redoSize);
                for (int r = 0; r < redoSize; r++) {
                    long operatprType = OspUtils.getUintFromBuf(unCompressData, index);
                    index += 4;
                    IOperation op = null;
                    if (operatprType != 0) {
                        switch ((int) operatprType) {
                            case 2:
                                long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long clearEntityCount =  OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                ClearScreenOperation cse = new ClearScreenOperation();
                                List<Graph> list = new ArrayList<Graph>();
                                for(int c=0;c<clearEntityCount;c++){
                                    byte buffer[] = synchronouseGraphEntity(unCompressData);
                                    if (buffer != null) {
                                        Graph ge = parseGraphData(buffer, false);
                                        if(ge!=null){
                                            list.add(0,ge);
                                        }
                                    }
                                    buffer = null;
                                }
                                op = cse;
                                break;
                            case 3:

                                break;
                            case 1:
                                m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long m_emRotateDirection  = OspUtils.getUintFromBuf(unCompressData, index);
                                index += 4;
                                long oldAngle = 0;
                                if(m_emRotateDirection==0){
                                    oldAngle = angle + 90;
                                }else{
                                    oldAngle = angle - 90;
                                }
                                RotateOperation re = new RotateOperation();
                                re.setCurAngle((int)angle);
                                re.setOldAngle((int) oldAngle);
                                op = re;
                                break;
                        }
                    } else {*/
                        /**
                         * eoAdd = 0,			//添加操作
                         * eoRotate =1,			//旋转
                         * eoClear = 2,			//清屏
                         * eoEarse = 3,			//橡皮擦

                        long m_bIsInUndo = OspUtils.getUintFromBuf(unCompressData, index);
                        index+=4;
                        long graphtype = OspUtils.getUintFromBuf(unCompressData, index);//图源类型
                        byte buffer[] = synchronouseGraphEntity(unCompressData);

                        if (buffer != null) {
                            Graph ge = parseGraphData(buffer, false);
                            if(ge==null)continue;
                            Graph ge2 =subPage.getGraph(ge.getId());

                            GraphOperation gop = new GraphOperation();
                            if(ge2!=null){
                                gop.setGraph(ge2);
                            }else{
                                gop.setGraph(ge);
                            }
                            op = gop;
                        }
                        buffer = null;
                    }

                    if (op != null) {
                        subPage.addRedo(op);
                    }
                } //redo
                page.addSubPage(subPage);
            } //page
            page.scale(zoom/100f);
            page.rotate((int) angle, false);
            page.selectSubPage((int) curpageIndex+1);
            PageList.add(page);
        } //tab

        long m_nFirstVisibleItemIndex = OspUtils.getUintFromBuf(unCompressData, index);
        index += 4;
        long m_nCurTabPageId = OspUtils.getUintFromBuf(unCompressData, index);
        index += 4;
        print("当前显示的tabId = "+m_nCurTabPageId);
        long m_nTotalDocPage = OspUtils.getUintFromBuf(unCompressData, index);
        index += 4;
        TPLog.printError("m_nTotalDocPage----------------------->"+m_nTotalDocPage);
        long m_nPageIndex = OspUtils.getUintFromBuf(unCompressData, index);
        index += 4;


        WhiteBoardUtils.curPageIndex = (int)m_nTotalDocPage;

        //Log.e("msg","tabEntity.getName ------------------->"+tabEntity.getTabName());
        //tabEntity.getFragment().setSelectPageIndex((int) m_nPageIndex);
        //Log.e("msg", "index=====" + index + ",unCompressData.length=" + unCompressData.length);
        //mWriteBoardManager.displayLog();
        // int count1 = wbManager.getTabEntityCount();
        Map<String ,Object> map = new HashMap<String, Object>();
        map.put("curPageId", m_nCurTabPageId);
        map.put("PageList", PageList);
        sendHandlerMsg(NetHandler.SYNCHRONOUSE, map);
        unCompressData = null;
    }*/

    private byte[] synchronouseGraphEntity(byte unCompressData[]){

        long graphtype = OspUtils.getUintFromBuf(unCompressData, index);//图源类型
        byte buffer[] = null;

        int graphDataLength = 0; //图源信息公用头信息

        switch ((int) graphtype) {
            case WhiteBoardUtils.GRAPH_CIRCLE:
            case WhiteBoardUtils.GRAPH_LINE:
                //数据结构为 = 图源类型（4） + 图形Id（4） + block（4）+ 是否擦除（4） + needrollback（4）+beginx（4）+
                //            beginy(4) + endx（4） + endy（4）+ 线条宽条（4）+color(4) + regionIndex（4）+ nIsRegionNull（4）+regionLength(4)+regionData(n)+nIsRegionNull（4）*6
                graphDataLength += 4 * 12;
                for (int b = 0; b < 7; b++) {
                    long nIsRegionNull = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                    graphDataLength += 4;
                    if (nIsRegionNull == 0) {
                        long regionLength = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                        graphDataLength += 4;
                        graphDataLength += regionLength;
                    }
                }

                // circleDataLength = circleDataLength + (int)(regionLength + 4*7);
                buffer = new byte[graphDataLength];
                NetUtil.memcpy(buffer, unCompressData, 0, index, graphDataLength);
                index = index + graphDataLength;

                break;
            case WhiteBoardUtils.GRAPH_PEN:
            case WhiteBoardUtils.GRAPH_NITEPEN:
            case WhiteBoardUtils.GRAPH_ERASE_AREA:
                graphDataLength = 4 * 5;
                long pointLength = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                graphDataLength += 4; //加上点数的字节数
                graphDataLength += 9 * pointLength;//每个点有9个字节 x(4),y(4),type(1)
                graphDataLength += 4 * 3;//线条宽条 , 颜色，regionIndex
                for (int b = 0; b < 7; b++) {
                    long nIsRegionNull = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                    graphDataLength += 4;
                    if (nIsRegionNull == 0) {
                        long regionLength = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                        graphDataLength += 4;
                        graphDataLength += regionLength;
                    }
                }

                buffer = new byte[graphDataLength];
                NetUtil.memcpy(buffer, unCompressData, 0, index, graphDataLength);

                index = index + graphDataLength;
                break;
            case WhiteBoardUtils.GRAPH_ERASE:
                graphDataLength = 4 * 7;
                pointLength = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                graphDataLength += 4; //加上点数的字节数
                graphDataLength += 9 * pointLength;//每个点有9个字节 x(4),y(4),type(1)
                graphDataLength += 4 * 2;//颜色，regionIndex
                for (int b = 0; b < 7; b++) {
                    long nIsRegionNull = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                    graphDataLength += 4;
                    if (nIsRegionNull == 0) {
                        long regionLength = OspUtils.getUintFromBuf(unCompressData, index + graphDataLength);
                        graphDataLength += 4;
                        graphDataLength += regionLength;
                    }
                }
                buffer = new byte[graphDataLength];
                NetUtil.memcpy(buffer, unCompressData, 0, index, graphDataLength);

                index = index + graphDataLength;
                break;
            case WhiteBoardUtils.GRAPH_IMAGE:

//                graphDataLength = 4*10;
//
//                int strEndIndex = index;
//                for (int a = (index+graphDataLength); a < unCompressData.length; a++) {
//                    if (unCompressData[a] == 0) {
//
//                        if (a - strEndIndex == 1) {
//                            break;
//                        }
//
//                        strEndIndex = a;
//                    }
//                }
//
//                int strByteLength = strEndIndex -1- (index+graphDataLength);
//
//                if (strByteLength < 0) {
//                    return null;
//                }
//
//                graphDataLength = strByteLength + graphDataLength +2;
//
//                long is = OspUtils.getUintFromBuf(unCompressData,index+graphDataLength);
//
//                if(is==0){
//                    graphDataLength +=4;
//                }else{ //这里暂时先写成这样，这个参数目前还没有出现，也不知道是什么样子
//                    graphDataLength +=8;
//                }
//                graphDataLength = graphDataLength +1;

                graphDataLength = 60+4*9;
                buffer = new byte[graphDataLength];

                NetUtil.memcpy(buffer, unCompressData, 0, index, graphDataLength);
                index = index + graphDataLength;
                break;
        }  //switch

        return buffer;
    }

    /**
     * 解析图元数据
     * @param data
     * @param hasTabId
     * @return
     */
    private Graph parseGraphData(byte data[],boolean hasTabId){
        if(data==null){
            return null;
        }
        byte ucGraphData[] = null;

        if(hasTabId) {
            long contentLength = OspUtils.getUintFromBuf(data, 0); //4
//         byte even = data[5]; //1
//         long userIndex = NetUtil.getUintFromBuf(data,6);//4

            int extraLength = 4 + 1 + 4 + 1;

            int graphDataLength = (int) (contentLength - 4 * 2 - 1 - 1);
            byte graphData[] = new byte[graphDataLength];

            for (int i = 9; i < contentLength - 1; i++) {
                graphData[i - 9] = data[i];
            }

            ucGraphData = NetUtil.UnCompressBuffer(graphData);
            if (ucGraphData == null) {
                return null;
            }
        }else{
            ucGraphData = data;
        }

        long type = OspUtils.getUintFromBuf(ucGraphData, 0);

        Graph graph = null;

        switch((int)type){
            case WhiteBoardUtils.GRAPH_PEN:
                graph =  parsePenData(ucGraphData,0, hasTabId);
                break;
            case WhiteBoardUtils.GRAPH_NITEPEN:
                break;
            case WhiteBoardUtils.GRAPH_LINE:
                break;
            case WhiteBoardUtils.GRAPH_CIRCLE:
                break;
            case WhiteBoardUtils.GRAPH_IMAGE:
                break;
            case WhiteBoardUtils.GRAPH_ERASE:   //该图元目前只有Android 对通时使用，PC端暂不使用
                graph =  parseEraseData2(ucGraphData);  //2017.07.06添加
                break;
            case WhiteBoardUtils.GRAPH_ERASE_AREA: //该图元目前只有Android 对通时使用，PC端暂不使用
                graph =  parsePenData(ucGraphData,2, hasTabId);
                break;
        }
        return graph;
    }


    private Graph parseEraseData2(byte[] graphdata){
//        print("parseEraseData2------------------->");
//        NetUtil.displayArray(graphdata);

        Graph graph = new Erase();
        int index = 0;
        long type = OspUtils.getUintFromBuf(graphdata, index);
        print("parseEraseData2---->type="+type);
        index = index +4;
        long tuxingId = OspUtils.getUintFromBuf(graphdata,index);//C++中使用的，每个图形的唯一标识
        print("parseEraseData2---->tuxingId="+tuxingId);
        index = index +4;
        long block = OspUtils.getUintFromBuf(graphdata,index);//?? 取值 0/1
        print("parseEraseData2---->block="+block);
        index = index +4;
        long regionhaschanged = OspUtils.getUintFromBuf(graphdata,index);//是否被擦除 取值0/1
        print("parseEraseData2---->regionhaschanged="+regionhaschanged);
        index = index +4;
        long needrollback = OspUtils.getUintFromBuf(graphdata,index);//?? 取值0/1
        print("parseEraseData2---->needrollback="+needrollback);
        index = index +4;
        long width = OspUtils.getUintFromBuf(graphdata,index);
        print("parseEraseData2---->width="+width);
        index = index +4;
        long height = OspUtils.getUintFromBuf(graphdata,index);
        print("parseEraseData2---->height="+height);
        index = index +4;
        long pointCount = OspUtils.getUintFromBuf(graphdata, index);
        print("parseEraseData2---->pointCount="+pointCount);
        index = index +4;

        graph.setStrokeWidth(1);
        ((Erase)graph).setEraseWidth((int)width);
        ((Erase)graph).setEraseHeight((int)height);

        for(int i=0;i<pointCount;i++){
            float pointX = OspUtils.getUFloatFromBuf(graphdata, index);
            index +=4;
            float pointY = OspUtils.getUFloatFromBuf(graphdata, index);
            index +=4;
            byte pType = graphdata[index];
            index +=1;
            graph.addPoint(pointX,pointY);
        }
        long color = OspUtils.getUintFromBuf(graphdata,index);
        index = index +4;
        long regionIndex = OspUtils.getUintFromBuf(graphdata,index);
        index = index +4;
        //下面的在android里面暂时无效
        long nIsRegionNull = OspUtils.getUintFromBuf(graphdata,index); //判断Region是否为空 1：标识该区域为空，不用序列化，0：应该序列化该区域
        index = index +4;
        long regionLength = OspUtils.getUintFromBuf(graphdata,index); //Region数据长度  Android中无用
        index = index +4;

        graph.setId((int) tuxingId);
        graph.setColor((int)color);

        return graph;
    }

    //解析铅笔数据
    private  Graph parsePenData(byte[] graphdata,int mode,boolean hasTabId) {
        //Utils.displayArray(graphdata);

        Graph graph = null;
        if(mode==0){
            graph = new Pen();
        }else if(mode==1){
            graph = new Erase();
        }else if(mode == 2){
            AreaErase ae = new AreaErase();
            ae.commitErase();
            graph = ae;
        }

        if(graph==null){
            return null;
        }

        int index = 0;
        long type = OspUtils.getUintFromBuf(graphdata, index);
        index = index +4;
        long tabId = 0;
        if(hasTabId) {
            tabId = OspUtils.getUintFromBuf(graphdata, index);
            index = index +4;
            long pageId = OspUtils.getUintFromBuf(graphdata, index);
            index = index +4;
        }
        long tuxingId = OspUtils.getUintFromBuf(graphdata,index);//C++中使用的，每个图形的唯一标识
        index = index +4;
        long block = OspUtils.getUintFromBuf(graphdata,index);//?? 取值 0/1
        index = index +4;
        long regionhaschanged = OspUtils.getUintFromBuf(graphdata,index);//是否被擦除 取值0/1
        index = index +4;
        long needrollback = OspUtils.getUintFromBuf(graphdata,index);//?? 取值0/1
        index = index +4;
        long pointCount = OspUtils.getUintFromBuf(graphdata, index);
        index = index +4;

        for(int i=0;i<pointCount;i++){
            float pointX = OspUtils.getUFloatFromBuf(graphdata, index);
            index +=4;
            float pointY = OspUtils.getUFloatFromBuf(graphdata, index);
            index +=4;
            byte pType = graphdata[index];
            index +=1;
            graph.addPoint(pointX,pointY);
        }

        long linewidth = OspUtils.getUintFromBuf(graphdata,index);
        index = index +4;
        long color = OspUtils.getUintFromBuf(graphdata,index);
        index = index +4;
        long regionIndex = OspUtils.getUintFromBuf(graphdata,index);
        index = index +4;
        //下面的在android里面暂时无效
        long nIsRegionNull = OspUtils.getUintFromBuf(graphdata,index); //判断Region是否为空 1：标识该区域为空，不用序列化，0：应该序列化该区域
        index = index +4;
        long regionLength = OspUtils.getUintFromBuf(graphdata,index); //Region数据长度  Android中无用
        index = index +4;
        graph.setTabId(tabId);
        graph.setId((int) tuxingId);
        graph.setStrokeWidth(linewidth);
        graph.setColor((int)color);

        return graph;
    }


    /**
     * 解析入会同步时的图片数据
     * @param ucData
     * @param curPagePos
     */
    private  ImageGraph parseSynchronouseImageEntity(byte ucData[],long curPagePos){
        int index = 0;
        long type = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long graphId = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long x  = OspUtils.getUintFromBuf(ucData,index); //图片显示的X坐标
        index+=4;
        long y  = OspUtils.getUintFromBuf(ucData,index); //图片显示y坐标
        index+=4;
        long imageWidth  = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long imageHeight  = OspUtils.getUintFromBuf(ucData, index);
        index+=4;

        float matrixValue[] = new float[9];

        for(int i = 0;i<matrixValue.length;i++){
            matrixValue[i] = OspUtils.getUFloatFromBuf(ucData,index);
            index+=4;
        }

//        float drawMatrixValue[] = new float[9];
//
//        for(int i = 0;i<drawMatrixValue.length;i++){
//            drawMatrixValue[i] = OspUtils.getUFloatFromBuf(ucData,index);
//            index+=4;
//        }

        ImageGraph ig = new ImageGraph();
        ig.setId((int)graphId);
        ig.setX(x);
        ig.setY(y);
        ig.setWidth((int)imageWidth);
        ig.setHeight((int)imageHeight);
        ig.setMatrixValues(matrixValue);
        return ig;
    }


    //处理服务器发送过来的图源信息
    private void parseDrawEntityData(byte data[]) {
        if (data == null) {
            return;
        }

        long pageIndex = OspUtils.getUintFromBuf(data, 8);
        long graphId = OspUtils.getUintFromBuf(data, 12);

        //校验等到项目正式成型后再添加
        // boolean hasEntity = mWriteBoardManager.getCurSelectTabEntity().getFragment().containsGraphEntity(pageIndex, graphId);

        //final Graph graph = Graph.createGraph(data, true);

        long contentLength = OspUtils.getUintFromBuf(data, 0); //4
        int extraLength = 4 + 1 + 4 + 1;

        int graphDataLength = (int) (contentLength - 4 * 2 - 1 - 1);
        byte graphData[] = new byte[graphDataLength];

        for (int i = 9; i < contentLength - 1; i++) {
            graphData[i - 9] = data[i];
        }

        byte  ucGraphData[] = NetUtil.UnCompressBuffer(graphData);

        if (ucGraphData == null) {
            return;
        }

        long type = OspUtils.getUintFromBuf(ucGraphData, 0);

        Graph graph = null;

        switch((int)type){
            case WhiteBoardUtils.GRAPH_PEN:
                graph = parsePenData(ucGraphData,0,true);
                break;
        }
        //    if (!hasEntity) {//如果该图源不存在就添加到集合
        //将图元的字节数组直接传递到白板模块
        if(graph!=null){
            sendHandlerMsg(NetHandler.GRAPH_UPDATE, graph);
        }
    }

    /**
     * 解析缩放数据
     *
     * @param data
     */
    private void parseZoomData(byte data[]){
        // 消息结构  = 消息体长度（4） + 消息号(1) + 后续消息长度 + tabId + 缩放级别（float） + 消息结束位
        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        byte event = data[index++];
        long length = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;

        //由于缩放级别数据与之前所有的数据的高低位是相反的因此需要先进行排序
        byte zoomData[] = new byte[4];
        zoomData[3] = data[index++];
        zoomData[2] = data[index++];
        zoomData[1] = data[index++];
        zoomData[0] = data[index++];

        float zoom = OspUtils.getUFloatFromBuf(zoomData, 0);

//        TPLog.printKeyStatus("接收到缩放信息：scale="+zoom);
        sendHandlerMsg(NetHandler.SCALE_CHANGED, zoom);
    }


    private void parseGestureZoomData(byte data[]){
        // 消息结构  = 消息体长度（4） + 消息号(1) + 后续消息长度 + tabId + 缩放级别（float） + 消息结束位
        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        byte event = data[index++];
        long length = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;

        //由于缩放级别数据与之前所有的数据的高低位是相反的因此需要先进行排序
        byte zoomData[] = new byte[4];
        zoomData[3] = data[index++];
        zoomData[2] = data[index++];
        zoomData[1] = data[index++];
        zoomData[0] = data[index++];
        long focusX = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long focusY = OspUtils.getUintFromBuf(data, index);
        index += 4;

//        TPLog.printKeyStatus("手势消息结束位："+data[index]);

        float zoomFactor = OspUtils.getUFloatFromBuf(zoomData, 0);

//        TPLog.printKeyStatus("接收到手势缩放信息：scale="+zoomFactor/100f);
        GestureScaleEntity gse = new GestureScaleEntity();
        gse.setScaleFactor(zoomFactor/100f);
        gse.setFocusX((int)focusX);
        gse.setFocusY((int)focusY);

        sendHandlerMsg(NetHandler.SCALE_CHANGED_FROM_GESTURE, gse);
    }


    //解析服务器发送来的滚动事件消息
    //结构：消息长度 + 消息类型 + 后续消息长度 + tabID + curPageIndex + 水平滚定偏移量 + 垂直滚动偏移量 +消息结束位
    private void parseScrollData(byte data[]) {

        //NetUtil.displayArray(data);

        if (data == null) {
            return;
        }
        // Utils.displayArray(data);

        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long event = data[index++];
        long lastMsglength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long curPageIndex = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long x = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long y = OspUtils.getUintFromBuf(data, index);

//        TPLog.printKeyStatus("接收到的偏移：ox="+x+",oy="+y);

        Point scrollPoint = new Point();
        scrollPoint.set((int) x, (int) y);

        sendHandlerMsg(NetHandler.SCROLL_CHANGED, scrollPoint);
    }


    /**
     * 创建选项卡数据解析
     */
    public void parseNewTab(byte data[]) {

        if (data == null) {
            return;
        }

        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        byte event = data[index++];
        long lastContentLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long subPageCount = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long totalDocPage = OspUtils.getUintFromBuf(data, index); //创建的选项卡总数
        index += 4;

        byte byteTabName[] = new byte[128];

        index += NetUtil.memcpy(byteTabName, data, 0, index, byteTabName.length);

        long tabMode = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long canvasWidth = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long canvasHeight = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long ownerIndex = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long time = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long inConvert = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long isAnoymous = OspUtils.getUintFromBuf(data, index);


        String name = null;

        if (isAnoymous == 0) { //1是匿名的，0不是匿名
            int last = 0;
            for (int i = 0; i < byteTabName.length; i++) {
                if (byteTabName[i] != 0) { //取出最后一个非0位
                    last = i;
                }
            }
            byte byteTabName2[] = new byte[last + 1];
            NetUtil.memcpy(byteTabName2, byteTabName, 0, 0, byteTabName2.length);
            try {
//                name = new String(byteTabName2,"Unicode");
                name = new String(byteTabName2, "GBK");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//TAB数据后续处理，这里暂时先注释掉
//        mWriteBoardManager.setTabNameIndex((int) totalDocPage);
//        TabEntity te = mWriteBoardManager.makeNewTab();
//        te.setM_bInConvert((int) inConvert);
//        te.setM_nTime((int) time);
//        te.setOwnerIndex((int) ownerIndex);
//        te.setTabId(tabId);
//        te.setWbMode((int) tabMode);
//        if (name != null) {
//            te.setTabName(name);
//        }
        //改版后宽度高度，已经没有意义了
//        te.getFragment().setWbWidth((int) canvasWidth);
//        te.getFragment().setWbHeight((int) canvasHeight);
        //

        Page page = new Page();
        if (name != null) {
            page.setName(name);
        }
        page.setId(tabId);
        page.setM_bInConvert((int) inConvert);
        page.setM_nTime((int) time);
        page.setOwnerIndex((int) ownerIndex);
        page.setPageMode((int)tabMode);
        page.setBackGroundColor(WhiteBoardUtils.curBackground);
        if(name!=null){
            page.setName(name);
            page.setAnoymous(false);
        }else{
            page.setName(WhiteBoardUtils.getPageName());
            page.setAnoymous(true);
        }

        SubPage sPage = new SubPage();
        page.addSubPage(sPage);

        sendHandlerMsg(NetHandler.CREATE_TAB, page);
    }

    /**
     * 解析删除tab消息
     * @param data
     */
    private void parseDeleteTabMsg(byte data[]){
        if(data==null){
            return;
        }
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long displayTabId = OspUtils.getUintFromBuf(data, index);
        index+=4;
        String msg = tabId + "_" + displayTabId;
        sendHandlerMsg(NetHandler.DEL_TAB, msg);
    }

    private void parseDeleteAllTabMsg(byte data[]){
        if(data==null){
            return;
        }
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        sendHandlerMsg(NetHandler.DEL_ALL_TAB, tabId);
    }

    /**
     * 解析翻页数据
     *
     * @param data
     */
    private void parseChangePage(byte data[]) {
        if (data == null) {
            return;
        }

        int index = 0;

        long dataLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        byte event = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long pageIndex = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long isPageOrTab = OspUtils.getUintFromBuf(data, index);

        Map<String, Long> map = new HashMap<String, Long>();
        map.put("tabId", tabId);
        map.put("pageIndex", pageIndex);
        //if (selectTabId != tabId) { //如果tabId和当前显示的不一致 就切换tab4
        //sendHandlerMsg(NetHandler.NOTIFY_TABLE, tabId);
        //} else {  //tabId一致就翻页
        sendHandlerMsg(NetHandler.CHANGE_PAGE, map);
        //}
    }

    //处理服务器发送的请求翻页消息
    private void parseServerReqChangePage(byte data[]) {
        if (data == null) {
            return;
        }

        //NetUtil.displayArray(data);

        int index = 0;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long imageIndex = OspUtils.getUintFromBuf(data, index);

        //int pageIndex = mWriteBoardManager.getCurSelectTabEntity().getFragment().getCurrentPageIndex();

        Map<String, Long> map = new HashMap<String, Long>();
        map.put("tabId", tabId);
        map.put("pageIndex", imageIndex);
        sendHandlerMsg(NetHandler.SR_CHANGE_PAGE, map);

    }

    /**
     * 解析橡皮擦数据
     *
     * @param data
     */
    private void parseEraseData(byte data[]) {
        int index = 4 + 1 + 4;
        long erasureMode = OspUtils.getUintFromBuf(data, index); //擦除模式 0连续擦除，1区域擦除
        Graph ge = null;
        if (erasureMode == 0) {
            ge = parseNormalEraseData(data);
        } else if (erasureMode == 1) {
            ge = parseAreaEraseData(data);
        }
        if (ge != null) {
            sendHandlerMsg(NetHandler.GRAPH_UPDATE, ge);
        }
    }

    //解析普通橡皮擦数据
    private Graph parseNormalEraseData(byte graphdata[]){
//        TPLog.printError("parseEraseData------------------------------》");
//        NetUtil.displayArray(graphdata);

        Erase mErase = new Erase();
        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(graphdata, index);//消息长度 含结束位
        print("contentLength="+contentLength);
        index += 4;
        long msgEvent = graphdata[index]; //消息事件
        print("msgEvent="+msgEvent);
        index += 1;
        long contentLength2 = OspUtils.getUintFromBuf(graphdata,index);//后续消息体长度 不含结束位
        print("contentLength2="+contentLength2);
        index += 4;
        long erasureMode = OspUtils.getUintFromBuf(graphdata,index); //擦除模式 0连续擦除，1区域擦除
        print("erasureMode="+erasureMode);
        index += 4;
        long tabId = OspUtils.getUintFromBuf(graphdata,index);
        print("tabId="+tabId);
        index += 4;
        index += 16;//跳过传入的起点和终点坐标

        long contentLength3 = OspUtils.getUintFromBuf(graphdata, index);//消息长度 不含结束位
        print("contentLength3="+contentLength3);
        index += 4;
        long width =  OspUtils.getUintFromBuf(graphdata, index);
        print("width="+width);
        index += 4;
        long height =  OspUtils.getUintFromBuf(graphdata, index);//2017.07.06新增
        print("height="+height);
        index += 4;
        long pointCount =  OspUtils.getUintFromBuf(graphdata, index);//擦除点的个数
        print("pointCount="+pointCount);
        index += 4;
        mErase.setStrokeWidth(1);
        mErase.setEraseWidth((int) width);
        mErase.setEraseHeight((int)height);

        for(int i=0;i<pointCount;i++){
            long x = OspUtils.getUintFromBuf(graphdata, index);
            index += 4;
            long y = OspUtils.getUintFromBuf(graphdata, index);
            index += 4;
            mErase.addPoint(x, y);
        }


        return mErase;
    }

    //解析区域擦除数据
    private Graph parseAreaEraseData(byte graphdata[]){
        AreaErase cae = new AreaErase();

        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(graphdata, index);//消息长度 含结束位
        index += 4;
        long msgEvent = graphdata[index]; //消息事件
        index += 1;
        long contentLength2 = OspUtils.getUintFromBuf(graphdata,index);//后续消息体长度 不含结束位
        index += 4;
        long erasureMode = OspUtils.getUintFromBuf(graphdata,index); //擦除模式 0连续擦除，1区域擦除
        index += 4;
        long tabId = OspUtils.getUintFromBuf(graphdata,index);
        index += 4;
        long begionX = OspUtils.getUintFromBuf(graphdata,index);
        index += 4;
        long begionY = OspUtils.getUintFromBuf(graphdata,index);
        index += 4;
        long endX = OspUtils.getUintFromBuf(graphdata,index);
        index += 4;
        long endY = OspUtils.getUintFromBuf(graphdata,index);

        cae.addPoint(begionX, begionY);
        cae.addPoint(endX, endY);
        cae.commitErase();
        return cae;
    }

    private void parseRedoMsg(byte data[]){
        if(data == null){
            return;
        }

        int index = 0;
        long msgLen = OspUtils.getUintFromBuf(data, index);
        index+=4;
        byte event = data[index++];
        long contentLen = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long pageIndex = OspUtils.getUintFromBuf(data, index);
        index+=4;

        Map<String, Long> map = new HashMap<String, Long>();
        map.put("tabId", tabId);
        map.put("pageIndex", pageIndex);
        sendHandlerMsg(NetHandler.REDO, map);

    }

    /**
     * 解析撤销数据
     * @param data
     */
    private void parseUndoMsg(byte data[]){
        if(data == null){
            return;
        }

        int index = 0;
        long msgLen = OspUtils.getUintFromBuf(data, index);
        index+=4;
        byte event = data[index++];
        long contentLen = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long pageIndex = OspUtils.getUintFromBuf(data, index);
        index+=4;

        Map<String, Long> map = new HashMap<String, Long>();
        map.put("tabId", tabId);
        map.put("pageIndex", pageIndex);
        sendHandlerMsg(NetHandler.UNDO, map);
    }

    /**
     * 解析分段数据  拆包后的数据
     *
     * @param data
     */
    private void parseSegmentData(byte data[]) {
        if (data == null) {
            return;
        }

//        NetUtil.displayArray(data);

        int index = 0;

        long length1 = OspUtils.getUintFromBuf(data, index);
        index += 4;
        byte event = data[index++];

        long length2 = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long curSegmentId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long lastSegmentId = OspUtils.getUintFromBuf(data, index);
        index += 4;

        long userId = OspUtils.getUintFromBuf(data, index);
        index += 4;
        long length3 = OspUtils.getUintFromBuf(data, index);
        index += 4;

        if (lastSegmentId == 0) {  //上一包ID为0的话就是第一包
            firstSegmentId = curSegmentId;
            long length4 = OspUtils.getUintFromBuf(data, index);
            regroupSegment = new byte[(int) length4 + 4];
            segmentMaxSize = length3;
            curRegroupLength = 0;
        }

        byte buffer[] = new byte[(int) length3];
        NetUtil.memcpy(buffer, data, 0, index, buffer.length);

        if (curSegmentId == -1) { //最后一包数据
            long lastSegmentPos = regroupSegment.length - length3;
            NetUtil.memcpy(regroupSegment, buffer, (int) lastSegmentPos);
//            NetUtil.displayArray(regroupSegment);
        } else {
            long pos = curSegmentId - this.firstSegmentId;
            pos = pos * segmentMaxSize;
            NetUtil.memcpy(regroupSegment, buffer, (int) pos);
        }

        curRegroupLength = curRegroupLength + length3;

//        TPLog.printError("length3========"+length3+",,,,curRegroupLength--------"+curRegroupLength+"    :    regroupSegment.length="+regroupSegment.length);
        data = null;
        if (curRegroupLength == regroupSegment.length) {
            handleSynMsg(regroupSegment);
        }
    }


    private void parseRotateMsg(byte data[]){
        //NetUtil.displayArray(data);

        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data, index);
        index +=4;
        byte cmd = data[index++];
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long tabId = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long angle = OspUtils.getUintFromBuf(data, index);
        index+=4;
        long isFinish = OspUtils.getUintFromBuf(data, index);
        index+=4;

        Map<String,Long> map = new HashMap<String, Long>();
        map.put("angle", angle);
        map.put("isFinish", isFinish);

        sendHandlerMsg(NetHandler.ROTATE_CHANGED, map);
    }

    /**
     * 解析服务器返回的已经存在的文件消息
     */
    private void parseExistFileNotifyData(byte data[]){
        if (data == null) {
            return;
        }

        //Log.e("msg","同步已经存在的文件列表------------------》");
        //NetUtil.displayArray(data);

        int index = 0;
        long length = OspUtils.getUintFromBuf(data,index);
        index+=4;
        byte event = data[index++];

        while(index<data.length-1){
            long imageId = OspUtils.getUintFromBuf(data,index);
            index+=4;
            SynFileManager.getInstance().addServerExistFile(imageId);
        }
    }


    /********************************************************************/

    /**
     * 解析服务发送过来的会议列表
     * @param msg 会议列表字符串
     * @return 会议列表数组
     */
    public  String analysisConfList(String msg){
        if(msg==null)return null;

        //去掉所有的空字符
        msg = msg.replaceAll(String.valueOf(((char)0)),"");

        //通过换行符号分割出会议名称  每个会议名称前面会带有0
        String baseConfList[] = msg.split("\\n");

        String meetingName = null;
        if(baseConfList!=null&&baseConfList.length>=1){
            for(String conf : baseConfList){
                if(conf!=null&&conf.length()>0){
                    meetingName = conf.substring(1,conf.length());
                    break;
                }
            }
        }
//        TPLog.printError("解析到的会议名称->"+meetingName);

        return meetingName;
    };

    //发送消息给UI线程
    private  void sendHandlerMsg(int what,Object obj){
        if(mHandler!=null) {
            mHandler.sendMessage(what, obj);
        }
    }

    private void print(String msg){
        TPLog.printKeyStatus(msg);
    }
}
