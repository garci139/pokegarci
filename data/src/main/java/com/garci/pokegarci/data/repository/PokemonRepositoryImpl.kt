package com.garci.pokegarci.data.repository

import com.garci.pokegarci.data.local.PokemonLocalDataSource
import com.garci.pokegarci.data.remote.PokemonRemoteDataSource
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRepositoryImpl @Inject constructor(
    private val remoteDataSource: PokemonRemoteDataSource,
    private val localDataSource: PokemonLocalDataSource,
) : PokemonRepository {

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    override val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val pokemonList = mutableListOf<Pokemon>()

    override fun getPokemonList(): List<Pokemon> = pokemonList.toList()

    override suspend fun loadPokemon(limit: Int, language: String): Result<Unit> {
        _loadFailed.value = false
        return runCatching {
            localDataSource.getCachedPokemon(limit, language)?.let { cached ->
                applyPokemonList(cached)
                return@runCatching
            }

            _isDataLoaded.value = false

            localDataSource.getCachedPokemonIgnoringLanguage(limit)?.let { cached ->
                val updated = remoteDataSource.refreshLocalizedContent(cached, language)
                applyPokemonList(updated)
                localDataSource.saveAll(updated, language)
                return@runCatching
            }

            val fetched = remoteDataSource.fetchAllPokemon(limit, language)
            applyPokemonList(fetched)
            localDataSource.saveAll(fetched, language)
        }.onFailure {
            _loadFailed.value = true
        }
    }

    override suspend fun refreshLocalizedContent(language: String): Result<Unit> {
        _loadFailed.value = false
        return runCatching {
            if (pokemonList.isEmpty()) return@runCatching
            _isDataLoaded.value = false
            val updated = remoteDataSource.refreshLocalizedContent(pokemonList, language)
            applyPokemonList(updated)
            localDataSource.saveAll(updated, language)
        }.onFailure {
            _loadFailed.value = true
        }
    }

    private fun applyPokemonList(list: List<Pokemon>) {
        pokemonList.clear()
        pokemonList.addAll(list)
        _isDataLoaded.value = list.isNotEmpty()
    }
}
