package com.kedacom.touchdata.net.callback;

import android.graphics.Point;

import com.kedacom.touchdata.net.entity.CoordinateChangedMsg;
import com.kedacom.touchdata.net.entity.DeleteGraphMsg;
import com.kedacom.touchdata.net.entity.GraphCoordinateChangedMsg;
import com.kedacom.touchdata.whiteboard.graph.Graph;
import com.kedacom.touchdata.whiteboard.graph.ImageGraph;
import com.kedacom.touchdata.whiteboard.page.Page;
import com.kedacom.touchdata.whiteboard.page.SubPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zhanglei on 2016/12/16.
 */
public interface NetCallback {

    void onServerConnected();  //与服务器连接成功

    void onLoginServerSuccess();

    void onRecMeetingName(String meetingName);

    void onServerDisconnected();  //与服务器断开链接

    void onServerConnectException(Exception e); //链接服务器异常

    void onRecCreateMeeting(long resultCode);  //创建/加入会议返回结果

    void onRecJoinMeeting(long resultCode);  //创建/加入会议返回结果

    void onRecServerCurBufferSzie(long size);//服务器当前接收流量大小，发送文件时获取

    void onRecServerReqSyn(long synReqId); //服务器请求同步数据

    void onRecSynFailed(); //同步数据失败

    void onRecSynData(long curPageId,List<Page> pageList); //接收到同步数据  map = {curPageId,PageList}

    void onRecGraphData(Graph graph); //接收到同步图元

    void onRecImageData(SubPage subpage); // 接收到同步图片

    void  onRecZoomData(float zoom); //接收到缩放数据

    void  onRecGestureZoomData(float scaleFactor,int focusX,int focusY); //接收到手势缩放数据

    void onRecScrollData(Point scrollData); //接收到滚动白板命令

    void onRecClearScreen();  //接收到清屏命令

    void onRecCreateWbData(Page page);//接收到创建白板数据

    void onRecDelWbData(long delWbId,long nextWbId); //接收到删除白板消息

    void onRecDelAllWbData(long newTabId); //接收到删除所有白板消息

    void onRecChangePage(long wbId,long subPageIndex); //切换白板或者子页

    void onRecServerReqChangePage(long wbId,long subPageIndex); //服务器请求翻页

    void onRecImageDownloaded(long imageId); //接收到图片实体完毕

    void onRecRedoData(long wbId,long subPageIndex); //接收到恢复数据

    void onRecUndoData(long wbId,long subPageIndex); //接收到撤销数据

    void onRecLeftRotate(); //接收到左旋转指令

    void onRecRightRotate(); //接收到右旋转指令

    void onRecRotate(long angle,long isFinish);//接收到旋转消息

    void onRecInsertImg(ImageGraph img); //接收到插入图片消息

    void onRecCoordinateChanged(CoordinateChangedMsg msg); //接收到坐标系改变消息

    void onRecSelectGrpahCoordinateChanged(ArrayList<GraphCoordinateChangedMsg> list); //图元状态改变

    void onRecDeleteGraph(DeleteGraphMsg msg);  //删除单个图元

    void onRecConfMemberListUpdate(int num);

    void onRecServerConnectNumNtf(int num);

}
