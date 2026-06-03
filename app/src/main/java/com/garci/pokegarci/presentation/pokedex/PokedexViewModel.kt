package com.garci.pokegarci.presentation.pokedex

import android.content.Context
import androidx.lifecycle.viewModelScope
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.GetPokemonListUseCase
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.presentation.common.PokemonDataViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
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

    val filteredPokemon: StateFlow<List<Pokemon>> = combine(allPokemon, searchQuery) { pokemon, query ->
        if (query.isBlank()) {
            pokemon
        } else {
            val normalizedQuery = query.trim().lowercase()
            pokemon.filter {
                it.name.lowercase().startsWith(normalizedQuery) ||
                    it.id.toString().contains(normalizedQuery)
            }
        }
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
}
