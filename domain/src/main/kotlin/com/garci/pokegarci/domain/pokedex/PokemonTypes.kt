package com.garci.pokegarci.domain.pokedex

object PokemonTypes {
    val ALL: Set<String> = setOf(
        "normal",
        "fire",
        "water",
        "grass",
        "electric",
        "ice",
        "fighting",
        "poison",
        "ground",
        "flying",
        "psychic",
        "bug",
        "rock",
        "ghost",
        "dragon",
        "dark",
        "steel",
        "fairy",
    )

    fun isFullSelection(selectedTypes: Set<String>): Boolean {
        return selectedTypes.size >= ALL.size
    }
}
