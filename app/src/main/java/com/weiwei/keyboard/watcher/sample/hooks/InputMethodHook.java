package com.weiwei.keyboard.watcher.sample.hooks;

import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.lifecycle.LifecycleOwner;

import com.weiwei.hooks.inputmethod.InputMethodHolder;
import com.weiwei.hooks.inputmethod.InputMethodListener;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * @author weiwei
 * @date 2022.09.29
 *
 * 测试在 Android 11 以下调用 showSoftInput 时，立即开始动画，尝试解决键盘已经完全展示时动画才开始（效果并不太好）
 */
public class InputMethodHook {
    private static final String TAG = "InputMethodHook";

    private static final String METHOD_SHOW_INPUT = "showSoftInput";
    private static final String METHOD_HIDE_INPUT = "hideSoftInputFromWindow";

    public static void hookByDexposed() {
        DexposedBridge.findAndHookMethod(InputMethodManager.class, METHOD_SHOW_INPUT, View.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "show beforeHookedMethod: " + param.thisObject);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "show afterHookedMethod: " + param.thisObject);
            }
        });

        DexposedBridge.findAndHookMethod(InputMethodManager.class, METHOD_SHOW_INPUT, IBinder.class, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "hide beforeHookedMethod: " + param.thisObject);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d(TAG, "hide afterHookedMethod: " + param.thisObject);
            }
        });
    }

    public static void hookByReflect(LifecycleOwner viewLifecycleOwner) {
        InputMethodHolder.startObserve(viewLifecycleOwner, new InputMethodListener() {
            @Override
            public void onShow(boolean result) {
                Log.d(TAG, "onShow: " + result);
            }

            @Override
            public void onHide(boolean result) {
                Log.d(TAG, "onHide: " + result);
            }
        });
    }
}
