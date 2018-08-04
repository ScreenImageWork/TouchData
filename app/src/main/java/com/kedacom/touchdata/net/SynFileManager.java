package com.kedacom.touchdata.net;


import java.io.File;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Message;
import android.util.Log;

import com.kedacom.osp.OspUtils;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.net.utils.Command;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.ISubPage;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.TPTimer;

/**
 * Created by zhanglei on 2016/3/18.
 * 主要用于同步文件工作
 */
public class SynFileManager {

    private static SynFileManager mSynFileManager;

    private List<Long> serverExistFile = new ArrayList<Long>();

    private PageManager mPageManager;

    private NetHandler mNetHandler;

    private Sender mSender;

    //2017.07.04添加， 以后下载列表由SynFileManager进行维护
    private  List<Image> imgList = new ArrayList<Image>();

    //不想对以前的代码进行大面积的修改了，因此在这里添加了图片ID集合，主要是用于保存图片下载任务
    private List<Integer> imgIdList = new ArrayList<Integer>();

    private TPTimer mTimer = new TPTimer(){
        @Override
        public void onTPTimerTask() {
            TPLog.printError("下载文件超时。。。");
            reDownloadCurFile();
        }
    };

    public boolean containsImage(long imgId){
        int imgCount = imgList.size();
        for(int i = 0;i<imgCount;i++){
            if(imgList.get(i).getId() == imgId){
                return true;
            }
        }
        return false;
    }

    public void addImage(Image img){
        if(!imgList.contains(img)){
            imgList.add(img);
        }
    }

    public Image getImage(long imgId){
        int imgCount = imgList.size();
        for(int i = 0;i<imgCount;i++){
            if(imgList.get(i).getId() == imgId){
                return imgList.get(i);
            }
        }
        return null;
    }

    public static SynFileManager getInstance(){
        if(mSynFileManager==null){
            mSynFileManager = new SynFileManager();
        }

        return mSynFileManager;
    }

    public void init(PageManager manager,NetHandler handler){
        mPageManager = manager;
        mNetHandler = handler;
    }

    public void setPageManager(PageManager manager){
        mPageManager = manager;
    }

    public void setNetHandler(NetHandler handler){
        mNetHandler = handler;
    }

    public void setSender(Sender sender){
        mSender = sender;
    }


    public void addServerExistFile(long imageId){
        TPLog.printError("数据会议服务器已经存在 "+imageId +" 文件");
        if(!isServerContainsFile(imageId)){
            serverExistFile.add(imageId);
        }
    }

    public void removeServerExistFile(long imageId){
        if(serverExistFile.contains(imageId)){
            serverExistFile.remove(imageId);
        }
    }

    public boolean isServerContainsFile(long imageId){
        return serverExistFile.contains(imageId);
    }

