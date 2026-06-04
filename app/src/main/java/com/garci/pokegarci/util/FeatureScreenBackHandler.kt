package com.garci.pokegarci.util

import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.garci.pokegarci.R
import com.garci.pokegarci.utils.vibrate

class FeatureScreenBackHandler(
    private val fragment: Fragment,
    private val backButton: View,
    private val applyStatusBarInsets: Boolean = true,
    private val onBack: () -> Boolean
) {

    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            performBack()
        }
    }

    fun install() {
        backButton.contentDescription = fragment.getString(R.string.back_to_main_menu)
        if (applyStatusBarInsets)
            applyStatusBarInsetsToButton()
        backButton.setOnClickListener {
            fragment.requireContext().vibrate()
            fragment.requireContext().playClickEmeraldSound()
            performBack()
        }
        fragment.requireActivity().onBackPressedDispatcher.addCallback(
            fragment.viewLifecycleOwner,
            backPressedCallback
        )
    }

    private fun performBack() {
        if (onBack()) return
        fragment.findNavController().navigateUp()
    }

    private fun applyStatusBarInsetsToButton() {
        val baseMargin = fragment.resources.getDimensionPixelSize(R.dimen.feature_back_margin)
        ViewCompat.setOnApplyWindowInsetsListener(backButton) { view, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = statusBarTop + baseMargin
                marginStart = baseMargin
            }
            insets
        }
        ViewCompat.requestApplyInsets(backButton)
    }
}

fun Fragment.setupFeatureBackNavigation(
    backButton: View,
    applyStatusBarInsets: Boolean = true,
    onBack: () -> Boolean = { false }
) {
    FeatureScreenBackHandler(
        fragment = this,
        backButton = backButton,
        applyStatusBarInsets = applyStatusBarInsets,
        onBack = onBack
    ).install()
}