package com.kedacom.touchdata.net.mtnet.entity;

/**
 * Created by zhanglei on 2018/5/9.
 */

public class ImgUploadUrl {
    public String achPicUrl;
    public String achTabId;
    public int achSubPageIndex;

    public ImgUploadUrl(){

    }

    public ImgUploadUrl(String achPicUrl,String achTabId,int achSubPageIndex){
        this.achPicUrl = achPicUrl;
        this.achTabId = achTabId;
        this.achSubPageIndex = achSubPageIndex;
    }

}
