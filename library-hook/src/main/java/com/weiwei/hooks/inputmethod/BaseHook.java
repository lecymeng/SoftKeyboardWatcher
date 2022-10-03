package com.weiwei.hooks.inputmethod;

import android.content.Context;

/**
 * @author weiwei
 * @date 2022.09.29
 */
abstract class BaseHook {

    Context mContext;
    Object mOriginObj;

    BaseHook(Context context) {
        mContext = context;
    }

    Object getOriginObj() {
        return mOriginObj;
    }

    public abstract void onHook(ClassLoader classLoader) throws Throwable;
}
