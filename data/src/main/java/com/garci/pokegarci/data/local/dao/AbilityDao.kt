package com.garci.pokegarci.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garci.pokegarci.data.local.entity.AbilityNameEntity

@Dao
interface AbilityDao {

    @Query("SELECT displayName FROM ability_names WHERE originalName = :originalName AND language = :language LIMIT 1")
    suspend fun getDisplayName(originalName: String, language: String): String?

    @Query("SELECT DISTINCT originalName FROM ability_names")
    suspend fun getCachedOriginalNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(names: List<AbilityNameEntity>)
}
