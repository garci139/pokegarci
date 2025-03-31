package com.garci.pokegarci

import android.graphics.drawable.Drawable

// Datos que recojo de cada Pokemon
data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val backgroundDrawable: Drawable,
    val type1: String,
    val type2: String?,
    var description: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
    val height: Int,
    val weight: Int,
    val firstAbility: Ability
)

data class Ability (
    val originalName: String,
    var languageName: String
)


