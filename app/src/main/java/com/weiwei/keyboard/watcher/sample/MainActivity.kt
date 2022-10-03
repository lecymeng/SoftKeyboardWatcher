package com.weiwei.keyboard.watcher.sample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import com.weiwei.keyboard.watcher.SoftKeyboardWatcher
import com.weiwei.keyboard.watcher.sample.databinding.ActivityMainBinding
import com.weiwei.keyboard.watcher.sample.test.ImeTestActivity
import kotlin.math.max

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
                // val sendFragment = SendFragment()
                // sendFragment.show(supportFragmentManager, "send")

                val alertDialog = AlertDialog.Builder(this)
                    .setView(R.layout.fragment_second)
                    .create()
                alertDialog.show()
                alertDialog.setOnShowListener {
                    val buttonSend: View = alertDialog.findViewById(R.id.buttonSend)!!
                    val textInputLayout: View = alertDialog.findViewById(R.id.textInputLayout)!!
                    alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)

                    // fixme dialog test
                    val softInputAnimDelegate = SoftInputAnimDelegate()
                    val keyboardWatcher = SoftKeyboardWatcher(alertDialog.window!!)
                    keyboardWatcher.startWatch(this, this) { imeHeight, navigationBarsHeight, animated ->
                        val translation = -max(imeHeight - navigationBarsHeight, 0).toFloat()
                        if (animated) {
                            // Android 11+, Window insets animation callback
                            // set views padding/margin/translationY
                            buttonSend.translationY = translation
                            textInputLayout.translationY = translation
                        } else {
                            // set views padding/margin/translationY with animation or without animation

                            if (imeHeight > 0) {
                                buttonSend.alpha = 0f
                                textInputLayout.alpha = 0f
                                buttonSend.translationY = translation
                                textInputLayout.translationY = translation
                                val areaHeight = textInputLayout.top - buttonSend.bottom
                                softInputAnimDelegate.showSoftInput(translation - areaHeight, translation) { anim ->
                                    buttonSend.alpha = anim.animatedFraction
                                    textInputLayout.alpha = anim.animatedFraction
                                    // val animatedValue = anim.animatedValue as Float
                                    // buttonSend.translationY = animatedValue
                                    // textInputLayout.translationY = animatedValue
                                }
                            } else {
                                softInputAnimDelegate.hideSoftInput(buttonSend.translationY) { animatedValue ->
                                    buttonSend.translationY = animatedValue
                                    textInputLayout.translationY = animatedValue
                                }
                            }
                        }
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}