package com.garci.pokegarci.util

import androidx.annotation.ColorInt
import com.garci.pokegarci.domain.guess.PokemonGeneration

object RegionGenerationColors {

    @ColorInt
    fun colorsFor(generation: PokemonGeneration): IntArray {
        return when (generation) {
            PokemonGeneration.GEN_I -> intArrayOf(0xFFE0115A.toInt(), 0xFF1E50DC.toInt())
            PokemonGeneration.GEN_II -> intArrayOf(0xFFD4AF37.toInt(), 0xFFC0C0C0.toInt())
            PokemonGeneration.GEN_III -> intArrayOf(
                0xFFCC0000.toInt(),
                0xFF0F52BA.toInt(),
                0xFF50C878.toInt(),
            )
            PokemonGeneration.GEN_IV -> intArrayOf(0xFF98D8E8.toInt(), 0xFF4B0082.toInt())
            PokemonGeneration.GEN_V -> intArrayOf(0xFF000000.toInt(), 0xFFFFFFFF.toInt())
            PokemonGeneration.GEN_VI -> intArrayOf(0xFF5DADE2.toInt(), 0xFFFF69B4.toInt())
            PokemonGeneration.GEN_VII -> intArrayOf(0xFFFFD700.toInt(), 0xFF40E0D0.toInt())
            PokemonGeneration.GEN_VIII -> intArrayOf(0xFFC8102E.toInt(), 0xFF012169.toInt())
            PokemonGeneration.GEN_IX -> intArrayOf(0xFFFF6600.toInt(), 0xFF800080.toInt())
        }
    }
}