    /**
     * 解析文件信息   调用此方法时图片对象已经存在
     */
    public void parseFileInfo(byte data[]){
        if(data==null){return;}
        int index = 0;
        long msgLength = OspUtils.getUintFromBuf(data,index);
        index+=4;
        byte event = data[index++];
        long fileSize = OspUtils.getUintFromBuf(data,index);
        index+=4;
        long dwTimeId = OspUtils.getUintFromBuf(data,index);//图源Id
        index+=4;

        int nameLength = (int)msgLength - 4-4-1-1-1;

        byte nameData[] = new byte[nameLength];

        NetUtil.memcpy(nameData, data, 0, index, nameLength);

        String fileName = "";
        // Log.e("msg","parseFileInfo------------------->"+event);
        try {
            // fileName = new String(nameData,"Unicode");
            fileName = new String(nameData,"GBK");
            int i = fileName.lastIndexOf("/");
            if(i>=0){
                fileName = fileName.substring(i+1,fileName.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String filePath = FileUtils.REC_IMAGE_DIR + fileName;
        Image mImageEntity = null;

        if(!containsImage((int) dwTimeId)){
            mImageEntity = new Image();
            mImageEntity.setFileName(fileName);
            mImageEntity.setFileSize(fileSize);
            mImageEntity.setId((int) dwTimeId);
            mImageEntity.setFilePath(filePath);
            imgList.add(mImageEntity);
            if(serverExistFile.contains(dwTimeId)){
                mImageEntity.setExistOnServer(true);
            }
        }else{
            mImageEntity = getImage((int) dwTimeId);
            mImageEntity.setFileName(fileName);
            mImageEntity.setFileSize(fileSize);
            mImageEntity.setFilePath(filePath);
            if(serverExistFile.contains(dwTimeId)){
                mImageEntity.setExistOnServer(true);
            }
            TPLog.printError("parseFileInfo fileSize:"+fileSize);
            TPLog.printError("parseFileInfo filePath:"+filePath);
        }

        int id = mImageEntity.getId();

        ArrayList<IPage> pageList = mPageManager.getPageList();
        for(int i = 0;i<pageList.size();i++){
            ArrayList<SubPage> subPageList = pageList.get(i).getSubPageList();
            for(int j = 0;j<subPageList.size();j++){
                ArrayList<Graph> graphList = subPageList.get(j).getImageGraphList();
                for(int k = 0;k<graphList.size();k++){
                    Graph graph = graphList.get(k);
                    if(graph.getId() == id){
                        ((ImageGraph)graph).setImgPath(mImageEntity.getFilePath());
                    }
                }
            }
        }
//       ArrayList<Graph> imageList =  mPageManager.getCurSelectSubPage().getImageGraphList();
//        int count = imageList.size();
//        for(int i = 0;i<count;i++){
//            if((imageList.get(i)).getId() == id){
//                ((ImageGraph)imageList.get(i)).setImgPath(mImageEntity.getFilePath());
//            }
//        }

        //拿到文件信息后就去请求文件内容
        requestFileBody(mImageEntity);
    }

    /**
     * 解析文件数据
     * @param data
     */
    public void parseFileBody(byte data[]){
        if(data==null)return;
        try {

            int index = 0;
            long msgLength = OspUtils.getUintFromBuf(data, index);
            index += 4;
            byte event = data[index++];
            long dwTimeId = OspUtils.getUintFromBuf(data, index);
            index += 4;
            long dwCurBlock = OspUtils.getUintFromBuf(data, index); //已经发送的字节数
            index += 4;
            long dwBlockSize = OspUtils.getUintFromBuf(data, index); //本次发送文件内容的字节数
            index += 4;

            String fileName = "";
            Image ie = getImage((int) dwTimeId);
            if (containsImage((int) dwTimeId)) {
                //curBlock = fileMaps.get((int) dwTimeId).getDwCurBlock();//获取当前写入文件的字节数
                fileName = ie.getFileName();
            }

            if (fileName==null||fileName.equals("")) {
                return;
            }


            String filePath = ie.getFilePath();

            File file = new File(filePath);
            if(file.exists()&&dwCurBlock==0){
                file.delete();
            }

            //将文件保存在本地
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

            raf.seek(dwCurBlock);

            raf.write(data, index, (int) dwBlockSize);

            raf.close();

            ie.setDwCurBlock((int) dwCurBlock + (int) dwBlockSize); //统计总的字节数

            data = null;

            raf = null;

            //判断文件是否下载完毕，如果没有下载完毕将继续请求
            if(ie.isDlSuccess()){
                TPLog.printError("parseFileBody->"+ie.getId()+"下载："+ie.getDwCurBlock()+"/"+ie.getFileSize());
                TPLog.printError("parseFileBody->"+ie.getId()+"已经下载完成");
                TPLog.printError("parseFileBody->保存路径:"+filePath);
                ie.setExistOnServer(true);
                sendHandlerMessage(NetHandler.IMAGE_DOWNLOAD, dwTimeId);
                downloadNext(ie);
                mTimer.cancelTimer();
                return;
            }else{
                TPLog.printError("parseFileBody->"+ie.getId()+"下载："+ie.getDwCurBlock()+"/"+ie.getFileSize());
            }

            requestFileBody(ie);

        }catch(Exception e){
            TPLog.printError("解析图片内容出现异常：");
            TPLog.printError(e);
        }
    }

    /**
     * 解析打开文件时，文件同步信息 文件打开时，图片的信息会一张一张的传递过来
     * @param data
     */
    public void parseImageInfo(byte data[]){

        if(data==null){
            return;
        }

        int index = 0;
        long contentLength = OspUtils.getUintFromBuf(data, index);
        index+=4;
        byte event = data[index++];
        long lastLength = OspUtils.getUintFromBuf(data,index);
        index+=4;

        byte cData[] = new byte[(int)lastLength];

        index+=NetUtil.memcpy(cData,data,0,index,(int)lastLength);

        if((index+1)!=(contentLength+4)){ //解析数据错误
            return;
        }
        // Log.e("msg","parseImageInfo------------------->"+event);
        //解压数据
        byte ucData[] = NetUtil.UnCompressBuffer(cData);

        addImageEntityFromArray(ucData);
        data = null;
        cData = null;
        ucData = null;
    }


    /**
     * 接收到服务发送过来的图片信息  一张
     * @param ucData
     */
    public void addImageEntityFromArray(byte ucData[]){
        int index = 0;
        long tabId = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long curPagePos = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long dwTimeId = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long m_bBlock = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long m_bRegionHasChanged  = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long m_bNeedRollBack  = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long x  = OspUtils.getUintFromBuf(ucData,index); //图片显示的X坐标
        index+=4;
        long y  = OspUtils.getUintFromBuf(ucData,index); //图片显示y坐标
        index+=4;
        long imageWidth  = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long imageHeight  = OspUtils.getUintFromBuf(ucData,index);
        index+=4;
        long netWorkState = OspUtils.getUintFromBuf(ucData,index);
        index+=4;

        int fileNameLength = ucData.length - index - 4 - 2;

        byte byteFileName[] = new byte[fileNameLength];

        index+=NetUtil.memcpy(byteFileName,ucData,0,index,fileNameLength);

        String fileName = "";
        try {
            //fileName = new String(byteFileName,"Unicode");
            fileName = new String(byteFileName);
            int i = fileName.lastIndexOf("/");
            if(i>=0){
                fileName = fileName.substring(i+1,fileName.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        index+=2;//跳过中间的两个用于表示名字结尾的0

        long fdwHasFileBody = OspUtils.getUintFromBuf(ucData,index);

        if(containsImage((int)dwTimeId)){
            return;
        }

        Image ie = new Image();
        ie.setFileName(fileName);
        //ie.setFilePath(FileUtils.REC_IMAGE_DIR + fileName);
        ie.setId((int) dwTimeId);
        ie.setHeight((int) imageHeight);
        ie.setWidth((int) imageWidth);
        ie.setX((int) x);
        ie.setY((int) y);
        ie.setSubpageIndex((int) curPagePos);

//        TPLog.printError("fileName="+fileName);
//        TPLog.printError("dwTimeId="+dwTimeId);
//        TPLog.printError("imageHeight="+imageHeight);
//        TPLog.printError("imageWidth="+imageWidth);
//        TPLog.printError("x="+x);
//        TPLog.printError("y="+y);


        addImage(ie);

        if(serverExistFile.contains(dwTimeId)){
            ie.setExistOnServer(true);
        }
    }

    /**
     * 请求文件信息
     * @param ie
     */
    public void requestFileInfo(Image ie){
        mTimer.reStartTimer();
        if(ie==null)return;
        int contentLength = 6;
        byte event = Command.CS_GETFILEINFO;
        int dwTimeId = ie.getId();
        byte msgEnd = Command.MSG_TERM;

        byte reqFileInfoData[] = new byte[contentLength+4];

        int index = 0;
        index += NetUtil.memcpy(reqFileInfoData,OspUtils.uintToByte(contentLength),index);
        reqFileInfoData[index++] = event;
        index += NetUtil.memcpy(reqFileInfoData,OspUtils.uintToByte(dwTimeId),index);
        reqFileInfoData[index++] = msgEnd;

        mSender.sendMsg(reqFileInfoData, NetUtil.EVEN_SYNCHRONOUS_REQ);

//        Log.e("msg", "请求文件信息--------------------》");
//        Utils.displayArray(reqFileInfoData);
    }

    public synchronized void requestDownload(int imgId){
        TPLog.printError("requestFileInfo->请求下载图片:"+imgId+",当前有 "+imgIdList.size()+" 张图片正在等待中！");
        if(!imgIdList.isEmpty()){
            if(!imgIdList.contains(imgId)){
                imgIdList.add(imgId);
            }
            return;
        }

        imgIdList.add(imgId);

        requestFileInfo(imgId);
    }

    public synchronized void reDownloadCurFile(){
        mTimer.cancelTimer();
        if(imgIdList.isEmpty()){
            TPLog.printError("现在列表为空，不需要重新下载！");
            return;
        }
        int imgId = imgIdList.remove(0);
        TPLog.printError("重新添加文件到下载列表："+imgId);
        imgIdList.add(imgId);
        requestFileInfo(imgIdList.get(0));
    }

    /**
     * 请求文件信息
     * @param imgId
     */
    public void requestFileInfo(final int imgId){
        new Thread(){
            @Override
            public void run() {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                mTimer.reStartTimer();

                TPLog.printError("开始下载图片："+imgId);
//                mTimer.startTimer();
                int contentLength = 6;
                byte event = Command.CS_GETFILEINFO;
                int dwTimeId = imgId;
                byte msgEnd = Command.MSG_TERM;

                byte reqFileInfoData[] = new byte[contentLength+4];

                int index = 0;
                index += NetUtil.memcpy(reqFileInfoData,OspUtils.uintToByte(contentLength),index);
                reqFileInfoData[index++] = event;
                index += NetUtil.memcpy(reqFileInfoData,OspUtils.uintToByte(dwTimeId),index);
                reqFileInfoData[index++] = msgEnd;

                mSender.sendMsg(reqFileInfoData, NetUtil.EVEN_SYNCHRONOUS_REQ);
            }
        }.start();

    }

    /**
     * 请求文件  将会分段请求文件，一次请求文件内容最大32000个字节
     * @param ie
     */
    public void requestFileBody(Image ie){
        //  Log.d("msg","requestFileBody----------");
        mTimer.reStartTimer();
        if(ie==null)return;

        int contentLength = 14;
        byte event = Command.CC_REQUESTFILEBODY;
        int dwTimeId = ie.getId();
        int dwCurBlock = ie.getDwCurBlock();
        int dwBlockSize  = ie.getDwBlockSize();
        byte msgEnd = Command.MSG_TERM;

        byte reqFileBodyData[] = new byte[contentLength+4];

        int index = 0;
        index += NetUtil.memcpy(reqFileBodyData,OspUtils.uintToByte(contentLength),index);
        reqFileBodyData[index++] = event;
        index += NetUtil.memcpy(reqFileBodyData,OspUtils.uintToByte(dwTimeId),index);
        index += NetUtil.memcpy(reqFileBodyData,OspUtils.uintToByte(dwCurBlock),index);
        index += NetUtil.memcpy(reqFileBodyData,OspUtils.uintToByte(dwBlockSize),index);
        reqFileBodyData[index++] = msgEnd;

        mSender.sendMsg(reqFileBodyData,NetUtil.EVEN_SYNCHRONOUS_REQ);
    }

    /**
     * 发送图片信息， 打开文件时调用
     * @param ie
     */
    public void sendImageInfo(Image ie){

        if(ie==null){
            return;
        }

        imgList.add(ie);

        long tabId = mPageManager.getSelectPage().getId();
        int pageIndex = ie.getSubpageIndex();
        int dwTimeId = ie.getId();
        int m_bBlock = 1;
        int m_bRegionHasChanged = 1;
        int m_bNeedRollBack = 0;
        int x = (int)ie.getX();
        int y = (int)ie.getY();
        int width = (int)ie.getWidth();
        int height = (int)ie.getHeight();
        int m_nNetWorkState = 4;
        byte fileName[] =null;
        try {
            fileName = ie.getFileName().getBytes("Unicode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int fdwHasFileBody = 0;

        int contentLength = 4*12+fileName.length+2;

        byte content[] = new byte[contentLength];

        int index = 0;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(pageIndex),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(dwTimeId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bBlock),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bRegionHasChanged),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bNeedRollBack),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(x),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(y),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(width),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(height),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_nNetWorkState),index);
        index+=NetUtil.memcpy(content,fileName,index);
        index+=2;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(fdwHasFileBody),index);

//        Log.e("msg", "压缩之前的数据：");
//        Utils.displayArray(content);

        byte cContent[] = NetUtil.CompressBuffer(content);

//        Log.e("msg", "压缩之后的数据：");
//        Utils.displayArray(cContent);

        content = null;

        mSender.sendEntityPacket(cContent,Command.CC_IMAGE);
    }


    /**
     * 发送图片信息， 打开文件时调用
     * @param ie
     */
    public void sendImageInfoForJoinMeeting(Image ie){

        if(ie==null){
            return;
        }

        imgList.add(ie);

        long tabId = mPageManager.getSelectPage().getId();
        int pageIndex = ie.getSubpageIndex();
        int dwTimeId = ie.getId();
        int m_bBlock = 1;
        int m_bRegionHasChanged = 1;
        int m_bNeedRollBack = 0;
        int x = (int)ie.getX();
        int y = (int)ie.getY();
        int width = (int)ie.getWidth();
        int height = (int)ie.getHeight();
        int m_nNetWorkState = 4;
        byte fileName[] =null;
        try {
            fileName = ie.getFileName().getBytes("Unicode");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        int fdwHasFileBody = 0;

        int contentLength = 4*12+fileName.length+2;

        byte content[] = new byte[contentLength];

        int index = 0;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(tabId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(pageIndex),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(dwTimeId),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bBlock),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bRegionHasChanged),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_bNeedRollBack),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(x),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(y),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(width),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(height),index);
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(m_nNetWorkState),index);
        index+=NetUtil.memcpy(content,fileName,index);
        index+=2;
        index+=NetUtil.memcpy(content,OspUtils.uintToByte(fdwHasFileBody),index);

//        Log.e("msg", "压缩之前的数据：");
//        Utils.displayArray(content);

        byte cContent[] = NetUtil.CompressBuffer(content);

//        Log.e("msg", "压缩之后的数据：");
//        Utils.displayArray(cContent);

        content = null;

        mSender.sendEntityPacket(cContent,Command.CC_IMAGE);
    }


