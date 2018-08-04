package com.kedacom.touchdata.net.mtnet;

import android.telecom.Call;

import com.kedacom.kdv.mt.mtapi.IMtcCallback;
import com.kedacom.kdv.mt.mtapi.MtcLib;
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
import com.kedacom.touchdata.whiteboard.graph.AreaErase;
import com.kedacom.touchdata.whiteboard.graph.Circle;
import com.kedacom.touchdata.whiteboard.graph.Erase;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.graph.Line;
import com.kedacom.touchdata.whiteboard.graph.Pen;
import com.kedacom.touchdata.whiteboard.graph.Rectangle;
import com.kedacom.touchdata.whiteboard.page.Page;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/3/20.
 */

public interface MtNetCallback{

    void onMtConnect(boolean rst);  //连接终端反馈结果

    void onMtDisconnect(); //终端断开连接

    void onMtConfInfo();    //终端会议信息返回结果

    void onMtConfDatil();  //终端会议详细信息返回结果

    void onMtConfMemberList(); //当前与会者列表返回结果

    void onMtCreateWbConf(CreateDcsConfRsp rsp); //创建白板会议返回结果

    void onMtJoinWbConfNtf(MtEntity mt); //用户加入白板会议通知

    void onMtSynCreateWbRsp(boolean success); //同步新建白板消息反馈结果

    void onMtCreateWhiteBoardNtf(Page page); //新建白板同步消息

    void onMtSynPenGraphRsp(boolean success); //同步画笔图元反馈消息

    void onMtSynPenGraphNtf(Pen pen); //同步铅笔图元消息

    void onMtStartConfNtf(); //会议开始通知

    void onMtOverConfNtf();  //会议结束通知

    void onMtMemberLeaveConfNtf();  //有用户退出会议通知

    void onMtIsAlreadyInConf(boolean isIn); //终端是否已经在会议中

    void onMtTerminalE164Rsp(String e164);  //终端E164响应结果

    void onMtGetAllWbRsp(ArrayList<Page> list);  //获取所有白板响应

    void onMtSwitchTabPage(String tabId);  //白板翻页通知

    void onMtSynAreaEraseGraphNtf(AreaErase ae); //区域擦除通知

    void onMtDelWbPageNtf(String tabId); //删除白板

    void onMtClearScreenNtf(ClearScreenNtf csn); //清屏

    void onMtUndoNtf(UnDoOrReDoNtf udn);

    void onMtRedoNtf(UnDoOrReDoNtf rdn);

    void onMtJoinConfSynEnd(ElementOperFinalNtf eofn);

    void onMtAddOperator(ArrayList<String> operratorList);

    void onMtQuitDcsConfNtf(String userE164);

    void onMtQuitDcsConfRsp(boolean success);

    void onMtOperImgInfoNtf(ImageGraph img);

    void onMtDownloadImageNtf(DownloadInfo dInfo);

    void onMtDownloadImageRsp(DownloadInfo dInfo);

    void onMtSynLineNtf(Line line);

    void onMtSynCircleNtf(Circle circle);

    void onMtSynRectangleNtf(Rectangle rectangle);

    void onMtSynEraseNtf(Erase erase);

    void onMtUploadUrlRsp(ImgUploadUrl upload);

    void onMtDisplayCurWbRsp(String tabid);

    void onMtReleaseDcsConf(String confE164);

    void onMtDcsConfInfoNtf();

    void onMtDelOperatorNtf();

    void onMtChairTokenGetNtf(boolean mng);

    void onMtUserApplyOperNtf(MtEntity me);

    void onMtReqOperNtf(MtEntity me); //其他与会方申请协作权限

    //DcsRejectOper_Ntf
    void onMtRejectOperNtf(MtEntity me);  //管理方拒绝协作申请

    void onMtSynCoordinateMsgNtf(SynCoordinateMsg scm);

    void onMtSynTLScrollNtf(TLScrollChangedNtf tkscn);

    void onMtSynTLZoomNtf(TLZoomChangeNtf tlzcn);

    void onMtDCSGetUserList(List<MtEntity> list);

    void onMtDelAllWhiteBoard(boolean success);

    void onMtDcsConfInfoUpdateNtf();

    void onMtApplyChair(ApplyChairNtf acn);

    void onMtApplyOperFailed(int errorCode);

    void onMtSelectImgCoordinateChanged(SelectImgCoordinateEntity selectImgCoordinateEntity);

    void onMtDelSelectImg(DelSelectImgEntity delSelectImgEntity);

}
