package com.kedacom.touchdata.net.mtnet.entity;

import org.json.JSONArray;

/**
 * Created by zhanglei on 2018/7/20.
 */

public class SelectImgCoordinateEntity {

   private String achTabId ;
   private int dwSubPageId;
   private  ImgCoordinate imgCoordinates[];

   public SelectImgCoordinateEntity(String achTabId, int dwSubPageId,ImgCoordinate imgCoordinates[]){
       this.achTabId = achTabId;
       this.dwSubPageId = dwSubPageId;
       this.imgCoordinates = imgCoordinates;
   }

    public String getAchTabId() {
        return achTabId;
    }

    public int getDwSubPageId() {
        return dwSubPageId;
    }

    public ImgCoordinate[] getImgCoordinates() {
        return imgCoordinates;
    }
}
