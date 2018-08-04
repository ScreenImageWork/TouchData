package com.kedacom.touchdata.net.utils;

/**
 * Created by yujinjin on 2016/1/28.
 */
public class NetError {
    public static final int ESUCCESS = 0;

    public static final int ENO_IDLE_INSTANCE = ESUCCESS + 1;//has no idle instance
    public static final int EONLY_ONE_USER = ESUCCESS + 2;//the first one joined into the conference
    public static final int ETOO_MANY_CONFERENCES = ESUCCESS + 3;	//extends the limit of conference accounts
    public static final int ETOO_MANY_USERS = ESUCCESS + 4;	//extends the limit of user counts
    public static final int ENO_USER_ALREADY = ESUCCESS + 5;	//no user is already
    public static final int ECONFERENCE_ALREADY_EXIST = ESUCCESS + 6; //conference already exist
    public static final int ECONFERENCE_NOT_EXIST = ESUCCESS + 7;       // conference not exist
    public static final int EOVER_LICENSE = ESUCCESS + 8;	//超过授权最大用户量
    public static final int EINVALID_PWD = ESUCCESS + 9;		//无效的密码
    public static final int EALREADY_IN_CONFERENCE = ESUCCESS + 10;     //已在会中，重复入会/创会
    public static final int ECLIENT_VER_TOO_LOW = ESUCCESS + 11;
    public static final int ESUCCESS_NOT_NEED_SYN = ESUCCESS + 12; //入会后不需要同步数据
    public static final int PROHIBIT_CREAR_CONFERNENCE = ESUCCESS + 13; //禁止创建会议
}