    /**
     * 发送文件信息
     * @param requestData
     */
    public void sendFileInfo(byte requestData[]){

        if(requestData==null){
            return;
        }

        long dwTimeId = OspUtils.getUintFromBuf(requestData,6);
        Image ie = getImage((int) dwTimeId);
        sendFileInfo(ie);
    }

    /**
     * 发送文件信息
     * @param ie
     */
    public void sendFileInfo(Image ie){
        if(ie==null){ //不存在该图片就不进行处理
            return;
        }

        byte event = Command.SOCK_FILEINFO;
        long fileSize = ie.getFileSize();
        String fileNameStr = ie.getFileName();

        byte end = Command.SOCK_TERM;

//        try {
//            fileNameBytes = fileNameStr.getBytes("Unicode");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

        byte fileNameBytes[] = fileNameStr.getBytes();

        int contentLength = 1+4+4+fileNameBytes.length+1+1;

        byte data[] = new byte[contentLength+4];

        int index = 0;
        index += NetUtil.memcpy(data,OspUtils.uintToByte(contentLength),index); //消息长度
        data[index++] = event;                                               //消息号
        index += NetUtil.memcpy(data,OspUtils.uintToByte(fileSize),index);      //文件长度
        index += NetUtil.memcpy(data,OspUtils.uintToByte(ie.getId()),index);      //文件ID
        index += NetUtil.memcpy(data,fileNameBytes,index);//文件名称
        index++; //文件名称后面有个0
        data[index++] = end;                                                 //消息结束位

        // NetUtil.displayArray(data);
        mSender.sendMsg(data,NetUtil.EVEN_SYNCHRONOUS_REQ);
    }

