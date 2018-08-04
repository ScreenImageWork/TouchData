package com.kedacom.osp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.util.Log;

import com.kedacom.osp.callback.OspCallback;
import com.kedacom.osp.entity.OspMsgEntity;
import com.kedacom.osp.entity.OspMsgHeadEntity;
import com.kedacom.osp.msg.MsgQueue;
import com.kedacom.osp.msg.SendMsg;
import com.kedacom.tplog.TPLog;

/**
 * Created by zhanglei on 2016/4/7.
 */
public class Osp extends Thread{

    private final static int TIMEOUT = 10*1000;  //通信阻塞超时时间

    private final static int CONNECT_TIMEOUT = 10*1000; //链接超时时间

    private ExecutorService executorService;//线程池，用于消息处理

    private Socket socket;

    private InputStream in;

    private OutputStream out;

    private char dstInsId  = 0xfffc;

    private boolean isDisplayHead = false;

    private OspCallback callback;

    private String ip;

    private int port;

    public Osp(String ip,int port){
        this(ip,port,null);
    }

    public Osp(String ip,int port,OspCallback callback){
        TPLog.printError("初始化OSP。。。");
        this.callback = callback;
        this.ip = ip;
        this.port = port;

        executorService = Executors.newSingleThreadExecutor();

        mWriteThread = new WriteThread();
        mWriteThread.start();
    }
    
    public void connect(){
    	this.start();
    }
    
    //初始化socket
    private void initSocket() throws Exception {
            TPLog.printError("初始化Socket。。。");
            InetAddress addr = InetAddress.getByName(ip);
            InetSocketAddress isa = new InetSocketAddress(addr, port);
            socket = new Socket();
            socket.setKeepAlive(true);
            //读取数据阻塞超时时间
            socket.setSoTimeout(TIMEOUT);
            socket.connect(isa,CONNECT_TIMEOUT);
            if (socket != null) {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                //建立连接之后，发送连接消息到服务器
                callbackOspConnected();
            } else {
                callbackOspDisConnect();
            }
    }

    /**
     * 获取socket当前是否关闭
     * @return
     */
    public boolean isConnect(){
        if(socket==null){
            return false;
        }
        
        boolean boo = socket.isClosed();
        
        return !boo;
    }

