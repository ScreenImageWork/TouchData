package com.kedacom.touchdata.net.mtnet;

import com.kedacom.kdv.mt.mtapi.ConfCtrl;
import com.kedacom.kdv.mt.mtapi.ConfigCtrl;
import com.kedacom.kdv.mt.mtapi.DcsCtrl;
import com.kedacom.kdv.mt.mtapi.IMtcCallback;
import com.kedacom.kdv.mt.mtapi.MtcLib;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.net.mtnet.entity.DownloadInfo;
import com.kedacom.touchdata.net.mtnet.entity.MtEntity;
import com.kedacom.touchdata.whiteboard.data.PageManager;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.Image;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.helper.EraseHelper;
import com.kedacom.touchdata.whiteboard.page.IPage;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.tplog.TPLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/3/19.
 */

public class MtNetSender {

    private boolean init = false;

    MtNetSender(){
        TPLog.printError("->MtNetSender init...");
    }

    /**
     * 连接终端
     * @param mtIp 终端Ip
     * @param mtUserName 终端连接用户名
     * @param mtPwd 终端连接密码
     * @param mtPort 终端连接端口
     */
    public void connectMt(final String mtIp, final String mtUserName, final String mtPwd, final int mtPort, final IMtcCallback callback){
        new Thread(){
            @Override
            public void run() {
                TPLog.printError("connect mt begin....");

                TPLog.printKeyStatus("mtIp->"+mtIp);
                TPLog.printKeyStatus("mtUserName->"+mtUserName);
                TPLog.printKeyStatus("mtPwd->"+mtPwd);
                TPLog.printKeyStatus("mtPort->"+mtPort);

                MtcLib.start();
                JSONObject tagTMtcLoginParam_Api = new JSONObject();
                try {
                    tagTMtcLoginParam_Api.put("emAppType", 9);
                    tagTMtcLoginParam_Api.put("emAuthMode",1);
                    tagTMtcLoginParam_Api.put("achUsrName",mtUserName);
                    tagTMtcLoginParam_Api.put("achPwd",mtPwd);
                    tagTMtcLoginParam_Api.put("achMtIp",mtIp);
                    tagTMtcLoginParam_Api.put("wMtListenPort",mtPort);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TPLog.printError("Mt connectMt -> tagTMtcLoginParam_Api = "+tagTMtcLoginParam_Api.toString());
                if(!init) { //这块感觉不用每次重连都调用，只调用一次应该就可以了
                    init = true;
                    MtcLib.Start(true, false, new StringBuffer(tagTMtcLoginParam_Api.toString()));
                    MtcLib.Setcallback(callback);
                }
//                StringBuffer strMtIp= new StringBuffer(mtIp);
//                StringBuffer userName= new StringBuffer(mtUserName);
//                StringBuffer password = new StringBuffer(mtPwd);
//                int nMtcType = 2;//1表示mtc，2表示pcdv，3表示nct ( emMtLocMode_Api模式无效 )
                int rst = MtcLib.Connect(MtNetUtils.strSessionId,new StringBuffer(tagTMtcLoginParam_Api.toString()));
                //int rst = MtcLib.Connect(MtNetUtils.strSessionId,strMtIp,mtPort,userName,password,nMtcType);
      //        int rst = MtcLib.Connect(MtNetUtils.strSessionId,strMtIp,mtPort,new StringBuffer(),new StringBuffer(),nMtcType);
                TPLog.printError("connect rst -> " + rst);
                if(rst == 0){
                    try {
                        JSONObject rstJson = new JSONObject(MtNetUtils.strSessionId.toString());
                        MtNetUtils.sessionId = rstJson.getInt("basetype");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                TPLog.printError("connect mt end...");
            }
        }.start();

    }

    /**
     * 断开与终端的连接
     */
    public void disConnectMt(){
        MtcLib.DisConnect( MtNetUtils.sessionId);
        MtNetUtils.sessionId = -1;
    }

    /**
     * 获取会议信息
     */
    public void requestConfInfo(){
        TPLog.printError("requestConfInfo->开始请求会议信息。。");
        ConfCtrl.ConfGetConfInfoCmd();
    }

    /**
     * 获取会议详细信息
     * @param achConfE164 会议E164号
     * @param sessionId  当前会话id
     */
    public void requestConfDetail(String achConfE164,int sessionId){
        TPLog.printError("requestConfDetail...");
        ConfCtrl.ConfGetConfDetailCmd(new StringBuffer(achConfE164),sessionId);
    }

    /**
     * 获取当前所有与会者
     */
    public void reqConfMtMember(){
        TPLog.printError("reqConfMtMember...");
        ConfCtrl.ConfGetOnLineTerListReq();
    }

    /**
     * 创建数据会议
     * @param achConfE164 视频会议E164号
     * @param achConfName 视频会议名称
     * @param mtEntitys 当前与会者列表
     * @throws JSONException
     */
    public void createWBConf(String achConfE164,String achConfName,List<MtEntity> mtEntitys){
        TPLog.printError("createWBConf...");
        if(mtEntitys.isEmpty()){
            return;
        }
        try {
            JSONObject createConf = new JSONObject();
            createConf.put("emConfType", 1); //多点会议
            createConf.put("achConfE164", achConfE164);
            createConf.put("achConfName", achConfName);
            createConf.put("emConfMode", MtNetUtils.emConfModeAuto_Api); //默认为自由协作
//            createConf.put("dwListNum", mtEntitys.size());
            createConf.put("dwListNum",0);

            JSONArray atUserList = new JSONArray();

//            for (int i = 0; i < mtEntitys.size(); i++) {
//                JSONObject item = new JSONObject();
//                MtEntity mtEntity = mtEntitys.get(i);
//                item.put("achE164", mtEntity.getE164());
//                item.put("achName", mtEntity.getName());
//                item.put("emMttype", mtEntity.getEmMttype());
//
//                if (i == 0) {
//                    item.put("bIsConfAdmin", true);
//                    createConf.put("achConfAdminE164", mtEntity.getE164());
//                    createConf.put("emAdminMtType", mtEntity.getEmMttype());
//                } else {
//                    item.put("bIsConfAdmin", false);
//                }
//
//                item.put("bOnline", true);
//                item.put("bIsOper", true);
//
//                atUserList.put(item);
//            }
            createConf.put("atUserList", atUserList);

            TPLog.printError(createConf.toString());

            DcsCtrl.DCSCreateConfReq(new StringBuffer(createConf.toString()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 加入数据会议
     * @param achConfE164 视频会议E164号
     * @param achConfName 视频会议名称
     * @param achE164 加入者E164号
     * @param achName 加入者名称
     * @param emMttype 加入者类型
     */
    public void joinWBConf(String achConfE164,String achConfName,String achE164,String achName,int emMttype){
        TPLog.printError("joinWBConf...");
        try {
            JSONObject createConf = new JSONObject();
            createConf.put("emConfType", 1);
            createConf.put("achConfE164", achConfE164);
            createConf.put("achConfName", achConfName);
            createConf.put("emConfMode", 1);
            createConf.put("dwListNum", 1);

            JSONArray atUserList = new JSONArray();

            JSONObject item = new JSONObject();
            item.put("achE164", achE164);
            item.put("achName", achName);
            item.put("emMttype", emMttype);
            item.put("bIsConfAdmin", false);
            createConf.put("achConfAdminE164", achE164);
            createConf.put("emAdminMtType", emMttype);
            item.put("bOnline", true);
            item.put("bIsOper", true);

            atUserList.put(item);

            createConf.put("atUserList", atUserList);

            TPLog.printError("Callback-loginDcs", createConf.toString());

            DcsCtrl.DCSCreateConfReq(new StringBuffer(createConf.toString()));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 退出会议
     * @param strConfE164 会议E164号
     * @param nForceFlag  未知 文档未说明
     *
     *dwQuitFlag: 0 - 表示视频会议的同时结束数据会议，即视频会议结束后主动退出数据会议
     * 1 - 表示仅仅操作数据会议，即数据会议中途退出数据会议
     * bForceQuit：强制退出数据会议，要保证视频会议结束后，对应的数据会议立即结束
     * 如果终端是主动退出数据会议，则DCS会议服务器不再自动重呼终端。但是移动端目前没有主动入数据会议的入口，
     * 因为移动端目前的需求是只能被动接收显示，不能进行数据协作，视频会议中不能主动退出，因为没有主动入数
     * 据会议的入口，要退出数据会议，是和视频会议一起退出。由于退出视频会议后，终端会主动退出数据会议，按照
     *之前的约定，主动退出数据会议，DCS会议服务器将不会自动重呼，即使重新加入之前的视频会议DCS会议业务服务
     * 器也不会重呼。这样移动端再次加入之前的视频会议，由于DCS会议服务器不会重呼，并且没有主动入会的入口，
     *就进入不了对应的数据会议了。为了解决移动端这种问题，所以平台提出退出数据会议要将两种情况区分开来：
     * 1）同时退出视频会议和数据会议，这种情况再次进入视频会议平台数据会议会自动重呼；2）仅退出数据会议（TL
     * 支持中途退出，TL有中途再加入数据会议的入口），所以KdvMt_DCSQuitConfReq中提供了dwQuitFlag参数进行区分，
     *此处需要特别注释一下，不然时间一久就忘了，add by zzx 2018/03/16
     */
    public void quitWBConf(String strConfE164,int nForceFlag ){
        TPLog.printError("quitWBConf...");
        String strJsonConfE164 = MtNetUtils.strE164ToStrJsonE164(strConfE164);
        TPLog.printKeyStatus("Mt quitWBConf strJsonConfE164 = "+strJsonConfE164);
        int r = DcsCtrl.DCSQuitConfReq(new StringBuffer(strJsonConfE164),nForceFlag,MtNetUtils.sessionId);
        TPLog.printKeyStatus("Mt quitWBConf rst = "+ r);
    }

    /**
     * 释放会议
     * @param strConfE164  会议E164号
     */
    public void releaseConf(String  strConfE164){
        TPLog.printError("releaseConf...");
        DcsCtrl.DCSReleaseConfReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(strConfE164)));
    }

    /**
     * 申请协作
     * @param strConfE164 会议E164号
     */
    public void confOperReq(String strConfE164){
        DcsCtrl.DCSApplyOperReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(strConfE164)));
    }

    /**
     * 获取当前数据会议与会用户列表
     * @param strConfE164  会议E164号
     */
    public void reqConfUserList(String strConfE164){
        DcsCtrl.DCSGetUserListReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(strConfE164)));
    }

    /**
     * 创建白板同步消息
     * @param page  新建白板
     * @param achConfE164 会议E164号
     */
    public void createNewWbPage(Page page,String achConfE164){
        /**
         *  { "achConfE164" : "", "tBoardinfo" : { "achWbName" : "", "emWbMode" : 1, "dwWbPageNum" : 0, "dwWbCreateTime" : 0,
         *  "achTabId" : "", "dwPageId" : 0, "dwWbSerialNumber" : 0, "achWbCreatorE164" : "", "dwWbWidth" : 0, "dwWbHeight" : 0,
         *  "achElementUrl" : "", "achDownloadUrl" : "", "achUploadUrl" : "" "dwWbAnonyId" : 0 } }
         */

        TPLog.printError("Mt DcsNewWhiteBoard begin... ");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("achConfE164", achConfE164);
            jsonObject.put("tBoardinfo",MtNetUtils.pageToJSONObject(page));
            TPLog.printError(jsonObject.toString());
            DcsCtrl.DCSNewWhiteBoardReq(new StringBuffer(jsonObject.toString()));
        }catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("Mt DcsNewWhiteBoard end... ");
    }

    /**
     * 删除白板
     * @param strConfE164 会议E164号
     * @param pageId  白板Id
     */
    public void delWbPage(String strConfE164,String pageId){
        String strJsonConfE164 = MtNetUtils.strE164ToStrJsonE164(strConfE164);
        String strJsonPageId =pageId;
        DcsCtrl.DCSDelWhiteBoardReq(new StringBuffer(strJsonConfE164),new StringBuffer(strJsonPageId));
    }

    /**
     * 获取所有的白板，感觉上该接口应该是入会同步数据
     * @param strConfE164 会议E164号
     */
    public void reqAllWbPage(String strConfE164){
        String strJsonConfE164 = MtNetUtils.strE164ToStrJsonE164(strConfE164);
        DcsCtrl.DCSGetAllWhiteBoardReq(new StringBuffer(strJsonConfE164));
    }

    /**
     * 获取单个白板数据
     * @param strConfE164
     * @param pageId
     */
    public void reqWbPage(String strConfE164,String pageId){
        String strJsonConfE164 = MtNetUtils.strE164ToStrJsonE164(strConfE164);
        String strJsonPageId = MtNetUtils.strE164ToStrJsonE164(pageId+"");
        DcsCtrl.DCSGetWhiteBoardReq(new StringBuffer(strJsonConfE164),new StringBuffer(strJsonPageId));
    }

    /**
     * 白板选择
     * @param strConfE164 会议E164号
     * @param pageId       白板Id
     * @param subPageIndex 猜测是白板子页Id，但终端帮助文档里面写的却是页数，匪夷所思
     */
    public void switchWbPage(String strConfE164,String pageId,long subPageIndex){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("strConfE164", strConfE164);
            jsonObject.put("achTabId",pageId);
            jsonObject.put("dwWbPageId",subPageIndex);
        }catch(Exception e){
            e.printStackTrace();
        }
        DcsCtrl.DCSSwitchReq(new StringBuffer(jsonObject.toString()));
    }

    /**
     * 铅笔图元同步
     * @param strConfE164  会议E164号
     * @param tabId         白板Id
     * @param subPageIndex 子页索引
     * @param pen            需要同步的铅笔图元
     */
    public void synPenGraph(String strConfE164,String tabId,long subPageIndex,Pen pen){
        TPLog.printError("synPenGraph -- >begin...");
        JSONObject jsonPencilOperInfo = new JSONObject();
        ArrayList<Point> points = pen.getPoints();
        TPLog.printError("synPenGraph -- > points.size = "+points.size());
        try {
            jsonPencilOperInfo.put("achTabId",""+tabId);
            jsonPencilOperInfo.put("dwSubPageId",subPageIndex);

            JSONObject tPencil = new JSONObject();
            tPencil.put("dwPointNum",points.size());
            tPencil.put("dwLineWidth",pen.getStrokeWidth());
            tPencil.put("dwRgb",MtNetUtils.getUnsignedInt(pen.getColor()));
//            tPencil.put("dwRgb",4293729316L);

            JSONObject tEntity = new JSONObject();
            tEntity.put("achEntityId",pen.getRemoteId());
            tEntity.put("bLock",false);

            JSONArray atPList = new JSONArray();
            for(int i = 0;i<points.size();i++){
                JSONObject jsonPoint = new JSONObject();
                jsonPoint.put("nPosx",points.get(i).x);
                jsonPoint.put("nPosy",points.get(i).y);
                atPList.put(jsonPoint);
//                atPList.put(points.get(i).x);
//                atPList.put(points.get(i).y);
            }

            tPencil.put("tEntity",tEntity);
            tPencil.put("atPList",atPList);
            jsonPencilOperInfo.put("tPencil",tPencil);

            String strJsonOperReq = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);

            TPLog.printError("synPenGraph -- > jsonPencilOperInfo = "+jsonPencilOperInfo.toString());
            TPLog.printError("synPenGraph -- > jsonPencilOperInfo = "+strJsonOperReq);

            DcsCtrl.DCSOperPencilOperInfoCmd (new StringBuffer(strJsonOperReq),new StringBuffer(jsonPencilOperInfo.toString()));
        }catch(Exception e){
            TPLog.printError("同步图元信息时出现异常:"+e);
            TPLog.printError(e);
        }
        TPLog.printError("synPenGraph -- >end...");
    }


