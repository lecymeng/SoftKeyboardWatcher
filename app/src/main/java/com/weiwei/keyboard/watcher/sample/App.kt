package com.weiwei.keyboard.watcher.sample

import android.app.Application
import android.content.Context

/**
 * @author weiwei
 * @date 2022.09.29
 */
class App : Application() {
    override fun attachBaseContext(base: Context?) {
        // InputMethodHolder.init(base)
        super.attachBaseContext(base)
    }

    override fun onCreate() {
        super.onCreate()
    }
}