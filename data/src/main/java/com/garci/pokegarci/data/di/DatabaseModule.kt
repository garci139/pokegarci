package com.garci.pokegarci.data.di

import android.content.Context
import androidx.room.Room
import com.garci.pokegarci.data.local.MIGRATION_1_2
import com.garci.pokegarci.data.local.MIGRATION_2_3
import com.garci.pokegarci.data.local.MIGRATION_3_4
import com.garci.pokegarci.data.local.MIGRATION_4_5
import com.garci.pokegarci.data.local.MIGRATION_5_6
import com.garci.pokegarci.data.local.PokeGarciDatabase
import com.garci.pokegarci.data.local.dao.AbilityDao
import com.garci.pokegarci.data.local.dao.PokemonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PokeGarciDatabase {
        return Room.databaseBuilder(
            context,
            PokeGarciDatabase::class.java,
            "pokegarci.db",
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
        ).build()
    }

    @Provides
    fun providePokemonDao(database: PokeGarciDatabase): PokemonDao {
        return database.pokemonDao()
    }

    @Provides
    fun provideAbilityDao(database: PokeGarciDatabase): AbilityDao {
        return database.abilityDao()
    }
}
