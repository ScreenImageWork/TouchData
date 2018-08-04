package com.kedacom.touchdata.net.mtnet.utils;

/**
 * Created by zhanglei on 2018/4/3.
 */

import com.kedacom.tplog.TPLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MethodExecutor {

    /** Target object to invoke. */
    private final Object mTarget;
    /** Method to execute. */
    private final Method mMethod;

    public MethodExecutor(Object target, Method method) {
        mTarget = target;
        mMethod = method;
        method.setAccessible(true);
    }

    public Object execute(Object[] args) {
        Object result = null;
        String resultMsg = String.format("Call method '%s' successfully!", mMethod.getName());
        Throwable throwable = null;
        try {
            result = mMethod.invoke(mTarget, args);
        } catch (Exception e) {
            TPLog.printError("执行 "+mMethod.getName()+"函数时出现异常:"+e);
            TPLog.printError(e);
        }
        return result;
    }

}