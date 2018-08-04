package com.kedacom.touchdata.net;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Matrix;
import android.util.Log;

import com.kedacom.osp.Osp;
import com.kedacom.osp.OspUtils;
import com.kedacom.touchdata.net.utils.Command;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.SelectGraph;
import com.kedacom.touchdata.whiteboard.op.ClearScreenOperation;
import com.kedacom.touchdata.whiteboard.op.GraphOperation;
import com.kedacom.touchdata.whiteboard.op.IOperation;
import com.kedacom.touchdata.whiteboard.op.RotateOperation;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

public class SendHelper {

    private static SendHelper mSendHelper;

    private Sender mSender;

    private boolean sendEnable;

    private SendHelper(){

    }

    public synchronized static SendHelper getInstance(){
        if(mSendHelper==null){
            mSendHelper = new SendHelper();
        }
        return mSendHelper;
    }

    public void init(Sender sender){
        mSender = sender;
    }

    public void sendEnable(boolean boo){
        sendEnable = boo;
    }

    /**
     * 建立连接后发送的第一条消息  相当于登录
     */
    public void loginServer(){
        OspUtils.DST_CURRENT_INSID = OspUtils.DST_INSID;

        byte inst[] = OspUtils.uintToByte(2);

        //创建消息体
        byte msg[] ={
                0,0,0,1,49,46,49,46,49,46,49,46,48,46,50,48,49,54,48,49,49,57,
        };

        msg[0] = inst[0];
        msg[1] = inst[1];
        msg[2] = inst[2];
        msg[3] = inst[3];

        byte devType[] = null;

        if(VersionUtils.isImix()){
            devType = OspUtils.uintToByte(1);
        }else{
            devType = OspUtils.uintToByte(0);
        }

        TPLog.printError("devType：");
        NetUtil.displayArray(devType);

        byte content[] = new byte[68+4];

        int index = 0;

        for(int i=0;i<content.length;i++){
            if(i<msg.length){
                content[index++] = msg[i];
            }
        }

            content[68] = devType[0];
            content[69] = devType[1];
            content[70] = devType[2];
            content[71] = devType[3];

        TPLog.printError("登录数据：");
        NetUtil.displayArray(content);

        mSender.sendMsg(content,NetUtil.EVEN_CONNECT_REQ);
    }


    /**
     * 加入会议 会议消息体 = 会议名称长度 + 会议名称 + 密码长度 + 密码 + 0x0D
     * @param meetingName 会议名称
     * @param password 会议密码
     */
    public void createMeeting(String meetingName,String password){
        if(!sendEnable){
            return;
        }
        if(meetingName==null||meetingName.equals("")) return;

        char even = 0;

        even =NetUtil.EVEN_CREATE_CONFERENCE_REQ;

        byte byteMeetingName[] = meetingName.getBytes();
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

//        Utils.displayArray(data);

       mSender.sendMsg(data, even);
    }


    /**
     * 同步图元数据
     * @param graph 需要同步的图元
     */
    public void sendGraphMsg(Graph graph,int tabId, int pageIndex){
        TPLog.printError("SyncGraph","Sync Graph begin....");
        TPLog.printError("SyncGraph","Sync Graph tabId = "+tabId);
//        TPLog.printKeyStatus("-------------->sendGraphMsg");
//        TPLog.printKeyStatus("sendEnable="+sendEnable);
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(graph==null){
          //  TPLog.printKeyStatus("graph=null");
            return;
        }else{
           // TPLog.printKeyStatus("graph!=null");
        }

        if(graph.getGraphType() == WhiteBoardUtils.GRAPH_PEN){ //铅笔
            sendPenGraphMsg(graph,tabId, pageIndex);
        }else if(graph.getGraphType() == WhiteBoardUtils.GRAPH_ERASE){  //橡皮擦
            sendEraseMsg(graph,tabId, pageIndex);
        }else if(graph.getGraphType() == WhiteBoardUtils.GRAPH_ERASE_AREA){ //选择型橡皮擦
            sendEraseAreaMsg(graph,tabId, pageIndex);
        }

        TPLog.printError("SyncGraph","Sync Graph end....");
    }

    //发送铅笔数据
    public  void sendPenGraphMsg(Graph graph,int tabId, int pageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }

        TPLog.printError("SyncGraph","Sync Pen Graph begin....");

        int params[] = {
                graph.getGraphType(),tabId,pageIndex,
                graph.getId(), 0, 1, 0, graph.getPoints().size()
        };

        int paramsEnd[] = {
                (int)graph.getStrokeWidth(),graph.getColor(), 0, 1, 1, 1, 1, 1, 1, 1
        };

        int graphdataLength = params.length*4+graph.getPoints().size()*9 +paramsEnd.length*4;

        byte graphdata[] = new byte[graphdataLength];

        int index = 0;

        for(int i=0;i<params.length;i++){
            index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(params[i]), index);
        }

        int pointCount = graph.getPoints().size();
        TPLog.printError("SyncGraph","Pen Graph Point Count "+pointCount);

        TPLog.printError("SyncGraph","Pen Graph points to bytes begin...");
        long beginTime = System.currentTimeMillis();
        long endTime = 0;

//        for(int i=0;i<pointCount;i++){
////            long beginTime = System.currentTimeMillis();
////            long endTime = 0;
//            Point point = graph.getPoints().get(i);
//            index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.x), index);
//            index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.y), index);
//            if(i==0) {
//                graphdata[index++] = 0;
//            }else{
//                graphdata[index++] = 1;
//            }
////            endTime = System.currentTimeMillis();
////            TPLog.printError("SyncGraph-itemTime","time-------------------> "+(endTime - beginTime));
//        }
        ArrayList<Point> points = graph.getPoints();
        int l = 0;
        for(Point point:points){
//            long beginTime = System.currentTimeMillis();
//            long endTime = 0;
//            Point point = graph.getPoints().get(i);
            index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.x), index);
            index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.y), index);
            if(l==0) {
                graphdata[index++] = 0;
            }else{
                graphdata[index++] = 1;
            }
            l++;
