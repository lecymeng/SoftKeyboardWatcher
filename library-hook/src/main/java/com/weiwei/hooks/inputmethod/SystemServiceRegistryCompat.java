package com.weiwei.hooks.inputmethod;

import android.annotation.SuppressLint;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class SystemServiceRegistryCompat {

    private static Class<?> sClass = null;
    private static boolean foundClassSystemServiceRegistry = false;

    @SuppressLint("PrivateApi")
    public static Class<?> forClass() throws ClassNotFoundException {
        if (sClass == null) {
            try {
                sClass = Class.forName("android.app.SystemServiceRegistry");
                foundClassSystemServiceRegistry = true;
            } catch (Exception e) {
                sClass = Class.forName("android.app.ContextImpl");
                foundClassSystemServiceRegistry = false;
            }
        }
        return sClass;
    }

    public static Object getSystemFetcher(String serviceName) throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        String name = foundClassSystemServiceRegistry ? "SYSTEM_SERVICE_FETCHERS" : "SYSTEM_SERVICE_MAP";
        Field declaredField = forClass().getDeclaredField(name);
        declaredField.setAccessible(true);
        Object serviceFetchers = declaredField.get(null);

        if (serviceFetchers instanceof Map) {
            Map fetcherMap = (Map) serviceFetchers;
            return fetcherMap.get(serviceName);
        }
        return null;
    }
}