    /**
     * 发送一张完整的图片到服务器
     * @param ie
     */
    public void sendImageBodyFirstJoinMeeting(Image ie){

        if(ie==null){
            return;
        }

        String filePath = ie.getFilePath();

        long fileSize = ie.getFileSize();

        int dwBlockSize = ie.getDwBlockSize();

        //分包
        int count =  (int)(fileSize/ie.getDwBlockSize());
        count = fileSize%dwBlockSize==0?count:count+1;
        long dwCurBlock = 0;
        for(int i=0;i<count;i++){
            sendFileBodyFirstJoinMeeting(ie,dwCurBlock,dwBlockSize);
            dwCurBlock = dwCurBlock + dwBlockSize;
        }

        //发送文件发送结束命令
        mSender.sendFileEndMsg();
    }

    /**
     * 发送一张完整的图片到服务器
     * @param ie
     */
    public void sendImageBody(Image ie){

        if(ie==null){
            return;
        }

        String filePath = ie.getFilePath();

        long fileSize = ie.getFileSize();

        int dwBlockSize = ie.getDwBlockSize();

        //分包
        int count =  (int)(fileSize/ie.getDwBlockSize());
        count = fileSize%dwBlockSize==0?count:count+1;
        long dwCurBlock = 0;
        for(int i=0;i<count;i++){
            sendFileBody(ie,dwCurBlock,dwBlockSize);
            dwCurBlock = dwCurBlock + dwBlockSize;
        }

        //发送文件发送结束命令
        mSender.sendFileEndMsg();
    }


