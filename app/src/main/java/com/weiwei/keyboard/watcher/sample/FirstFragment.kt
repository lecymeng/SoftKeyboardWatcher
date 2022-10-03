package com.weiwei.keyboard.watcher.sample

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import com.weiwei.keyboard.watcher.SoftKeyboardWatcher
import com.weiwei.keyboard.watcher.sample.databinding.FragmentFirstBinding
import kotlin.math.max

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 * see SoftKeyboard.md
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

        binding.shouButton.setOnClickListener {
            Log.d("SoftKeyboardWatcher", "onClick: show")
            editText.isFocusable = true
            editText.isFocusableInTouchMode = true
            editText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT) // 0
        }

        binding.hideButton.setOnClickListener {
            Log.d("SoftKeyboardWatcher", "onClick: hide")
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }

        val softInputAnimDelegate = SoftInputAnimDelegate()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}