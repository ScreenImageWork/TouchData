package com.kedacom.touchdata.net;

import android.os.Handler;
import android.os.Message;

/**
 * Created by zhanglei on 2016/1/25.
 */
public class NetHandler extends Handler{

    public static final int OSP_CONNECTED = 100;  //OSP连接成功

    public static final int LOGIN_TIMEOUT = 1;  //登录超时

    public static final int RECONNECT = 200;  //重新登陆

    public static final int IMAGE_DOWNLOAD = 3; //图片下载

    public static final int LOGIN = 4;  //登录

    public static final int CREATE_MEETING = 5; //创建会议

    public static final int JOIN_MEETING = 6; //加入会议响应

    public static final int GRAPH_UPDATE = 7; //图元更新，一般为新增图元

    public static final int SYNCHRONOUSE = 8; //入会数据同步

    public static final int NOTIFY_TABLE = 9; //选项卡更新

    public static final int CHANGE_PAGE = 10; //切换页面

    public static final int SR_CHANGE_PAGE = 11; //服务器请求翻页

    public static final int OSP_DISCONNECT = 12; //OSP断开连接

    public static final int SCROLL_CHANGED = 13;  //SCROLL位置改变

    public static final int SC_SYNCHRONOUS = 14; //服务器请求同步数据

    public static final int EXCEPTION = 15;  //异常

    public static final int ROTATE_CHANGED = 17;  //屏幕旋转

    public static final int SCALE_CHANGED = 18;  //屏幕缩放

    public static final int CLEAR_SCREEN = 19; //清屏

    public static final int ROTATE_LEFT = 20; //左旋转  pc端发送过来的旋转消息

    public static final int ROTATE_RIGHT = 21; //右旋转  pc端发送过来的旋转消息

    public static final int CREATE_TAB = 22; //新建选项卡

    public static final int DEL_TAB = 23; //删除选项卡

    public static final int DEL_ALL_TAB = 37; //删除选项卡

    public static final int UNDO = 24;  //撤销

    public static final int REDO = 25;  //还原

    public static final int ADD_IMAGE = 26;

    public static final int IMAGE_DOWNSUCCESS = 27;

    public static final int BUF_SIZE = 28;

    public static final int MEETING_NAME= 29;

    public static final int OSP_EXCEPTION = 30;

    public static final int SYNCHRONOUSE_FAILED = 31;

    public static final int SCALE_CHANGED_FROM_GESTURE = 32;

    public static final int INSERT_IMG = 33;

    public static final int COORDINATE_CHANGED = 34;

    public static final int GRAPH_COORDINATE_CHANGED = 35;

    public static final int DELETE_GRAPH = 36;

    public static final int CONF_MEMBER_NUM_UPDATE = 38; //参会人员列表更新

    public static final int SERVER_CONNECT_NUM_NTF = 39;  //连接人员列表更新

    public static final int TABID_TEST = 16; //测试使用  设置tabId

    public void sendMessage(int what,Object obj){
        Message msg = this.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        msg.sendToTarget();
    }

    public void removeLoginTimer(){
        if(this.hasMessages(LOGIN_TIMEOUT)){
            removeMessages(LOGIN_TIMEOUT);
            if(this.hasMessages(LOGIN_TIMEOUT)){
                removeLoginTimer();
            }
        }
    }

    public void removeReLoginTimer(){
        if(this.hasMessages(RECONNECT)){
            removeMessages(RECONNECT);
            if(this.hasMessages(RECONNECT)){
                removeLoginTimer();
            }
        }
    }
}