    /**
     * 响应请求，发送文件内容
     * @param requestData
     */
    public void sendFileBody(byte requestData[]){
        int index = 6;
        long dwTimeId = OspUtils.getUintFromBuf(requestData, index);
        index+=4;
        long dwCurBlock = OspUtils.getUintFromBuf(requestData, index);
        index+=4;
        long dwBlockSize = OspUtils.getUintFromBuf(requestData, index);

        Image ie=getImage((int) dwTimeId);
        if(ie==null||!ie.isDlSuccess()){//该文件不存在，或者没有下载完毕就不进行处理
            return;
        }

        sendFileBody(ie, dwCurBlock, dwBlockSize);
    }

    /**
     * 发送一个完整的图片文件 文件信息+文件内容
     */
    public void sendImage(final Image ie){
        new Thread(){
            @Override
            public void run() {
                sendFileInfo(ie);
                sendImageBody(ie);
                ie.setExistOnServer(true);
            }
        }.start();
    }

    /**
     * 发送一个完整的图片文件 文件信息+文件内容
     */
    public void sendImageFirstJoinMeeting(final Image ie){
//        new Thread(){
//            @Override
//            public void run() {
                sendFileInfo(ie);
                sendImageBodyFirstJoinMeeting(ie);
                ie.setExistOnServer(true);
//            }
//        }.start();
    }




