package com.garci.pokegarci.util

import android.content.Context
import com.garci.pokegarci.R
import com.garci.pokegarci.domain.guess.PokemonGeneration

fun Context.getRegionLabel(generation: PokemonGeneration): String {
    val resId = when (generation) {
        PokemonGeneration.GEN_I -> R.string.pokedex_region_gen_i
        PokemonGeneration.GEN_II -> R.string.pokedex_region_gen_ii
        PokemonGeneration.GEN_III -> R.string.pokedex_region_gen_iii
        PokemonGeneration.GEN_IV -> R.string.pokedex_region_gen_iv
        PokemonGeneration.GEN_V -> R.string.pokedex_region_gen_v
        PokemonGeneration.GEN_VI -> R.string.pokedex_region_gen_vi
        PokemonGeneration.GEN_VII -> R.string.pokedex_region_gen_vii
        PokemonGeneration.GEN_VIII -> R.string.pokedex_region_gen_viii
        PokemonGeneration.GEN_IX -> R.string.pokedex_region_gen_ix
    }
    return getString(resId)
}
