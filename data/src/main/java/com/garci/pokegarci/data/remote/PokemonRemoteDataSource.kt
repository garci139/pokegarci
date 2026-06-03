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
) {

    suspend fun fetchAllPokemon(limit: Int, language: String): List<Pokemon> = coroutineScope {
        val pokemonResponse = api.getPokemonList(limit)

        pokemonResponse.results.map { pokemonInfo ->
            async {
                runCatching {
                    fetchPokemon(pokemonInfo.name, language)
                }.getOrNull()
            }
        }.awaitAll().filterNotNull()
    }

    suspend fun refreshLocalizedContent(
        currentPokemon: List<Pokemon>,
        language: String,
    ): List<Pokemon> = coroutineScope {
        currentPokemon.map { pokemon ->
            async {
                runCatching {
                    val species = api.getPokemonSpecies(pokemon.id)
                    val ability = api.getAbilityDetails(pokemon.firstAbility.originalName.lowercase())
                    PokemonMapper.updateLocalizedContent(pokemon, species, ability, language)
                }.getOrDefault(pokemon)
            }
        }.awaitAll()
    }

    private suspend fun fetchPokemon(name: String, language: String): Pokemon {
        val details = api.getPokemonDetails(name)
        val species = api.getPokemonSpecies(details.id)
        val firstAbilityName = details.abilities.firstOrNull()?.ability?.name ?: "unknown"
        val abilityResponse = runCatching {
            api.getAbilityDetails(firstAbilityName.lowercase())
        }.getOrNull()

        return PokemonMapper.mapToDomain(details, species, abilityResponse, language)
    }
}
