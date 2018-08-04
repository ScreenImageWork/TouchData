package com.kedacom.httpserver.service;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.kedacom.httpserver.http.RequestListenerThread;
import com.kedacom.httpserver.utils.Constant;
import com.kedacom.tplog.TPLog;


public class WebService extends Service
{

    private RequestListenerThread thread;

    private static WebService mWebServicel;

    public WebService()
    {
    }

    //注意这里不是单态  只是为了方便得到该类对象
    public static WebService getInstance(){
        return mWebServicel;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mWebServicel = this;
        TPLog.printKeyStatus("WebService start...");
        thread = new RequestListenerThread(Constant.Config.PORT, Constant.Config.Web_Root);
        thread.setDaemon(false);
        thread.start();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        TPLog.printKeyStatus("WebService destroy");
        new Thread()
        {
            public void run()
            {
                if (thread != null)
                    thread.destroy();
            }
        }.start();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