//            endTime = System.currentTimeMillis();
//            TPLog.printError("SyncGraph-itemTime","time-------------------> "+(endTime - beginTime));
        }
        endTime = System.currentTimeMillis();
        TPLog.printError("SyncGraph","Pen Graph points to bytes end,elapsed time "+(endTime - beginTime));

        for(int i=0;i<paramsEnd.length;i++){
            index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(paramsEnd[i]), index);
        }

        TPLog.printError("SyncGraph","Sync Pen Graph data length "+graphdata.length);

        if(mSender!=null){
            mSender.sendGraphMsg(graphdata);
        }

        TPLog.printError("SyncGraph","Sync Pen Graph end....");
    }

    //发送普通擦除数据
    public void sendEraseMsg(Graph graph,int tabId, int pageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        TPLog.printError("SyncGraph","Sync Erase Graph begin....");
        ArrayList<Point> points = graph.getPoints();
        //最内层擦除数据长度
        //线条宽度 + 点的个数 + 所有的点
        int length1 = 4 + 4 + points.size()*8; //
        //擦除总数据长度 含擦除模式等
        //擦除模式+tabId+起始和终点坐标+擦除数据长度 + 擦除数据
        int length2 = 4+4+2*8+4+length1;
        //总数据长度 = 消息号 + 擦除数据总数 + 消息结束位
        int contentLength = 1+4+length2 +1;

        int totalLength = 4 + contentLength +4;

        Point begion = points.get(0); //起点

        Point end = points.get(points.size() - 1); //终点

        int width = ((Erase)graph).getEraseWidth();
        int height = ((Erase)graph).getEraseHeight();

        int params[] = {
                length2,0, tabId,
                begion.x,begion.y,end.x,end.y,length1, width,height,points.size()
        };

        byte data[] = new byte[totalLength];

        int index = 0;
        index+=NetUtil.memcpy(data,OspUtils.uintToByte(contentLength),index);
        data[index++] = Command.CC_ERASE;

        for(int i:params){
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(i),index);
        }

        for(Point p:points){
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(p.x),index);
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(p.y),index);
        }

        data[index++] = Command.MSG_TERM;

        if(index!=contentLength){
            Log.d("msg","ClearEntity----error---------》index="+index+",totalLength="+totalLength);
        }

        //  NetUtil.displayArray(data);

        if(mSender!=null){
            mSender.sendMsg(data, NetUtil.EVEN_SYNCHRONOUS_REQ);
        }
        TPLog.printError("SyncGraph","Sync Erase Graph end....");
    }

    //发送擦除数据
    public void sendEraseAreaMsg(Graph graph,int tabId, int pageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        ArrayList<Point> points = graph.getPoints();
        if(points.size()!=2){
            return;
        }
        //擦除总数据长度 含擦除模式等
        //擦除模式+tabId+起始和终点坐标+擦除数据长度 + 擦除数据
        int length = 4+4+2*8;
        //总数据长度 = 消息号 + 擦除数据总数 + 消息结束位
        int contentLength = 1+4+length +1;

        int totalLength = 4 + contentLength;

        Point begion = points.get(0); //起点

        Point end = points.get(1); //终点

        int params[] = {
                length,1, (int) tabId,
                begion.x,begion.y,end.x,end.y
        };

        byte data[] = new byte[totalLength];

        int index = 0;

        index+=NetUtil.memcpy(data,OspUtils.uintToByte(contentLength),index);
        data[index++] = Command.CC_ERASE;


        for(int i:params){
            index+= NetUtil.memcpy(data, OspUtils.uintToByte(i), index);
        }

        data[index++] = Command.MSG_TERM;

        if(mSender!=null){
            mSender.sendMsg(data, NetUtil.EVEN_SYNCHRONOUS_REQ);
        }
    }

    /**
     * 发送屏幕滚动消息
     * @param ox
     * @param oy
     * @param tabId
     * @param pageIndex
     */
    public void sendScrollMsg(float ox,float oy,long tabId,long pageIndex,boolean isFinish){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null)
            mSender.sendScrollChangedMsg(tabId, pageIndex, (int)ox, (int)oy,isFinish);

        //	TPLog.printError("sendScrollMsg++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++==>");
    }

    /***
     * 发送缩放信息
     * @param tabId  当前tab页Id
     * @param zoom   缩放级别
     */
    public void sendScaleMsg(long tabId,float zoom){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null)
            mSender.sendZoomMsg(tabId, zoom);
    }

    public void sendGestureZoomMsg(long tabId,float zoomFactor,int focusX,int focusY){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null)
            mSender.sendGestureZoomMsg(tabId, zoomFactor,focusX,focusY);
    }



    /**
     * 发送清屏命令
     */
    public void sendClearScreenMsg(){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        byte msg[] = {0x00,0x00,0x00,0x06,0x11,0x00,0x00,0x00,0x00,0x0D};
        mSender.sendMsg(msg, NetUtil.EVEN_SYNCHRONOUS_REQ);
    }

    /**
     * 发送旋转数据
     * @param tabId
     * @param angle
     */
    public void sendRotateMsg(int tabId,int angle,boolean isFinish){

        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }

        //TPLog.printError("sendRotateMsg------------------->tabId="+tabId+",angle="+angle);

        byte event = Command.CC_ROTATE;

        byte tabIdArray[] = OspUtils.uintToByte(tabId);
        byte angleArray[] = OspUtils.uintToByte(angle);

        int finish = isFinish?0:1;

        byte finishArray[] = OspUtils.uintToByte(finish);

        byte data[] = new byte[tabIdArray.length + angleArray.length + finishArray.length];

        int index = 0;
        index += NetUtil.memcpy(data, tabIdArray, index);
        index += NetUtil.memcpy(data, angleArray, index);
        index += NetUtil.memcpy(data, finishArray, index);

        mSender.sendEntityPacket(data, event);
    }

    public void sendLeftOrRightRotateMsg(boolean isLeft,long tabId){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        mSender.sendRotateMsg(isLeft,tabId);
    }

    /**
     * 发送创建选项卡数据
     * @param page
     */
    public void sendCreateNewPageMsg(Page page){

        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }

        if(page==null){
            return;
        }

        long tabId = page.getId();
        int nSubPageCount = page.getSubPageCount();
        int m_nTotalDocPage =WhiteBoardUtils.curPageIndex;
        int mode = page.getPageMode();
        int width = (int)WhiteBoardUtils.whiteBoardWidth;
        int height = (int)WhiteBoardUtils.whiteBoardHeight;
        int owerIndex =page.getOwnerIndex();
        //int time = te.getM_nTime();
        int time = m_nTotalDocPage;
        int inConvert = page.getM_bInConvert();
        int isAnoymous = page.getIsAnoymous();


        byte byteTabName[] = null;
        if(isAnoymous==0) {
            try {
                String tabName = page.getName();
            //    TPLog.printError("tabName----------------->"+tabName);
//                byteTabName = tabName.getBytes("UTF-8");
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

        mSender.sendEntityPacket(content, Command.CC_NEWPAGE);
    }

    /**
     * 发送撤销消息
     * @param tabId
     * @param subpageIndex
     */
    public void sendUndoMsg(long tabId,long subpageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null){
            mSender.sendUnDoOrReDoMsg(true, tabId, subpageIndex);
        }
    }

    /**
     * 发送还原消息
     * @param tabId
     * @param subpageIndex
     */
    public void sendRedoMsg(long tabId,long subpageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null){
            mSender.sendUnDoOrReDoMsg(false, tabId, subpageIndex);
        }
    }

    /**
     * 发送页面切换消息
     */
    public void sendChangePageMsg(long tabId,long subpageIndex,Image image){

        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }

        subpageIndex = subpageIndex - 1;

        byte data[] = new byte[12];
        int index = 0;
        byte tabIdArray[] = OspUtils.uintToByte(tabId);
        byte spIndexArray[] = OspUtils.uintToByte(subpageIndex);
        byte isReqImageArray[] = OspUtils.uintToByte(0);

        index+=NetUtil.memcpy(data, tabIdArray, index);
        index+=NetUtil.memcpy(data, spIndexArray, index);
        index+=NetUtil.memcpy(data, isReqImageArray, index);

        mSender.sendEntityPacket(data,Command.CC_TABPAGE);

        //等发完页面切换后判断下，看需不需要发送图片
        if(image!=null&&image.isDlSuccess()&&!image.isExistOnServer()){
            SynFileManager.getInstance().sendImage(image);
            image.setExistOnServer(true);//只发送一次，以后就不用再次发送了
        }
    }


    /**
     * 向服务器请求翻页
     * @param tabId
     * @param pageIndex
     * @param imageId
     * @param ownerIndex
     */
    public void sendRequestServerTurnPageMsg(long tabId , long pageIndex,int imageId,int ownerIndex){

        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }

        byte content[] = new byte[16];
        int index = 0;
        index += NetUtil.memcpy(content, OspUtils.uintToByte(imageId), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(tabId), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(pageIndex), index);
        index += NetUtil.memcpy(content, OspUtils.uintToByte(ownerIndex), index);
        if(mSender!=null)
            mSender.sendEntityPacket(content, Command.CS_REQUESTPAGETURN);
    }



    /**
     * 发送文档打开成功命令
     */
    public void sendOpenOfficeComplete(long tabId){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        byte content[] = OspUtils.uintToByte(tabId);
        if(mSender!=null)
            mSender.sendEntityPacket(content, Command.CC_DOCPAGECONVERTCOMPLETE);
    }

    /**
     * 发送子页总数
     * @param count
     */
    public void sendSubpageCountMsg(long tabId,int count){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null)
            mSender.sendSubpageCountMsg(tabId, count);
    }

    public void requestServerRecFlow(){
        if(!sendEnable){
            return;
        }
        if(mSender!=null)
            mSender.requestServerRecFlow();
    }

    public void quitMeeting(){
        TPLog.printError("准备发送本地数据会议退出消息。。。");
        if(!sendEnable){
            return;
        }
        TPLog.printKeyStatus("发送退出会议消息。。。。。。。。");
        mSender.sendQuitMeetingMsg();
    }

    /**
     * 响应服务器请求同步的消息
     */
