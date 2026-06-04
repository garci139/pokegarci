package com.garci.pokegarci.domain.guess

import com.garci.pokegarci.domain.model.Pokemon

enum class PokemonGeneration(
    val romanNumeral: String,
    val idRange: IntRange,
) {
    GEN_I("I", 1..151),
    GEN_II("II", 152..251),
    GEN_III("III", 252..386),
    GEN_IV("IV", 387..493),
    GEN_V("V", 494..649),
    GEN_VI("VI", 650..721),
    GEN_VII("VII", 722..809),
    GEN_VIII("VIII", 810..905),
    GEN_IX("IX", 906..1025),
    ;

    fun contains(id: Int): Boolean = id in idRange

    companion object {
        val ALL: Set<PokemonGeneration> = entries.toSet()

        fun filterPokemon(
            pokemon: List<Pokemon>,
            selectedGenerations: Set<PokemonGeneration>,
        ): List<Pokemon> {
            if (selectedGenerations.isEmpty()) return emptyList()
            return pokemon.filter { entry ->
                selectedGenerations.any { generation -> generation.contains(entry.id) }
            }
        }
    }
}
