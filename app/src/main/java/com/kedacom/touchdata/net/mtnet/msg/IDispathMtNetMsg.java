package com.kedacom.touchdata.net.mtnet.msg;

import org.json.JSONObject;

/**
 * Created by zhanglei on 2018/3/21.
 * 终端反馈消息列表
 */

public interface IDispathMtNetMsg {

    void MTCLoginRsp(JSONObject msg);//连接终端返回结果

    void MTDisconnectNtf(JSONObject msg);//终端连接断开通知

    void ConfInfoNtf(JSONObject msg);//会议信息

    void GetConfDetailInfoNtf(JSONObject msg);//会议详细信息

    void ImMulitChatMemberLeaveNtf(JSONObject msg);//退会消息

    void OnLineTerListRsp(JSONObject msg);//与会者列表

    void DcsCreateConf_Rsp(JSONObject msg);//创建白板会议返回结果

    void DcsUserJoinConf_Ntf(JSONObject msg);//加入白板会议通知

    void DcsNewWhiteBoard_Rsp(JSONObject msg);//请求同步创建白板消息响应

    void DcsNewWhiteBoard_Ntf(JSONObject msg); //创建白板同步消息

    void DcsOperPencilOperInfo_Rsp(JSONObject msg); //同步画线图元反馈结果

    void DcsOperPencilOperInfo_Ntf(JSONObject msg); //画线图元同步

    void MulConfStartedNtf(JSONObject msg);//开始会议通知

    void MulConfEndedNtf(JSONObject msg); //结束会议通知

    void CallLinkState_Rsp(JSONObject msg); //当前会议状态响应

    void GetApsLoginParamCfg_Rsp(JSONObject msg); //获取当前终端的E164号返回结果\

    void DcsGetAllWhiteBoard_Rsp(JSONObject msg); //入会同步获取所有的白板信息

    void DcsElementOperFinal_Ntf(JSONObject msg);//入会同步数据完成

    void DcsSwitch_Ntf(JSONObject msg);  //白板翻页通知

    void DcsOperEraseOperInfo_Ntf(JSONObject msg);  //区域擦除图元同步

    void DcsDelWhiteBoard_Ntf(JSONObject msg); //删除白板

    void DcsOperClearScreen_Ntf(JSONObject msg); //清屏

    void DcsOperUndo_Ntf(JSONObject msg); //撤销

    void DcsOperRedo_Ntf(JSONObject msg); //恢复

    void DcsAddOperator_Ntf(JSONObject msg);//添加协作方

    void DcsUserQuitConf_Ntf(JSONObject msg); //有用户退出了白板会议

    void ConfCanceledNtf(JSONObject msg);//同视频会议结束

    void DcsQuitConf_Rsp(JSONObject msg);  //退出白板会议响应

    void DcsOperImageOperInfo_Ntf(JSONObject msg); //图片同步信息

    void DcsDownloadImage_Ntf(JSONObject msg);  //图片下载连接同步

    void DcsDownloadImage_Rsp(JSONObject msg);  //图片下载地址请求返回结果

    void DcsDownloadFile_Rsp(JSONObject msg); //图片下载返回结果

    void DcsOperColorPenOperInfo_Ntf(JSONObject msg);  //终端水彩笔图元

    void DcsOperLineOperInfo_Ntf(JSONObject msg);  //终端直线图元

    void DcsOperCircleOperInfo_Ntf(JSONObject msg); //终端圆型图元

    void DcsOperRectangleOperInfo_Ntf(JSONObject msg); //画矩形通知

    void DcsOperReginErase_Ntf(JSONObject msg); //擦板擦除同步

    void DcsUploadImage_Rsp(JSONObject msg);  //上传图片路径获取响应地址

    void DcsGetCurWhiteBoard_Rsp(JSONObject msg); //获取当前显示白板响应结果

    void DcsUploadFile_Ntf(JSONObject msg); //图片上传通知

    void DcsReleaseConf_Ntf(JSONObject msg); //DCS会议销毁

    void DcsGetConfInfo_Ntf(JSONObject msg); //连接时，当前会议状态通知

    void DcsDelOperator_Ntf(JSONObject msg); //协作权限禁止通知

    void ChairTokenGetNtf(JSONObject msg);  //管理员权限获取结果通知

    void ChairPosNtf(JSONObject msg); //主席移位，也可以理解为失去主席位置

    void DcsUserApplyOper_Ntf(JSONObject msg); //用户请求协作权限

    void DcsRejectOper_Ntf(JSONObject msg); //拒绝申请协作方请求通知

    void DcsOperInsertPic_Ntf(JSONObject msg); //插入图片通知

    void DcsOperFullScreen_Ntf(JSONObject msg); //同步坐标系信息

    void DcsOperScrollScreen_Ntf(JSONObject msg);  //TL白板拖动

    void DcsOperZoomInfo_Ntf(JSONObject msg); //TL白板缩放

    void DcsGetUserList_Rsp(JSONObject msg);  //获取当前协作用户列表

    void DcsDelAllWhiteBoard_Ntf(JSONObject msg); //删除所有白板通知

    void DcsUpdateConfInfo_Ntf(JSONObject msg);  //数据会议信息改变通知

    void ApplyChairNtf(JSONObject msg); //终端请求管理权限

    void DcsApplyOper_Rsp(JSONObject msg);

    void DcsOperPitchPicRotate_Ntf(JSONObject msg);

    void DcsOperPitchPicDel_Ntf(JSONObject msg);

}
