package com.weiwei.hooks.inputmethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author weiwei
 * @date 2022.09.29
 */
final class InputMethodListenerManager {

    private final List<InputMethodListener> callbackList = new ArrayList<>();

    public void registerListener(InputMethodListener callback) {
        if (callback == null) {
            return;
        }
        synchronized (callbackList) {
            if (!callbackList.contains(callback)) {
                callbackList.add(callback);
            }
        }
    }

    public void unregisterListener(InputMethodListener callback) {
        if (callback == null) {
            return;
        }
        synchronized (callbackList) {
            Iterator<InputMethodListener> iterator = callbackList.iterator();
            while (iterator.hasNext()) {
                InputMethodListener next = iterator.next();
                if (next == callback) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    private static final String METHOD_SHOW_INPUT = "showSoftInput";
    private static final String METHOD_HIDE_INPUT = "hideSoftInput";

    public void onMethod(Method method, Object result) {
        String name = method.getName();
        if (METHOD_SHOW_INPUT.equals(name)) {
            synchronized (callbackList) {
                for (InputMethodListener callback : callbackList) {
                    callback.onShow((Boolean) result);
                }
            }
        } else if (METHOD_HIDE_INPUT.equals(name)) {
            synchronized (callbackList) {
                for (InputMethodListener callback : callbackList) {
                    callback.onHide((Boolean) result);
                }
            }
        }
    }

    public void clear() {
        synchronized (callbackList) {
            callbackList.clear();
        }
    }
}
