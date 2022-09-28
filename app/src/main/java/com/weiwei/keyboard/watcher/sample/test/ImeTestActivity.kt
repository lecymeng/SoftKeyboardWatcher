package com.weiwei.keyboard.watcher.sample.test

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.weiwei.keyboard.watcher.sample.EdgeInsetDelegate
import com.weiwei.keyboard.watcher.sample.R
import com.weiwei.keyboard.watcher.sample.databinding.ActivityImeTestBinding
import com.weiwei.keyboard.watcher.sample.doOnApplyWindowInsets
import com.weiwei.keyboard.watcher.sample.statusBarTop

/**
 * @author weiwei
 * @date 2022.08.25
 */
class ImeTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        // window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val binding: ActivityImeTestBinding = ActivityImeTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // EdgeInsetDelegate(this).start()
        //
        // binding.statusBarView.isVisible = true
        // binding.root.doOnApplyWindowInsets { windowInsets ->
        //     binding.statusBarView.updateLayoutParams { height = windowInsets.statusBarTop }
        // }

        binding.toolbar.setOnClickListener {
            EdgeInsetDelegate(this).start()

            binding.statusBarView.isVisible = true
            binding.root.doOnApplyWindowInsets { windowInsets ->
                binding.statusBarView.updateLayoutParams { height = windowInsets.statusBarTop }
            }
        }

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("tab1"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("tab2"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("tab3"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.text) {
                    "tab1" -> {
                        navigateFragment(ImeTest1Fragment())
                    }
                    "tab2" -> {
                        navigateFragment(ImeTest2Fragment())
                    }
                    "tab3" -> {
                        navigateFragment(ImeTest3Fragment())
                    }
                }
            }
        })
        navigateFragment(ImeTest1Fragment())
    }

    private fun navigateFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}