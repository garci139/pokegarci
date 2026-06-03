package com.garci.pokegarci.domain.repository

import com.garci.pokegarci.domain.model.Pokemon
import kotlinx.coroutines.flow.StateFlow

interface PokemonRepository {
    val isDataLoaded: StateFlow<Boolean>
    val loadFailed: StateFlow<Boolean>

    fun getPokemonList(): List<Pokemon>

    suspend fun loadPokemon(limit: Int, language: String): Result<Unit>

    suspend fun refreshLocalizedContent(language: String): Result<Unit>
}
