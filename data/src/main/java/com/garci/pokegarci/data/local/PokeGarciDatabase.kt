package com.garci.pokegarci.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.garci.pokegarci.data.local.dao.AbilityDao
import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.AbilityNameEntity
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity

@Database(
    entities = [
        PokemonEntity::class,
        CacheMetadataEntity::class,
        AbilityNameEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class PokeGarciDatabase : RoomDatabase() {
    abstract fun pokemonDao(): PokemonDao
    abstract fun abilityDao(): AbilityDao
}
