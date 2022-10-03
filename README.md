# SoftKeyboardWatcher

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/lecymeng/SoftKeyboardWatcher/blob/main/LICENSE)
![maven-central](https://img.shields.io/maven-central/v/com.weicools/keyboard-watcher.svg)

[![Android API](https://img.shields.io/badge/api-21%2B-brightgreen.svg?style=for-the-badge)](https://android-arsenal.com/api?level=21)
[![kotlin](https://img.shields.io/github/languages/top/adrielcafe/voyager.svg?style=for-the-badge&color=blueviolet)](https://kotlinlang.org/)

## Getting started

In your `build.gradle`:

```groovy
dependencies {
    implementation 'com.weicools:keyboard-watcher:1.0.1'
}
```

## Usage

1. set window SoftInputMode to `adjustNothing`

    ```shell
    Activity#onCreate
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

    AndroidManifest.xml activity tag
    android:windowSoftInputMode="adjustNothing"

    application/activity theme
    <item name="android:windowSoftInputMode">adjustNothing</item>
    ```

2. setDecorFitsSystemWindows to false

    ```kotlin
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ```

3. watch keyboard state

    ```kotlin
    val keyboardWatcher = SoftKeyboardWatcher(requireActivity().window)
    keyboardWatcher.startWatch(requireActivity(), viewLifecycleOwner) { imeHeight, navigationBarsHeight, animated ->
        val translation = -max(imeHeight - navigationBarsHeight, 0).toFloat()
        if (animated) {
            // Android 11+, Window insets animation callback
            // set views padding/margin/translationY
            buttonSend.translationY = translation
            textInputLayout.translationY = translation
        } else {
            // set views padding/margin/translationY with animation or without animation
            // do ...
        }
    }
    ```

## Samples

## License

```license
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
```
