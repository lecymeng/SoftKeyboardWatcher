---
theme: seriph
layout: cover
background: https://images.unsplash.com/photo-1530819568329-97653eafbbfa?ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&ixlib=rb-1.2.1&auto=format&fit=crop&w=2092&q=80
themeConfig:
  primary: '#4d7534'
---

# Android 软键盘

---

## 前言

- 软键盘是 Android 进行用户交互的重要途径之一，Android 应用开发基本无法避免不使用它。
- 然而官方没有提供一套明确的 API 来获取软键盘相关信息：
  - 软键盘是否正在展示
  - 软键盘高度等

- 本次分享将从以下内容来分析软键盘
  - 软键盘开启与关闭
  - 软键盘示例分析
  - softInputMode 使用及原理
  - 如何获取可见区域
  - WindowInsets API
  - 软键盘适配最佳实践

---

## 软键盘弹出和关闭

<img align="right" class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-1.gif"/>

平时和软键盘交互最多的就是 `EditText`.

当点击 `EditText` 时键盘就会弹出，当点击返回按钮时键盘收起：

---

### 软键盘弹出分析

从 TextView (EditText extends TextView) 源码看看如何点击时弹出软键盘

```java {all|10|13}
@Override  
public boolean onTouchEvent(MotionEvent event) {  
  final int action = event.getActionMasked();  
  //...  
  if ((mMovement != null || onCheckIsTextEditor()) && isEnabled()  
      && mText instanceof Spannable && mLayout != null) {  
    //...  
    if (touchIsFinished && (isTextEditable() || textIsSelectable)) {  
      // Show the IME, except when selecting in read-only text.  
      final InputMethodManager imm = getInputMethodManager();  
      viewClicked(imm);
      if (isTextEditable() && mEditor.mShowSoftInputOnFocus && imm != null && !showAutofillDialog()) {
        imm.showSoftInput(this, 0);  
      }  
      //...  
    }  
    //...  
  }  
  //...  
}
```

---

### 弹出软键盘

可以看出弹出键盘只需要两个步骤：

> 1、获取 InputMethodManager 实例  
> 2、调用 showSoftInput(xx)

---

### 关闭软键盘

```java {all|4|6}
public void onEditorAction(int actionCode) {
	// ...
	if (actionCode == EditorInfo.IME_ACTION_DONE) {  
	    InputMethodManager imm = getInputMethodManager();  
	    if (imm != null && imm.isActive(this)) {  
	        imm.hideSoftInputFromWindow(getWindowToken(), 0);  
	    }
	    return;
	}
	// ...
}
```

可以得出关闭键盘也只需要两步：

> 1、获取 InputMethodManager 实例  
> 2、调用 hideSoftInputFromWindow()

---

### 注意 📢

1. `imm.showSoftInput(view, code)` 一般来说传入的是 EditText 类型。
   - 如果传入其它 `View`，需要设置 `Button.setFocusableInTouchMode(true)` 才能弹出键盘。
   - 比较完善的实现还需在 `onTouchEvent()` 里弹出键盘、将 `Button` 与键盘关联，实际上就是模仿 EditText 的实现。
2. `imm.showSoftInput(view, code)`, `imm.hideSoftInputFromWindow(windowToken, code)` 两个方法的最后一个参数用来匹配关闭键盘时判断当初弹出键盘时传入的类型，一般填0即可。
3. `imm.hideSoftInputFromWindow(windowToken, code)` 第一个参数传入的 IBinder windowToken 类型。
   - 每个 Activity 创建时候会生成 windowToken，该值存储在 AttachInfo 里。
   - 因此对于同一个 Activity 里的 ViewTree，每个 View 持有的 windowToken 都是指向相同对象。

---

## 软键盘示例分析

<img align="right" class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-1.gif"/>

- 当键盘弹起的时，当前能看到的是两个 Window: `Activity#Window` 和 `IME#Window`
- `IME#Window` 展示遮住 `Activity#Window` 的部分区域，为了使 `EditText` 能够被看到，`Activity` 布局会向上偏移

---

### Window 区域构成和变化情况

<style>
  img {
      margin-top: 10px;
  }
</style>

<img class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/H0CsIU.jpg">

---

