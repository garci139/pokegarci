package com.garci.pokegarci.domain.pokedex

import com.garci.pokegarci.domain.model.Pokemon

data class StatThreshold(
    val min: Int? = null,
    val max: Int? = null,
) {
    fun isActive(): Boolean = min != null || max != null

    fun isValid(): Boolean {
        if (min != null && min < 0) return false
        if (max != null && max < 0) return false
        if (min != null && max != null && min > max) return false
        return true
    }

    fun matches(value: Int): Boolean {
        if (min != null && value < min) return false
        if (max != null && value > max) return false
        return true
    }
}

data class PokedexStatFilter(
    val hp: StatThreshold = StatThreshold(),
    val attack: StatThreshold = StatThreshold(),
    val defense: StatThreshold = StatThreshold(),
    val specialAttack: StatThreshold = StatThreshold(),
    val specialDefense: StatThreshold = StatThreshold(),
    val speed: StatThreshold = StatThreshold(),
) {
    fun isActive(): Boolean = listOf(
        hp,
        attack,
        defense,
        specialAttack,
        specialDefense,
        speed,
    ).any { it.isActive() }

    fun isValid(): Boolean = listOf(
        hp,
        attack,
        defense,
        specialAttack,
        specialDefense,
        speed,
    ).all { it.isValid() }

    fun matches(pokemon: Pokemon): Boolean {
        return hp.matches(pokemon.hp) &&
            attack.matches(pokemon.attack) &&
            defense.matches(pokemon.defense) &&
            specialAttack.matches(pokemon.specialAttack) &&
            specialDefense.matches(pokemon.specialDefense) &&
            speed.matches(pokemon.speed)
    }
}
