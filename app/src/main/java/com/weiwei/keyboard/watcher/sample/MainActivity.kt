package com.weiwei.keyboard.watcher.sample

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.weiwei.keyboard.watcher.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set windowSoftInputMode 👇
        // set activity manifest: android:windowSoftInputMode="adjustNothing"
        // set theme: <item name="android:windowSoftInputMode">adjustNothing</item>
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        // adaptation status bar and navigation bar 👇
        EdgeInsetDelegate(this).start()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.doOnApplyWindowInsets { windowInsets ->
            binding.statusBarView.updateLayoutParams { height = windowInsets.statusBarTop }
        }
    }
}