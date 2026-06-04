package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.local.PokemonCryLocalDataSource
import com.garci.pokegarci.data.mapper.PokemonMapper
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonRemoteDataSource @Inject constructor(
    private val api: PokeApiService,
    private val abilityTranslationService: AbilityTranslationService,
    private val cryLocalDataSource: PokemonCryLocalDataSource,
) {

    suspend fun fetchAllPokemon(language: String): List<Pokemon> = coroutineScope {
        val pokemonList = (1..PokeApiConstants.POKEMON_CATALOG_MAX_ID).map { id ->
            async {
                runCatching {
                    fetchPokemonBase(id, language)
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()

        val abilityNames = pokemonList
            .flatMap { pokemon -> pokemon.abilities.map(Ability::originalName) }
            .toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        val localized = pokemonList.map { pokemon ->
            abilityTranslationService.applyAbilityLanguage(pokemon, language)
        }
        cryLocalDataSource.ensureCriesCached(localized)
    }

    suspend fun refreshLocalizedContent(
        currentPokemon: List<Pokemon>,
        language: String,
    ): List<Pokemon> = coroutineScope {
        val abilityNames = currentPokemon
            .flatMap { pokemon -> pokemon.abilities.map(Ability::originalName) }
            .toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        val refreshed = currentPokemon.map { pokemon ->
            async {
                runCatching {
                    val species = api.getPokemonSpecies(pokemon.id)
                    val localized = PokemonMapper.updateLocalizedContent(pokemon, species, language)
                    abilityTranslationService.applyAbilityLanguage(localized, language)
                }.getOrDefault(pokemon)
            }
        }.awaitAll()
        cryLocalDataSource.ensureCriesCached(refreshed)
    }

    suspend fun refreshMissingCryUrls(pokemon: List<Pokemon>): List<Pokemon> = coroutineScope {
        pokemon.map { entry ->
            async {
                if (entry.legacyCryUrl.isNotBlank()) {
                    return@async entry
                }
                runCatching {
                    val details = api.getPokemonDetails(entry.id)
                    entry.copy(legacyCryUrl = PokemonMapper.cryUrlFromDetails(details))
                }.getOrDefault(entry)
            }
        }.awaitAll()
    }

    private suspend fun fetchPokemonBase(id: Int, language: String): Pokemon {
        val details = api.getPokemonDetails(id)
        val species = api.getPokemonSpecies(id)
        return PokemonMapper.mapToDomain(details, species, language)
    }
}
