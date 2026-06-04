package com.garci.pokegarci.data.repository

import com.garci.pokegarci.data.local.PokemonCryLocalDataSource
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
    private val cryLocalDataSource: PokemonCryLocalDataSource,
) : PokemonRepository {

    private val _isDataLoaded = MutableStateFlow(false)
    override val isDataLoaded: StateFlow<Boolean> = _isDataLoaded.asStateFlow()

    private val _loadFailed = MutableStateFlow(false)
    override val loadFailed: StateFlow<Boolean> = _loadFailed.asStateFlow()

    private val pokemonList = mutableListOf<Pokemon>()

    override fun getPokemonList(): List<Pokemon> = pokemonList.toList()

    override     suspend fun loadPokemon(language: String): Result<Unit> {
        _loadFailed.value = false
        return runCatching {
            localDataSource.getCachedPokemon(language)?.let { cached ->
                applyPokemonList(persistCriesIfNeeded(cached, language))
                return@runCatching
            }

            _isDataLoaded.value = false

            localDataSource.getCachedPokemonIgnoringLanguage()?.let { cached ->
                val updated = remoteDataSource.refreshLocalizedContent(cached, language)
                applyPokemonList(persistCriesIfNeeded(updated, language))
                return@runCatching
            }

            val fetched = remoteDataSource.fetchAllPokemon(language)
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
            applyPokemonList(persistCriesIfNeeded(updated, language))
        }.onFailure {
            _loadFailed.value = true
        }
    }

    private fun applyPokemonList(list: List<Pokemon>) {
        pokemonList.clear()
        pokemonList.addAll(list)
        _isDataLoaded.value = list.isNotEmpty()
    }

    private suspend fun persistCriesIfNeeded(
        pokemon: List<Pokemon>,
        language: String,
    ): List<Pokemon> {
        val withCryUrls = remoteDataSource.refreshMissingCryUrls(pokemon)
        val withLocalCries = cryLocalDataSource.ensureCriesCached(withCryUrls)
        if (withLocalCries != pokemon) {
            localDataSource.saveAll(withLocalCries, language)
        }
        return withLocalCries
    }
}
