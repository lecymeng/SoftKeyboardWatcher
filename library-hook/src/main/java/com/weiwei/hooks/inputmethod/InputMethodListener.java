package com.weiwei.hooks.inputmethod;

/**
 * @author weiwei
 * @date 2022.09.29
 */
public interface InputMethodListener {

    void onShow(boolean result);

    /**
     * 主动调用 hideSoftInputFromWindow 会走到该方法
     * 通过系统按钮关闭软键盘的时候未调用，因为键盘在一个独立的进程中，hook 只能实现 hook 当前进程的一个 binder 代理
     */
    void onHide(boolean result);
}