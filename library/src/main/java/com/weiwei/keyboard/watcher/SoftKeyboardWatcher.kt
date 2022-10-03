/*
 * Copyright (c) 2020 Weiwei
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *    https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.weiwei.keyboard.watcher

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.Window
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * @author weiwei
 * @date 2022.08.13
 *
 * Must be set the activity windowSoftInputMode to adjustNothing
 */
class SoftKeyboardWatcher(window: Window) {
    fun interface WatcherCallback {
        fun onChanged(imeHeight: Int, navigationBarsHeight: Int, animated: Boolean)
    }

    private var callback: WatcherCallback? = null

    private val decorView: View = window.decorView

    fun startWatch(activity: Activity, lifecycleOwner: LifecycleOwner, callback: WatcherCallback) {
        this.callback = callback

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            watchViaWindowInsetsAnimationCallback()
        } else {
            watchViaPopupWindow(activity, lifecycleOwner)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun watchViaWindowInsetsAnimationCallback() {
        ViewCompat.setWindowInsetsAnimationCallback(decorView, object : WindowInsetsAnimationCompat.Callback(DISPATCH_MODE_CONTINUE_ON_SUBTREE) {
            private var imeVisibleOnPrepare = false
            private var navigationBarsHeight = 0

            override fun onPrepare(animation: WindowInsetsAnimationCompat) {
                super.onPrepare(animation)

                val rootInsets = ViewCompat.getRootWindowInsets(decorView)
                if (rootInsets != null) {
                    imeVisibleOnPrepare = rootInsets.isVisible(WindowInsetsCompat.Type.ime())
                    navigationBarsHeight = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
                }
            }

            override fun onProgress(insets: WindowInsetsCompat, runningAnimations: MutableList<WindowInsetsAnimationCompat>): WindowInsetsCompat {
                // val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
                val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                callback?.onChanged(imeHeight, navigationBarsHeight, true)
                // Log.d("SoftKeyboardWatcher", "onProgress: imeHeight = $imeHeight")
                return insets
            }
        })
    }

    private fun watchViaPopupWindow(activity: Activity, lifecycleOwner: LifecycleOwner) {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val screenRealHeight: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            wm.currentWindowMetrics.bounds.height()
        } else {
            Point().also {
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getRealSize(it)
            }.y
        }

        var keyboardHeight = 0

        val popupRect = Rect()

        val popupView = FrameLayout(activity).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }

        val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            popupView.getWindowVisibleDisplayFrame(popupRect)

            var statusBarTop = 0
            var navigationBarBottom = 0
            val rootInsets = ViewCompat.getRootWindowInsets(decorView)
            if (rootInsets != null) {
                statusBarTop = rootInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top
                navigationBarBottom = rootInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            }

            val heightDiff = decorView.height - popupRect.height()
            // If the heightDiff is greater than 1/5, the soft keyboard is visible
            val imeVisible = heightDiff > screenRealHeight / 5
            // When full screen statusBarTop = 0, No need to consider full screen
            val imeHeight = if (imeVisible) heightDiff - statusBarTop else 0

            // Log.d("SoftKeyboardWatcher", "watchViaPopupWindow: imeHeight = $imeHeight")
            if (keyboardHeight != imeHeight) {
                keyboardHeight = imeHeight
                callback?.onChanged(imeHeight, navigationBarBottom, false)
            }
        }

        val popupWindow = PopupWindow(activity).apply {
            contentView = popupView
            // PopupWindow to be resized when the soft keyboard pops up
            @Suppress("DEPRECATION")
            softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

            width = 0 // Width set to 0 to avoid obscuring the ui
            height = WindowManager.LayoutParams.MATCH_PARENT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                /**
                 * windowLayoutType default = [WindowManager.LayoutParams.TYPE_APPLICATION_PANEL]
                 */
                windowLayoutType = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG
            }
            setBackgroundDrawable(ColorDrawable(0))
        }

        // todo 支持手动 dismiss
        lifecycleOwner.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (event == Lifecycle.Event.ON_CREATE) {
                    popupView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
                    decorView.post {
                        if (!popupWindow.isShowing && decorView.windowToken != null) {
                            popupWindow.showAtLocation(decorView, Gravity.NO_GRAVITY, 0, 0)
                        }
                    }
                } else if (event == Lifecycle.Event.ON_DESTROY) {
                    popupWindow.dismiss()
                    popupView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                    lifecycleOwner.lifecycle.removeObserver(this)
                }
            }
        })
    }
}