package com.garci.pokegarci.presentation.pokedex

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.pokedex.PokedexFilterEngine
import com.garci.pokegarci.domain.pokedex.PokedexStatFilter
import com.garci.pokegarci.domain.pokedex.PokedexTypeFilter
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.GetPokemonListUseCase
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.presentation.common.PokemonDataViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PokedexViewModel @Inject constructor(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    repository: PokemonRepository,
    loadPokemonUseCase: LoadPokemonUseCase,
    @ApplicationContext context: Context,
) : PokemonDataViewModel(repository, loadPokemonUseCase, context) {

    private val allPokemon = MutableStateFlow<List<Pokemon>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val regionFilter = MutableStateFlow<Set<PokemonGeneration>?>(null)
    private val typeFilter = MutableStateFlow<PokedexTypeFilter?>(null)
    private val statFilter = MutableStateFlow<PokedexStatFilter?>(null)

    val activeRegionFilter: StateFlow<Set<PokemonGeneration>?> = regionFilter.asStateFlow()
    val activeTypeFilter: StateFlow<PokedexTypeFilter?> = typeFilter.asStateFlow()
    val activeStatFilter: StateFlow<PokedexStatFilter?> = statFilter.asStateFlow()

    val filteredPokemon: StateFlow<List<Pokemon>> = combine(
        allPokemon,
        searchQuery,
        regionFilter,
        typeFilter,
        statFilter,
    ) { pokemon, query, regions, types, stats ->
        PokedexFilterEngine.filter(
            pokemon = pokemon,
            searchQuery = query,
            regionFilter = regions,
            typeFilter = types,
            statFilter = stats,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun refreshPokemonList() {
        allPokemon.value = getPokemonListUseCase()
    }

    fun updateSearchQuery(query: String) {
        searchQuery.update { query }
    }

    fun applyRegionFilter(selected: Set<PokemonGeneration>) {
        regionFilter.value = selected.ifEmpty { null }
    }

    fun clearRegionFilter() {
        regionFilter.value = null
    }

    fun applyTypeFilter(filter: PokedexTypeFilter) {
        typeFilter.value = if (filter.isActive()) filter else null
    }

    fun clearTypeFilter() {
        typeFilter.value = null
    }

    fun applyStatFilter(filter: PokedexStatFilter) {
        statFilter.value = filter
    }

    fun clearStatFilter() {
        statFilter.value = null
    }

    fun clearAllFilters() {
        regionFilter.value = null
        typeFilter.value = null
        statFilter.value = null
    }
}
