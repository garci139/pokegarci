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

    private val _loadProgress = MutableStateFlow(0)
    override val loadProgress: StateFlow<Int> = _loadProgress.asStateFlow()

    private val pokemonList = mutableListOf<Pokemon>()

    override fun getPokemonList(): List<Pokemon> = pokemonList.toList()

    override suspend fun loadPokemon(language: String): Result<Unit> {
        _loadFailed.value = false
        reportProgress(0)
        return runCatching {
            localDataSource.getCachedPokemon(language)?.let { cached ->
                applyPokemonList(persistCriesIfNeeded(cached, language, ::reportProgress))
                reportProgress(100)
                return@runCatching
            }

            _isDataLoaded.value = false

            localDataSource.getCachedPokemonIgnoringLanguage()?.let { cached ->
                val updated = remoteDataSource.refreshLocalizedContent(cached, language) { progress ->
                    reportProgress((progress * 85) / 100)
                }
                applyPokemonList(
                    persistCriesIfNeeded(updated, language) { progress ->
                        reportProgress(85 + ((progress - 5).coerceAtLeast(0) * 15 / 95))
                    },
                )
                reportProgress(100)
                return@runCatching
            }

            val fetched = remoteDataSource.fetchAllPokemon(language, ::reportProgress)
            applyPokemonList(fetched)
            localDataSource.saveAll(fetched, language)
            reportProgress(100)
        }.onFailure {
            _loadFailed.value = true
        }
    }

    override suspend fun refreshLocalizedContent(language: String): Result<Unit> {
        _loadFailed.value = false
        reportProgress(0)
        return runCatching {
            if (pokemonList.isEmpty()) return@runCatching
            _isDataLoaded.value = false
            val updated = remoteDataSource.refreshLocalizedContent(pokemonList, language, ::reportProgress)
            applyPokemonList(persistCriesIfNeeded(updated, language, ::reportProgress))
            reportProgress(100)
        }.onFailure {
            _loadFailed.value = true
        }
    }

    private fun applyPokemonList(list: List<Pokemon>) {
        pokemonList.clear()
        pokemonList.addAll(list)
        _isDataLoaded.value = list.isNotEmpty()
    }

    private fun reportProgress(percent: Int) {
        _loadProgress.value = percent.coerceIn(0, 100)
    }

    private suspend fun persistCriesIfNeeded(
        pokemon: List<Pokemon>,
        language: String,
        onProgress: (Int) -> Unit,
    ): List<Pokemon> {
        val withCryUrls = remoteDataSource.refreshMissingCryUrls(pokemon) { completed, total ->
            onProgress(mapSegmentProgress(completed, total, start = 5, end = 45))
        }
        val withLocalCries = cryLocalDataSource.ensureCriesCached(withCryUrls) { completed, total ->
            onProgress(mapSegmentProgress(completed, total, start = 45, end = 100))
        }
        if (withLocalCries != pokemon) {
            localDataSource.saveAll(withLocalCries, language)
        }
        return withLocalCries
    }

    private fun mapSegmentProgress(
        completed: Int,
        total: Int,
        start: Int,
        end: Int,
    ): Int {
        if (total <= 0) return end
        val fraction = completed.toFloat() / total.toFloat()
        return start + ((end - start) * fraction).toInt()
    }
}
