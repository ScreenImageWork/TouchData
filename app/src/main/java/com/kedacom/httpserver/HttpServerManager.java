package com.kedacom.httpserver;

import android.content.Context;
import android.content.Intent;

import com.kedacom.httpserver.service.WebService;
import com.kedacom.httpserver.utils.Constant;
import com.kedacom.httpserver.utils.Network;

/**
 * Created by zhanglei on 2017/3/10.
 */
public class HttpServerManager {

    private static boolean httpServerStart = false;

    /**
     * 获取白板分享链接
     * @return
     */
    public static String getWbShareUri(Context context){
        String ip = Network.getLocalIp();
        return "http://" + ip + ":" + Constant.Config.PORT + Constant.Config.Web_Root;
    }

    public static void startHttpServer(Context context){
        if(context==null){
            return;
        }
        context.startService(new Intent(context, WebService.class));
        httpServerStart = true;
    }

    public static void stopHttpServere(Context context){
        context.stopService(new Intent(context, WebService.class));
        httpServerStart = false;
    }

    /**
     * 设置分享目录
     * @param fileDir 需要分享的文件夹目录
     */
    public static void setShareDir(String fileDir){
        Constant.shareDir = fileDir;
    }

    /**
     * 设置分享页面标题
     * @param title 分享标题
     */
    public static void setShareTitle(String title){
        Constant.shareTitle = title;
    }

    /**
     *设置分享页面副标题
     * @param subTitle 分享副标题
     */
    public static void setShareSubtitle(String subTitle){
        Constant.sharSubtitle = subTitle;
}

    /**
     * 设置分享参数
     * @param shareDir  分享目录
     * @param title 分享标题
     * @param subTitle 分享副标题
     */
    public static void setShareParams(String shareDir,String title,String subTitle){
        Constant.shareDir = shareDir;
        Constant.shareTitle = title;
        Constant.sharSubtitle = subTitle;
    }
}
