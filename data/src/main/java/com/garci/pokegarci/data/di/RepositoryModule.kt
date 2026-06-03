package com.garci.pokegarci.data.di

import com.garci.pokegarci.data.repository.PokemonRepositoryImpl
import com.garci.pokegarci.domain.repository.PokemonRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPokemonRepository(
        impl: PokemonRepositoryImpl,
    ): PokemonRepository
}
