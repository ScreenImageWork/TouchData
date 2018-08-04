package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/3/15.
 */

public class MtEntity {

    private String name = "";              //终端名称

    private String e164 = "";             //终端e164

    private  int emMttype;          //终端类型

    private boolean bIsConfAdmin;  //是否是会议管理员

    private  boolean bIsOper;      //是否可以进行协作

    private  boolean bOnline;      //是否在会议中

    private int dwMcuId;   //当前会议mcuId

    private int dwTerId;   //当前会议中的终端Id

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getE164() {
        return e164;
    }

    public void setE164(String e164) {
        this.e164 = e164;
    }

    public int getEmMttype() {
        return emMttype;
    }

    public void setEmMttype(int emMttype) {
        this.emMttype = emMttype;
    }

    public boolean isConfAdmin() {
        return bIsConfAdmin;
    }

    public void setConfAdmin(boolean bIsConfAdmin) {
        this.bIsConfAdmin = bIsConfAdmin;
    }

    public boolean isOper() {
        return bIsOper;
    }

    public void setOper(boolean bIsOper) {
        this.bIsOper = bIsOper;
    }

    public boolean isOnline() {
        return bOnline;
    }

    public void setOnline(boolean bOnline) {
        this.bOnline = bOnline;
    }

    public int getDwMcuId() {
        return dwMcuId;
    }

    public void setDwMcuId(int dwMcuId) {
        this.dwMcuId = dwMcuId;
    }

    public int getDwTerId() {
        return dwTerId;
    }

    public void setDwTerId(int dwTerId) {
        this.dwTerId = dwTerId;
    }

    @Override
    public String toString() {
        return "MTEntity:[name="+name+",e164="+e164+",emMttype="+emMttype+",bIsConfAdmin="+bIsConfAdmin+",bIsOper="+bIsOper+",bOnline="+bOnline+"]";
    }
}