//    public void sendSynData(PageManager manager,long dwRequestId){
//        if(!sendEnable){
//            return;
//        }
//        try{
//            ArrayList<IPage> tabList = manager.getPageList();
//            //由于数据过于庞大，数组的长度不好确定，暂定使用List
//            List<Byte> data = new ArrayList<Byte>();
//
//            long tabCount = tabList.size();
//            cpyArrayToList(data,OspUtils.uintToByte(tabCount));
//
//            print("tabCount="+tabCount);
//
//            for(int t=0;t<tabCount;t++){
//
//                Page te = (Page)tabList.get(t);
//                //子页集合
//                List<SubPage> pageList = te.getSubPageList();
//
//                //选项卡ID
//                long tabId = te.getId();
//                cpyArrayToList(data,OspUtils.uintToByte(tabId));
//
//                print("tabId="+tabId);
//
//                //选项卡名称
//                char tabNameChar[] = te.getName().toCharArray();
//
//                print("tabName="+te.getName());
//
//                //C++中一个字符占2个字节
//                byte tabNames[] = new byte[tabNameChar.length*2];
//                int nameIndex = 0;
//                for(char c:tabNameChar){
//                    byte temp[] = OspUtils.uShortToByte(c);
//                    tabNames[nameIndex++] = temp[0];
//                    tabNames[nameIndex++] = temp[1];
//                    temp = null;
//                }
//
//                tabNameChar = null;
//
//                cpyArrayToList(data, tabNames);
//                data.add((byte) 0);
//                data.add((byte) 0);
//
//                //当前模式
//                long wbmode = te.getPageMode();
//                cpyArrayToList(data,OspUtils.uintToByte(wbmode));
//
//                print("wbmode="+wbmode);
//
//                //当前缩放级别
//                float zoom = te.getCurScale()*100f;
//                byte zoomBytes[] = OspUtils.uFloatToByte(zoom);
//                //PC端发送的时候 字节序是反的，这里也反着发过去
//                for(int i = zoomBytes.length-1;i>=0;i--){
//                    data.add(zoomBytes[i]);
//                }
//
//                print("zoom="+zoom);
//
//                //旋转角度
//                long angle = te.getCurAngle();
//                cpyArrayToList(data,OspUtils.uintToByte(angle));
//
//                print("angle="+angle);
//
//                //是否是同步时序列化标识/是否是文档拥有者  这里暂时固定成0
//                cpyArrayToList(data,OspUtils.uintToByte(0));
//
//                //当前选项卡子页总数
//                long pageCount = pageList.size();
//                if(pageCount==0){
//                    pageCount = 1;
//                }
//                cpyArrayToList(data,OspUtils.uintToByte(pageCount));
//
//                print("subPageCount="+pageCount);
//
//                //当前显示页面索引
//                long curPageIndex = te.getCurSubPageIndex() - 1;
//                cpyArrayToList(data,OspUtils.uintToByte(curPageIndex));
//
//                print("curPageIndex="+curPageIndex);
//
//                //当前白板宽度
//                long wbWidth = (long)WhiteBoardUtils.whiteBoardWidth;
//                cpyArrayToList(data, OspUtils.uintToByte(wbWidth));
//
//                print("wbWidth="+wbWidth);
//
//                //当前白板高度
//                long wbHeight = (long)WhiteBoardUtils.whiteBoardHeight;
//                cpyArrayToList(data,OspUtils.uintToByte(wbHeight));
//
//                print("wbHeight="+wbHeight);
//
//                //当前用户在服务器上面的索引
//                //看PC端貌似一直是0，这里先固定填写0
//                cpyArrayToList(data,OspUtils.uintToByte(te.getOwnerIndex()));
//
//                //好像是操作次数，目前不知道有什么用
//                long times = WhiteBoardUtils.curPageIndex;
//                cpyArrayToList(data,OspUtils.uintToByte(times));
//
//                print("times="+times);
//
//                //是否正在进行文档转换 这里目前也先写死
//                long inConvert = te.getM_bInConvert();
//                cpyArrayToList(data,OspUtils.uintToByte(inConvert));
//
//                print("inConvert="+inConvert);
//
//                for(int p = 0;p<pageCount;p++) {
//                    SubPage subPage = (SubPage)te.getSubPage(p);
//                    //进度
//                    long progress = subPage.getProgress();
//                    cpyArrayToList(data, OspUtils.uintToByte(progress));
//
//                    //水平滚动条位置
//                    long scrollX = (long)subPage.getOffsetX();
//                    cpyArrayToList(data, OspUtils.uintToByte(scrollX));
//
//                    //垂直滚动条位置
//                    long scrollY = (long)subPage.getOffsetY();
//                    cpyArrayToList(data, OspUtils.uintToByte(scrollY));
//
//                    //图源集合
//                    List<Graph> graphList = subPage.getGraphList();
//
//                    if(graphList==null){
//                        graphList = new ArrayList<Graph>();
//                    }
//
//
//                    //图源个数
//                    long graphCount = graphList.size();
//
//                    Image ie = subPage.getImage();
//
//                    if(ie!=null){
//                        graphCount = graphCount+1;
//                    }
//
//                    cpyArrayToList(data, OspUtils.uintToByte(graphCount));
//                    print("graphCount="+graphCount);
//                    //插入所有的图源
//                    if(graphCount!=0){
//
//                        //如果有图片的话第一个图源放入图片
//                        if(ie!=null){
//                            //类型
//                            long type = ie.getType();
//                            cpyArrayToList(data, OspUtils.uintToByte(type));
//                            //图形Id
//                            long graphId = ie.getId();
//                            cpyArrayToList(data, OspUtils.uintToByte(graphId));
//                            //m_bLock
//                            long mblock = 1;
//                            cpyArrayToList(data, OspUtils.uintToByte(mblock));
//                            //m_bRegionHasChanged
//                            cpyArrayToList(data, OspUtils.uintToByte(1));
//                            //m_bNeedRollBack
//                            cpyArrayToList(data, OspUtils.uintToByte(0));
//                            //图片显示的X坐标
//                            long x = (long)ie.getX();
//                            cpyArrayToList(data, OspUtils.uintToByte(x));
//                            //图片显示的Y坐标
//                            long y = (int)ie.getY();
//                            cpyArrayToList(data, OspUtils.uintToByte(y));
//                            //图片宽度
//                            long iWidth = (long)ie.getWidth();
//                            cpyArrayToList(data, OspUtils.uintToByte(iWidth));
//                            //图片的高度
//                            long iHeight = (long)ie.getHeight();
//                            cpyArrayToList(data, OspUtils.uintToByte(iHeight));
//                            //网络状态
//                            cpyArrayToList(data, OspUtils.uintToByte(4));
//
//                            String imageName = ie.getFileName();
//                            char nameChar[] = imageName.toCharArray();
//                            byte nameByte[] = new byte[nameChar.length*2];
//                            int index = 0;
//                            for(int i = 0;i<nameChar.length;i++){
//                                nameByte[index++] = 0;
//                                nameByte[index++] = (byte)nameChar[i];
//                            }
//                            cpyArrayToList(data, nameByte);
//                            data.add((byte)0);
//                            data.add((byte)0);
//                            //fdwHasFileBody
//                            cpyArrayToList(data, OspUtils.uintToByte(0));
//                        }
//
//                        for(int g=0;g<graphList.size();g++){
//
//                            Graph ge = graphList.get(g);
//                            byte geData[] =  getGraphData(ge);
//                            cpyArrayToList(data, geData);
//
//                        }
//
//                    }  // if graphCount!=0
//
//
//                    //撤销集合
//                    List<IOperation> undoList = subPage.getUndoList();
//                    //撤销步数
//                    long undoCount = 0;
//                    if(undoList!=null){
//                        undoCount = undoList.size();
//                    }
//                    cpyArrayToList(data, OspUtils.uintToByte(undoCount));
//                    print("undoSize="+undoCount);
//
//                    if(undoCount>0){
//
//                        for(int u=0;u<undoCount;u++){
//
//                            IOperation oe = undoList.get(u);
//                            //操作类型
//                            int optype = oe.getType();
//                            cpyArrayToList(data, OspUtils.uintToByte(optype));
//                            /**
//                             * eoAdd = 0,			//添加操作
//                             * eoRotate =1,			//旋转
//                             * eoClear = 2,			//清屏
//                             * eoEarse = 3,			//橡皮擦
//                             */
//                            switch(optype){
//                                case 3:
//
//                                    break;
//                                case 1:
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    byte rotateArray[] = getRotateOptData((RotateOperation)oe);
//                                    cpyArrayToList(data,rotateArray);
//                                    break;
//                                case 0:
//                                    //isUndo
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    //图源数据
//                                    byte geArray[] = getGraphData(((GraphOperation)oe).getGraph());
//                                    cpyArrayToList(data,geArray);
//                                    break;
//                                case 2:
//                                    //isUndo
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    //图源数据
//                                    List<Graph> list = ((ClearScreenOperation)oe).getGraphList();
//                                    byte cArray[] = getClearScreenOptData(list);
//                                    cpyArrayToList(data,cArray);
//                                    break;
//                            }
//
//                        }
//
//                    } //if undoCount!=0
//
//
//                    //恢复数据
//                    List<IOperation> redoList = subPage.getRedoList();
//                    //撤销步数
//                    long redoCount = 0;
//                    if(redoList!=null){
//                        redoCount = redoList.size();
//                    }
//                    cpyArrayToList(data, OspUtils.uintToByte(redoCount));
//
//                    print("redoCount="+redoCount);
//
//                    if(redoCount>0){
//
//                        for(int u=0;u<redoCount;u++){
//
//                            IOperation oe = redoList.get(u);
//                            //操作类型
//                            int optype = oe.getType();
//                            cpyArrayToList(data, OspUtils.uintToByte(optype));
//                            /**
//                             * eoAdd = 0,			//添加操作
//                             * eoRotate =1,			//旋转
//                             * eoClear = 2,			//清屏
//                             * eoEarse = 3,			//橡皮擦
//                             */
//                            switch(optype){
//                                case 3:
//
//                                    break;
//                                case 1:
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    byte rotateArray[] = getRotateOptData((RotateOperation)oe);
//                                    cpyArrayToList(data,rotateArray);
//                                    break;
//                                case 0:
//                                    //isUndo
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    //图源数据
//                                    byte geArray[] = getGraphData(((GraphOperation)oe).getGraph());
//                                    cpyArrayToList(data,geArray);
//                                    break;
//                                case 2:
//                                    //isUndo
//                                    cpyArrayToList(data,OspUtils.uintToByte(1));
//                                    //图源数据
//                                    List<Graph> list = ((ClearScreenOperation)oe).getGraphList();
//                                    byte cArray[] = getClearScreenOptData(list);
//                                    cpyArrayToList(data,cArray);
//                                    break;
//                            }
//
//                        }
//                    } //if redoCount!=0
//                } //page
//            }  //tab
//
//            //未知字段，不知道干什么的
//            long m_nFirstVisibleItemIndex = 0;
//            cpyArrayToList(data, OspUtils.uintToByte(m_nFirstVisibleItemIndex));
//            //当前显示的tabId
//            long m_nCurTabPageId = manager.getSelectPage().getId();
//            cpyArrayToList(data, OspUtils.uintToByte(m_nCurTabPageId));
//            print("当前显示的tabId = "+m_nCurTabPageId);
//
//            //不具名tab页总数  不知道干什么的
//            long m_nTotalDocPage = WhiteBoardUtils.curPageIndex;
//            cpyArrayToList(data, OspUtils.uintToByte(m_nTotalDocPage));
//            //自动生成tab页索引
//            long m_nPageIndex = WhiteBoardUtils.curPageIndex;
//            cpyArrayToList(data, OspUtils.uintToByte(m_nPageIndex));
//
//            byte array[] = new byte[data.size()];
//            int index = 0;
//            for(byte b:data){
//                array[index++] = b;
//            }
//
//            byte[] cArray = NetUtil.CompressBuffer(array);
//
//            long wCheckFlag = cArray.length + 1 + 4 + 4 + 1;
//
//            long msgLength = wCheckFlag + 4+4+1 +1 +4;
//
//            byte pack[] = new byte[(int)msgLength];
//            index = 0;
//            index+=NetUtil.memcpy(pack,OspUtils.uintToByte(msgLength-4),index);  // 4
//            pack[index++] = Command.CC_USERDATA;                            //1
//            //dwRecvId  指定用户Id
//            index+=NetUtil.memcpy(pack,OspUtils.uintToByte(dwRequestId),index);//4
//            //checkflags
//            index+=NetUtil.memcpy(pack,OspUtils.uintToByte(wCheckFlag),index); //4
//            pack[index++] = (byte)(Command.CC_SYNCHRONOUSEBODY|0X80);        //1
//            //bs length
//            index+=NetUtil.memcpy(pack,OspUtils.uintToByte(cArray.length+4),index);  //4
//            //bs length
//            index+=NetUtil.memcpy(pack,OspUtils.uintToByte(cArray.length),index); //4
//            //压缩的数据
//            index+=NetUtil.memcpy(pack,cArray,index);
//            pack[index++] = Command.MSG_TERM;                               //1
//            pack[index++] = Command.MSG_C2S;                                //1
//
//            NetUtil.displayArray(pack);
//
//            if(mSender!=null)
//                mSender.sendMsg(pack, NetUtil.EVEN_SYNCHRONOUS_REQ);
//
//        }catch(Exception e){
//            TPLog.printError("发送同步数据时报错了----------》"+e);
//            StackTraceElement ste[] = e.getStackTrace();
//            for(StackTraceElement s : ste){
//                String msg = "";
//                msg = msg + s.getClassName() + "-----"+s.getMethodName()+"----"+s.getLineNumber();
//                TPLog.printError(msg);
//            }
//        }
//    }

    /**
     * 响应服务器请求同步的消息
     */
    public void sendSynData(PageManager manager,long dwRequestId){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            TPLog.printError("sendSynData->sendEnable:"+sendEnable);
            return;
        }
        try{
            ArrayList<IPage> tabList = manager.getPageList();
            //由于数据过于庞大，数组的长度不好确定，暂定使用List
            List<Byte> data = new ArrayList<Byte>();

            long tabCount = tabList.size();
            cpyArrayToList(data,OspUtils.uintToByte(tabCount)); //白板数量

            cpyArrayToList(data,OspUtils.uintToByte( manager.getSelectPage().getId()));//当前现实的白板Id
           ;

            print("tabCount="+tabCount);

            for(int t=0;t<tabCount;t++){

                Page te = (Page)tabList.get(t);
                //子页集合
                List<SubPage> pageList = te.getSubPageList();

                //选项卡ID
                long tabId = te.getId();
                cpyArrayToList(data,OspUtils.uintToByte(tabId));

                print("tabId="+tabId);

                //选项卡名称
                byte tabNameBytes[] = te.getName().getBytes("Unicode");
                print("tabName="+te.getName());

                cpyArrayToList(data, OspUtils.uintToByte(tabNameBytes.length));
                cpyArrayToList(data, tabNameBytes);

                //当前选项卡子页总数
                long pageCount = pageList.size();
                if(pageCount==0){
                    pageCount = 1;
                }
                cpyArrayToList(data,OspUtils.uintToByte(pageCount));

                print("subPageCount="+pageCount);

                //当前显示页面索引
                long curPageIndex = te.getCurSubPageIndex() - 1;
                cpyArrayToList(data,OspUtils.uintToByte(curPageIndex));

                print("curPageIndex="+curPageIndex);

                //当前用户在服务器上面的索引
                //看PC端貌似一直是0，这里先固定填写0
                cpyArrayToList(data,OspUtils.uintToByte(te.getOwnerIndex()));

                //好像是操作次数，目前不知道有什么用
                long times = WhiteBoardUtils.curPageIndex;
                cpyArrayToList(data,OspUtils.uintToByte(times));

                print("times="+times);


                for(int p = 0;p<pageCount;p++) {
                    SubPage subPage = (SubPage)te.getSubPage(p);
                    float matrixValue[] = new float[9];
                    subPage.getMatrix().getValues(matrixValue);

                    byte matrixValueBytes[] = new byte[4*9];
                    int index = 0;
                    for(float f:matrixValue){
                        byte temp[] = OspUtils.uFloatToByte(f);
                        index += NetUtil.memcpy(matrixValueBytes,temp,index);
                    }
                    cpyArrayToList(data,matrixValueBytes);


                    //图源集合
                    List<Graph> graphList = new ArrayList<Graph>();
                    List<Graph> gpList = subPage.getGraphList();
                    List<Graph> imgList = subPage.getImageGraphList();

                    print("img size="+imgList.size());

                    print("graph size="+gpList.size());

                    for(int i = 0;i<gpList.size();i++){
                        if(gpList.get(i).getGraphType() == WhiteBoardUtils.GRAPH_SELECT){
                            continue;
                        }
                        graphList.add(gpList.get(i));
                    }

                    for(int i = 0;i<imgList.size();i++){
                        graphList.add(imgList.get(i));
                    }

                    //图源个数
                    long graphCount = graphList.size();

                    cpyArrayToList(data, OspUtils.uintToByte(graphCount));
                    print("graphCount="+graphCount);
                    //插入所有的图源
                    if(graphCount!=0){
                        for(int g=0;g<graphList.size();g++){
                            Graph ge = graphList.get(g);
                            byte geData[] =  getGraphData(ge);
                            cpyArrayToList(data, geData);
                        }
                    }  // if graphCount!=0


                    //撤销集合
                    List<IOperation> undoList = subPage.getUndoList();
                    //撤销步数
                    long undoCount = 0;
                    if(undoList!=null){
                        undoCount = undoList.size();
                    }
                    cpyArrayToList(data, OspUtils.uintToByte(undoCount));
                    print("undoSize="+undoCount);

                    if(undoCount>0){

//                        for(int u=0;u<undoCount;u++){
                        for(int u=(int)(undoCount-1);u>=0;u--){

                            IOperation oe = undoList.get(u);
                            //操作类型
                            int optype = oe.getType();
                            cpyArrayToList(data, OspUtils.uintToByte(optype));
                            /**
                             * eoAdd = 0,			//添加操作
                             * eoRotate =1,			//旋转
                             * eoClear = 2,			//清屏
                             * eoEarse = 3,			//橡皮擦
                             */
                            switch(optype){
                                case 3:

                                    break;
                                case 1:
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    byte rotateArray[] = getRotateOptData((RotateOperation)oe);
                                    cpyArrayToList(data,rotateArray);
                                    break;
                                case 0:
                                    //isUndo
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    //图源数据
                                    byte geArray[] = getGraphData(((GraphOperation)oe).getGraph());
                                    cpyArrayToList(data,geArray);
                                    break;
                                case 2:
                                    //isUndo
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    //图源数据
                                    List<Graph> list = ((ClearScreenOperation)oe).getGraphList();
                                    byte cArray[] = getClearScreenOptData(list);
                                    cpyArrayToList(data,cArray);
                                    break;
                            }

                        }

                    } //if undoCount!=0


                    //恢复数据
                    List<IOperation> redoList = subPage.getRedoList();
                    //撤销步数
                    long redoCount = 0;
                    if(redoList!=null){
                        redoCount = redoList.size();
                    }
                    cpyArrayToList(data, OspUtils.uintToByte(redoCount));

                    print("redoCount="+redoCount);

                    if(redoCount>0){

//                        for(int u=0;u<redoCount;u++){
                        for(int u=(int)(redoCount-1);u>=0;u--){

                            IOperation oe = redoList.get(u);
                            //操作类型
                            int optype = oe.getType();
                            cpyArrayToList(data, OspUtils.uintToByte(optype));
                            /**
                             * eoAdd = 0,			//添加操作
                             * eoRotate =1,			//旋转
                             * eoClear = 2,			//清屏
                             * eoEarse = 3,			//橡皮擦
                             */
                            switch(optype){
                                case 3:

                                    break;
                                case 1:
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    byte rotateArray[] = getRotateOptData((RotateOperation)oe);
                                    cpyArrayToList(data,rotateArray);
                                    break;
                                case 0:
                                    //isUndo
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    //图源数据
                                    byte geArray[] = getGraphData(((GraphOperation)oe).getGraph());
                                    cpyArrayToList(data,geArray);
                                    break;
                                case 2:
                                    //isUndo
                                    cpyArrayToList(data,OspUtils.uintToByte(1));
                                    //图源数据
                                    List<Graph> list = ((ClearScreenOperation)oe).getGraphList();
                                    byte cArray[] = getClearScreenOptData(list);
                                    cpyArrayToList(data,cArray);
                                    break;
                            }

                        }
                    } //if redoCount!=0
                } //page
            }  //tab


            byte array[] = new byte[data.size()];
            int index = 0;
            for(byte b:data){
                array[index++] = b;
            }

            byte[] cArray = NetUtil.CompressBuffer(array);

            long wCheckFlag = cArray.length + 1 + 4 + 4 + 1;

            long msgLength = wCheckFlag + 4+4+1 +1 +4;

            byte pack[] = null;
            index = 0;

//            TPLog.printError("msgLength==================="+msgLength);

            if(msgLength<=NetUtil.PACKET_MAX_SIZE) {
                pack = new byte[(int)msgLength];
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(msgLength - 4), index);  // 4
                pack[index++] = Command.CC_USERDATA;                            //1
                //dwRecvId  指定用户Id
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(dwRequestId), index);//4
                //checkflags
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(wCheckFlag), index); //4
                pack[index++] = (byte) (Command.CC_SYNCHRONOUSEBODY | 0X80);        //1
                //bs length
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(cArray.length + 4), index);  //4
                //bs length
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(cArray.length), index); //4
                //压缩的数据
                index += NetUtil.memcpy(pack, cArray, index);
                pack[index++] = Command.MSG_TERM;                               //1
                pack[index++] = Command.MSG_C2S;                                //1

                //            NetUtil.displayArray(pack);

                if(mSender!=null){
                    TPLog.printError("准备发出同步消息。。。");
                    mSender.sendMsg(pack, NetUtil.EVEN_SYNCHRONOUS_REQ);
                }

            }else{//拆包
                wCheckFlag = cArray.length + 1+1+4+4;
                pack = new byte[(int)wCheckFlag + 4];
                index = 0;
                //checkflags
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(wCheckFlag), index); //4
                pack[index++] = (byte) (Command.CC_SYNCHRONOUSEBODY | 0X80);        //1
                //bs length
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(cArray.length + 4), index);  //4
                //bs length
                index += NetUtil.memcpy(pack, OspUtils.uintToByte(cArray.length), index); //4
                //压缩的数据
                index += NetUtil.memcpy(pack, cArray, index);
                pack[index++] = Command.MSG_TERM;