    //发送文件内容
    private void sendFileBody(Image ie,long dwCurBlock,long dwBlockSize){
        try {

//            if(OspUtils.currentServerRecFlow==0){ //如果返回的流量是0就继续去请求
//                ConnectManager.getInstance().requestServerRecFlow();
//                return;
//            }

            // int packetSize = OspUtils.currentServerRecFlow/4>=OspUtils.PACKET_MAX_SIZE?OspUtils.PACKET_MAX_SIZE:OspUtils.currentServerRecFlow/4;

            String filePath = ie.getFilePath();

            long fileSize = ie.getFileSize();
            dwBlockSize = dwBlockSize>fileSize-dwCurBlock?fileSize-dwCurBlock:dwBlockSize;
            //计算发送包的数量
//            int packetCount = (int)(dwBlockSize/packetSize);
//            packetCount = dwBlockSize%packetSize==0?packetCount:packetCount+1;

            //将文件保存在本地
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

            raf.seek(dwCurBlock);

            byte fileContent[] = new byte[(int)dwBlockSize];

            raf.read(fileContent);

            raf.close();

            //将文件内容发送给服务器

            byte event  = Command.SOCK_FILEBODY;

            int contentLength = 1+4+1+fileContent.length;

            byte data[] = new byte[contentLength+4];

            int index = 0;
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(contentLength),index);
            data[index++] = event;
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(ie.getId()),index);
            index+=NetUtil.memcpy(data,fileContent,index);
            data[index] = Command.MSG_TERM;

            mSender.sendMsg(data, NetUtil.EVEN_SYNCHRONOUS_REQ);

