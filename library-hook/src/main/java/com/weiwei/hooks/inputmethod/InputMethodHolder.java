package com.weiwei.hooks.inputmethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * @author weiwei
 * @date 2022.09.29
 */
public class InputMethodHolder {

    private static final String TAG = "InputMethodHolder";

    @SuppressLint("StaticFieldLeak")
    private static InputMethodManagerHook inputMethodManagerHook;

    private static InputMethodListenerManager listenerManager;

    public static void startObserve(LifecycleOwner lifecycleOwner, InputMethodListener listener) {
        registerListener(listener);
        lifecycleOwner.getLifecycle().addObserver((LifecycleEventObserver) (source, event) -> {
            if (event == Lifecycle.Event.ON_DESTROY) {
                unregisterListener(listener);
            }
        });
    }

    public static void registerListener(InputMethodListener listener) {
        if (listenerManager != null) {
            listenerManager.registerListener(listener);
        }
    }

    public static void unregisterListener(InputMethodListener listener) {
        if (listenerManager != null) {
            listenerManager.unregisterListener(listener);
        }
    }

    /**
     * @param context Application context
     */
    public static void init(final Context context) {
        if (inputMethodManagerHook != null) {
            return;
        }
        try {
            listenerManager = new InputMethodListenerManager();
            inputMethodManagerHook = new InputMethodManagerHook(context);
            inputMethodManagerHook.onHook(context.getClassLoader());
            inputMethodManagerHook.setMethodInvokeListener((obj, method, result) -> {
                if (listenerManager != null) {
                    listenerManager.onMethod(method, result);
                }
            });
        } catch (Throwable throwable) {
            Log.w(TAG, "hook failed! detail:" + Log.getStackTraceString(throwable));
        }
    }

    public static void release() {
        listenerManager.clear();
        inputMethodManagerHook.setMethodInvokeListener(null);
        inputMethodManagerHook = null;
        listenerManager = null;
    }
}