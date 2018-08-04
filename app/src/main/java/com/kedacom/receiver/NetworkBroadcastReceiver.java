package com.kedacom.receiver;

import android.content.Context;
import android.content.Intent;

import com.kedacom.app.TouchDataApp;
import com.kedacom.tplog.TPLog;
import com.kedacom.utils.NetworkUtil;
import com.kedacom.utils.VersionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2017/1/9.
 */
public class NetworkBroadcastReceiver extends BootBroadcastReceiver{

    private static List<OnNetworkChangeListener> callBacks = new ArrayList<OnNetworkChangeListener>();

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean boo = NetworkUtil.getNetworkState(context);
        TPLog.printKeyStatus("当前网络状态发生改变，当前网络："+(boo?"可用":"不可用"));
        if(callBacks.isEmpty())return;
        if(VersionUtils.is55InchDevice()){
            return;
        }
        for(OnNetworkChangeListener callback:callBacks){
            if(boo){
                callback.onNetworkUsable();
            }else{
                callback.onNetworkUnusable();
            }
        }
    }

    public static void registerOnNetworkChangeListener(OnNetworkChangeListener listener){
        callBacks.add(listener);
    }

    public static void unRegisterOnNetworkChangeListener(OnNetworkChangeListener listener){
        callBacks.remove(listener);
    }

    public interface OnNetworkChangeListener{
        void onNetworkUsable();
        void onNetworkUnusable();
    }
}
