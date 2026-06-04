package com.garci.pokegarci.util

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.garci.pokegarci.R

object AppFont {

    private var typeface: Typeface? = null

    fun get(context: Context): Typeface {
        typeface?.let { return it }
        return ResourcesCompat.getFont(context, R.font.pokemon_emerald)!!.also { typeface = it }
    }

    fun applyTo(root: View) {
        when (root) {
            is TextView -> root.typeface = get(root.context)
            is ViewGroup -> {
                for (i in 0 until root.childCount) {
                    applyTo(root.getChildAt(i))
                }
            }
        }
    }
}
