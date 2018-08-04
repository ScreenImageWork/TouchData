package com.kedacom.touchdata.net.mtnet;

import com.kedacom.kdv.mt.mtapi.IMtcCallback;
import com.kedacom.touchdata.net.mtnet.msg.DispathMtNetMsg;
import com.kedacom.touchdata.net.mtnet.utils.MethodExecutor;
import com.kedacom.tplog.TPLog;

import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhanglei on 2018/3/19.
 * 终端消息读取并分发
 */

public class MtNetReader implements IMtcCallback {

    //终端回调消息注册集合
    private Map<String,MethodExecutor> registerMtMsg = new HashMap<String,MethodExecutor>();

    private ExecutorService executorService;//线程池，用于消息处理

    MtNetReader(){
        TPLog.printError("->MtNetReader init...");
        initRegisterMtMsg();
        executorService = Executors.newSingleThreadExecutor();
    }

    private void initRegisterMtMsg(){
        TPLog.printError("->Register Mt Msg...");
        DispathMtNetMsg target = new DispathMtNetMsg();
        Class<?>[] interfaces = target.getClass().getInterfaces();
        if(interfaces.length==1){
            Class<?> clazz = interfaces[0];
            String clsName = clazz.getSimpleName();
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods){
                TPLog.printKeyStatus(clsName+"-"+method.getName());
                registerMtMsg.put(method.getName(),new MethodExecutor(target,method));
            }
        }
    }

    //为了不影响终端回调函数，这里采用线程池异步处理终端发送过来的消息
    private void dispatchMtMsg(String s){
        if(executorService==null||executorService.isShutdown())
            return;
        executorService.execute(new MtNetMsgTask(s));
    }

    @Override
    public void Callback(String s) {
        dispatchMtMsg(s);
    }


    public void destroy(){
        executorService.shutdown();
        executorService = null;
        registerMtMsg.clear();
        registerMtMsg = null;
    }


    class MtNetMsgTask implements Runnable{

        private String msg = "";

        public MtNetMsgTask(String s){
            msg = s;
        }

        @Override
        public void run() {
            TPLog.printError(msg);
            try {
                JSONObject rstJson = new JSONObject(msg);
                JSONObject head = rstJson.getJSONObject("head");
                JSONObject body = rstJson.getJSONObject("body");
                String eventName = head.getString("eventname");
//                if(!"ConfInfoNtf".equals(eventName)){
//                    TPLog.printError(msg);
//                }
                MethodExecutor executor = registerMtMsg.get(eventName);
                if(executor!=null){
                    executor.execute(new Object[]{body});
                }
            }catch(Exception e){
                TPLog.printError("分发终端消息时出现异常："+e);
                TPLog.printError(e);
            }
        }
    }
}
