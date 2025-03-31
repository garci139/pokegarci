package com.garci.pokegarci

import android.graphics.drawable.Drawable

// Datos que recojo de cada Pokemon
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val backgroundDrawable: Drawable,
    val type1: String,
    val type2: String?
)
