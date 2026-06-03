package com.garci.pokegarci.data.di

import android.content.Context
import androidx.room.Room
import com.garci.pokegarci.data.local.PokeGarciDatabase
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
        ).build()
    }

    @Provides
    fun providePokemonDao(database: PokeGarciDatabase): PokemonDao {
        return database.pokemonDao()
    }
}
