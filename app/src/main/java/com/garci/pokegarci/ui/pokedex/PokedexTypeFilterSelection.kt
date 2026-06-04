package com.garci.pokegarci.ui.pokedex

import com.garci.pokegarci.domain.pokedex.PokemonTypes
import com.garci.pokegarci.domain.pokedex.PokedexTypeFilter

class PokedexTypeFilterSelection(
    selectedTypes: Set<String> = PokemonTypes.ALL,
    var monotypeSelected: Boolean = false,
) {
    val selectedTypes: MutableSet<String> = selectedTypes
        .intersect(PokemonTypes.ALL)
        .toMutableSet()

    var allTypesToggleSelected: Boolean = PokemonTypes.isFullSelection(selectedTypes)
        private set

    init {
        syncAllTypesToggleFromSelection()
    }

    fun isApplyEnabled(): Boolean {
        return monotypeSelected || selectedTypes.isNotEmpty()
    }

    fun toFilter(): PokedexTypeFilter {
        return PokedexTypeFilter(
            selectedTypes = selectedTypes.toSet(),
            monotypeOnly = monotypeSelected,
        )
    }

    fun onAllTypesToggleClicked() {
        monotypeSelected = false
        if (allTypesToggleSelected) {
            allTypesToggleSelected = false
            selectedTypes.clear()
        } else {
            allTypesToggleSelected = true
            selectedTypes.clear()
            selectedTypes.addAll(PokemonTypes.ALL)
        }
    }

    fun onTypeToggleClicked(typeKey: String) {
        if (typeKey in selectedTypes) {
            selectedTypes.remove(typeKey)
        } else {
            selectedTypes.add(typeKey)
        }
        syncAllTypesToggleFromSelection()
    }

    fun syncAllTypesToggleFromSelection() {
        allTypesToggleSelected = PokemonTypes.isFullSelection(selectedTypes)
    }

    companion object {
        fun fromActiveFilter(filter: PokedexTypeFilter?): PokedexTypeFilterSelection {
            if (filter == null) {
                return PokedexTypeFilterSelection(
                    selectedTypes = PokemonTypes.ALL,
                    monotypeSelected = false,
                )
            }
            return PokedexTypeFilterSelection(
                selectedTypes = filter.selectedTypes,
                monotypeSelected = filter.monotypeOnly,
            )
        }
    }
}
