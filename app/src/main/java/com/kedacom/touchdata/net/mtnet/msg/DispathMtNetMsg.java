package com.kedacom.touchdata.net.mtnet.msg;

import android.graphics.Color;

import com.kedacom.touchdata.net.entity.ApplyChairNtf;
import com.kedacom.touchdata.net.mtnet.MtConnectManager;
import com.kedacom.touchdata.net.mtnet.MtNetUtils;
import com.kedacom.touchdata.net.mtnet.entity.ClearScreenNtf;
import com.kedacom.touchdata.net.mtnet.entity.CreateDcsConfRsp;
import com.kedacom.touchdata.net.mtnet.entity.DelSelectImgEntity;
import com.kedacom.touchdata.net.mtnet.entity.DownloadInfo;
import com.kedacom.touchdata.net.mtnet.entity.ElementOperFinalNtf;
import com.kedacom.touchdata.net.mtnet.entity.ImgCoordinate;
import com.kedacom.touchdata.net.mtnet.entity.ImgUploadUrl;
import com.kedacom.touchdata.net.mtnet.entity.MtEntity;
import com.kedacom.touchdata.net.mtnet.entity.SelectImgCoordinateEntity;
import com.kedacom.touchdata.net.mtnet.entity.SynCoordinateMsg;
import com.kedacom.touchdata.net.mtnet.entity.TLScrollChangedNtf;
import com.kedacom.touchdata.net.mtnet.entity.TLZoomChangeNtf;
import com.kedacom.touchdata.net.mtnet.entity.UnDoOrReDoNtf;
import com.kedacom.touchdata.net.mtnet.utils.EmMtCallState_Api;
import com.kedacom.touchdata.net.mtnet.utils.MTEntity;
import com.kedacom.touchdata.net.utils.NetUtil;
import com.kedacom.touchdata.whiteboard.data.Point;
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Circle;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.GraphFactory;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.Line;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.graph.Rectangle;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;
import com.kedacom.touchdata.whiteboard.utils.WhiteBoardUtils;
import com.kedacom.tplog.TPLog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/3/21.
 * 终端反馈消息分发，主要通过MtManager中的Callback通过反射调用
 * 反射所有函数列表为IDispathMtMsg
 */

public class DispathMtNetMsg implements IDispathMtNetMsg {

