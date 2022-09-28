package com.weiwei.keyboard.watcher.sample.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.weiwei.keyboard.watcher.sample.databinding.FragmentImeTest2Binding
import com.weiwei.keyboard.watcher.sample.doOnApplyWindowInsets
import com.weiwei.keyboard.watcher.sample.navigationBarBottom
import com.weiwei.keyboard.watcher.sample.recordInitialMargin

/**
 * @author weiwei
 * @date 2022.08.26
 */
class ImeTest2Fragment : Fragment() {
    private lateinit var binding: FragmentImeTest2Binding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentImeTest2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonSendMargin = binding.buttonSend.recordInitialMargin()
        view.doOnApplyWindowInsets { windowInsets ->
            binding.buttonSend.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = buttonSendMargin.bottom + windowInsets.navigationBarBottom
            }
        }
    }
}