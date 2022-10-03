package com.weiwei.hooks.inputmethod;

import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class ServiceManagerHook extends BaseHook implements InvocationHandler {

    private final String mServiceName;
    private Object mProxyIInterface;

    public ServiceManagerHook(Context context, String serviceName) {
        super(context);
        mServiceName = serviceName;
    }

    public void setProxyIInterface(Object proxyIInterface) {
        this.mProxyIInterface = proxyIInterface;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals(ServiceManagerCompat.METHOD_QUERY_LOCAL_INTERFACE)) {
            if (mProxyIInterface != null) {
                return mProxyIInterface;
            }
        }
        return method.invoke(mOriginObj, args);
    }

    @Override
    public void onHook(ClassLoader classLoader) throws Throwable {
        Map sCache = ServiceManagerCompat.sCache();
        if (sCache == null) {
            return;
        }
        Object cachedObj = sCache.get(mServiceName);
        sCache.remove(mServiceName);
        mOriginObj = ServiceManagerCompat.getService(mServiceName);
        if (mOriginObj == null) {
            if (cachedObj instanceof IBinder && !Proxy.isProxyClass(cachedObj.getClass())) {
                mOriginObj = cachedObj;
            }
        }
        if (mOriginObj instanceof IBinder) {
            Object proxyBinder = ReflectUtils.makeProxy(classLoader, mOriginObj.getClass(), this);
            sCache.put(mServiceName, proxyBinder);
        }
    }
}
