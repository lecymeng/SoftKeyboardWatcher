package com.weiwei.keyboard.watcher.sample

import android.animation.ValueAnimator
import android.util.Log

/**
 * @author weiwei
 * @date 2022.09.29
 */
class SoftInputAnimDelegate {
    private var animator: ValueAnimator? = null

    fun showSoftInput(currentValue: Float, targetValue: Float, duration: Long = 240L, animatorUpdateListener: ValueAnimator.AnimatorUpdateListener) {
        Log.d("Fuck", "startAnimation: currentValue=$currentValue")
        animator?.cancel()
        val anim = ValueAnimator.ofFloat(currentValue, targetValue)
        anim.duration = duration
        anim.addUpdateListener(animatorUpdateListener)
        anim.start()
        animator = anim
    }

    fun hideSoftInput(currentValue: Float, targetValue: Float = 0f, duration: Long = 160L, doOnUpdate: (animatedValue: Float) -> Unit) {
        Log.d("Fuck", "startAnimation: currentValue=$currentValue")
        animator?.cancel()
        val anim = ValueAnimator.ofFloat(currentValue, targetValue)
        anim.duration = duration
        anim.addUpdateListener {
            doOnUpdate(it.animatedValue as Float)
        }
        anim.start()
        animator = anim
    }
}