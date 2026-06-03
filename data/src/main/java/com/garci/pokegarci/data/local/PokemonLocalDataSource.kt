package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.mapper.PokemonEntityMapper
import com.garci.pokegarci.data.remote.PokeApiConstants
import com.garci.pokegarci.domain.model.Pokemon
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PokemonLocalDataSource @Inject constructor(
    private val pokemonDao: PokemonDao,
) {

    suspend fun getCachedPokemon(language: String): List<Pokemon>? {
        val metadata = pokemonDao.getMetadata() ?: return null
        if (!isValidCatalog(metadata) || metadata.language != language) {
            return null
        }
        return pokemonDao.getAllOrderedById().map(PokemonEntityMapper::toDomain)
    }

    suspend fun getCachedPokemonIgnoringLanguage(): List<Pokemon>? {
        val metadata = pokemonDao.getMetadata() ?: return null
        if (!isValidCatalog(metadata)) return null
        return pokemonDao.getAllOrderedById().map(PokemonEntityMapper::toDomain)
    }

    suspend fun saveAll(pokemon: List<Pokemon>, language: String) {
        pokemonDao.replaceAll(
            pokemon = pokemon.map(PokemonEntityMapper::toEntity),
            metadata = CacheMetadataEntity(
                language = language,
                pokemonCount = pokemon.size,
                isFullCatalog = true,
                catalogMaxId = PokeApiConstants.POKEMON_CATALOG_MAX_ID,
            ),
        )
    }

    private fun isValidCatalog(metadata: CacheMetadataEntity): Boolean {
        return metadata.isFullCatalog &&
            metadata.catalogMaxId == PokeApiConstants.POKEMON_CATALOG_MAX_ID &&
            metadata.pokemonCount > 0
    }
}
