package com.garci.pokegarci.util

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable

object TypeBackgroundProvider {

    private val typeColors = mapOf(
        "grass" to Color.GREEN,
        "water" to Color.BLUE,
        "electric" to Color.YELLOW,
        "psychic" to Color.MAGENTA,
        "ice" to Color.CYAN,
        "flying" to Color.WHITE,
        "fire" to Color.parseColor("#FF5100"),
        "ground" to Color.parseColor("#8B4513"),
        "normal" to Color.parseColor("#D0D0D0"),
        "fighting" to Color.parseColor("#8B0000"),
        "poison" to Color.parseColor("#800080"),
        "bug" to Color.parseColor("#A8B820"),
        "rock" to Color.parseColor("#B8A038"),
        "ghost" to Color.parseColor("#705898"),
        "dragon" to Color.parseColor("#7038F8"),
        "dark" to Color.parseColor("#705848"),
        "steel" to Color.parseColor("#B8B8D0"),
        "fairy" to Color.parseColor("#EE99AC"),
    )

    fun createBackground(type1: String, type2: String?): Drawable {
        val color1 = typeColors[type1] ?: Color.GRAY
        val color2 = type2?.let { typeColors[it] } ?: color1

        return GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(color1, color2),
        ).apply {
            shape = GradientDrawable.RECTANGLE
        }
    }
}
