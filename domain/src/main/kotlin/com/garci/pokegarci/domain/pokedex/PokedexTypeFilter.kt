package com.garci.pokegarci.domain.pokedex

import com.garci.pokegarci.domain.model.Pokemon

data class PokedexTypeFilter(
    val selectedTypes: Set<String> = emptySet(),
    val monotypeOnly: Boolean = false,
) {
    fun isActive(): Boolean {
        if (monotypeOnly) return true
        if (selectedTypes.isEmpty()) return false
        return !PokemonTypes.isFullSelection(selectedTypes)
    }

    fun matches(pokemon: Pokemon): Boolean {
        val type1 = pokemon.type1.lowercase()
        val type2 = pokemon.type2?.lowercase()

        if (monotypeOnly && selectedTypes.isEmpty()) {
            return type2 == null
        }

        if (monotypeOnly) {
            return type2 == null && type1 in selectedTypes
        }

        if (PokemonTypes.isFullSelection(selectedTypes)) {
            return true
        }

        if (selectedTypes.isEmpty()) return false

        if (selectedTypes.size == 1) {
            val selected = selectedTypes.first()
            return type1 == selected || type2 == selected
        }

        if (type2 == null) return false
        return type1 in selectedTypes && type2 in selectedTypes
    }
}
