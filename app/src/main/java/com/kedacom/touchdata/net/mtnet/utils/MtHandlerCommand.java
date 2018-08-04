package com.kedacom.touchdata.net.mtnet.utils;

/**
 * Created by zhanglei on 2018/3/21.
 */

public interface MtHandlerCommand {
    static int CONNECT_MT = 1;//连接终端反馈消息

    static int CONF_INFO = 2;//当前会议信息

    static int CONF_DETAIL = 3;//当前会议详细信息

    static int CONF_MT_MEMBER = 4;//当前会议中与会者列表

    static int CREATE_DCONF = 5;//创建数据会议反馈结果

    static int JOIN_DCONF_NTF = 6;//加入白板会议通知

    static int CREATE_WB_RSP = 7; //同步创建白板消息返回结果

    static int CREATE_WB_NTF = 8; //同步创建白板消息

    static int SYN_PENGRAPH_RSP = 9; //同步铅笔图元反馈结果

    static int SYN_PENGRAPH_NTF = 10; //同步铅笔图元消息

    static int START_CONF_NTF = 11; //同步铅笔图元消息

    static int OVER_CONF_NTF = 12; //同步铅笔图元消息

    static int LEAVE_CONF_NTF = 13; //用户退会通知

    static int IS_ALREADY_IN_CONF = 14;//主动获取终端信息，判断终端是否已经在会议中

    static int GET_TERMINAL_E164 = 15; //当前连接终端的E164号获取

    static int MT_DISCONNECT_NTF = 16; //终端断开连接通知

    static int MT_RECONNECT = 17; //重连终端，程序内部定时消息

    static int MT_CONNECT_TIMEOUT = 18; //连接超时

    static int GET_ALL_WB = 19;   //入会同步时请求所有白板

    static int SWITCH_TAB_PAGE = 20; //白板翻页

    static int AREA_ERASE = 21; //区域擦除

    static int DEL_WB_PAGE = 22;//删除白板页

    static int CLEAR_SCREN = 23; //清屏

    static int SYN_UNDO = 24; //撤销

    static int SYN_REDO = 25; //恢复

    static int JOIN_CONF_SYN_END = 26;  //入会同步完成返回结果

    static int ADD_OPERATOR_NTF = 27; //添加协作方通知

    static int QUIT_DCS_CONF_NTF = 28; //退出白板会议通知，用可能不是当前用户

    static int QUIT_DCS_CONF_RSP = 29;  //退出数据会议响应

    static int OPER_IMG_INFO_NTF = 30; // 图片图元信息同步

    static int DOWNLOAD_IMAGE_NTF = 31; // 下载图片通知 ，只有第一次插入图片是才会通知

    static int DOWNLOAD_FILE_RSP = 32;// 下载文件响应

    static int SYN_LINE_GRAPH = 33; //  同步直线图元

    static int SYN_CIRCLE_GRAPH = 34; //  同步直线图元

    static int SYN_RECTANGLE_GRAPH = 35; //  同步直线图元

    static int SYN_ERASE = 36; //同步擦除图元

    static int UPLOAD_IMG_URL_RSP = 37;

    static int GET_CUR_DISPLAY_WB_RSP = 38;

    static int DCS_RELEASE_CONF = 39;

    static int DCS_CONF_INFO = 40;
    //DelOperator
    static int DCS_DEL_OPERATOR = 41;
    //ChairTokenGetNtf
    static int CHAIR_TOKEN_GET_NTF = 42;  //管理员结果通知
    //UserApplyOper_Ntf
    static int DCS_USER_APPLY_OPER_NTF = 43;
    // DcsRejectOper_Ntf
    static int DCS_REJECT_OPER_NTF = 44;  //管理方协作申请
    //SynCoordinateMsg
    static int DCS_SYN_COORDINATE_MSG_NTF = 45;//白板坐标系改变

    static int DCS_SYN_TL_SCROLL_NTF = 46;//TL白板滚动条拖动

    static int DCS_SYN_TL_ZOOM_NTF = 47; //TL白板缩放

    static int DCS_GET_USER_LIST = 48; //数据会议与会人员列表

    static int DCS_DEL_ALL_WB = 49;   //删除所有白板

    static int DCS_CONF_INFO_UPDATE = 50; //数据会议信息更新

    static int APPLY_CHAIR_NTF = 51; // 其它终端申请管理员权限

    static int APPLY_OPER_FAILED = 52;//申请协作权限失败

    static int IMAGE_COORDINATE_CHANGED = 53; //图片坐标系改变

    static int DEL_SELECT_IMG = 54;//删除选中图片


}
