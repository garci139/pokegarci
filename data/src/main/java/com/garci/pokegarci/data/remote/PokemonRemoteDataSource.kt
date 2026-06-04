package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.local.PokemonCryLocalDataSource
import com.garci.pokegarci.data.mapper.PokemonMapper
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRemoteDataSource @Inject constructor(
    private val api: PokeApiService,
    private val abilityTranslationService: AbilityTranslationService,
    private val cryLocalDataSource: PokemonCryLocalDataSource,
) {

    suspend fun fetchAllPokemon(
        language: String,
        onProgress: (Int) -> Unit = {},
    ): List<Pokemon> = coroutineScope {
        val totalIds = PokeApiConstants.POKEMON_CATALOG_MAX_ID
        val completed = AtomicInteger(0)
        val fetchEndPercent = 82

        val pokemonList = (1..totalIds).map { id ->
            async {
                runCatching {
                    fetchPokemonBase(id, language)
                }.getOrNull().also {
                    val done = completed.incrementAndGet()
                    onProgress((done * fetchEndPercent) / totalIds)
                }
            }
        }.awaitAll().filterNotNull()

        onProgress(84)
        val abilityNames = pokemonList
            .flatMap { pokemon -> pokemon.abilities.map(Ability::originalName) }
            .toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        onProgress(88)
        val localized = pokemonList.map { pokemon ->
            abilityTranslationService.applyAbilityLanguage(pokemon, language)
        }

        onProgress(90)
        cryLocalDataSource.ensureCriesCached(localized) { completedCount, total ->
            onProgress(90 + ((completedCount * 10) / total.coerceAtLeast(1)))
        }.also {
            onProgress(100)
        }
    }

    suspend fun refreshLocalizedContent(
        currentPokemon: List<Pokemon>,
        language: String,
        onProgress: (Int) -> Unit = {},
    ): List<Pokemon> = coroutineScope {
        onProgress(2)
        val abilityNames = currentPokemon
            .flatMap { pokemon -> pokemon.abilities.map(Ability::originalName) }
            .toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        onProgress(8)
        val total = currentPokemon.size.coerceAtLeast(1)
        val refreshEndPercent = 88
        val completed = AtomicInteger(0)

        val refreshed = currentPokemon.map { pokemon ->
            async {
                runCatching {
                    val species = api.getPokemonSpecies(pokemon.id)
                    val localized = PokemonMapper.updateLocalizedContent(pokemon, species, language)
                    abilityTranslationService.applyAbilityLanguage(localized, language)
                }.getOrDefault(pokemon).also {
                    val done = completed.incrementAndGet()
                    onProgress(8 + ((done * (refreshEndPercent - 8)) / total))
                }
            }
        }.awaitAll()

        onProgress(100)
        refreshed
    }

    suspend fun refreshMissingCryUrls(
        pokemon: List<Pokemon>,
        onItemCompleted: ((completed: Int, total: Int) -> Unit)? = null,
    ): List<Pokemon> = coroutineScope {
        val total = pokemon.size.coerceAtLeast(1)
        val completed = AtomicInteger(0)
        pokemon.map { entry ->
            async {
                val result = if (entry.legacyCryUrl.isNotBlank()) {
                    entry
                } else {
                    runCatching {
                        val details = api.getPokemonDetails(entry.id)
                        entry.copy(legacyCryUrl = PokemonMapper.cryUrlFromDetails(details))
                    }.getOrDefault(entry)
                }
                onItemCompleted?.invoke(completed.incrementAndGet(), total)
                result
            }
        }.awaitAll()
    }

    private suspend fun fetchPokemonBase(id: Int, language: String): Pokemon {
        val details = api.getPokemonDetails(id)
        val species = api.getPokemonSpecies(id)
        return PokemonMapper.mapToDomain(details, species, language)
    }
}