    /**
     * 同步图片图元信息
     * @param strConfE164   会议E164号
     * @param tabId          白板Id
     * @param subPageIndex  子页索引
     * @param img            图片图元
     */
    public void synImageGraph(String strConfE164,String tabId,long subPageIndex,Image img){
       String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);
       JSONObject jsonImageOper = new JSONObject();
        try {

            jsonImageOper.put("achTabId",""+tabId);
            jsonImageOper.put("dwSubPageId",subPageIndex);

            JSONObject tImage  = new JSONObject();
            tImage.put("dwWidth",img.getWidth());
            tImage.put("dwHeight",img.getHeight());
            tImage.put("emNetworkstate",0);
            tImage.put("achFileName",img.getFileName());
            tImage.put("bBkImg",false);

            JSONObject tEntity = new JSONObject();
            JSONObject tBoardPt = new JSONObject();

            tEntity.put("achEntityId",img.getId());
            tEntity.put("bLock",false);

            tBoardPt.put("nPosx",img.getX());
            tBoardPt.put("nPosx",img.getY());

            tImage.put("tEntity",tEntity);
            tImage.put("tBoardPt",tBoardPt);

            jsonImageOper.put("tImage",tImage);
        }catch(Exception e){
            e.printStackTrace();
        }
        DcsCtrl.DCSOperImageOperInfoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonImageOper.toString()));
    }

    /**
     * 区域擦除
     * @param strConfE164    会议E164号
     * @param tabId           白板Id
     * @param subPageIndex   子页索引
     * @param erase            区域擦出图元
     */
    public void synAreaEraseGraph(String strConfE164,String tabId,long subPageIndex,AreaErase erase){
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);
        JSONObject jsonEraseOper = new JSONObject();

        ArrayList<Point> points = erase.getPoints();
        try {

            jsonEraseOper.put("achTabId",""+tabId);
            jsonEraseOper.put("dwSubPageId",""+subPageIndex);
            jsonEraseOper.put("achEntityId",erase.getRemoteId());  //我这边自己加的，Android这边需要使用

            JSONObject tBeginPt = new JSONObject();
            tBeginPt.put("nPosx",points.get(0).x);
            tBeginPt.put("nPosy",points.get(0).y);

            JSONObject tEndPt = new JSONObject();
            tEndPt.put("nPosx",points.get(1).x);
            tEndPt.put("nPosy",points.get(1).y);

            jsonEraseOper.put("tBeginPt",tBeginPt);
            jsonEraseOper.put("tEndPt",tEndPt);
            jsonEraseOper.put("bNexVision",true);

            DcsCtrl.DCSOperEraseOperInfoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonEraseOper.toString()));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 网呈区域擦除
     * @param strConfE164    会议E164号
     * @param tabId           白板Id
     * @param subPageIndex   子页索引
     * @param erase            区域擦出图元
     */
    public void synTPAreaEraseGraph(String strConfE164,String tabId,long subPageIndex,AreaErase erase){
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);
        JSONObject jsonEraseOper = new JSONObject();

        ArrayList<Point> points = erase.getPoints();
        try {

            jsonEraseOper.put("achTabId",""+tabId);
            jsonEraseOper.put("dwSubPageId",""+subPageIndex);
            jsonEraseOper.put("achEntityId",erase.getRemoteId());  //我这边自己加的，Android这边需要使用

            JSONObject tBeginPt = new JSONObject();
            tBeginPt.put("nPosx",points.get(0).x);
            tBeginPt.put("nPosy",points.get(0).y);

            JSONObject tEndPt = new JSONObject();
            tEndPt.put("nPosx",points.get(1).x);
            tEndPt.put("nPosy",points.get(1).y);

            jsonEraseOper.put("tBeginPt",tBeginPt);
            jsonEraseOper.put("tEndPt",tEndPt);

            DcsCtrl.DCSOperEraseOperInfoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonEraseOper.toString()));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 擦板擦除
     * @param strConfE164
     * @param tabId
     * @param subPageIndex
     * @param erase
     */
    public void synEraseGraph(String strConfE164, String tabId,long subPageIndex,Erase erase){
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);

        JSONObject eraseInfo = new JSONObject();

        try{
            eraseInfo.put("achTabId",tabId);
            eraseInfo.put("dwSubPageId",0);
            eraseInfo.put("achGraphsId",erase.getRemoteId());
            eraseInfo.put("dwEraseWidth",erase.getEraseWidth());
            eraseInfo.put("dwEraseHeight",erase.getEraseHeight());
            eraseInfo.put("dwPointCount",erase.getPoints().size());

            JSONArray pointArray = new JSONArray();

            for(int i = 0;i<erase.getPoints().size();i++){
                JSONObject point = new JSONObject();
                point.put("nPosx",erase.getPoints().get(i).x);
                point.put("nPosy",erase.getPoints().get(i).y);
                pointArray.put(point);
            }

            eraseInfo.put("atPoint",pointArray);

        }catch(Exception e){
            e.printStackTrace();
        }
        TPLog.printError("synEraseGraph -- strJsonOper = "+strJsonOper);
        TPLog.printError("synEraseGraph -- eraseInfo = "+eraseInfo.toString());
        int r = DcsCtrl. DCSOperReginEraseCmd(new StringBuffer(strJsonOper),new StringBuffer(eraseInfo.toString()));
        TPLog.printError("synEraseGraph -- rst = "+r);
    }

    public void synInsertImg(String strConfE164, String tabId,long subPageIndex,ImageGraph image){

        TPLog.printError("synInsertImg  begin...");

        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);
        JSONObject imgInfo = new JSONObject();

        try{

            imgInfo.put("achTabId",tabId);
            imgInfo.put("dwSubPageId",subPageIndex);
            imgInfo.put("achImgId",image.getRemoteId());
            JSONObject point = new JSONObject();
            point.put("nPosx",image.getX());
            point.put("nPosy",image.getY());
            imgInfo.put("tPoint",point);
            imgInfo.put("dwImgWidth",image.getWidth());
            imgInfo.put("dwImgHeight",image.getHeight());
            imgInfo.put("achPicName",new File(image.getImgPath()).getName());

            JSONArray matrixValue = new JSONArray();
            float value[] = new float[9];
            image.getMatrix().getValues(value);

            for(int i = 0;i<value.length;i++){
                matrixValue.put(value[i]+"");
            }

            imgInfo.put("aachMatrixValue",matrixValue);

        } catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("synInsertImg  strJsonOper == "+strJsonOper);
        TPLog.printError("synInsertImg  imgInfo == "+imgInfo.toString());

        int r = DcsCtrl.DCSOperInsertPicCmd(new StringBuffer(strJsonOper),new StringBuffer(imgInfo.toString()));

        TPLog.printError("synInsertImg rst:"+r);
    }


    /**
     *同步缩放
     * @param strConfE164    会议E164号
     * @param tabId           白板Id
     * @param subPageIndex   子页索引
     * @param zoom            缩放级别
     */
    public void synZoom(String strConfE164,String tabId,long subPageIndex,float zoom){
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164,tabId,subPageIndex);
        JSONObject jsonZoomOper = new JSONObject();
        try {

            jsonZoomOper.put("achTabId",tabId+"");
            jsonZoomOper.put("dwZoom",(int)(zoom*100));

        }catch(Exception e){
            e.printStackTrace();
        }

        DcsCtrl.DCSOperZoomInfoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonZoomOper.toString()));
    }

    /**
     * 同步撤销
     * @param strConfE164   会议E164号
     * @param tabId          白板Id
     * @param subPageIndex  白板子页索引
     */
    public void synUndo(String strConfE164,String tabId,long subPageIndex) {
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        JSONObject jsonUndoOper = new JSONObject();
        try {

            jsonUndoOper.put("achTabId",tabId+"");
            jsonUndoOper.put("dwSubPageId",subPageIndex);

        }catch(Exception e){
            e.printStackTrace();
        }

        DcsCtrl.DCSOperUndoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonUndoOper.toString()));
    }

    /**
     * 同步恢复
     * @param strConfE164   会议E164号
     * @param tabId          白板Id
     * @param subPageIndex  白板子页索引
     */
    public void synRedo(String strConfE164,String tabId,long subPageIndex) {
        TPLog.printError("synRedo...");
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        JSONObject jsonRedoOper = new JSONObject();
        try {

            jsonRedoOper.put("achTabId",tabId+"");
            jsonRedoOper.put("dwSubPageId",subPageIndex);

        }catch(Exception e){
            e.printStackTrace();
        }

        DcsCtrl.DCSOperRedoCmd(new StringBuffer(strJsonOper),new StringBuffer(jsonRedoOper.toString()));
    }

    /**
     * 同步清屏
     * @param strConfE164   会议E164号
     * @param tabId          白板Id
     * @param subPageIndex  白板子页索引
     */
    public void synClearScreen(String strConfE164,String tabId,long subPageIndex) {
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        DcsCtrl.DCSOperClearScreenCmd(new StringBuffer(strJsonOper));
    }

    /**
     * 获取图片下载URL
     * @param strConfE164  会议E164号
     * @param tabId         白板Id
     * @param subPageIndex 子页索引
     * @param imgId         图片Id
     */
     public void reqDownloadImageUrl(String strConfE164,String tabId,long subPageIndex,String imgId){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("achConfE164", strConfE164);
            jsonObject.put("achTabId", tabId);
            jsonObject.put("dwPageId", subPageIndex);
            jsonObject.put("achPicUrl", "");
            jsonObject.put("achWbPicentityId", imgId);
            DcsCtrl.DCSDownloadImageReq(new StringBuffer(jsonObject.toString()));
        }catch(Exception e){
            e.printStackTrace();
        }
     }

    /**
     *上传图片URL获取
     * @param strConfE164   会议E164号
     * @param tabId          白板Id
     * @param subPageIndex  白板子页索引
     * @param imgId          图片Id
     */
     public void reqUploadImageUrl(String strConfE164,String tabId,long subPageIndex,String imgId){
         JSONObject jsonObject = new JSONObject();
         try {
             jsonObject.put("achConfE164", strConfE164);
             jsonObject.put("achTabId", tabId);
             jsonObject.put("dwPageId", subPageIndex);
             jsonObject.put("achWbPicentityId", imgId);
             jsonObject.put("achPicUrl", "");
         }catch(Exception e){
             e.printStackTrace();
         }
         TPLog.printError("Mt reqUploadImageUrl -> imgId = "+imgId);
         TPLog.printError("Mt reqUploadImageUrl -> jsonObject = "+jsonObject.toString());
         DcsCtrl.DCSUploadImageReq (new StringBuffer(jsonObject.toString()));
     }

    /**
     * 上传图片文件
     *
     * @param imgUploadUrl
     * @param filePath
     * @param tabId
     */

     public void uploadImgFile(String imgUploadUrl,String filePath,String tabId,String imgId){
         TPLog.printError("Mt uploadImgFile  begin... ");
         String uploadUrlJsonStr = MtNetUtils.strE164ToStrJsonE164(imgUploadUrl);
         try {

             JSONObject uploadJson = new JSONObject();
             uploadJson.put("achFilePathName",filePath);
             uploadJson.put("achWbPicentityId",imgId);
             uploadJson.put("achTabid",tabId);
             uploadJson.put("bElementCacheFile",false);
             uploadJson.put("dwFileSize",new File(filePath).length());

             TPLog.printError("Mt uploadImgFile -> uploadJson = "+uploadJson.toString());
             DcsCtrl.DCSUploadFileCmd(new StringBuffer(uploadUrlJsonStr),new StringBuffer(uploadJson.toString()));

         }catch(Exception e){
             e.printStackTrace();
         }

         TPLog.printError("Mt uploadImgFile end... ");
     }

    /**
     * 请求当前终端是否在会议中
     */
    public void reqMtConfState(){
        TPLog.printError("reqMtConfState  begin...");
         ConfCtrl.ConfGetConfLinkStateReq();
     }

     public void  reqConfInfo(){
         DcsCtrl.DCSGetConfInfoReq();
     }

    /**
     * 退出远程数据协作，看接口还需要传入一个nForceFlag，不知道是用来做什么的这里暂时固定传入1
     * @param confE164 会议的E164号
     */
     public void quitConfReq(String confE164){
         DcsCtrl.DCSQuitConfReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(confE164)),1);
     }

    /**
     * 请求终端同步当前白板的所有详细内容
     * 入会同步会议数据第二步
     * @param pages 当前所有的白板内容
     */
    public void reqSynWbData(ArrayList<Page> pages){
         TPLog.printError("reqSynWbData......................");
         for(int i = 0;i<pages.size();i++){
             String elementUrl = MtNetUtils.strE164ToStrJsonE164(pages.get(i).getAchElementUrl());

             JSONObject json = new JSONObject();
             try {
                 json.put("achFilePathName", FileUtils.RUNNING_CACHE+"element1.json");
                 json.put("achWbPicentityId", "");
                 json.put("achTabid", pages.get(0).getRemotePageId());
                 json.put("bElementCacheFile", true);
                 json.put("dwFileSize", 0);

             }catch(Exception e){
                 e.printStackTrace();
             }

             TPLog.printError("DCSDownloadFileReq--->elementUrl="+elementUrl);
             TPLog.printError("DCSDownloadFileReq--->json="+json.toString());
             DcsCtrl.DCSDownloadFileReq(new StringBuffer(elementUrl),new StringBuffer(json.toString()));
         }
     }

     public void reqSynWbData(IPage iPage){
        if(iPage == null){
            return;
        }

         Page page = (Page)iPage;

         String elementUrl = MtNetUtils.strE164ToStrJsonE164(page.getAchElementUrl());

         JSONObject json = new JSONObject();
         try {
             json.put("achFilePathName", FileUtils.RUNNING_CACHE+"element1.json");
             json.put("achWbPicentityId", "");
             json.put("achTabid", page.getRemotePageId());
             json.put("bElementCacheFile", true);
             json.put("dwFileSize", 0);

         }catch(Exception e){
             e.printStackTrace();
         }

         TPLog.printError("DCSDownloadFileReq--->elementUrl="+elementUrl);
         TPLog.printError("DCSDownloadFileReq--->json="+json.toString());
         DcsCtrl.DCSDownloadFileReq(new StringBuffer(elementUrl),new StringBuffer(json.toString()));
     }

     public void downloadImage(DownloadInfo dlInfo){
         TPLog.printError("Mt downloadImage  begin...");
         if(dlInfo == null){
             return;
         }

         String elementUrl = MtNetUtils.strE164ToStrJsonE164(dlInfo.achPicUrl);

         JSONObject json = new JSONObject();
         try {
             json.put("achFilePathName", FileUtils.RUNNING_CACHE+dlInfo.getFileName());
             json.put("achWbPicentityId", dlInfo.achWbPicentityId);
             json.put("achTabid", dlInfo.achTabId);
             json.put("bElementCacheFile", false);
             json.put("dwFileSize", 0);

         }catch(Exception e){
             e.printStackTrace();
         }

         TPLog.printError("Mt downloadImage--->elementUrl="+elementUrl);
         TPLog.printError("Mt downloadImage--->json="+json.toString());
         DcsCtrl.DCSDownloadFileReq(new StringBuffer(elementUrl),new StringBuffer(json.toString()));
     }



    /**
     * 获取会议中的所有白板
     * 入会同步会议数据第一步
     * @param confE164 当前所有的白板内容
     */
    public void reqGetAllWbPage(String confE164) {
        TPLog.printError("reqGetAllWbPage......................");
        try {
             DcsCtrl.DCSGetAllWhiteBoardReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(confE164)));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取终端平台配置参数，主要是为了获取终端的E164号
     */
    public void getApsLoginParamCfgReq(){
       int r =  ConfigCtrl.GetApsLoginParamCfgReq ();
       TPLog.printError("Mt r---------------->"+r);
    }

    /**
     * 获取当前显示白板
     * @param confE164 当前会议E164号
     */
    public void reqCurDisplayWb(String confE164){
        TPLog.printError("reqCurDisplayWb begin...");
        int r =  DcsCtrl.DCSGetCurWhiteBoardReq(new StringBuffer(MtNetUtils.strE164ToStrJsonE164(confE164)));
        TPLog.printError("Mt reqCurDisplayWb r---------------->"+r);
    }

    /**
     * 删除所有白板
     * @param strConfE164
     */
    public void delAllWb(String strConfE164){
        TPLog.printError("Mt delAllWb begin...");
        String strJsonConfE164 = MtNetUtils.strE164ToStrJsonE164(strConfE164);
        int rst = DcsCtrl.DCSDelAllWhiteBoardReq(new StringBuffer(strJsonConfE164));
        TPLog.printError("Mt delAllWb end rst = "+rst);
    }

    public void synCoordinateChanged(String strConfE164,String tabId,int subPageIndex,float matrixValue[]){
        TPLog.printError("Mt synCoordinateChanged begin...");

        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        JSONObject coordinateJson = new JSONObject();
        try{
            coordinateJson.put("achTabId",tabId);
            coordinateJson.put("dwSubPageId",subPageIndex);
            JSONArray afMatrixValue = new JSONArray();
            for(int i = 0;i<matrixValue.length;i++){
                afMatrixValue.put(matrixValue[i]+"");
            }
            coordinateJson.put("aachMatrixValue",afMatrixValue);

//            String strMatrixValue = "";
//            for(int i = 0;i<matrixValue.length;i++){
//                strMatrixValue = strMatrixValue + matrixValue[i]+",";
//            }
//            TPLog.printError("Mt synCoordinateChanged strMatrixValue = "+strMatrixValue);
            TPLog.printError("Mt synCoordinateChanged strJsonOper = "+strJsonOper);
            TPLog.printError("Mt synCoordinateChanged coordinateJson = "+coordinateJson.toString());
            int rst = DcsCtrl.DCSOperFullScreenCmd(new StringBuffer(strJsonOper),new StringBuffer(coordinateJson.toString()));

            TPLog.printError("Mt synCoordinateChanged end rst = "+ rst);
        }catch(Exception e){
            e.printStackTrace();
            TPLog.printError("synCoordinateChanged 出现异常:");
            TPLog.printError(e);
        }
    }

    public synchronized void synSelectImgCoordinateChanged(String strConfE164,String tabId,int subPageIndex, ArrayList<Graph> selectGraphs){
        TPLog.printError("Mt synSelectImgCoordinateChanged begin...");

        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        JSONObject coordinateJson = new JSONObject();
        try{

            coordinateJson.put("achTabId",tabId);
            coordinateJson.put("dwSubPageId",subPageIndex);
            coordinateJson.put("dwGraphsCount",selectGraphs.size());

            JSONArray graphes = new JSONArray();
            for(int i = 0;i<selectGraphs.size();i++){
                JSONObject item = new JSONObject();
                item.put("achGraphsId",selectGraphs.get(i).getRemoteId());
                float matrixValue[] = new float[9];
                ((ImageGraph)selectGraphs.get(i)).getMatrix().getValues(matrixValue);

                JSONArray  matrixValueJson = new JSONArray();
                for(int j = 0;j<matrixValue.length;j++){
                    matrixValueJson.put(matrixValue[j]+"");
                }

                item.put("aachMatrixValue",matrixValueJson);
                graphes.put(item);
            }

            coordinateJson.put("atGraphsInfo",graphes);

        }catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("Mt synSelectImgCoordinateChanged strJsonOper = "+strJsonOper);
        TPLog.printError("Mt synSelectImgCoordinateChanged coordinateJson = "+coordinateJson.toString());

        int rst = DcsCtrl.DCSOperPitchPicRotateCmd(new StringBuffer(strJsonOper),new StringBuffer(coordinateJson.toString()));
        TPLog.printError("Mt synSelectImgCoordinateChanged end rst = "+rst);
    }


    public void synDelImg(String strConfE164,String tabId,int subPageIndex, String imgId){
        TPLog.printError("Mt synDelImg begin...");
        String strJsonOper = MtNetUtils.getStrJsonOper(strConfE164, tabId, subPageIndex);
        JSONObject delImgJson = new JSONObject();
        try{
            delImgJson.put("achTabId",tabId);
            delImgJson.put("dwSubPageId",subPageIndex);
            delImgJson.put("dwGraphsCount",1);
            JSONArray graphArr = new JSONArray();
            graphArr.put(imgId);
            delImgJson.put("achGraphsId",graphArr);
        }catch(Exception e){
            e.printStackTrace();
        }
        TPLog.printError("Mt synDelImg strJsonOper = "+strJsonOper);
        TPLog.printError("Mt synDelImg delImgJson = "+delImgJson.toString());
        DcsCtrl.DCSOperPitchPicDelCmd(new StringBuffer(strJsonOper),new StringBuffer(delImgJson.toString()));
        TPLog.printError("Mt synDelImg end...");
    }


    /**
     * 申请协作权限
     * @param achConfE164
     */
    public void reqApplyOper(String achConfE164){
        TPLog.printError("Mt reqApplyOper begin...");
        String achConfE164Json = MtNetUtils.strE164ToStrJsonE164(achConfE164);
        TPLog.printError("Mt reqApplyOper achConfE164Json = "+achConfE164Json);
        int rst = DcsCtrl.DCSApplyOperReq(new StringBuffer(achConfE164Json.toString()));
        TPLog.printError("Mt reqApplyOper rst = "+rst);
        TPLog.printError("Mt reqApplyOper end...");
    }

    /**
     * 添加协作方，同意协作请求
     * @param confE164 会议E164号
     * @param me 添加成为协作方的终端
     */
    public void reqAddOperate(String confE164,MtEntity me){
        TPLog.printError("Mt reqAddOperate begin...");
        try{
            JSONObject reqAddOperate = new JSONObject();

            reqAddOperate.put("achConfE164",confE164);
            reqAddOperate.put("dwListNum",1);

            JSONArray atUserInfoList = new JSONArray();
            JSONObject itemJsonObject = new JSONObject();
            itemJsonObject.put("achE164",me.getE164());
            itemJsonObject.put("achName",me.getName());
            itemJsonObject.put("emMttype",255);
            itemJsonObject.put("bOnline",true);
            itemJsonObject.put("bIsOper",true);
            itemJsonObject.put("bIsConfAdmin",me.isConfAdmin());
            atUserInfoList.put(itemJsonObject);
            reqAddOperate.put("atOperList",atUserInfoList);

            TPLog.printError("Mt reqAddOperate reqAddOperate = "+reqAddOperate.toString());

            int rst = DcsCtrl.DCSAddOperatorReq(new StringBuffer(reqAddOperate.toString()));
            TPLog.printError("Mt reqAddOperate rst = "+rst);
        }catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("Mt reqAddOperate end ...");
    }

    /**
     * 拒绝协作请求
     * @param confE164 会议E164号
     * @param me 被拒绝的终端
     */
    public void rejectOperator(String confE164,MtEntity me){
        TPLog.printError("Mt rejectOperator begin...");
        try{
            JSONObject reqAddOperate = new JSONObject();

            reqAddOperate.put("achConfE164",confE164);
            reqAddOperate.put("dwListNum",1);

            JSONArray atUserInfoList = new JSONArray();
            JSONObject itemJsonObject = new JSONObject();
            itemJsonObject.put("achE164",me.getE164());
            itemJsonObject.put("achName",me.getName());
            itemJsonObject.put("emMttype",me.getEmMttype());
            itemJsonObject.put("bOnline",me.isOnline());
            itemJsonObject.put("bIsOper",me.isOper());
            itemJsonObject.put("bIsConfAdmin",me.isConfAdmin());
            atUserInfoList.put(itemJsonObject);
            reqAddOperate.put("atOperList",atUserInfoList);

            TPLog.printError("Mt rejectOperator reqAddOperate = "+reqAddOperate.toString());

            int rst = DcsCtrl.DCSRejectOperatorCmd(new StringBuffer(reqAddOperate.toString()));
            TPLog.printError("Mt rejectOperator rst = "+rst);
        }catch(Exception e){
            e.printStackTrace();
        }

        TPLog.printError("Mt rejectOperator end ...");
    }

    /**
     * 释放管理员权限
     */
    public void releaseChairmanRsq(){
        TPLog.printError("Mt releaseChairmanRsq begin...");
        int rst = ConfCtrl.ConfWithDrawChairmanCmd();
        TPLog.printError("Mt releaseChairmanRsq rst = "+rst);
        TPLog.printError("Mt releaseChairmanRsq end...");
    }

    /**
     * 查询当前会议主席终端
     */
    public void reqQueryChair(){
        TPLog.printError("Mt reqQueryChair begin...");
        int rst = ConfCtrl.ConfWhoIsChairCmd();
        TPLog.printError("Mt reqQueryChair rst = "+rst);
        TPLog.printError("Mt reqQueryChair end...");
    }

    /**
     * 获取数据会议与会人员列表
     */
    public void reqDCSGetUserList(String confE164){
        TPLog.printError("Mt reqQueryChair begin...");
        String jsonStr = MtNetUtils.strE164ToStrJsonE164(confE164);
        int rst = DcsCtrl.DCSGetUserListReq (new StringBuffer(jsonStr));
        TPLog.printError("Mt reqQueryChair rst = "+rst);
        TPLog.printError("Mt reqQueryChair end...");
     }

    /**
     * 指定新的管理员
     * 同意管理员申请
     * @param acn 新的管理员信息
     */
     public void confChairSpecNewChair(ApplyChairNtf acn){
         TPLog.printError("Mt confChairSpecNewChair begin...");
         //{ "dwMcuId":0, "dwTerId":0 }
         JSONObject strJsonMtId = new JSONObject();
         try{
             strJsonMtId.put("dwMcuId",acn.getDwMcuId());
             strJsonMtId.put("dwTerId",acn.getDwTerId());
         }catch(Exception e){
             e.printStackTrace();
         }
         int rst = ConfCtrl.ConfChairSpecNewChairCmd(new StringBuffer(strJsonMtId.toString()));
         TPLog.printError("Mt reqQueryChair rst = "+rst);
         TPLog.printError("Mt reqQueryChair end...");
     }

    /**
     * 拒绝管理员权限申请
     */
    public void confRejectApplyChairman(){
        TPLog.printError("Mt confChairSpecNewChair begin...");
        int rst = ConfCtrl.ConfRejectApplyChairmanCmd();
        TPLog.printError("Mt reqQueryChair rst = "+rst);
        TPLog.printError("Mt reqQueryChair end...");
    }

}
