package com.weiwei.hooks.inputmethod;

import android.annotation.SuppressLint;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class IInputMethodManagerCompat {

    // private static Class<?> sClass;
    //
    // @SuppressLint("PrivateApi")
    // public static Class<?> forClass() throws ClassNotFoundException {
    //     if (sClass == null) {
    //         sClass = Class.forName("com.android.internal.view.IInputMethodManager");
    //     }
    //     return sClass;
    // }

    public static Object asInterface(IBinder binder) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        @SuppressLint("PrivateApi")
        Class<?> clazz = Class.forName("com.android.internal.view.IInputMethodManager$Stub");

        Method declaredMethod = clazz.getDeclaredMethod("asInterface", new Class[]{ IBinder.class });
        declaredMethod.setAccessible(true);
        return declaredMethod.invoke(null, new Object[]{ binder });
    }
}