### 软键盘弹出时 Window 状态

Window {d855900 InputMethod} 正好在 ImeTestActivity 之上

```shell {all|6,7}
$ adb shell dumpsys window|grep WindowStateAnimator
      Window #0: WindowStateAnimator{e30fe7 com.android.systemui.ImageWallpaper}
      Window #1: WindowStateAnimator{3305794 com.google.android.apps.nexuslauncher/com.google.android.apps.nexuslauncher.NexusLauncherActivity}
      Window #2: WindowStateAnimator{6bc923d com.google.android.apps.nexuslauncher/com.google.android.apps.nexuslauncher.NexusLauncherActivity}
      Window #3: WindowStateAnimator{b070a32 com.weiwei.keyboard.watcher.sample/com.weiwei.keyboard.watcher.sample.MainActivity}
      Window #4: WindowStateAnimator{bc6c383 com.weiwei.keyboard.watcher.sample/com.weiwei.keyboard.watcher.sample.test.ImeTestActivity}
      Window #5: WindowStateAnimator{d855900 InputMethod}
      Window #6: WindowStateAnimator{8e51539 ShellDropTarget}
      Window #7: WindowStateAnimator{917977e StatusBar}
      Window #8: WindowStateAnimator{8a814df NotificationShade}
      Window #9: WindowStateAnimator{967052c NavigationBar0}
      Window #10: WindowStateAnimator{78cbf5 pip-dismiss-overlay}
      Window #11: WindowStateAnimator{611ad8a EdgeBackGestureHandler0}
      Window #12: WindowStateAnimator{b231ffb SecondaryHomeHandle0}
      Window #13: WindowStateAnimator{8d54818 ScreenDecorOverlay}
      Window #14: WindowStateAnimator{9573271 ScreenDecorOverlayBottom}
```

---

### 是谁控制了 Window 向上偏移呢？

Window 中控制软键盘的参数就是: WindowManager.LayoutParams.softInputMode

> 试想以下问题如何解决：
> 
> 1、当键盘弹出时，底部 Button 恰好保持在键盘之上。
> 
> 2、当键盘弹出时，任何 View 都不需要顶上去。

---

## softInputMode 说明

softInputMode 顾名思义：**软键盘模式**，控制软键盘是否可见、关联的 EditText 是否跟随键盘移动等

重点关注以下属性：

```java
// WindowManager.java
public static final int SOFT_INPUT_ADJUST_UNSPECIFIED = 0x00;
public static final int SOFT_INPUT_ADJUST_RESIZE = 0x10;
public static final int SOFT_INPUT_ADJUST_PAN = 0x20;
public static final int SOFT_INPUT_ADJUST_NOTHING = 0x30;
```

从注释的文档上看：

- adjustUnspecified: 不指定调整方式，系统自行决定使用哪种调整方式
- adjustResize: 显示软键盘时调整窗口大小，使其内容不被输入法覆盖
- adjustPan: 显示软键盘时，窗口将回平移来保证输入焦点可见
- adjustNothing: 不做任何操作

---

## softInputMode 设置

softInputMode 默认是 `adjustUnspecified` 模式，其他模式设置方法：

```kotlin
方法1 Activity#onCreate  
window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

方法2 AndroidManifest.xml activity tag
android:windowSoftInputMode="adjustNothing"

方法3 Application/Activity theme  
<item name="android:windowSoftInputMode">adjustNothing</item>
```

---

## softInputMode 示例

以这三种布局约束排列结构为示例：分别点击两个 EditText 来测试弹出和收起软键盘

<img class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/byqhVE.jpg">

---

## adjustResize

| <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-1.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-2.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-3.gif" class="h-100" /> |
| ---- | ---- | ---- |

---

从三个示例 gif 可以看出：软键盘弹出/关闭时，布局高度会随着改变，布局中的控件会重新布局。

所以与底部没有约束关系的 View 在布局高度变化的时候，不会跟随移动。

- 那么思考一下 `adjustResize` 模式下的几个问题：
  - 如何改变布局高度？
  - 改变了哪个布局的高度？
  - 是否一定会改变布局高度？

---

### 原理分析

