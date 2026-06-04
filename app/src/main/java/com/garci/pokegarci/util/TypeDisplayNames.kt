package com.garci.pokegarci.util

import android.content.Context
import com.garci.pokegarci.R

val pokemonTypeKeys: List<String> = typeIconMap.keys.sorted()

fun Context.getTypeDisplayName(typeKey: String): String {
    val resId = typeNameResIds[typeKey.lowercase()] ?: return typeKey.replaceFirstChar { it.uppercase() }
    return getString(resId)
}

private val typeNameResIds = mapOf(
    "normal" to R.string.type_normal,
    "fire" to R.string.type_fire,
    "water" to R.string.type_water,
    "grass" to R.string.type_grass,
    "electric" to R.string.type_electric,
    "ice" to R.string.type_ice,
    "fighting" to R.string.type_fighting,
    "poison" to R.string.type_poison,
    "ground" to R.string.type_ground,
    "flying" to R.string.type_flying,
    "psychic" to R.string.type_psychic,
    "bug" to R.string.type_bug,
    "rock" to R.string.type_rock,
    "ghost" to R.string.type_ghost,
    "dragon" to R.string.type_dragon,
    "dark" to R.string.type_dark,
    "steel" to R.string.type_steel,
    "fairy" to R.string.type_fairy,
)