            //Log.e("msg", "文件内容数据：");
            //Utils.displayArray(data);

        }catch(Exception e){
            Log.e("msg","发送文件时出现异常："+e);
        }
    }
    //发送文件内容
    private void sendFileBodyFirstJoinMeeting(Image ie,long dwCurBlock,long dwBlockSize){
        try {

//            if(OspUtils.currentServerRecFlow==0){ //如果返回的流量是0就继续去请求
//                ConnectManager.getInstance().requestServerRecFlow();
//                return;
//            }

            // int packetSize = OspUtils.currentServerRecFlow/4>=OspUtils.PACKET_MAX_SIZE?OspUtils.PACKET_MAX_SIZE:OspUtils.currentServerRecFlow/4;

            String filePath = ie.getFilePath();

            long fileSize = ie.getFileSize();
            dwBlockSize = dwBlockSize>fileSize-dwCurBlock?fileSize-dwCurBlock:dwBlockSize;
            //计算发送包的数量
//            int packetCount = (int)(dwBlockSize/packetSize);
//            packetCount = dwBlockSize%packetSize==0?packetCount:packetCount+1;

            //将文件保存在本地
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

            raf.seek(dwCurBlock);

            byte fileContent[] = new byte[(int)dwBlockSize];

            raf.read(fileContent);

            raf.close();

            //将文件内容发送给服务器

            byte event  = Command.SOCK_FIRST_SAVE_FILE;

            int contentLength = 1+4+1+fileContent.length;

            byte data[] = new byte[contentLength+4];

            int index = 0;
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(contentLength),index);
            data[index++] = event;
            index+=NetUtil.memcpy(data,OspUtils.uintToByte(ie.getId()),index);
            index+=NetUtil.memcpy(data,fileContent,index);
            data[index] = Command.MSG_TERM;

            mSender.sendMsg(data, NetUtil.EVEN_SYNCHRONOUS_REQ);

            //Log.e("msg", "文件内容数据：");
            //Utils.displayArray(data);

        }catch(Exception e){
            Log.e("msg","发送文件时出现异常："+e);
        }
    }

    private void sendHandlerMessage(int what,Object obj){
        if(mNetHandler==null){
            return;
        }

        Message msg = mNetHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        mNetHandler.sendMessageDelayed(msg,500);
//        msg.sendToTarget();
    }

    private void downloadNext(Image ie){
//        mTimer.cancelTimer();
        imgList.remove(ie);
        imgIdList.remove(Integer.valueOf(ie.getId()));
        //如果还有图片没有下载，那么就继续执行图片下载任务
        if(!imgIdList.isEmpty()){
            requestFileInfo(imgIdList.get(0));
        }
    }

    public void uploadImg(final PageManager mPageManager){
        new Thread(){
            @Override
            public void run() {
                ArrayList<IPage> list = mPageManager.getPageList();
                if(list==null||list.isEmpty()){
                    return;
                }
                for(int i = 0;i<list.size();i++){
                    IPage page = list.get(i);
                    ArrayList<SubPage> subPageList = page.getSubPageList();
                    if(subPageList==null||subPageList.isEmpty()){
                        continue;
                    }
                    for(int j = 0;j<subPageList.size();j++){
                        SubPage subPage = subPageList.get(j);
                        if(subPage==null){
                            continue;
                        }
                        ArrayList<Graph> imgList = subPage.getImageGraphList();
                        if(imgList==null||imgList.isEmpty()){
                            continue;
                        }
                        for(int k = 0;k<imgList.size();k++){
                            ImageGraph imgGraph = (ImageGraph)imgList.get(k);
                            if(imgGraph == null){
                                continue;
                            }
                            TPLog.printError("上传图片："+imgGraph.getId());
                            // mSendHelper.sendInsertImgMsg(imgGraph); // 发送图片图元基本信息
                            Image image = WhiteBoardUtils.createImage(imgGraph.getImgPath(),true);
                            image.setId(imgGraph.getId());
                            image.setSubpageIndex(0);
//                        SynFileManager.getInstance().sendImageInfo(image); //发送图片信息
                            sendImageFirstJoinMeeting(image); //发送图片文件
                            try {//延时发送下一张图片到服务器
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }.start();

    }

    public void reset(){
        imgIdList.clear();
        imgList.clear();
    }

    public boolean isDownloading(int id){
        return imgIdList.contains(id);
    }
}
