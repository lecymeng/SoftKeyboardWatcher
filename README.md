# SoftKeyboardWatcher

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/lecymeng/SoftKeyboardWatcher/blob/main/LICENSE)
![maven-central](https://img.shields.io/maven-central/v/com.weicools/keyboard-watcher.svg)

## Getting started

In your `build.gradle`:

```groovy
dependencies {
    implementation 'com.weicools:keyboard-watcher:1.0.0'
}
```

## Usage

1. set SoftInputMode to `adjustNothing`

```
Activity#onCreate window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

or

AndroidManifest.xml android:windowSoftInputMode="adjustNothing"

or

themes.xml <item name="android:windowSoftInputMode">adjustNothing</item>
```

2. setDecorFitsSystemWindows to false

```kotlin
WindowCompat.setDecorFitsSystemWindows(window, false)
```

3. watch keyboard

```kotlin
var animator: ValueAnimator? = null
SoftKeyboardWatcher(requireActivity(), viewLifecycleOwner) { imeVisible, imeHeight, navigationBarsHeight, animated ->
    // set views padding/margin/translationY

    if (animated) {
        // Android 11+, Window insets animation callback
        val translation = -(max(imeHeight - navigationBarsHeight, 0)).toFloat()
        buttonSend.translationY = translation
        textInputLayout.translationY = translation
    } else {
        val currentTranslationY = buttonSend.translationY
        val endTranslationY = if (imeVisible) imeHeight - navigationBarsHeight else 0
        animator?.cancel()
        animator = ValueAnimator.ofFloat(currentTranslationY, -endTranslationY.toFloat()).apply {
            duration = 120L
            addUpdateListener {
                val value = it.animatedValue as Float
                buttonSend.translationY = value
                textInputLayout.translationY = value
            }
            start()
        }
    }
}
```

## TODO

License
-------

    Copyright (c) 2021. Weiwei

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.