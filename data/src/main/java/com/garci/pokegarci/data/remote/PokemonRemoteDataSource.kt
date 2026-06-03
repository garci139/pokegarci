package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.mapper.PokemonMapper
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
) {

    suspend fun fetchAllPokemon(language: String): List<Pokemon> = coroutineScope {
        val pokemonList = (1..PokeApiConstants.POKEMON_CATALOG_MAX_ID).map { id ->
            async {
                runCatching {
                    fetchPokemonBase(id, language)
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()

        val abilityNames = pokemonList.map { it.firstAbility.originalName }.toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        pokemonList.map { pokemon ->
            abilityTranslationService.applyAbilityLanguage(pokemon, language)
        }
    }

    suspend fun refreshLocalizedContent(
        currentPokemon: List<Pokemon>,
        language: String,
    ): List<Pokemon> = coroutineScope {
        val abilityNames = currentPokemon.map { it.firstAbility.originalName }.toSet()
        abilityTranslationService.ensureAllCached(abilityNames)

        currentPokemon.map { pokemon ->
            async {
                runCatching {
                    val species = api.getPokemonSpecies(pokemon.id)
                    val localized = PokemonMapper.updateLocalizedContent(pokemon, species, language)
                    abilityTranslationService.applyAbilityLanguage(localized, language)
                }.getOrDefault(pokemon)
            }
        }.awaitAll()
    }

    private suspend fun fetchPokemonBase(id: Int, language: String): Pokemon {
        val details = api.getPokemonDetails(id)
        val species = api.getPokemonSpecies(id)
        return PokemonMapper.mapToDomain(details, species, language)
    }
}
