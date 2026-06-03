package com.garci.pokegarci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey val id: Int = 0,
    val language: String,
    val pokemonCount: Int,
)
