package com.garci.pokegarci.domain.usecase

import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository

class GetPokemonListUseCase(
    private val repository: PokemonRepository,
) {
    operator fun invoke(): List<Pokemon> = repository.getPokemonList()
}