//                NetUtil.displayArray(pack);

//                TPLog.printError("pack.length==================="+pack.length+",,,index="+index);

               int nSegmentLen = NetUtil.PACKET_MAX_SIZE - 36;
                int segmentCount = pack.length / nSegmentLen;
                segmentCount = segmentCount + ((pack.length % nSegmentLen)==0?0:1);
                int nPreId = 0;
                 //  4                      1            4           4               1                4           4          4              n           1         1
                //(nSegmentMsgLen+10) + CC_USERDATA + dwRecvId + nSegmentMsgLen + CC_SEGMENTMSG + m_SegmentId + nPreId + g_user_index+ sub_stream + MSG_TERM + MSG_C2S
                for(int i = 0;i<segmentCount;i++){

                    index = 0;

                    byte subPack[] = null;

                    if(i!=(segmentCount-1)){//不是最后一包
                        subPack = new byte[NetUtil.PACKET_MAX_SIZE];
                    }else{//最后一包
                        int len = (pack.length - i*nSegmentLen)+ 36;
                        subPack = new byte[len];
                    }

                    System.arraycopy(OspUtils.uintToByte(subPack.length-4),0,subPack,0,4);                   //  4
                    index+=4;
                    subPack[index++] = (byte)(Command.CC_USERDATA);                                                                      //  1
                    System.arraycopy(OspUtils.uintToByte(dwRequestId), 0, subPack, index, 4);                               // 4
                    index+=4;
                    System.arraycopy(OspUtils.uintToByte(subPack.length-index-4),0,subPack,index,4);                                   // 4
                    index+=4;
//                    int length1 = subPack.length - index -4;
//                    System.arraycopy(OspUtils.uintToByte(length1), 0, subPack, index, 4);    //-------------------------
//                    index+=4;

                    subPack[index++] = (byte)(Command.CC_SEGMENTMSG | 0x80);                                                             // 1

                    int length2 = subPack.length - index -4;
                    System.arraycopy(OspUtils.uintToByte(length2), 0, subPack, index, 4);    //-------------------------
                    index+=4;

                    if(i!=(segmentCount-1)) {                                                                                              // 4
                        System.arraycopy(OspUtils.uintToByte(i + 1), 0, subPack, index, 4);
                    }else{
                        System.arraycopy(OspUtils.uintToByte(-1), 0, subPack, index, 4);
                    }
                    index+=4;
                    System.arraycopy(OspUtils.uintToByte(nPreId), 0, subPack, index, 4);                                // 4
                    index+=4;
                    System.arraycopy(OspUtils.uintToByte(NetUtil.curUserId), 0, subPack, index, 4);                            //4
                    index+=4;

                    int length3 = subPack.length-36;
                    System.arraycopy(OspUtils.uintToByte(length3), 0, subPack, index, 4);    //-------------------------
                    index+=4;

                    System.arraycopy(pack, (i*(nSegmentLen)), subPack, index,  subPack.length-36);                                                     // n
                    index += subPack.length-36;
                    subPack[index++] = Command.MSG_TERM;                                                                                    // 1
                    subPack[index++] = Command.MSG_C2S;                                                                                     // 1

                    nPreId = i+1;
//                    NetUtil.displayArray(subPack);

                    if(mSender!=null){
                        TPLog.printError("准备发送拆包数据。。。"+i);
                        mSender.sendMsg(subPack, NetUtil.EVEN_SYNCHRONOUS_REQ);
                    }
                }
            }


            TPLog.printError("同步数据已经发送给服务器...");
        }catch(Exception e){
            TPLog.printError("发送同步数据时出现异常：");
            TPLog.printError(e);
        }
    }



    //获取清屏数据
    public byte[] getClearScreenOptData(List<Graph> list){
        List<Byte> data = new ArrayList<Byte>();
        int count = 0;
        if(list==null){
            count = 0;
        }else{
            count = list.size();
        }

        cpyArrayToList(data, OspUtils.uintToByte(count));

        if(count>0) {
            for (int i = 0; i < list.size(); i++) {
                Graph ge = list.get(i);
                byte geData[] = getGraphData(ge);
                cpyArrayToList(data, geData);
            }
        }

        byte dataArray[] = new byte[data.size()];
        int index = 0;
        for(byte b:data){
            dataArray[index++] = b;
        }

        return dataArray;
    }

    //入会同步时的旋转撤销数据
    private byte[] getRotateOptData(RotateOperation rp){
//        int o = 0;
//        if(angle>oldAngle){
//            o = 0;
//        }else{
//            o = 1;
//        }
//
//        byte data[] = OspUtils.uintToByte(o);
//
//        if(o==1){  //高低位从新排布
//            byte temp = data[0];
//            data[0] = data[3];
//            data[3] = temp;
//        }

        byte curAngleBuffer[] = OspUtils.uintToByte(rp.getCurAngle());
        byte oldAngleBuffer[] = OspUtils.uintToByte(rp.getOldAngle());

        int length = curAngleBuffer.length + oldAngleBuffer.length;

        byte data[] = new byte[length];
        int index = 0;
        index += NetUtil.memcpy(data, curAngleBuffer, index);
        index += NetUtil.memcpy(data, oldAngleBuffer, index);

        return data;
    }

    private byte[] getGraphData(Graph graph) {
        byte graphdata[] = null;
        if (graph.getGraphType() != WhiteBoardUtils.GRAPH_IMAGE) {
            if(graph.getGraphType() ==  WhiteBoardUtils.GRAPH_ERASE){
                int params[] = null;
                List<Point> points = graph.getPoints();

                int width = ((Erase)graph).getEraseWidth();
                int height = ((Erase)graph).getEraseHeight();

                int p[] = {
                        graph.getGraphType(),
                        graph.getId(), 0, 1, 0, width,height,points.size()
                };
                params = p;



                int paramsEnd[] = {
                        graph.getColor(), 0, 1, 1, 1, 1, 1, 1, 1
                };

                int graphdataLength = params.length * 4 + points.size() * 9 + paramsEnd.length * 4;

                graphdata = new byte[graphdataLength];

                int index = 0;

                for (int i = 0; i < params.length; i++) {
                    index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(params[i]), index);
                }

                for (int i = 0; i < points.size(); i++) {
                    Point point = points.get(i);
                    index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.x), index);
                    index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.y), index);
                    if (i == 0) {
                        graphdata[index++] = 0;
                    } else {
                        graphdata[index++] = 1;
                    }
                }

                for (int i = 0; i < paramsEnd.length; i++) {
                    index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(paramsEnd[i]), index);
                }
            }else {
                int params[] = null;
                List<Point> points = graph.getPoints();
                int p[] = {
                        graph.getGraphType(),
                        graph.getId(), 0, 1, 0, points.size()
                };
                params = p;

                int paramsEnd[] = {
                        (int) graph.getStrokeWidth(), graph.getColor(), 0, 1, 1, 1, 1, 1, 1, 1
                };

                int graphdataLength = params.length * 4 + points.size() * 9 + paramsEnd.length * 4;

                graphdata = new byte[graphdataLength];

                int index = 0;

                for (int i = 0; i < params.length; i++) {
                    index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(params[i]), index);
                }

                for (int i = 0; i < points.size(); i++) {
                    Point point = points.get(i);
                    index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.x), index);
                    index += NetUtil.memcpy(graphdata, OspUtils.uFloatToByte(point.y), index);
                    if (i == 0) {
                        graphdata[index++] = 0;
                    } else {
                        graphdata[index++] = 1;
                    }
                }

                for (int i = 0; i < paramsEnd.length; i++) {
                    index += NetUtil.memcpy(graphdata, OspUtils.uintToByte(paramsEnd[i]), index);
                }
            }
        }else{
            ImageGraph tempImg = (ImageGraph)graph;
            int p[] = {
                    graph.getGraphType(),
                    graph.getId(),(int)tempImg.getX(),(int)tempImg.getY(),tempImg.getWidth(),tempImg.getHeight()
            };
            graphdata = new byte[p.length*4+4*9+4*9];
            int index = 0;
            for(int i:p){
                index += NetUtil.memcpy(graphdata,OspUtils.uintToByte(i),index);
            }

            float matrixValue[] = new float[9];
            tempImg.getMatrix().getValues(matrixValue);
            for(float f:matrixValue){
                index += NetUtil.memcpy(graphdata,OspUtils.uFloatToByte(f),index);
            }

            //2017.07.08临时添加，为了协作协调
//            float drawMatrixValue[] = new float[9];
//            tempImg.getDrawMatrix().getValues(drawMatrixValue);
//            for(float f:drawMatrixValue){
//                index += NetUtil.memcpy(graphdata,OspUtils.uFloatToByte(f),index);
//            }
        }
        return graphdata;
    }

    public void requestImage(Image image,long tabId,long pageIndex,int ownerIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(image==null)return;
        if (!image.isDlSuccess() && !image.isExistOnServer()) {
            //向服务器请求翻页
            int imageId = image.getId();
            mSendHelper.sendRequestServerTurnPageMsg(tabId, pageIndex, imageId, ownerIndex);
        } else if (!image.isDlSuccess() && image.isExistOnServer()) {
            //请求文件信息
            SynFileManager.getInstance().requestFileInfo(image);
        } else if (image.isDlSuccess() && !image.isExistOnServer()) {
            SynFileManager.getInstance().sendImage(image);
    }
    }

    /**
     * 发送关闭白板消息
     * @param removeTabId
     * @param lastTabId
     */
    public void sendCloseWb(long removeTabId,long lastTabId){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        mSender.sendRemoveTableMsg(removeTabId,lastTabId);
    }


    /**
     * 2017.07.04新增接口
     * 插入图片消息
     *
     */
    public void sendInsertImgMsg(ImageGraph img){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        byte event = Command.CC_INSERT_IMG;

        int graphType = img.getGraphType();
        long tabId = img.getTabId();
        long subPageIndex = img.getPageIndex();
        int imgId = img.getId();
        int x = (int)img.getX();
        int y = (int)img.getY();
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        float matrixValues[] = new float[9];
        img.getMatrix().getValues(matrixValues);

        int contentLength = 68;
        byte content[] = new byte[contentLength];

        int index = 0;

        byte graphTypeBytes[] = OspUtils.uintToByte(graphType);
        byte tabIdBytes[] = OspUtils.uintToByte(tabId);
        byte subPageIndexBytes[] = OspUtils.uintToByte(subPageIndex);
        byte imgIdBytes[] = OspUtils.uintToByte(imgId);
        byte xBytes[]= OspUtils.uintToByte(x);
        byte yBytes[] = OspUtils.uintToByte(y);
        byte imgWidthBytes[] = OspUtils.uintToByte(imgWidth);
        byte imgHeightBytes[] = OspUtils.uintToByte(imgHeight);

        byte matrixValuesBytes[] = new byte[36];
        for(int i = 0;i<matrixValues.length;i++){
            byte tempBytes[] = OspUtils.uFloatToByte(matrixValues[i]);
            for(byte b:tempBytes){
                matrixValuesBytes[index++] = b;
            }
        }

        //填充数据
        index = 0;
        index += NetUtil.memcpy(content,graphTypeBytes,index);
        index += NetUtil.memcpy(content,tabIdBytes,index);
        index += NetUtil.memcpy(content,subPageIndexBytes,index);
        index += NetUtil.memcpy(content,imgIdBytes,index);
        index += NetUtil.memcpy(content,xBytes,index);
        index += NetUtil.memcpy(content,yBytes,index);
        index += NetUtil.memcpy(content,imgWidthBytes,index);
        index += NetUtil.memcpy(content,imgHeightBytes,index);
        index += NetUtil.memcpy(content,matrixValuesBytes,index);

        mSender.sendEntityPacket(content,event);
    }

    /**
     * 画布状态改变消息 替换之前的旋转、缩放和平移
     * @param matrix
     * @param tabId
     * @param subPageId
     */
    public void sendCoordinateChanged(Matrix matrix,long tabId,long subPageId){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        byte event = Command.CC_COORDINATE_CHANGED;
        float matrixValues[] = new float[9];
        matrix.getValues(matrixValues);

        String valueStr = "";
        for(float f:matrixValues){
            valueStr = valueStr + f +",";
        }

        byte tabIdBytes[] = OspUtils.uintToByte(tabId);
        byte subPageIdBytes[] = OspUtils.uintToByte(subPageId);

        int index = 0;
        byte matrixValuesBytes[] = new byte[36];
        for(int i = 0;i<matrixValues.length;i++){
            byte tempBytes[] = OspUtils.uFloatToByte(matrixValues[i]);
            for(byte b:tempBytes){
                matrixValuesBytes[index++] = b;
            }
        }

        index = 0;
        byte content[] = new byte[44];
        index += NetUtil.memcpy(content,tabIdBytes,index);
        index += NetUtil.memcpy(content,subPageIdBytes,index);
        index += NetUtil.memcpy(content,matrixValuesBytes,index);

        //NetUtil.displayArray(content);

        mSender.sendEntityPacket(content,event);
    }

    public void sendSelectGraphChanged(ArrayList<Graph> selectGraphs,long tabId,int subPageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(selectGraphs==null){
            return;
        }
        int graphCount = selectGraphs.size();

        int contentLength = (4*3)+((4+4*9+4*9)*graphCount);
        byte content[] = new byte[contentLength];

        byte tabIdByates[] = OspUtils.uintToByte(tabId);
        byte subPageIndexBytes[] = OspUtils.uintToByte(subPageIndex);
        byte graphCountBytes[] = OspUtils.uintToByte(graphCount);

        int index = 0;
        index += NetUtil.memcpy(content,tabIdByates,index);
        index += NetUtil.memcpy(content,subPageIndexBytes,index);
        index += NetUtil.memcpy(content,graphCountBytes,index);
        for(int i = 0;i<graphCount;i++){
            ImageGraph ig = (ImageGraph)selectGraphs.get(i);
            byte igIdBytes[] = OspUtils.uintToByte(ig.getId());
            index += NetUtil.memcpy(content,igIdBytes,index);
            float matrixValues[] = new float[9];
            Matrix matrix = ig.getMatrix();
            matrix.getValues(matrixValues);
            for(int j = 0;j<matrixValues.length;j++){
                byte valueBytes[] = OspUtils.uFloatToByte(matrixValues[j]);
                index += NetUtil.memcpy(content,valueBytes,index);
            }
        }
        mSender.sendEntityPacket(content,Command.CC_GRAPH_COORDINATE_CHANGED);
    }

    public void sendDeleteGraph(int graphId,long tabId,int subPageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        byte graphIdBytes[] = OspUtils.uintToByte(graphId);
        byte tabIdBytes[]= OspUtils.uintToByte(tabId);
        byte subPageIndexBytes[] = OspUtils.uintToByte(subPageIndex);

        byte content[] = new byte[12];
        int index = 0;
        index += NetUtil.memcpy(content,tabIdBytes,index);
        index += NetUtil.memcpy(content,subPageIndexBytes,index);
        index += NetUtil.memcpy(content,graphIdBytes,index);

        mSender.sendEntityPacket(content,Command.CC_DELETE_GRAPH);
    }

    public void sendCloseAllWb(long tabId,int subPageIndex){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        if(mSender!=null){
            mSender.sendRemoveAllTableMsg(tabId);
        }
    }

    /**
     * 当客户端 加入会议成功后 请求同步数据
     */
    public void requestSynchronous(){
        if(!sendEnable&&!NetUtil.isJoinMeeting){
            return;
        }
        mSender.requestSynchronous();
    }


    private static void cpyArrayToList(List<Byte> dit,byte src[]){
        for(byte b:src){
            dit.add(b);
        }
    }

    private void print(String msg){
//        TPLog.printError(msg);
    }
}
