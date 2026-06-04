package com.garci.pokegarci.domain.usecase

import com.garci.pokegarci.domain.repository.PokemonRepository

class LoadPokemonUseCase(
    private val repository: PokemonRepository,
) {
    suspend operator fun invoke(language: String): Result<Unit> {
        return repository.loadPokemon(language)
    }
}
