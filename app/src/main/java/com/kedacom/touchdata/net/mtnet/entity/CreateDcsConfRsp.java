package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/4/12.
 */

public class CreateDcsConfRsp {
    private boolean bCreator ;
    private boolean bSuccess ;
    private int dwErrorCode;
    private int emConfMode;
    private int emConfType;

    public CreateDcsConfRsp(){

    }

    public CreateDcsConfRsp(boolean bCreator,boolean bSuccess,int dwErrorCode,int emConfMode,int emConfType){
        setbCreator(bCreator);
        setbSuccess(bSuccess);
        setDwErrorCode(dwErrorCode);
        setEmConfMode(emConfMode);
        setEmConfType(emConfType);
    }

    public boolean isbCreator() {
        return bCreator;
    }

    public void setbCreator(boolean bCreator) {
        this.bCreator = bCreator;
    }

    public boolean isbSuccess() {
        return bSuccess;
    }

    public void setbSuccess(boolean bSuccess) {
        this.bSuccess = bSuccess;
    }

    public int getDwErrorCode() {
        return dwErrorCode;
    }

    public void setDwErrorCode(int dwErrorCode) {
        this.dwErrorCode = dwErrorCode;
    }

    public int getEmConfMode() {
        return emConfMode;
    }

    public void setEmConfMode(int emConfMode) {
        this.emConfMode = emConfMode;
    }

    public int getEmConfType() {
        return emConfType;
    }

    public void setEmConfType(int emConfType) {
        this.emConfType = emConfType;
    }
}
