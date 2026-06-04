package com.garci.pokegarci.util

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView

object SearchViewUtils {

    fun applyDefaultStyle(searchView: SearchView) {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.typeface = AppFont.get(searchView.context)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)

        val searchCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseButton.setColorFilter(Color.BLACK)
    }

    fun hideCursorOnFocus(searchView: SearchView) {
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text).isCursorVisible = false
            }
        }
    }
}

fun View.startGradientBackgroundAnimation(
    enterFadeDuration: Int = 1500,
    exitFadeDuration: Int = 3000,
) {
    val gradientAnimation = background as? AnimationDrawable ?: return
    gradientAnimation.setEnterFadeDuration(enterFadeDuration)
    gradientAnimation.setExitFadeDuration(exitFadeDuration)
    gradientAnimation.start()
}