    @Override
    public void run() {
        try {
            //Android 特点所有的网络访问必须放到子线程中执行否则会报错
            initSocket();

            if(socket==null||!socket.isConnected()){
                return;
            }
            TPLog.printError("Socket与服务端建立链接成功。。。");
            while(!socket.isClosed()){
                //读取消息头
                byte head[] = new byte[39];
                int len = in.read(head);
                if(len!=39){//如果消息头不够39，那么一定就是消息出现异常
                    return;
                }

                //length 是从20位开始，取两个字节
                int length = OspUtils.getUshortFromBuf(head,20);
                byte content[] = new byte[length];
                if(length>0){
                    //由于接收到的消息有可能过大，不可能一次发送完毕，因此在这里进行了多次接收设置
                    byte buffer[] = new byte[length];
                    int tl = 0;
                    int l = 0;
                    while(tl<length){
                        //为了防止读取过多的数据，在这里设置缓存区的大小动态变化
                        buffer = new byte[length - tl];
                        l = in.read(buffer);
                        int index = tl;
                        for(int i=0;i<l;i++){
                            content[index++] = buffer[i];
                        }
                        tl = tl + l;
                    }
                }

                executorService.execute(new HandleMessage(head,content));
            }

            //socket 已经断开连接
            closeConnect();
        }catch(Exception e){//出现异常时，重置SocketConnect
            callbackOspException(e);
            try {
                if(socket!=null&&!socket.isClosed()){
                    socket.close();
                }
                callbackOspDisConnect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 发送消息 完整消息-> 消息头+消息体
     * @param data 需要发送的byte数组
     */
    public synchronized void writeMsg(byte[] data){
//        try {
//            if (out != null) {
////                TPLog.printKeyStatus("OSP写出消息。。。");
//                out.write(data);
//                out.flush();
//            }
//        }catch(Exception e){
//            TPLog.printKeyStatus("OSP写出数据出现异常。。。");
//            TPLog.printError(e);
//            callbackOspException(e);
//            e.printStackTrace();
//        }

        msgQueue.addMsg(new SendMsg(data));

//        data = null;
    }

    /**
     * 发送消息 完整消息-> 消息头+消息体
     * @param data 需要发送的byte数组
     */
    public synchronized void writeMsgToServer(byte[] data){
        try {
            if (out != null&&socket!=null&&!socket.isClosed()) {
//                TPLog.printKeyStatus("OSP写出消息。。。");
                out.write(data);
                out.flush();
            }
        }catch(Exception e){
            TPLog.printKeyStatus("OSP写出数据出现异常。。。");
            TPLog.printError(e);
            callbackOspException(e);
            e.printStackTrace();
        }

        data = null;
    }

    /**
     * 发送消息到服务端
     * @param contentMsg 需要发送的消息体
     */
    public void writeContentMsg(String contentMsg,char even){

        char contentlength = 0;
        byte content[] = null;

        if(contentMsg!=null&&!contentMsg.equals("")){
            content = contentMsg.getBytes();
        }

        if(content!=null){
            contentlength = (char)content.length;
        }

        byte head[] = makeMsgHead(contentlength, even);

        byte data[] = new byte[(contentlength+head.length)];

        int index = 0;
        for(int i=0;i<head.length;i++){
            data[index++] = head[i];
        }

        if(contentlength>0) {
            for (int i = 0; i < content.length; i++) {
                data[index++] = content[i];
            }
        }

        writeMsg(data);
    }

    /**
     * 发送消息到服务器
     * @param content 需要发送的字节数组
     * @param even 消息事件
     */
    public void writeContentMsg(byte content[],char even){
        char contentlength = 0;

        if(content != null){
            contentlength = (char)content.length;
        }

        byte head[] = makeMsgHead(contentlength, even);

        byte data[] = new byte[(content.length+head.length)];

        TPLog.printError("writeContentMsg","head.length="+head.length);
        TPLog.printError("writeContentMsg","contentlength="+(int)contentlength);
        TPLog.printError("writeContentMsg","content.length="+content.length);
        TPLog.printError("writeContentMsg","data.length="+data.length);

        int index = 0;
        for(int i=0;i<head.length;i++){
            data[index++] = head[i];
        }

        if(contentlength>0) {
            for (int i = 0; i < content.length; i++) {
                data[index++] = content[i];
            }
        }

        writeMsg(data);

    }

    /**
     * 发送一个消息内容为空的消息
     * @param even
     */
    public void writeEmptyMsg(char even){
        byte head[] = makeMsgHead((char)0, even);
        writeMsg(head);
    }

    /**
     * 创建消息头
     * @param conLength 消息体的长度
     * @param even 消息事件
     * @return 消息头
     */
    private byte[] makeMsgHead(char conLength,char even){
        byte head[] = new byte[39];
        byte srcNode[] = OspUtils.uintToByte(OspUtils.SRC_NODE);
        byte dstNode[] = OspUtils.uintToByte(OspUtils.DST_NODE);
        byte dstInsId[] = OspUtils.uintToByte(OspUtils.MAKEIID(OspUtils.DST_APPID, OspUtils.DST_CURRENT_INSID));
        byte srcInsId[] = OspUtils.uintToByte(OspUtils.MAKEIID(OspUtils.SRC_APPID, OspUtils.SRC_INSID));
        byte type[] = OspUtils.uShortToByte(OspUtils.MSG_TYPE);
        byte evens[] =  OspUtils.uShortToByte(even);
        byte length[] =  OspUtils.uShortToByte(conLength);
        byte content[] = {0,0,0,0,0,0,0,0};
        byte alias[] = {0,0,0,0,0,0,0,0};
        byte aliasLength[] = {0};

        int index = 0;
        //添加源节点
        for(int i=0;i<srcNode.length;i++){
            head[index++] = srcNode[i];
        }
        //添加目的节点
        for(int i=0;i<dstNode.length;i++){
            head[index++] = dstNode[i];
        }
        //添加目的实例
        for(int i=0;i<dstInsId.length;i++){
            head[index++] = dstInsId[i];
        }
        //添加源实例
        for(int i=0;i<srcInsId.length;i++){
            head[index++] = srcInsId[i];
        }
        //添加消息类型
        for(int i=0;i<type.length;i++){
            head[index++] = type[i];
        }
        //添加消息事件
        for(int i=0;i<evens.length;i++){
            head[index++] = evens[i];
        }
        //添加消息体事件
        for(int i=0;i<length.length;i++){
            head[index++] = length[i];
        }
        //添加消息体指针
        for(int i=0;i<content.length;i++){
            head[index++] = content[i];
        }
        //添加别名指针
        for(int i=0;i<alias.length;i++){
            head[index++] = alias[i];
        }
        //添加别名指针
        for(int i=0;i<aliasLength.length;i++){
            head[index++] = aliasLength[i];
        }
        return head;
    }


    /**
     * 响应服务端断链检测
     */
    private void rspCheckConnect(){
        byte data[] = new byte[39];

        byte srcNode[] = OspUtils.uintToByte(OspUtils.SRC_NODE);
        byte dstNode[] = OspUtils.uintToByte(OspUtils.DST_NODE);
        byte dstInsId[] = OspUtils.uintToByte(OspUtils.CHECK_CONNECT_INSID_4BYTE);
        byte srcInsId[] = OspUtils.uintToByte(OspUtils.CHECK_CONNECT_INSID_4BYTE);
        byte type[] = OspUtils.uShortToByte(OspUtils.MSG_TYPE);
        byte evens[] =  OspUtils.uShortToByte(OspUtils.EVEN_CHECK_CONNECT_RSP);
        byte length[] =  OspUtils.uShortToByte((char)0);
        byte content[] = {0,0,0,0,0,0,0,0};
        byte alias[] = {0,0,0,0,0,0,0,0};
        byte aliasLength[] = {0};

        int index = 0;
        //添加源节点
        for(int i=0;i<srcNode.length;i++){
            data[index++] = srcNode[i];
        }
        //添加目的节点
        for(int i=0;i<dstNode.length;i++){
            data[index++] = dstNode[i];
        }
        //添加目的实例
        for(int i=0;i<dstInsId.length;i++){
            data[index++] = dstInsId[i];
        }
        //添加源实例
        for(int i=0;i<srcInsId.length;i++){
            data[index++] = srcInsId[i];
        }
        //添加消息类型
        for(int i=0;i<type.length;i++){
            data[index++] = type[i];
        }
        //添加消息事件
        for(int i=0;i<evens.length;i++){
            data[index++] = evens[i];
        }
        //添加消息体事件
        for(int i=0;i<length.length;i++){
            data[index++] = length[i];
        }
        //添加消息体指针
        for(int i=0;i<content.length;i++){
            data[index++] = content[i];
        }
        //添加别名指针
        for(int i=0;i<alias.length;i++){
            data[index++] = alias[i];
        }
        //添加别名指针
        for(int i=0;i<aliasLength.length;i++){
            data[index++] = aliasLength[i];
        }

        writeMsgToServer(data);

    }

    /**
     * 关闭当前的连接
     */
    public void closeConnect(){
        try {
            removeCallback();

            writing = false;

            if(in!=null){
                in.close();
                in = null;
            }
            if(out!=null){
                out.close();
                out = null;
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                socket = null;
            }
            if(executorService!=null&&!executorService.isShutdown()){
                executorService.shutdown();
                executorService = null;
            }

            if(mWriteThread!=null){
                mWriteThread.interrupt();
            }
            //callbackOspDisConnect();
        }catch(Exception e){
            callbackOspException(e);
            e.printStackTrace();
        }
    }

    /**
     * 打印消息头
     * @param head
     */
    private OspMsgHeadEntity makeHeadEntity(byte head[]){
        long scrnode = OspUtils.getUintFromBuf(head, 0);
        long dstnode = OspUtils.getUintFromBuf(head, 4);//节点ID
        long dstid = OspUtils.getUintFromBuf(head,8);
        long srcid = OspUtils.getUintFromBuf(head, 12);
        int type = OspUtils.getUshortFromBuf(head, 16);
        int even = OspUtils.getUshortFromBuf(head, 18);
        int length = OspUtils.getUshortFromBuf(head, 20);

        int srcInsId = OspUtils.getInsId(srcid);
        int dstInsId = OspUtils.getInsId(dstid);

        OspMsgHeadEntity headEntity = new OspMsgHeadEntity();
        headEntity.setContentLength(length);
        headEntity.setDstIns(dstInsId);
        headEntity.setDstNode(dstnode);
        headEntity.setEvent(even);
        headEntity.setMsgType(type);
        headEntity.setSourceIns(srcInsId);
        headEntity.setSourceNode(scrnode);
        if(isDisplayHead) {
            String msg = "scrnode=" + scrnode + ",dstnode=" + dstnode + ",type=" + type + ",even=" + even + ",srcInsId=" + srcInsId+",dstInsId="+dstInsId;
            Log.e("msg", msg);
        }
        return headEntity;
    }


    /**
     * 消息处理线程
     */
    class HandleMessage implements Runnable{
        private OspMsgEntity mOspMsgEntity;
        private OspMsgHeadEntity headEntity;
        public HandleMessage(byte head[],byte content[]){
            headEntity = makeHeadEntity(head);
            mOspMsgEntity = new OspMsgEntity();
            mOspMsgEntity.setMsgHead(headEntity);
            mOspMsgEntity.setContent(content);
        }
        @Override
        public void run() {
            try {
                int even = headEntity.getEvent();
                if(even == OspUtils.EVEN_CHECK_CONNECT_REQ){
                    rspCheckConnect();
                }else{
                    //获取对端InsId
                    if (OspUtils.DST_CURRENT_INSID != headEntity.getSourceIns()) {
                        OspUtils.DST_CURRENT_INSID = (char)headEntity.getSourceIns();
                    }

                    if(OspUtils.SRC_NODE!=headEntity.getSourceNode()){
                        //读取服务端的目标InsId 并赋值给本地的源InsId
                        OspUtils.SRC_NODE = (int)headEntity.getSourceNode();
                    }
                    callbackOspReceiveMsg(mOspMsgEntity);
                }

            }catch(Exception e){
                callbackOspException(e);
                e.printStackTrace();
            }
        }
    }


    public void displayHead(boolean isDisplayHead){
        this.isDisplayHead = isDisplayHead;
    }

    public void setCallback(OspCallback callback){
        this.callback = callback;
    }

    public void removeCallback(){
        callback = null;
    }

    private void callbackOspConnected(){
        if(callback!=null){
            callback.ospConnected();
        }
    }

    private void callbackOspDisConnect(){
        Log.e("msg","callbackOspDisConnect");
        if(callback!=null){
            callback.ospDisconnect();
        }
    }

    private void callbackOspReceiveMsg(OspMsgEntity msg){
        if(callback!=null){
            callback.ospReceiveMsg(msg);
        }
    }

    private void callbackOspException(Exception e){
        if(callback!=null){
            callback.ospException(e);
        }
    }


    private boolean writing = true;
    private MsgQueue<SendMsg> msgQueue = new MsgQueue<SendMsg>();
    private WriteThread mWriteThread;
    class WriteThread extends Thread{
        @Override
        public void run() {
            TPLog.printError("WriteThread birth...");
            while(writing){
                try {
                    SendMsg sm = msgQueue.nextMsg();
                    writeMsgToServer(sm.getMsg());
                    sm = null;
                }catch(Exception e){
                    TPLog.printError("发送数据时出现异常："+e);
                    TPLog.printError(e);
                }
            }
            TPLog.printError("WriteThread dead...");
        }
    }
}
