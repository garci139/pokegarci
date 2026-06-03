package com.garci.pokegarci.di

import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.GetPokemonListUseCase
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.domain.usecase.RefreshPokemonLocalizationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideLoadPokemonUseCase(repository: PokemonRepository): LoadPokemonUseCase {
        return LoadPokemonUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideRefreshPokemonLocalizationUseCase(
        repository: PokemonRepository,
    ): RefreshPokemonLocalizationUseCase {
        return RefreshPokemonLocalizationUseCase(repository)
    }

    @Provides
    @Singleton
    fun provideGetPokemonListUseCase(repository: PokemonRepository): GetPokemonListUseCase {
        return GetPokemonListUseCase(repository)
    }
}
