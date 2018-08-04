package com.kedacom.app;

import android.app.Application;
import android.util.Log;

import com.kedacom.frambuffer.SkiaPaint;
import com.kedacom.touchdata.filemanager.FileUtils;
import com.kedacom.touchdata.whiteboard.utils.BitmapManager;
import com.kedacom.touchdata.whiteboard.utils.ResUtils;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.CrashExceptionHandler;
import com.kedacom.utils.VersionUtils;
import com.touch.touchsdk.HuaXinSdkMng;

import java.io.File;


/**
 * Created by zhanglei on 2016/12/5.
 */
public class TouchDataApp extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("TouchDataApp","TouchData Application start");

        Log.d("TouchDataApp","->init CrashExceptionHandler");
        CrashExceptionHandler mCrashExceptionHandler = CrashExceptionHandler.getInstance();
        mCrashExceptionHandler.init(this);
        Log.d("TouchDataApp","->init TPLog");
        TPLog.setLocalSaveLogParams(FileUtils.LOG_DIR,10,TPLog.LOG_LVL_ERROR);
        Log.d("TouchDataApp","->init BitmapManager");
        BitmapManager.getInstence().init(this);
        Log.d("TouchDataApp","->init ResUtils");
        ResUtils.init(getApplicationContext());
        Log.d("TouchDataApp","->init VersionManager");
        VersionUtils.init();
//        Log.d("TouchDataApp","->init HuaXinSdk");
//        if(VersionUtils.isImix())
//            HuaXinSdkMng.start();

        if(VersionUtils.isImix()) {
            Log.d("TouchDataApp","->init SkiaPaint Log");
            SkiaPaint.showlog(false, false, "/log/frambuffer.log");
        }
    }

}
