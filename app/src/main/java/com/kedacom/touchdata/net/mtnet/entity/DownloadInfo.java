package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/5/3.
 */

public class DownloadInfo {

    public String achTabId;

    public String achPicUrl;

    public String achWbPicentityId;

    public int dwPageId;

    public String fileName;

    public String achFilePathName;

    public boolean bElementFile = false;

    public boolean bSuccess = false;

    public DownloadInfo(){

    }

    public DownloadInfo(String achTabId,String achPicUrl,String achWbPicentityId,int dwPageId){
        this.achTabId = achTabId;
        this.achPicUrl = achPicUrl;
        this.achWbPicentityId = achWbPicentityId;
        this.dwPageId = dwPageId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getAchFilePathName() {
        return achFilePathName;
    }

    public void setAchFilePathName(String achFilePathName) {
        this.achFilePathName = achFilePathName;
    }

    public boolean isbElementFile() {
        return bElementFile;
    }

    public void setbElementFile(boolean bElementFile) {
        this.bElementFile = bElementFile;
    }

    public boolean isbSuccess() {
        return bSuccess;
    }

    public void setbSuccess(boolean bSuccess) {
        this.bSuccess = bSuccess;
    }
}
