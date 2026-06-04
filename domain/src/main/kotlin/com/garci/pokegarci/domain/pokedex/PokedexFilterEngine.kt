package com.garci.pokegarci.domain.pokedex

import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.model.Pokemon

object PokedexFilterEngine {

    fun filter(
        pokemon: List<Pokemon>,
        searchQuery: String,
        regionFilter: Set<PokemonGeneration>?,
        typeFilter: PokedexTypeFilter?,
        statFilter: PokedexStatFilter?,
    ): List<Pokemon> {
        var result = pokemon

        if (searchQuery.isNotBlank()) {
            val normalizedQuery = searchQuery.trim().lowercase()
            result = result.filter {
                it.name.lowercase().startsWith(normalizedQuery) ||
                    it.id.toString().contains(normalizedQuery)
            }
        }

        if (regionFilter != null && regionFilter.isNotEmpty() && regionFilter != PokemonGeneration.ALL) {
            result = PokemonGeneration.filterPokemon(result, regionFilter)
        }

        if (typeFilter != null && typeFilter.isActive()) {
            result = result.filter { typeFilter.matches(it) }
        }

        if (statFilter != null && statFilter.isActive()) {
            result = result.filter { statFilter.matches(it) }
        }

        return result
    }
}
