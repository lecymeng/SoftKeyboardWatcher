package com.weiwei.hooks.inputmethod;

import android.content.Context;
import android.os.IBinder;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class InputMethodManagerHook extends BaseHook implements InvocationHandler {

    public interface MethodInvokeListener {
        void onMethod(Object obj, Method method, Object result);
    }

    private static final String TAG = "InputMethodManagerHook";

    private MethodInvokeListener methodInvokeListener;

    public InputMethodManagerHook(Context context) {
        super(context);
    }

    public void setMethodInvokeListener(MethodInvokeListener methodInvokeListener) {
        this.methodInvokeListener = methodInvokeListener;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        Object invoke = null;
        try {
            invoke = method.invoke(mOriginObj, args);
        } catch (Throwable e) {
            Log.w(TAG, "invoke failed!  " + Log.getStackTraceString(e));
        }
        if (methodInvokeListener != null) {
            methodInvokeListener.onMethod(mOriginObj, method, invoke);
        }
        return invoke;
    }

    @Override
    public void onHook(ClassLoader classLoader) throws Throwable {
        //其实有其他的 hook 点，比如 InputMethodManager#sInstance，初始化的时候可以将代理的 IInputMethodManager 传进构造函数
        //现在的这种方式是从获取 Binder 代理对象的唯一入口 ServiceManager 开始 hook，方便以后 hook 其他服务
        ServiceManagerHook serviceManagerHook = new ServiceManagerHook(mContext, Context.INPUT_METHOD_SERVICE);
        serviceManagerHook.onHook(classLoader);
        Object originBinder = serviceManagerHook.getOriginObj();
        if (originBinder instanceof IBinder) {
            mOriginObj = IInputMethodManagerCompat.asInterface((IBinder) originBinder);
            Object proxyInputMethodInterface = ReflectUtils.makeProxy(classLoader, mOriginObj.getClass(), this);
            serviceManagerHook.setProxyIInterface(proxyInputMethodInterface);
            //若 hook 之前调用过 mContext.getSystemService(Context.INPUT_METHOD_SERVICE)
            //则在 SystemServiceRegistry 中会有缓存，清理缓存后重建才会拿到我们 hook 的代理
            clearCachedService();
            //rebuild cache
            mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }

    private void clearCachedService() throws Throwable {
        Object sInstance = ReflectUtils.getStaticFiled(InputMethodManager.class, "sInstance");
        if (sInstance != null) {
            ReflectUtils.setStaticFiled(InputMethodManager.class, "sInstance", null);
            Object systemFetcher = SystemServiceRegistryCompat.getSystemFetcher(Context.INPUT_METHOD_SERVICE);
            if (systemFetcher != null) {
                ReflectUtils.setFiled(systemFetcher.getClass().getSuperclass(), "mCachedInstance", systemFetcher, null);
            }
        }
    }
}
