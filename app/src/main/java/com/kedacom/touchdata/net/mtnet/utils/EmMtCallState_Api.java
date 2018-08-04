package com.kedacom.touchdata.net.mtnet.utils;

/**
 * Created by zhanglei on 2018/4/12.
 */

public class EmMtCallState_Api {
   public static int  emCallIdle_Api = 0;

    public static int  emCallRasConfJoining_Api=1 ;// ras非标加入会议，等待mcu 呼叫

    public static int  emCallRasConfCreating_Api=2; //  ras非标创建会议，等待mcu 呼叫

    public static int   emCallOuting_Api = 3;// 呼出

    public static int  emCallIncoming_Api=4;// 呼入

    public static int   emCallAccepted_Api=5;//  接收

    public static int   emCallHanging_Api=6;//  挂断

    public static int  emCallConnected_Api=7;//  h323 是 225connected, sip是呼叫信令交互完

    public static int    emCallP2P_Api=8;//  点对点

    public static int  emCallMCC_Api=9;//  多点
}
