package com.weiwei.keyboard.watcher.sample

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import com.weiwei.keyboard.watcher.SoftKeyboardWatcher
import com.weiwei.keyboard.watcher.sample.databinding.FragmentSecondBinding
import kotlin.math.max

/**
 * @author weiwei
 * @date 2022.09.29
 */
class SendFragment : DialogFragment(R.layout.fragment_second) {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!

    // override fun getTheme(): Int = R.style.SoftKeyboardWatcherDialog

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSecondBinding.bind(view)

        val buttonSend = binding.buttonSend
        val editText = binding.editText
        val textInputLayout = binding.textInputLayout

        val keyboardWatcher = SoftKeyboardWatcher(dialog?.window!!)
        keyboardWatcher.startWatch(requireActivity(), viewLifecycleOwner) { imeHeight, navigationBarsHeight, animated ->
            // set views padding/margin/translationY
            val translation = -max(imeHeight - navigationBarsHeight, 0).toFloat()
            if (animated) {
                // Android 11+, Window insets animation callback
                buttonSend.translationY = translation
                textInputLayout.translationY = translation
            } else {
                // keyboardWatcher.startAnimation(buttonSend.translationY, translation) { animatedValue ->
                //     buttonSend.translationY = animatedValue
                //     textInputLayout.translationY = animatedValue
                // }
            }
        }
    }
}