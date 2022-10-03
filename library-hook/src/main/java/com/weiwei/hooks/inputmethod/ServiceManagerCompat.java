package com.weiwei.hooks.inputmethod;

import android.annotation.SuppressLint;
import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class ServiceManagerCompat {

    private static Class<?> sClass = null;

    public static final String METHOD_QUERY_LOCAL_INTERFACE = "queryLocalInterface";

    @SuppressLint("PrivateApi")
    public static Class<?> forClass() throws ClassNotFoundException {
        if (sClass == null) {
            sClass = Class.forName("android.os.ServiceManager");
        }
        return sClass;
    }

    public static IBinder getService(String name) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method declaredMethod = forClass().getDeclaredMethod("getService", new Class<?>[]{ String.class });
        declaredMethod.setAccessible(true);
        return (IBinder) declaredMethod.invoke(null, new Object[]{ name });
    }

    public static Map<?, ?> sCache() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        Field declaredField = forClass().getDeclaredField("sCache");
        declaredField.setAccessible(true);
        Object sCache = declaredField.get(null);

        if (sCache instanceof Map) {
            return (Map) sCache;
        }
        return null;
    }

}
