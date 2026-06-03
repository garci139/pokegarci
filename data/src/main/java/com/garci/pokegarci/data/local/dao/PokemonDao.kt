package com.garci.pokegarci.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity

@Dao
interface PokemonDao {

    @Query("SELECT COUNT(*) FROM pokemon")
    suspend fun getCount(): Int

    @Query("SELECT * FROM pokemon ORDER BY id ASC")
    suspend fun getAllOrderedById(): List<PokemonEntity>

    @Query("SELECT * FROM cache_metadata WHERE id = 0 LIMIT 1")
    suspend fun getMetadata(): CacheMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemon(pokemon: List<PokemonEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: CacheMetadataEntity)

    @Query("DELETE FROM pokemon")
    suspend fun clearPokemon()

    @Query("DELETE FROM cache_metadata")
    suspend fun clearMetadata()

    @Transaction
    suspend fun replaceAll(pokemon: List<PokemonEntity>, metadata: CacheMetadataEntity) {
        clearPokemon()
        clearMetadata()
        insertPokemon(pokemon)
        insertMetadata(metadata)
    }
}