键盘本身是一个 Window，键盘弹出影响了 `ActivityWindow` 大小，从而导致 ViewTree 变化，最终导致 ViewGroup 高度改变

而 Window 和 ViewTree 的联系则是通过 `ViewRootImpl.java` 实现的

`ViewRootImpl` 接收 `WMS 事件` 的处理过程如下：

```java {all|7,10}
// ViewRootImpl.java
final class ViewRootHandler extends Handler {
  private void handleMessageImpl(Message msg) {
    switch (msg.what) {
      //接收窗口变化事件
      case MSG_RESIZED: {
        //👉handle MSG_RESIZED
      }
      case MSG_RESIZED_REPORT:
        //👉handle MSG_RESIZED_REPORT
        break;
        //...
    }
  }
}
```

---

### handle MSG_RESIZED

```java
//args记录了各个区域大大小
SomeArgs args = (SomeArgs) msg.obj;
//👉Note1
//arg1--->Window的尺寸
//arg2--->内容区域限定边界
//arg3--->可见区域的限定边界
//arg6--->固定区域的限定边界
if (mWinFrame.equals(args.arg1)
    && mPendingOverscanInsets.equals(args.arg5)
    && mPendingContentInsets.equals(args.arg2)
    && mPendingStableInsets.equals(args.arg6)
    && mPendingDisplayCutout.get().equals(args.arg9)
    && mPendingVisibleInsets.equals(args.arg3)
    && mPendingOutsets.equals(args.arg7)
    && mPendingBackDropFrame.equals(args.arg8)
    && args.arg4 == null
    && args.argi1 == 0
    && mDisplay.getDisplayId() == args.argi3) {
  //各个区域大小都没变化，则不作任何操作
  break;
}
```

---

当键盘弹出时：

```shell
- arg1--->Rect(0, 0 - 1080, 1920)
- arg2--->Rect(0, 63 - 0, 972)
- arg3--->Rect(0, 63 - 0, 972)
- arg6--->Rect(0, 63 - 0, 126)
```

当键盘收起后：

```shell
- arg1--->Rect(0, 0 - 1080, 1920)
- arg2--->Rect(0, 63 - 0, 126)
- arg3--->Rect(0, 63 - 0, 126)
- arg6--->Rect(0, 63 - 0, 126)
```

到此可以得出：

```shell
- arg1 表示的屏幕尺寸。
- arg6 表示的是状态栏和导航栏的高度。
- arg6 赋值给了 mPendingStableInsets ，从名字可以看出，这值是不变的。
```

> 无论键盘弹出还是关闭，arg1 和 arg6 都不会改变，变的是 arg2 和 arg3，
> 
> arg2 赋值给了 mPendingContentInsets，arg3 赋值给了 mPendingVisibleInsets。

---

### handle MSG_RESIZED_REPORT

```java
// ViewRootImpl.java
if (mAdded) {
  SomeArgs args = (SomeArgs) msg.obj;
  //...
  //和 MSG_RESIZED 类似，都需要判断是否发生改变
  final boolean framesChanged = !mWinFrame.equals(args.arg1)
    || !mPendingOverscanInsets.equals(args.arg5)
    || !mPendingContentInsets.equals(args.arg2)
    || !mPendingStableInsets.equals(args.arg6)
    || !mPendingDisplayCutout.get().equals(args.arg9)
    || !mPendingVisibleInsets.equals(args.arg3)
    || !mPendingOutsets.equals(args.arg7);

  //重新设置 Window 尺寸
  setFrame((Rect) args.arg1);
  //将值记录到各个成员变量里
  mPendingOverscanInsets.set((Rect) args.arg5);
  mPendingContentInsets.set((Rect) args.arg2);
  mPendingStableInsets.set((Rect) args.arg6);
  mPendingDisplayCutout.set((DisplayCutout) args.arg9);
  mPendingVisibleInsets.set((Rect) args.arg3);
  //...

  args.recycle();
```

---

```java
  if (msg.what == MSG_RESIZED_REPORT) {
    reportNextDraw();
  }

  if (mView != null && (framesChanged || configChanged)) {
    forceLayout(mView); //尺寸发生变化，强制走 layout+draw 过程
  }
  requestLayout(); //重新 layout
}
```

