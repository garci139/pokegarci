package com.garci.pokegarci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.garci.pokegarci.data.remote.PokeApiConstants

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey val id: Int = 0,
    val language: String,
    val pokemonCount: Int,
    val isFullCatalog: Boolean = true,
    val catalogMaxId: Int = PokeApiConstants.POKEMON_CATALOG_MAX_ID,
)
