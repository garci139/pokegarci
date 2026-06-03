package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.mapper.PokemonEntityMapper
import com.garci.pokegarci.domain.model.Pokemon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonLocalDataSource @Inject constructor(
    private val pokemonDao: PokemonDao,
) {

    suspend fun getCachedPokemon(minCount: Int, language: String): List<Pokemon>? {
        val metadata = pokemonDao.getMetadata() ?: return null
        if (metadata.language != language || metadata.pokemonCount < minCount) return null
        return pokemonDao.getAllOrderedById().map(PokemonEntityMapper::toDomain)
    }

    suspend fun getCachedPokemonIgnoringLanguage(minCount: Int): List<Pokemon>? {
        if (pokemonDao.getCount() < minCount) return null
        return pokemonDao.getAllOrderedById().map(PokemonEntityMapper::toDomain)
    }

    suspend fun saveAll(pokemon: List<Pokemon>, language: String) {
        pokemonDao.replaceAll(
            pokemon = pokemon.map(PokemonEntityMapper::toEntity),
            metadata = CacheMetadataEntity(
                language = language,
                pokemonCount = pokemon.size,
            ),
        )
    }
}