    @Override
    public void MTCLoginRsp(JSONObject msg){
        boolean loginMtRst = false;
        try {
            loginMtRst = msg.getBoolean("bLogin");
            TPLog.printError("Login Mt Rst: " + loginMtRst);
        } catch (Exception e) {
            TPLog.printError("解析登录反馈消息出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }finally {
            MtConnectManager.getInstance().handMsg(MtConnectManager.CONNECT_MT,loginMtRst);
        }
    }

    @Override
    public void MTDisconnectNtf(JSONObject msg) {
        MtConnectManager.getInstance().handMsg(MtConnectManager.MT_DISCONNECT_NTF,null);
    }

    @Override
    public void ConfInfoNtf(JSONObject msg) {
        if(msg==null){
            TPLog.printError("parseConfInfoNtfMsg msg = null");
            return;
        }
        try {
            MtNetUtils.achConfE164 = msg.getString("achConfE164");
            MtNetUtils.achConfName = msg.getString("achConfName");

           JSONObject tChairman =  msg.getJSONObject("tChairman");
            MtNetUtils.dwMcuId = tChairman.getInt("dwMcuId");
            MtNetUtils.dwTerId = tChairman.getInt("dwTerId");

            MtConnectManager.getInstance().handMsg(MtConnectManager.CONF_INFO,null);
        }catch(Exception e){
            TPLog.printError("解析会议信息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void GetConfDetailInfoNtf(JSONObject msg) {
        TPLog.printError("GetConfDetailInfoNtf----------------------------");
    }

    @Override
    public void ImMulitChatMemberLeaveNtf(JSONObject msg) {
        TPLog.printError("ImMulitChatMemberLeaveNtf----------------------------");
        MtConnectManager.getInstance().handMsg(MtConnectManager.LEAVE_CONF_NTF,null);
    }

    @Override
    public void OnLineTerListRsp(JSONObject msg) {
        if(msg==null){
            TPLog.printError("parseOnLineTerListRspMsg msg = null");
            return;
        }
        try {
            MtNetUtils.confMemberList.clear();
            JSONArray atMtEntitiy = msg.getJSONArray("atMtEntitiy");
            int count = atMtEntitiy.length();
            for(int i = 0;i<count;i++){
                MtEntity mte = new MtEntity();
                JSONObject mtItem = atMtEntitiy.getJSONObject(i);
                JSONArray atLoc = mtItem.getJSONArray("atLoc");
                if(atLoc.length() == 0){
                    continue;
                }
                int dwMcuId = mtItem.getInt("dwMcuId");
                int dwTerId = mtItem.getInt("dwTerId");
                mte.setDwMcuId(dwMcuId);
                mte.setDwTerId(dwTerId);

                int emMtType = mtItem.getInt("emMtType");
                mte.setEmMttype(emMtType);
                JSONObject tMtAlias = mtItem.getJSONObject("tMtAlias");
                JSONArray arrAlias = tMtAlias.getJSONArray("arrAlias");
                int aliasCount = arrAlias.length();
                for(int j = 0;j<aliasCount;j++){
                    JSONObject achAliasItem = arrAlias.getJSONObject(j);
                    int emAliasType = achAliasItem.getInt("emAliasType");
                    String achAlias =  achAliasItem.getString("achAlias");
                    if(emAliasType==1){//e164
                        mte.setE164(achAlias);
                    }else if(emAliasType==2){
                        mte.setName(achAlias);
                    }
                }
                if(mte.getE164() == null){
                    return;
                }
                MtNetUtils.confMemberList.add(mte);
            }
            MtConnectManager.getInstance().handMsg(MtConnectManager.CONF_MT_MEMBER,null);
        } catch (JSONException e) {
            TPLog.printError("解析与会者列表信息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsCreateConf_Rsp(JSONObject msg) {
        try{

            boolean bCreator = msg.getBoolean("bCreator");
            boolean bSuccess = msg.getBoolean("bSuccess");
            int dwErrorCode = msg.getInt("dwErrorCode");
            int emConfMode = msg.getInt("emConfMode");
            int emConfType = msg.getInt("emConfType");
            MtNetUtils.curEmConfMode = emConfMode;
            MtConnectManager.getInstance().handMsg(MtConnectManager.CREATE_DCONF,new CreateDcsConfRsp(bCreator,bSuccess,dwErrorCode,emConfMode,emConfType));
        }catch(Exception e){
            TPLog.printError("解析创建白板会议返回结果时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUserJoinConf_Ntf(JSONObject msg) {
        try{

            String achConfE164 = msg.getString("achConfE164");
            String achConfName = msg.getString("achConfName");
            JSONObject tUserInfo = msg.getJSONObject("tUserInfo");
            String achE164 = tUserInfo.getString("achE164");
            String achName = tUserInfo.getString("achName");
            boolean bIsConfAdmin = tUserInfo.getBoolean("bIsConfAdmin");
            boolean bIsOper = tUserInfo.getBoolean("bIsOper");
            boolean bOnline = tUserInfo.getBoolean("bOnline");
            int emMttype = tUserInfo.getInt("emMttype");

            MtEntity mtEntity = new MtEntity();
            mtEntity.setConfAdmin(bIsConfAdmin);
            mtEntity.setE164(achE164);
            mtEntity.setEmMttype(emMttype);
            mtEntity.setName(achName);
            mtEntity.setOnline(bOnline);
            mtEntity.setOper(bIsOper);

            MtConnectManager.getInstance().handMsg(MtConnectManager.JOIN_DCONF_NTF,mtEntity);
        }catch(Exception e){
            TPLog.printError("解析加入白板会议通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsNewWhiteBoard_Rsp(JSONObject msg) {
        try {
            JSONObject mainParam = msg.getJSONObject("MainParam");
            boolean bSuccess = mainParam.getBoolean("bSuccess");
            MtConnectManager.getInstance().handMsg(MtConnectManager.CREATE_WB_RSP,bSuccess);
        }catch(Exception e){
            TPLog.printError("解析请求同步新建白板消息通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsNewWhiteBoard_Ntf(JSONObject msg) {
        try{
            String achDownloadUrl = msg.getString("achDownloadUrl");
            String achElementUrl = msg.getString("achElementUrl");
            String achTabId = msg.getString("achTabId");
            String achUploadUrl = msg.getString("achUploadUrl");
            String achWbCreatorE164 = msg.getString("achWbCreatorE164");
            String achWbName = msg.getString("achWbName");
            String dwPageId = msg.getString("dwPageId");
            String dwWbAnonyId = msg.getString("dwWbAnonyId");
            long dwWbCreateTime = msg.getLong("dwWbCreateTime");
            int dwWbHeight = msg.getInt("dwWbHeight");
            int dwWbWidth = msg.getInt("dwWbWidth");
            int dwWbPageNum = msg.getInt("dwWbPageNum");
            int dwWbSerialNumber = msg.getInt("dwWbSerialNumber");
            int emWbMode = msg.getInt("emWbMode");

            Page page = new Page();
            SubPage subPage = new SubPage();
            page.addSubPage(subPage);
            page.setId(achTabId.hashCode());
            page.setRemotePageId(achTabId);
            page.setName(achWbName);
            page.setPageMode(emWbMode);
            page.setAnoymous(dwWbAnonyId=="0"?false:true);
            page.setM_nTime((int)dwWbCreateTime);
            page.setBackGroundColor(WhiteBoardUtils.BACKGROUNDCOLOR[0]);

            MtConnectManager.getInstance().handMsg(MtConnectManager.CREATE_WB_NTF,page);
        }catch(Exception e){
            TPLog.printError("解析创建白板通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperPencilOperInfo_Rsp(JSONObject msg) {
        try{


        }catch(Exception e){
            TPLog.printError("解析同步铅笔图元反馈结果时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperPencilOperInfo_Ntf(JSONObject msg) {
        try{

            TPLog.printError("Mt achTabId------->"+msg.toString());

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            if(MtNetUtils.achTerminalE164.equals(achFromE164)&&!MtNetUtils.synConfData){//自己发送的就不要再同步给自己了
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tPencil = AssParam.getJSONObject("tPencil");

            int dwLineWidth = tPencil.getInt("dwLineWidth");
            int dwPointNum = tPencil.getInt("dwPointNum");
            int dwRgb = tPencil.getInt("dwRgb");
            String strDwRgb = Integer.toHexString(dwRgb);
            if(strDwRgb.length()<6){
                int count = 6- strDwRgb.length();
                for(int i = 0;i<count;i++){
                    strDwRgb = "0"+strDwRgb;
                }
            }

            if(strDwRgb.length()==7){
                strDwRgb = "0"+strDwRgb;
            }

            dwRgb = Color.parseColor("#"+strDwRgb);

            JSONObject tEntity = tPencil.getJSONObject("tEntity");
            String achEntityId = tEntity.getString("achEntityId");
            boolean bLock = tEntity.getBoolean("bLock");

            TPLog.printError("Mt achTabId----------->"+achTabId);

            Pen pen = new Pen();
            pen.setColor(dwRgb);
            pen.setStrokeWidth(dwLineWidth);
            pen.setTabId(achTabId.hashCode());
            pen.setRemotePageId(achTabId);
            pen.setPageIndex(dwSubPageId);

            long entityId = (achEntityId == null||achEntityId.trim().equals(""))?0:achEntityId.hashCode();
            pen.setId((int)entityId);
            pen.setRemoteId(achEntityId);

            JSONArray atPList = tPencil.getJSONArray("atPList");
            for(int i = 0;i<atPList.length();i++){
                JSONObject jsonPoint =  atPList.getJSONObject(i);
                pen.addPoint(jsonPoint.getInt("nPosx"),jsonPoint.getInt("nPosy"));
            }

            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_PENGRAPH_NTF,pen);

        }catch(Exception e){
            TPLog.printError("解析同步铅笔图元消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
     }

    @Override
    public void MulConfStartedNtf(JSONObject msg) {
        MtConnectManager.getInstance().handMsg(MtConnectManager.START_CONF_NTF,null);
    }

    @Override
    public void MulConfEndedNtf(JSONObject msg) {
        MtConnectManager.getInstance().handMsg(MtConnectManager.OVER_CONF_NTF,null);
    }

    @Override
    public void CallLinkState_Rsp(JSONObject msg) {
        try{
            int emCallState = msg.getInt("emCallState");
            boolean isInConf = false;
            if(emCallState == EmMtCallState_Api.emCallMCC_Api){//这里暂时先定为多点会议
                isInConf = true;
            }
            MtConnectManager.getInstance().handMsg(MtConnectManager.IS_ALREADY_IN_CONF,isInConf);
        }catch(Exception e){
            TPLog.printError("解析会议状态信息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void GetApsLoginParamCfg_Rsp(JSONObject msg) {
        try{
            String achUsername = msg.getString("achUsername");
            //如果E164没有变就不需要再进行后续操作了
            if(achUsername!=null&&achUsername.equals(MtNetUtils.achTerminalE164)){
                return;
            }
            MtNetUtils.achTerminalE164 = achUsername;
            TPLog.printError("GetApsLoginParamCfg_Rsp,MtNetUtils.achTerminalE164  --- >"+MtNetUtils.achTerminalE164 );
            MtConnectManager.getInstance().handMsg(MtConnectManager.GET_TERMINAL_E164,achUsername);
        }catch(Exception e){
            TPLog.printError("解析会议状态信息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsGetAllWhiteBoard_Rsp(JSONObject msg) {
        try{

            JSONObject AssParam = msg.getJSONObject("AssParam");
            JSONArray atBoardInfo = AssParam.getJSONArray("atBoardInfo");

            List<Page> pages = new ArrayList<Page>();
            for(int i = 0;i<atBoardInfo.length();i++){
                JSONObject jsonPage = atBoardInfo.getJSONObject(i);
                String achDownloadUrl = jsonPage.getString("achDownloadUrl");
                String achElementUrl = jsonPage.getString("achElementUrl");
                String achTabId = jsonPage.getString("achTabId");
                String achUploadUrl = jsonPage.getString("achUploadUrl");
                String achWbCreatorE164 = jsonPage.getString("achWbCreatorE164");
                String achWbName = jsonPage.getString("achWbName");
                String dwPageId = jsonPage.getString("dwPageId");
                String dwWbAnonyId = jsonPage.getString("dwWbAnonyId");
                long dwWbCreateTime = jsonPage.getLong("dwWbCreateTime");
                int dwWbHeight = jsonPage.getInt("dwWbHeight");
                int dwWbWidth = jsonPage.getInt("dwWbWidth");
                int dwWbPageNum = jsonPage.getInt("dwWbPageNum");
                int dwWbSerialNumber = jsonPage.getInt("dwWbSerialNumber");
                int emWbMode = jsonPage.getInt("emWbMode");

                Page page = new Page();
                SubPage subPage = new SubPage();
                page.addSubPage(subPage);
                page.setId(achTabId.hashCode());
                page.setRemotePageId(achTabId);
                page.setName(achWbName);
                page.setPageMode(emWbMode);
                page.setAnoymous(dwWbAnonyId=="0"?false:true);
                page.setM_nTime((int)dwWbCreateTime);
                page.setBackGroundColor(WhiteBoardUtils.BACKGROUNDCOLOR[0]);
                page.setAchElementUrl(achElementUrl);
                page.setDwWbSerialNumber(dwWbSerialNumber);
                pages.add(page);
            }

            MtConnectManager.getInstance().handMsg(MtConnectManager.GET_ALL_WB,pages);
        }catch(Exception e){
            TPLog.printError("解析获取所有白板信息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsElementOperFinal_Ntf(JSONObject msg) {
        try{

            String strAchTabId = msg.getString("achTabId");
            //long achTabId = strAchTabId == null||strAchTabId.trim().isEmpty()?0:Long.parseLong(strAchTabId);
            boolean bParseSuccess = msg.getBoolean("bParseSuccess");
            MtConnectManager.getInstance().handMsg(MtConnectManager.JOIN_CONF_SYN_END,new ElementOperFinalNtf(bParseSuccess,strAchTabId));
        }catch(Exception e){
            TPLog.printError("解析入会同步数据完成消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsSwitch_Ntf(JSONObject msg) {
        try{

            String strTabId = msg.getString("achTabId");
           // long tabId = strTabId==null||strTabId.trim().isEmpty()?0:Long.parseLong(strTabId);
            MtConnectManager.getInstance().handMsg(MtConnectManager.SWITCH_TAB_PAGE,strTabId);

        }catch(Exception e){
            TPLog.printError("解析翻页通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperEraseOperInfo_Ntf(JSONObject msg) {
        try{

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");

            if(MtNetUtils.achTerminalE164.equals(achFromE164)&&!MtNetUtils.synConfData){//自己发送的就不要再同步给自己了
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");
            String strTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tBeginPt = AssParam.getJSONObject("tBeginPt");
            JSONObject tEndPt = AssParam.getJSONObject("tEndPt");
            boolean bNexvision = true;
            if(AssParam.has("bNexVision")) {
                bNexvision = AssParam.getBoolean("bNexVision");
            }
            AreaErase ae = (AreaErase) GraphFactory.makeGraph(WhiteBoardUtils.GRAPH_ERASE_AREA);
//            long tabId = strTabId==null||strTabId.trim().isEmpty()?0:Long.parseLong(strTabId);
            ae.setbNexvision(bNexvision);
            ae.setRemotePageId(strTabId);
            ae.addPoint(tBeginPt.getInt("nPosx"),tBeginPt.getInt("nPosy"));
            ae.addPoint(tEndPt.getInt("nPosx"),tEndPt.getInt("nPosy"));
            ae.commitErase();
            MtConnectManager.getInstance().handMsg(MtConnectManager.AREA_ERASE,ae);
        }catch(Exception e){
            TPLog.printError("解析区域擦除通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsDelWhiteBoard_Ntf(JSONObject msg) {
        try{
            String strTabId = msg.getString("strIndex");
//            int tabId = strTabId == null||strTabId.trim().isEmpty()?0:Integer.parseInt(strTabId);
            MtConnectManager.getInstance().handMsg(MtConnectManager.DEL_WB_PAGE,strTabId);
        }catch(Exception e){
            TPLog.printError("解析删除白板通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperClearScreen_Ntf(JSONObject msg) {
        try{
            String achFromE164 = msg.getString("achFromE164");
            if(achFromE164.equals(MtNetUtils.achTerminalE164)&&!MtNetUtils.synConfData){
                return;
            }
            String strTabId = msg.getString("achTabId");
//            int tabId = strTabId == null||strTabId.trim().isEmpty()?0:Integer.parseInt(strTabId);
            int dwWbPageId = msg.getInt("dwWbPageId");
            MtConnectManager.getInstance().handMsg(MtConnectManager.CLEAR_SCREN,new ClearScreenNtf(strTabId,dwWbPageId));
        }catch(Exception e){
            TPLog.printError("解析删除白板通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperUndo_Ntf(JSONObject msg) {
        try{

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");

            TPLog.printError("onMtUndoNtf   ---->  achFromE164 = "+achFromE164);
            TPLog.printError("onMtUndoNtf   ---->  MtNetUtils.achTerminalE164 = "+MtNetUtils.achTerminalE164);
            TPLog.printError("onMtUndoNtf   ---->  MtNetUtils.synConfData = "+MtNetUtils.synConfData);
            if(achFromE164.equals(MtNetUtils.achTerminalE164)&&!MtNetUtils.synConfData){
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");

            String strTabId = AssParam.getString("achTabId");
//            int tabId = strTabId == null||strTabId.trim().isEmpty()?0:Integer.parseInt(strTabId);
            int dwWbPageId = AssParam.getInt("dwSubPageId");
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_UNDO,new UnDoOrReDoNtf(strTabId,dwWbPageId));
        }catch(Exception e){
            TPLog.printError("解析撤销通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperRedo_Ntf(JSONObject msg) {
        try{

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            if(achFromE164.equals(MtNetUtils.achTerminalE164)&&!MtNetUtils.synConfData){
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");

            String strTabId = AssParam.getString("achTabId");
//            int tabId = strTabId == null||strTabId.trim().isEmpty()?0:Integer.parseInt(strTabId);
            int dwWbPageId = AssParam.getInt("dwSubPageId");
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_REDO,new UnDoOrReDoNtf(strTabId,dwWbPageId));
        }catch(Exception e){
            TPLog.printError("解析恢复通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsAddOperator_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONArray atUserInfoList = msg.getJSONArray("atUserInfoList");
            ArrayList<String> userE164List = new ArrayList<String>();
            for(int i = 0;i<atUserInfoList.length();i++){
                JSONObject item = atUserInfoList.getJSONObject(i);
                userE164List.add(item.getString("achE164"));
            }

            MtConnectManager.getInstance().handMsg(MtConnectManager.ADD_OPERATOR_NTF,userE164List);
        }catch(Exception e){
            TPLog.printError("解析添加协作方通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUserQuitConf_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{

            JSONObject tUserInfo = msg.getJSONObject("tUserInfo");
            String achE164 = tUserInfo.getString("achE164");
            MtConnectManager.getInstance().handMsg(MtConnectManager.QUIT_DCS_CONF_NTF,achE164);
        }catch(Exception e){
            TPLog.printError("解析添加协作方通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void ConfCanceledNtf(JSONObject msg) {
        if(msg==null){
            return;
        }
        MtConnectManager.getInstance().handMsg(MtConnectManager.OVER_CONF_NTF,null);
    }

    @Override
    public void DcsQuitConf_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            boolean mSuccess = msg.getBoolean("bSuccess");
            MtConnectManager.getInstance().handMsg(MtConnectManager.QUIT_DCS_CONF_RSP,mSuccess);
        }catch(Exception e){
            TPLog.printError("解析添加协作方通知时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperImageOperInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            if(MtNetUtils.achTerminalE164.equals(achFromE164)){
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");

            String achTabId = AssParam.getString("achTabId");
            int  dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tImage =  AssParam.getJSONObject("tImage");
            String achFileName  = tImage.getString("achFileName");
            boolean bBkImg = tImage.getBoolean("bBkImg");
            int dwHeight = tImage.getInt("dwHeight");
            int dwWidth = tImage.getInt("dwWidth");
            int emNetworkstate = tImage.getInt("emNetworkstate");
            JSONObject tBoardPt = tImage.getJSONObject("tBoardPt");
            int nPosx = tBoardPt.getInt("nPosx");
            int nPosy = tBoardPt.getInt("nPosy");
            JSONObject tEntity = tImage.getJSONObject("tEntity");
            String achEntityId =  tEntity.getString("achEntityId");
            boolean bLock = tEntity.getBoolean("bLock");

            ImageGraph ig = new ImageGraph();
            ig.setRemotePageId(achTabId);
            ig.setPageIndex(dwSubPageId);
            ig.setHeight(dwHeight);
            ig.setWidth(dwWidth);
            ig.setX(nPosx);
            ig.setY(nPosy);
            ig.setRemoteId(achEntityId);
            ig.setFileName(achFileName);

            MtConnectManager.getInstance().handMsg(MtConnectManager.OPER_IMG_INFO_NTF,ig);
        }catch(Exception e){
            TPLog.printError("解析图片信息消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsDownloadImage_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String achPicUrl = msg.getString("achPicUrl");
            String achTabId = msg.getString("achTabId");
            String achWbPicentityId = msg.getString("achWbPicentityId");
            int dwPageId = msg.getInt("dwPageId");

            MtConnectManager.getInstance().handMsg(MtConnectManager.DOWNLOAD_IMAGE_NTF,new DownloadInfo(achTabId,achPicUrl,achWbPicentityId,dwPageId));
        }catch(Exception e){
            TPLog.printError("解析图片下载消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }

    }

    @Override
    public void DcsDownloadImage_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achPicUrl = AssParam.getString("achPicUrl");
            String achTabId = AssParam.getString("achTabId");
            String achWbPicentityId = AssParam.getString("achWbPicentityId");
            int dwPageId = AssParam.getInt("dwPageId");

            MtConnectManager.getInstance().handMsg(MtConnectManager.DOWNLOAD_IMAGE_NTF,new DownloadInfo(achTabId,achPicUrl,achWbPicentityId,dwPageId));
        }catch(Exception e){
            TPLog.printError("解析请求图片下载连接返回结果消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsDownloadFile_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String achPicUrl = "";
            String achTabId = msg.getString("achTabid");
            String achWbPicentityId = msg.getString("achWbPicentityId");
            int dwPageId = 0;

            String achFilePathName = msg.getString("achFilePathName");
            boolean bElementFile = msg.getBoolean("bElementFile");
            boolean bSuccess = msg.getBoolean("bSuccess");
            DownloadInfo dli = new DownloadInfo(achTabId,achPicUrl,achWbPicentityId,dwPageId);
            dli.setAchFilePathName(achFilePathName);
            dli.setbElementFile(bElementFile);
            dli.setbSuccess(bSuccess);

            MtConnectManager.getInstance().handMsg(MtConnectManager.DOWNLOAD_FILE_RSP,dli);
        }catch(Exception e){
            TPLog.printError("解析图片下载响应消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }

    }

    @Override
    public void DcsOperColorPenOperInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tColorPen = AssParam.getJSONObject("tColorPen");

            JSONObject tEntity = AssParam.getJSONObject("tEntity");
            String achEntityId = tEntity.getString("achEntityId");
            boolean bLock = tEntity.getBoolean("bLock");
            int dwColorPenNum = tColorPen.getInt("dwColorPenNum");
            int dwLineWidth = tColorPen.getInt("dwLineWidth");
            int dwRgb = tColorPen.getInt("dwRgb");

            String strDwRgb = Integer.toHexString(dwRgb);
            if(strDwRgb.length()<6){
                int count = 6- strDwRgb.length();
                for(int i = 0;i<count;i++){
                    strDwRgb = "0"+strDwRgb;
                }
            }

            if(strDwRgb.length()==7){
                strDwRgb = "0"+strDwRgb;
            }

            dwRgb = Color.parseColor("#"+strDwRgb);

            JSONArray atCPList = tColorPen.getJSONArray("atCPList");

            Pen pen = new Pen();
            pen.setColor(dwRgb);
            pen.setStrokeWidth(dwLineWidth);
            pen.setTabId(achTabId.hashCode());
            pen.setRemotePageId(achTabId);
            pen.setPageIndex(dwSubPageId);
            long entityId = (achEntityId == null||achEntityId.trim().equals(""))?0:achEntityId.hashCode();
            pen.setId((int)entityId);
            pen.setRemoteId(achEntityId);

            for(int i = 0;i<atCPList.length();i++){
                JSONObject point = atCPList.getJSONObject(i);
                pen.addPoint(point.getInt("nPosx"),point.getInt("nPosy"));
            }

            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_PENGRAPH_NTF,pen);

        }catch(Exception e){
            TPLog.printError("解析终端水彩笔图元通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperLineOperInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tLine = AssParam.getJSONObject("tLine");
            JSONObject tEntity = tLine.getJSONObject("tEntity");
            String achEntityId = tEntity.getString("achEntityId");
            boolean bLock =  tEntity.getBoolean("bLock");
            JSONObject tBeginPt = tLine.getJSONObject("tBeginPt");
            JSONObject tEndPt = tLine.getJSONObject("tEndPt");
            int dwLineWidth = tLine.getInt("dwLineWidth");
            int dwRgb = tLine.getInt("dwRgb");
            String strDwRgb = Integer.toHexString(dwRgb);
            if(strDwRgb.length()<6){
                int count = 6- strDwRgb.length();
                for(int i = 0;i<count;i++){
                    strDwRgb = "0"+strDwRgb;
                }
            }
            if(strDwRgb.length()==7){
                strDwRgb = "0"+strDwRgb;
            }
            dwRgb = Color.parseColor("#"+strDwRgb);

            Line line = new Line();
            line.setColor(dwRgb);
            line.setRemotePageId(achTabId);
            line.setPageIndex(dwSubPageId);
            line.setRemoteId(achEntityId);
            line.setId(achEntityId.hashCode());
            line.setStrokeWidth(dwLineWidth);
            line.addPoint(tBeginPt.getInt("nPosx"),tBeginPt.getInt("nPosy"));
            line.addPoint(tEndPt.getInt("nPosx"),tEndPt.getInt("nPosy"));
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_LINE_GRAPH,line);

        } catch(Exception e){
            TPLog.printError("解析终端水彩笔图元通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperCircleOperInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tCircle = AssParam.getJSONObject("tCircle");
            JSONObject tEntity = tCircle.getJSONObject("tEntity");
            String achEntityId = tEntity.getString("achEntityId");
            boolean bLock =  tEntity.getBoolean("bLock");
            JSONObject tBeginPt = tCircle.getJSONObject("tBeginPt");
            JSONObject tEndPt = tCircle.getJSONObject("tEndPt");
            int dwLineWidth = tCircle.getInt("dwLineWidth");
            int dwRgb = tCircle.getInt("dwRgb");

            String strDwRgb = Integer.toHexString(dwRgb);
            if(strDwRgb.length()<6){
                int count = 6- strDwRgb.length();
                for(int i = 0;i<count;i++){
                    strDwRgb = "0"+strDwRgb;
                }
            }
            if(strDwRgb.length()==7){
                strDwRgb = "0"+strDwRgb;
            }
            dwRgb = Color.parseColor("#"+strDwRgb);

            Circle circle = new Circle();
            circle.setColor(dwRgb);
            circle.setRemotePageId(achTabId);
            circle.setPageIndex(dwSubPageId);
            circle.setRemoteId(achEntityId);
            circle.setId(achEntityId.hashCode());
            circle.setStrokeWidth(dwLineWidth);
            circle.addPoint(tBeginPt.getInt("nPosx"),tBeginPt.getInt("nPosy"));
            circle.addPoint(tEndPt.getInt("nPosx"),tEndPt.getInt("nPosy"));
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_CIRCLE_GRAPH,circle);
        } catch(Exception e){
            TPLog.printError("解析终端水彩笔图元通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperRectangleOperInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tRectangle = AssParam.getJSONObject("tRectangle");
            JSONObject tEntity = tRectangle.getJSONObject("tEntity");
            String achEntityId = tEntity.getString("achEntityId");
            boolean bLock =  tEntity.getBoolean("bLock");
            JSONObject tBeginPt = tRectangle.getJSONObject("tBeginPt");
            JSONObject tEndPt = tRectangle.getJSONObject("tEndPt");
            int dwLineWidth = tRectangle.getInt("dwLineWidth");
            int dwRgb = tRectangle.getInt("dwRgb");

            String strDwRgb = Integer.toHexString(dwRgb);
            if(strDwRgb.length()<6){
                int count = 6- strDwRgb.length();
                for(int i = 0;i<count;i++){
                    strDwRgb = "0"+strDwRgb;
                }
            }
            if(strDwRgb.length()==7){
                strDwRgb = "0"+strDwRgb;
            }
            dwRgb = Color.parseColor("#"+strDwRgb);

            Rectangle rectangle = new Rectangle();
            rectangle.setColor(dwRgb);
            rectangle.setRemotePageId(achTabId);
            rectangle.setPageIndex(dwSubPageId);
            rectangle.setRemoteId(achEntityId);
            rectangle.setId(achEntityId.hashCode());
            rectangle.setStrokeWidth(dwLineWidth);
            rectangle.addPoint(tBeginPt.getInt("nPosx"),tBeginPt.getInt("nPosy"));
            rectangle.addPoint(tEndPt.getInt("nPosx"),tEndPt.getInt("nPosy"));
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_RECTANGLE_GRAPH,rectangle);
        } catch(Exception e){
            TPLog.printError("解析终端水彩笔图元通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperReginErase_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{

            JSONObject  MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            if(achFromE164.equals(MtNetUtils.achTerminalE164)&&!MtNetUtils.synConfData){
                return;
            }

            JSONObject  AssParam = msg.getJSONObject("AssParam");
            String achGraphsId = AssParam.getString("achGraphsId");
            String achTabId = AssParam.getString("achTabId");
            int dwEraseHeight = AssParam.getInt("dwEraseHeight");
            int dwEraseWidth = AssParam.getInt("dwEraseWidth");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONArray tPoint = AssParam.getJSONArray("tPoint");

            Erase erase = new Erase();
            erase.setEraseWidth(dwEraseWidth);
            erase.setEraseHeight(dwEraseHeight);
            erase.setRemoteId(achGraphsId);
            erase.setId(achGraphsId.hashCode());
            erase.setRemotePageId(achTabId);
            erase.setTabId(achTabId.hashCode());
            erase.setPageIndex(dwSubPageId);

            for(int i = 0;i<tPoint.length();i++){
                JSONObject item = tPoint.getJSONObject(i);
                int nPosx = item.getInt("nPosx");
                int nPosy = item.getInt("nPosy");
                erase.addPoint(nPosx,nPosy);
            }
            MtConnectManager.getInstance().handMsg(MtConnectManager.SYN_ERASE,erase);
        } catch(Exception e){
            TPLog.printError("解析终擦板擦除同步消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUploadImage_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achPicUrl = AssParam.getString("achPicUrl");
            String achTabId = AssParam.getString("achTabId");
            int dwPageId = AssParam.getInt("dwPageId");

            ImgUploadUrl  mImgUploadUrl = new ImgUploadUrl(achPicUrl,achTabId,dwPageId);
            MtConnectManager.getInstance().handMsg(MtConnectManager.UPLOAD_IMG_URL_RSP,mImgUploadUrl);
        } catch(Exception e){
            TPLog.printError("解析请求上传图片地址消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsGetCurWhiteBoard_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            MtConnectManager.getInstance().handMsg(MtConnectManager.GET_CUR_DISPLAY_WB_RSP,achTabId);
        } catch(Exception e){
            TPLog.printError("解析获取当前显示白板消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUploadFile_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
//            String achTabid = msg.getString("achTabid");
//
//            MtConnectManager.getInstance().handMsg(MtConnectManager.GET_CUR_DISPLAY_WB_RSP,achTabId);
        } catch(Exception e){
            TPLog.printError("解析获取当前显示白板消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsReleaseConf_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String confE164 = msg.getString("basetype");
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_RELEASE_CONF,confE164);
        } catch(Exception e){
            TPLog.printError("解析结束数据会议消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsGetConfInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject MainParam = msg.getJSONObject("MainParam");
            boolean bCreator = MainParam.getBoolean("bCreator");
            boolean bSuccess = MainParam.getBoolean("bSuccess");
            NetUtil.isRemoteConf = bSuccess;
            MtNetUtils.curEmConfMode = MainParam.getInt("emConfMode");
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_CONF_INFO,"");
        } catch(Exception e){
            TPLog.printError("解析数据会议状态消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsDelOperator_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONArray atUserInfoList = msg.getJSONArray("atUserInfoList");
            for(int i = 0;i<atUserInfoList.length();i++){
                JSONObject item = atUserInfoList.getJSONObject(i);
                String achE164 = item.getString("achE164");
                if(achE164.equals(MtNetUtils.achTerminalE164)){
                    MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_DEL_OPERATOR,"");
                    return;
                }
            }
        } catch(Exception e){
            TPLog.printError("解析删除协作通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void ChairTokenGetNtf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            boolean basetype = msg.getBoolean("basetype");
            MtNetUtils.isConfManager = basetype;
            MtConnectManager.getInstance().handMsg(MtConnectManager.CHAIR_TOKEN_GET_NTF,basetype);
        } catch(Exception e){
            TPLog.printError("解析管理员权限申请结果通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void ChairPosNtf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            int dwTerId = msg.getInt("dwTerId");
            int dwMcuId = msg.getInt("dwMcuId");


            MtNetUtils.dwTerId = dwTerId;
            MtNetUtils.dwMcuId = dwMcuId;

            MtNetUtils.isConfManager = MtNetUtils.checkConfManager();

            MtConnectManager.getInstance().handMsg(MtConnectManager.CHAIR_TOKEN_GET_NTF, MtNetUtils.isConfManager);
        } catch(Exception e){
            TPLog.printError("解析管理员权限申请结果通知消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUserApplyOper_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject tUserInfo = msg.getJSONObject("tUserInfo");
            String achE164 = tUserInfo.getString("achE164");
            String achName = tUserInfo.getString("achE164");
            boolean bIsConfAdmin = tUserInfo.getBoolean("bIsConfAdmin");
            boolean bIsOper = tUserInfo.getBoolean("bIsOper");
            boolean bOnline = tUserInfo.getBoolean("bOnline");
            int emMttype = tUserInfo.getInt("emMttype");

            MtEntity me =new MtEntity();
            me.setE164(achE164);
            me.setName(achName);
            me.setConfAdmin(bIsConfAdmin);
            me.setOper(bIsOper);
            me.setOnline(bOnline);
            me.setEmMttype(emMttype);

            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_USER_APPLY_OPER_NTF,me);
        } catch(Exception e){
            TPLog.printError("解析申请协作权限消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsRejectOper_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject tUserInfo = msg.getJSONObject("tUserInfo");
            String achE164 = tUserInfo.getString("achE164");
            String achName = tUserInfo.getString("achE164");
            boolean bIsConfAdmin = tUserInfo.getBoolean("bIsConfAdmin");
            boolean bIsOper = tUserInfo.getBoolean("bIsOper");
            boolean bOnline = tUserInfo.getBoolean("bOnline");
            int emMttype = tUserInfo.getInt("emMttype");

            MtEntity me =new MtEntity();
            me.setE164(achE164);
            me.setName(achName);
            me.setConfAdmin(bIsConfAdmin);
            me.setOper(bIsOper);
            me.setOnline(bOnline);
            me.setEmMttype(emMttype);

            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_REJECT_OPER_NTF,me);
        } catch(Exception e){
            TPLog.printError("解析拒绝申请协作权限消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperInsertPic_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achImgId = AssParam.getString("achImgId");
            String achPicName = AssParam.getString("achPicName");
            String achTabId = AssParam.getString("achTabId");
            int dwImgHeight = AssParam.getInt("dwImgHeight");
            int dwImgWidth = AssParam.getInt("dwImgWidth");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tPoint = AssParam.getJSONObject("tPoint");
            JSONArray afMatrixValue = AssParam.getJSONArray("aachMatrixValue");

            float matrixValue[] = new float[9];
            for(int i = 0;i<afMatrixValue.length();i++){
                matrixValue[i] = Float.valueOf(afMatrixValue.getString(i));
            }

            ImageGraph ig = new ImageGraph();
            ig.setRemotePageId(achTabId);
            ig.setPageIndex(dwSubPageId);
            ig.setHeight(dwImgHeight);
            ig.setWidth(dwImgWidth);
            ig.setX(tPoint.getInt("nPosx"));
            ig.setY(tPoint.getInt("nPosy"));
            ig.setRemoteId(achImgId);
            ig.setId(achImgId.hashCode());
            ig.setFileName(achPicName);
            ig.setMatrixValues(matrixValue);

            MtConnectManager.getInstance().handMsg(MtConnectManager.OPER_IMG_INFO_NTF,ig);
        } catch(Exception e){
            TPLog.printError("解析插入图片消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperFullScreen_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{

           JSONObject MainParam =  msg.getJSONObject("MainParam");
           String achFromE164 = MainParam.getString("achFromE164");
           if(MtNetUtils.achTerminalE164.equals(achFromE164)&&!MtNetUtils.synConfData){
               return;
           }

            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achImgId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONArray afMatrixValue = AssParam.getJSONArray("aachMatrixValue");

            float matrixValue[] = new float[9];

            for(int i = 0;i<afMatrixValue.length();i++){

                matrixValue[i] = (float)afMatrixValue.getDouble(i);
            }

            SynCoordinateMsg scm = new SynCoordinateMsg(achImgId,dwSubPageId,matrixValue);

            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_SYN_COORDINATE_MSG_NTF,scm);
        } catch(Exception e){
            TPLog.printError("解析插入图片消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperScrollScreen_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achImgId = AssParam.getString("achTabId");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONObject tPoint = AssParam.getJSONObject("tPoint");

            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_SYN_TL_SCROLL_NTF,new TLScrollChangedNtf(achImgId,dwSubPageId,tPoint.getInt("nPosx"),tPoint.getInt("nPosy")));
        } catch(Exception e){
            TPLog.printError("解析TL拖动消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperZoomInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwZoom = AssParam.getInt("dwZoom");
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_SYN_TL_ZOOM_NTF,new TLZoomChangeNtf(achTabId,dwZoom/100f));
        } catch(Exception e){
            TPLog.printError("解析TL拖动消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsGetUserList_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject AssParam = msg.getJSONObject("AssParam");
            JSONArray atUserList = AssParam.getJSONArray("atUserList");
            List<MtEntity> mtList = new ArrayList<MtEntity>();
            for(int i = 0;i<atUserList.length();i++){
                JSONObject item = atUserList.getJSONObject(i);
                String achE164 = item.getString("achE164");
                String name = item.getString("achName");
                boolean bIsConfAdmin = item.getBoolean("bIsConfAdmin");
                boolean bIsOper = item.getBoolean("bIsOper");
                boolean bOnline = item.getBoolean("bOnline");
                int emMttype = item.getInt("emMttype");

                MtEntity mtEntity = new MtEntity();
                mtEntity.setE164(achE164);
                mtEntity.setName(name);
                mtEntity.setConfAdmin(bIsConfAdmin);
                mtEntity.setOper(bIsOper);
                mtEntity.setOnline(bOnline);
                mtEntity.setEmMttype(emMttype);

                mtList.add(mtEntity);
            }
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_GET_USER_LIST,mtList);
        } catch(Exception e){
            TPLog.printError("解析DCS与会人员列表消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsDelAllWhiteBoard_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String achConfE164 = msg.getString("strConfE164");
//            boolean  bSuccess = msg.getBoolean("bSuccess");
//            List<MtEntity> mtList = new ArrayList<MtEntity>();
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_DEL_ALL_WB,true);
        } catch(Exception e){
            TPLog.printError("解析DCS与会人员列表消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsUpdateConfInfo_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String achConfE164 = msg.getString("achConfE164");
            String achConfName = msg.getString("achConfName");
            int emConfMode = msg.getInt("emConfMode");
            int emConfType = msg.getInt("emConfType");
            MtNetUtils.curEmConfMode = emConfMode;

//            boolean  bSuccess = msg.getBoolean("bSuccess");
//            List<MtEntity> mtList = new ArrayList<MtEntity>();
            MtConnectManager.getInstance().handMsg(MtConnectManager.DCS_CONF_INFO_UPDATE,"");
        } catch(Exception e){
            TPLog.printError("解析DCS与会人员列表消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void ApplyChairNtf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
             int dwMcuId = msg.getInt("dwMcuId");
             int dwTerId = msg.getInt("dwTerId");

            MtConnectManager.getInstance().handMsg(MtConnectManager.APPLY_CHAIR_NTF,new ApplyChairNtf(dwMcuId,dwTerId));
        } catch(Exception e){
            TPLog.printError("解析申请管理方消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsApplyOper_Rsp(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            String achConfE164 = msg.getString("achConfE164");
            boolean bSuccess = msg.getBoolean("bSuccess");
            int dwErrorCode = msg.getInt("dwErrorCode");

            if(!bSuccess){
                MtConnectManager.getInstance().handMsg(MtConnectManager.APPLY_OPER_FAILED,dwErrorCode);
            }

        } catch(Exception e){
            TPLog.printError("解析申请协作权限RSP消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperPitchPicRotate_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{
            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            TPLog.printError("DcsOperPitchPicRotate_Ntf ---- > MtNetUtils.achTerminalE164 = "+MtNetUtils.achTerminalE164+",achFromE164 = "+achFromE164 + ",MtNetUtils.synConfData = "+MtNetUtils.synConfData );
            if(MtNetUtils.achTerminalE164!=null&&MtNetUtils.achTerminalE164.equals(achFromE164)&&!MtNetUtils.synConfData){
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
            int dwGraphsCount = AssParam.getInt("dwGraphsCount");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONArray atGraphsInfo = AssParam.getJSONArray("atGraphsInfo");

            if(dwGraphsCount == 0){
                return;
            }

            ImgCoordinate imgCoordinates[] = new ImgCoordinate[dwGraphsCount];
            for(int i = 0;i<dwGraphsCount;i++){
                JSONObject item = atGraphsInfo.getJSONObject(i);
                String achGraphsId = item.getString("achGraphsId");
                JSONArray aachMatrixValue = item.getJSONArray("aachMatrixValue");
                float matrixValue[] = new float[9];
                for(int j = 0;j<aachMatrixValue.length();j++){
                    matrixValue[j] = Float.valueOf(aachMatrixValue.getString(j));
                }
                imgCoordinates[i] = new ImgCoordinate(achGraphsId,matrixValue);
            }

            SelectImgCoordinateEntity selectImgCoordinateEntity = new SelectImgCoordinateEntity(achTabId,dwSubPageId,imgCoordinates);

            MtConnectManager.getInstance().handMsg(MtConnectManager.IMAGE_COORDINATE_CHANGED,selectImgCoordinateEntity);

        } catch(Exception e){
            TPLog.printError("解析同步选中图片坐标系改变消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }

    @Override
    public void DcsOperPitchPicDel_Ntf(JSONObject msg) {
        if(msg==null){
            return;
        }
        try{

            JSONObject MainParam = msg.getJSONObject("MainParam");
            String achFromE164 = MainParam.getString("achFromE164");
            if(MtNetUtils.achTerminalE164!=null&&MtNetUtils.achTerminalE164.equals(achFromE164)&&!MtNetUtils.synConfData){
                return;
            }

            JSONObject AssParam = msg.getJSONObject("AssParam");
            String achTabId = AssParam.getString("achTabId");
//            int dwGraphsCount = AssParam.getInt("dwGraphsCount");
            int dwSubPageId = AssParam.getInt("dwSubPageId");
            JSONArray achGraphsId = AssParam.getJSONArray("achGraphsId");

            if(achGraphsId.length() == 0){
                return;
            }

            String delGraphId[] = new String[achGraphsId.length()];
            for(int i = 0;i<achGraphsId.length();i++){
                String achGraphId = achGraphsId.getString(i);
                delGraphId[i] = achGraphId;
            }

            DelSelectImgEntity delSelectImgEntity = new DelSelectImgEntity(achTabId,dwSubPageId,delGraphId);

            MtConnectManager.getInstance().handMsg(MtConnectManager.DEL_SELECT_IMG,delSelectImgEntity);

        } catch(Exception e){
            TPLog.printError("解析删除选中图片消息时出现异常:"+e+",Msg="+msg.toString());
            TPLog.printError(e);
        }
    }
}
