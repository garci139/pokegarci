package com.garci.pokegarci.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity

@Database(
    entities = [PokemonEntity::class, CacheMetadataEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class PokeGarciDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
}
