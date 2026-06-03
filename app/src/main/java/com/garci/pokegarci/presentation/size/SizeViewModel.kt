package com.garci.pokegarci.presentation.size

import android.content.Context
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
import javax.inject.Inject

@HiltViewModel
class SizeViewModel @Inject constructor(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    repository: PokemonRepository,
    loadPokemonUseCase: LoadPokemonUseCase,
    @ApplicationContext context: Context,
) : PokemonDataViewModel(repository, loadPokemonUseCase, context) {

    private val _pokemonList = MutableStateFlow<List<Pokemon>>(emptyList())
    val pokemonList: StateFlow<List<Pokemon>> = _pokemonList.asStateFlow()

    fun refreshPokemonList() {
        _pokemonList.value = getPokemonListUseCase()
    }
}
