package com.garci.pokegarci.util

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.IncludeAppTopBarBinding

object AppTopBar {

    fun applyWindowInsets(insetHost: View, topBarRoot: View) {
        val extraTop = insetHost.resources.getDimensionPixelSize(R.dimen.pokedex_content_top_inset)
        ViewCompat.setOnApplyWindowInsetsListener(insetHost) { _, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            topBarRoot.setPadding(0, statusBarTop + extraTop, 0, 0)
            insets
        }
        ViewCompat.requestApplyInsets(insetHost)
    }
}

fun Fragment.setupAppTopBar(
    topBar: IncludeAppTopBarBinding,
    title: CharSequence,
    insetHost: View,
    showBackButton: Boolean = true,
    onBack: () -> Boolean = { false }
) {
    topBar.appTopBarTitle.text = title
    topBar.appTopBarBackButton.isVisible = showBackButton
    AppTopBar.applyWindowInsets(insetHost, topBar.appTopBar)

    if (showBackButton) {
        setupFeatureBackNavigation(
            backButton = topBar.appTopBarBackButton,
            applyStatusBarInsets = false,
            onBack = onBack
        )
    }
}