尺寸发生了变化后调用：

forceLayout(mView)--->ViewTree 里 View/ViewGroup 打上 layout, draw 标记。

requestLayout()---> 触发执行三大流程

mPendingStableInsets, mPendingContentInsets, mPendingVisibleInsets 记录了尺寸的变化，继续跟踪这些值怎么使用。

---

调用 requestLayout() 将会触发执行 performTraversals() 方法：

```java
private void performTraversals() {
  if (mFirst || windowShouldResize || insetsChanged || viewVisibilityChanged || params != null || mForceNextWindowRelayout) {
    //...
    boolean hwInitialized = false;
    //内容边界是否发生变化
    boolean contentInsetsChanged = false;
    try {
      //...
      //内容区域变化 👉Note1
      contentInsetsChanged = !mPendingContentInsets.equals(
        mAttachInfo.mContentInsets);

      if (contentInsetsChanged || mLastSystemUiVisibility !=
          mAttachInfo.mSystemUiVisibility || mApplyInsetsRequested
          || mLastOverscanRequested != mAttachInfo.mOverscanRequested
          || outsetsChanged) {
        //...
        //分发Inset 👉Note2
        dispatchApplyInsets(host);
        contentInsetsChanged = true;
      }
      //...
    } catch (RemoteException e) {
    }
    //...
  }
  //...
}
```

---

主要看两个点：

**Note1**: 内容区域发生变化

- 当设置 adjustResize，键盘弹起时内容区域发生变化，因此会执行 dispatchApplyInsets()
- 当设置 adjustPan，键盘弹起时内容部区域不变，因此不会执行 dispatchApplyInsets()

**Note2**: 分发Inset

这些记录的值会存储在 AttachInfo 对应的变量里。

---

Insets 分发过程如下：

<img class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/24zfGf.jpg">

