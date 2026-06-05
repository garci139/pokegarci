package com.garci.pokegarci.util

import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import com.garci.pokegarci.R

object SearchViewUtils {

    fun applyDefaultStyle(searchView: SearchView) {
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        val inset = searchView.resources.getDimensionPixelSize(R.dimen.emerald_gba_chip_fill_inset)
        searchEditText.setPadding(inset, 0, inset, 0)
        searchEditText.typeface = AppFont.get(searchView.context)
        searchEditText.setTextColor(Color.BLACK)
        searchEditText.setHintTextColor(Color.GRAY)
        searchEditText.setBackgroundResource(android.R.color.transparent)

        val searchCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseButton.setColorFilter(Color.BLACK)

        applySearchEmeraldIcon(searchView)
    }

    private fun applySearchEmeraldIcon(searchView: SearchView) {
        val searchIcon = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_mag_icon) ?: return
        val resources = searchView.resources
        val iconWidth = resources.getDimensionPixelSize(R.dimen.search_emerald_icon_width)
        val iconHeight = resources.getDimensionPixelSize(R.dimen.search_emerald_icon_height)
        searchIcon.setImageResource(R.drawable.search_emerald_triangle_icon)
        searchIcon.scaleType = ImageView.ScaleType.FIT_CENTER
        searchIcon.adjustViewBounds = true
        searchIcon.setPadding(0, 0, 0, 0)
        searchIcon.layoutParams = searchIcon.layoutParams.apply {
            width = iconWidth
            height = iconHeight
        }
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
