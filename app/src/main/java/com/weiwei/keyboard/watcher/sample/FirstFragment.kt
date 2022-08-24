package com.weiwei.keyboard.watcher.sample

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.weiwei.keyboard.watcher.SoftKeyboardWatcher
import com.weiwei.keyboard.watcher.sample.databinding.FragmentFirstBinding
import kotlin.math.max

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 * @see [SoftKeyboard.md]
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val buttonSend = binding.buttonSend
        val editText = binding.editText
        val textInputLayout = binding.textInputLayout

        buttonSend.setOnClickListener {
            // EdgeInsetDelegate(requireActivity()).start()
        }

        val btnMarginRect = buttonSend.recordInitialMargin()
        buttonSend.doOnApplyWindowInsets { windowInsets ->
            val navigationBarBottom = windowInsets.navigationBarBottom
            buttonSend.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = navigationBarBottom + btnMarginRect.bottom }
        }

        var animator: ValueAnimator? = null
        SoftKeyboardWatcher(requireActivity(), viewLifecycleOwner) { imeVisible, imeHeight, navigationBarsHeight, animated ->
            // set views padding/margin/translationY

            val translation = max(imeHeight - navigationBarsHeight, 0).toFloat()
            if (animated) {
                // Android 11+, Window insets animation callback
                buttonSend.translationY = -translation
                textInputLayout.translationY = -translation
            } else {
                animator?.cancel()
                animator = ValueAnimator.ofFloat(buttonSend.translationY, -translation).apply {
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}