dispatchApplyWindowInsets(insets）里的 insets 构成是通过计算之前记录在 mPendingXx 里的边界值。

---

fitSystemWindowsInt():

```java {all|4|7|all}
// View.java
private boolean fitSystemWindowsInt(Rect insets) {
  //对于 DecorView 的子布局 LinearLayout 来说，默认 fitsSystemWindows=true
  if ((mViewFlags & FITS_SYSTEM_WINDOWS) == FITS_SYSTEM_WINDOWS) {
    //...
    //设置 padding
    internalSetPadding(localInsets.left, localInsets.top, localInsets.right, localInsets.bottom);
    return res;
  }
  return false;
}
```

所以 DecorView 的子布局 LinearLayout 设置 padding，从而影响 LinearLayout 子布局的高度，最终会影响到 Activity 布局文件的高度。

---

## adjustPan

| <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-1.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-2.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-3.gif" class="h-100" /> |
| ---- | ---- | ---- |

---

软键盘弹出/关闭时，整个布局会上移，布局高度不会改变，布局中的控件不会重新布局。

- `adjustPan` 如何移动整个布局？
- 为什么当点击输入框1的时候，界面没有移动，当点击输入框2的时候，界面向上移动了？

---

### 原理分析

`adjustPan` 和 `adjustResize` 流程差不多，同样在 ViewRootImpl 里接收窗口变化的通知，区别在于：

当键盘弹起时：

```shell
- arg1--->Rect(0, 0 - 1080, 1920)
- arg2--->Rect(0, 63 - 0, 126)
- arg3--->Rect(0, 63 - 0, 972)
- arg6--->Rect(0, 63 - 0, 126)
```

可以看出 arg2 没有变化，也就是内容区域没有变，最终不会执行 `ViewRootImp#dispatchApplyInsets()` ，当然布局的高度就不会变。

---

先来分析为什么点击输入框2能往上移动？

布局移动无非就是坐标发生改变，或者内容滚动了，不管是何种形式最终都需要通过对 Canvas 进行位移才能实现移动的效果。

当窗口事件到来之后，发起View的三大绘制流程，并且将限定边界存储到AttachInfo的成员变量里，有如下关系：

- mPendingContentInsets-->View#mAttachInfo.mContentInsets;
- mPendingVisibleInsets-->View#mAttachInfo.mVisibleInsets;

---

依旧从三大流程开启的方法开始分析:

```java
// ViewRootImpl.java
private void performTraversals() {
  //...
  //在执行Draw过程之前执行
  boolean cancelDraw = mAttachInfo.mTreeObserver.dispatchOnPreDraw() || !isViewVisible;

  if (!cancelDraw) {
    //...
    //开启Draw过程
    performDraw();
  } else {
    //...
  }
}
```

performDraw() 最终执行了 scrollToRectOrFocus() 方法：

---

## scrollToRectOrFocus()

```java {all|9|13|15}
boolean scrollToRectOrFocus(Rect rectangle, boolean immediate) {
  final Rect ci = mAttachInfo.mContentInsets; //窗口内容区域
  final Rect vi = mAttachInfo.mVisibleInsets; //窗口可见区域
  int scrollY = 0; //滚动距离
  boolean handled = false;

  if (vi.left > ci.left || vi.top > ci.top || vi.right > ci.right || vi.bottom > ci.bottom) {
    scrollY = mScrollY;
    //找到当前有焦点的View 👉Note1
    final View focus = mView.findFocus();
    //...
    if (focus == lastScrolledFocus && !mScrollMayChange && rectangle == null) {
      //焦点没有发生切换，不做操作 👉Note2
    } else {
      //焦点发生切换，计算滑动距离 👉Note3
    }
  }

  //开始滚动 👉Note4
  return handled;
}
```

---

### 焦点发生切换，计算滑动距离

```java {all|7|8|10|13}
mScrollMayChange = false;

// Try to find the rectangle from the focus view.
if (focus.getGlobalVisibleRect(mVisRect, null)) {
  //...
  //找到当前焦点与可见区域的相交部分, mVisRect 为当前焦点在 Window 里的可见部分
  if (mTempRect.intersect(mVisRect)) {
    if (mTempRect.height() > (mView.getHeight() - vi.top - vi.bottom)) {
      //如果焦点根本不适合，那么就保持原样
    } else if (mTempRect.top < vi.top) {
      //如果当前焦点位置在窗口可见区域上边，说明焦点 View 应该往下移动到可见区域里边
      scrollY = mTempRect.top - vi.top;
    } else if (mTempRect.bottom > (mView.getHeight() - vi.bottom)) {
      //如果当前焦点位置在窗口可见区域之下，说明其应该往上移动到可见区域里边
      scrollY = mTempRect.bottom - (mView.getHeight() - vi.bottom);
    } else {
      //无需滚动
      scrollY = 0;
    }
    handled = true;
  }
}
```

---

## 开始滑动

```java {all|8|10|13|all}
if (scrollY != mScrollY) {
  //滚动距离发生变化
  if (!immediate) {
    if (mScroller == null) {
      mScroller = new Scroller(mView.getContext());
    }
    //开始设置滚动
    mScroller.startScroll(0, mScrollY, 0, scrollY-mScrollY);
  } else if (mScroller != null) {
    mScroller.abortAnimation();
  }
  //记录滚动值
  mScrollY = scrollY;
}
```

滚动实现是借助 `Scroller` 类完成的

所以当设置 `adjustPan` 时，如果发现键盘遮住了当前有焦点的 View，

那么会对 DecorView 的 Canvas 进行平移，直至有焦点的 View 显示到可见区域为止。

---

## adjustNothing

软键盘弹出/关闭时，整个布局不会发生任何变化（不放 gif 图了）

没有事件发出，自然不会产生任何效果了。

---

## adjustUnspecified

默认的效果与 adjustPan 一致:

| <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-1.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-2.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-3.gif" class="h-100" /> |
| ---- | ---- | ---- |

---

在 View 里增加 isScrollContainer 属性，重新运行后效果如下：

```xml
android:isScrollContainer="true"
```

| <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-1.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-2.gif" class="h-100" /> | <img src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-resize-3.gif" class="h-100" /> |
| ---- | ---- | ---- |

---

可以看到 `adjustUnspecified` 模式下产生的效果可能与 `adjustPan` 相同，也可能与 `adjustResize` 相同。

接下来就来分析 `选择的标准` 是什么？

---

从 `ViewRootImpl#performTraversals()` 方法开始分析：

```java {all|8,9|11,12,13,14|16,17,18,19|all}
private void performTraversals() {
  //...
  if (mFirst || mAttachInfo.mViewVisibilityChanged) {
    mAttachInfo.mViewVisibilityChanged = false;
    int resizeMode = mSoftInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
    //如果没有设置，那么默认为 0，即 adjustUnspecified
    if (resizeMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED) {
      //查看 mScrollContainers 数组中的元素 👉Note1
      final int N = mAttachInfo.mScrollContainers.size();
      for (int i=0; i<N; i++) {
        if (mAttachInfo.mScrollContainers.get(i).isShown()) {
          //如果有且 ScrollContainer=true 则设为 adjustResize 模式 👉Note2
          resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        }
      }
      if (resizeMode == 0) {
        //如果没有设置为resize模式，则设置 adjustPan 模式 👉Note3
        resizeMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN;
      }
      if ((lp.softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) != resizeMode) {
        lp.softInputMode = (lp.softInputMode & ~WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST) | resizeMode;
        //最后赋值给params，让Window属性生效 👉Note4
        params = lp;
      }
    }
  }
  //...
}
```

---

Note1: `mAttachInfo.mScrollContainers: ArrayList<View>`  什么时候添加元素进去的呢？

调用 View#setScrollContainer 方法时会把 View 添加到 `mAttachInfo.mScrollContainers`

```java {all|4|10|all}
public void setScrollContainer(boolean isScrollContainer) {
  if (isScrollContainer) {
    if (mAttachInfo != null && (mPrivateFlags&PFLAG_SCROLL_CONTAINER_ADDED) == 0) {
      mAttachInfo.mScrollContainers.add(this); // 👉Note1 add
      mPrivateFlags |= PFLAG_SCROLL_CONTAINER_ADDED;
    }
    mPrivateFlags |= PFLAG_SCROLL_CONTAINER;
  } else {
    if ((mPrivateFlags&PFLAG_SCROLL_CONTAINER_ADDED) != 0) {
      mAttachInfo.mScrollContainers.remove(this); // 👉Note2 remove
    }
    mPrivateFlags &= ~(PFLAG_SCROLL_CONTAINER|PFLAG_SCROLL_CONTAINER_ADDED);
  }
}
```

---

我们常用的 RecyclerView 就在构造函数里默认做了设置：

```java
public RecyclerView(Context context, AttributeSet attrs, int defStyle) {
    setScrollContainer(true);
    //...
}
```

所以容器可以滚动的话，那么它的高度可以伸缩的，既然可以伸缩，那么刚好符合 adjustResize 模式。

---

<img align="right" class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/ime-pan-1.gif"/>

回到最开始的问题：

> 1、当键盘弹出时，底部 Button 恰好保持在键盘之上。
> 
> 2、当键盘弹出时，任何 View 都不需要顶上去。

- 任何 View 都不需要顶上去
  - 设置 adjustNothing 即可。

- 如何 Button 恰好保持在键盘之上？
  - adjustPan 肯定不行，它只能保持 EditText (焦点) View 在软键盘之上
  - adjustResize 看起来可以，但是有时不生效，Button 必须与父控件底部有 `约束关系`，且无法控制 Button `和软键盘的距离`

---

## 软键盘适配最佳实践

通过前面的示例和问题可以看出:

> adjustPan 页面整体移动，体验不友好。
> 
> adjustResize 在某些情况下又会失效，且不能完全满足需求。
> 
> adjustNothing 在软键盘弹出时，页面是完全不偏不移的。

如果想做到完全可控，那么只有 adjustNothing 可选择。

我们只要能在这种模式下监听到软键盘 `弹出、收起事件` 和 `高度`，就能完全控制页面的 `内容偏移` 或者 `resize`

那么在 adjustNothing 模式下，我们如何接管软键盘逻辑？

---

## 使用完全不可见的 Window

1. 在 Activity 层上加入一层完全看不见的 Window，由这个 Window 来监听键盘变化，
2. 每一个 Window 都可以设置 softInputMode，因此它可以单独设置为 `adjustResize`，
3. 这样当这个 Window 本身被挤压时，就能判断和计算出键盘的状态，进而再通知到 Activity。

那么如何 计算软键盘高度，以及 判断软键盘是否可见呢？

- 方法1：WindowInsets API
- 方法2：View#getWindowVisibleDisplayFrame()

---

## WindowInsets API

Android 提供了 WindowInsets 相关内容，并且做了向下兼容：

- 新增接口 `setDecorFitsSystemWindows` 能够方便的配置 `DoctorView` 是否 fit
  - 设置为 false 可以允许布局展示到状态栏和导航栏，同时 `adjustResize` 将会失效
- 通过 `ViewCompat.getRootWindowInsets(view)` 来快捷获取 `root insets`
  - 通过 root insets 即可获取到 `systemBarInsets`, `imeInsets` 信息
  - 从而可以快速获取到 systemBar, ime 状态、高度等信息
- 通过 `WindowCompat.getInsetsController()` 可以方便的控制 systemBar, ime 显示隐藏

另外还有 `WindowInsetsAnimationCallback` (API 30+) 可以实现丝滑软键盘动画

---

imeInserts API 兼容性

<img align="right" class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/H4cOzP.jpg">

- adjustPan 模式下兼容所有版本
- adjustResize 模式下兼容 API 23+
- adjustNothing 没有说明，测试兼容 API 30+

所以 imeInserts 难以兼容所有版本获取 高度以及显示状态

---

## getWindowVisibleDisplayFrame()

从前面的分析可以知道：状态栏、导航栏、屏幕可见区域、内容区域的限定边界 都是存在 View#mAttachInfo:

> AttachInfo.mStableInsets 状态栏导航栏
> 
> AttachInfo.mContentInsets 内容区域限定边界
> 
> AttachInfo.mVisibleInsets 可见区域限定边界

这些字段的访问权限是 "default"，可以通过反射获取，但是在 Android 10 之后不允许反射了。

<div v-click>但是  Android 还提供了另外一种方式：getWindowVisibleDisplayFrame()</div>

---

```java
// View.java
public void getWindowVisibleDisplayFrame(Rect outRect) {
  if (mAttachInfo != null) {
    try {
      //获取Window尺寸，注意此处的尺寸是包含状态栏、导航栏，与getWindowManager().getDefaultDisplay().getRealSize()尺寸一致;
      mAttachInfo.mSession.getDisplayFrame(mAttachInfo.mWindow, outRect);
    } catch (RemoteException e) {
      return;
    }
    final Rect insets = mAttachInfo.mVisibleInsets;
    //将可见区域记录在 outRect(相对于屏幕)
    outRect.left += insets.left;
    outRect.top += insets.top;
    outRect.right -= insets.right;
    outRect.bottom -= insets.bottom;
    return;
  }
  //...
}
```

---

通过 已知的屏幕尺寸, outRect 以及 systemBarInsets 即可计算出软键盘高度：

<img class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/TZyzId.jpg">

---

实现流程分析

<img class="h-100" src="https://blog-1251678165.cos.ap-chengdu.myqcloud.com/uPic/1Brg1C.jpg">

完整实现可参考：[lecymeng/SoftKeyboardWatcher](https://github.com/lecymeng/SoftKeyboardWatcher)

---

适配遇到的问题

- 如果用户使用 FooView 等类似悬浮组件，在 Android 9.0 之上将监听不到键盘状态
  - 解决方法：提高 Window type 值

---

## Links

- [# Android 监听软键盘的高度并解决其覆盖输入框的问题](https://juejin.cn/post/7064175843840360462)
- [# 这交互炸了系列： 仿微信键盘弹出体验](https://juejin.im/post/5ef850c9f265da231019f6e4)
- [# Android 软键盘的那些坑，原理篇来了！](https://mp.weixin.qq.com/s/6VJALLA1bAFSujkZkdSNVQ)
- [# Android 软键盘的那些坑，一招搞定！](https://mp.weixin.qq.com/s?__biz=MzAxMTI4MTkwNQ==&mid=2650835965&idx=1&sn=94d8776faa38a0c0f7a90e7951c11303)
- [android/user-interface-samples/WindowInsetsAnimation](https://github.com/android/user-interface-samples/tree/main/WindowInsetsAnimation)

---
layout: statement
---

## 张正维

2022.1008
