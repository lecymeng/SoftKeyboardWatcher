package com.weiwei.keyboard.watcher.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.weiwei.keyboard.watcher.sample.databinding.ActivityMainBinding
import com.weiwei.keyboard.watcher.sample.test.ImeTestActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set windowSoftInputMode 👇
        // set activity manifest: android:windowSoftInputMode="adjustNothing"
        // set theme: <item name="android:windowSoftInputMode">adjustNothing</item>
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

        // adaptation status bar and navigation bar 👇
        EdgeInsetDelegate(this)
            .setNavigationBarColor(0x20000000)
            .start()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        binding.root.doOnApplyWindowInsets { windowInsets ->
            binding.statusBarView.updateLayoutParams { height = windowInsets.statusBarTop }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.ime_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        return when (item.itemId) {
            R.id.ime_test -> {
                startActivity(Intent(this, ImeTestActivity::class.java))
                true
            }
            R.id.dialog_test -> {
                val sendFragment = SendFragment()
                sendFragment.show(supportFragmentManager, "send")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}