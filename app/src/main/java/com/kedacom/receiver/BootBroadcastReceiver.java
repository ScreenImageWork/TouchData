package com.kedacom.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.kedacom.app.TouchDataApp;
import com.kedacom.service.BootService;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.Utils;
import com.kedacom.utils.VersionUtils;

/**
 * Created by zhanglei on 2016/12/22.
 */
public class BootBroadcastReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        if(VersionUtils.is55InchDevice()){
            return;
        }

        boolean isStartService = false;

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            //Toast.makeText(context,"接收到开机启动广播",Toast.LENGTH_LONG).show();
            TPLog.printKeyStatus("BootBroadcastReceiver ->ACTION_BOOT_COMPLETED");
            isStartService = true;
        }

        if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED) || intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED))
        {
            TPLog.printKeyStatus("BootBroadcastReceiver ->ACTION_PACKAGE_ADDED||ACTION_PACKAGE_REPLACED");
            //如果更新了自己那么就自动启动下
            String packageName = intent.getDataString();
            if(packageName.contains(":")){
                packageName = packageName.split(":")[1];
            }
//            Toast.makeText(context,"接收到应用安装或者升级广播:"+packageName,Toast.LENGTH_LONG).show();
            if("com.kedacom.touchdata".equals(packageName)){
                isStartService = true;
            }
        }

      if(intent.getAction().equals(Intent.ACTION_MEDIA_MOUNTED) || intent.getAction().equals(Intent.ACTION_MEDIA_UNMOUNTED)){
//            Toast.makeText(context,"接收sdcard挂载广播",Toast.LENGTH_LONG).show();
//          TPLog.printKeyStatus("BootBroadcastReceiver ->ACTION_MEDIA_MOUNTED||ACTION_MEDIA_UNMOUNTED");  这个挂载太早了，很多东西都还没有准备好
//            isStartService = true;
        }

        if(isStartService){
            TPLog.printKeyStatus("启动BootService");
            if( !Utils.isServiceRunning(context,"com.kedacom.service.BootService")) {
                Intent service = new Intent(context, BootService.class);
                context.startService(service);
            }
        }
    }
}
