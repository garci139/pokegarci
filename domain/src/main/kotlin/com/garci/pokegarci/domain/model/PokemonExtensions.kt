package com.garci.pokegarci.domain.model

fun Pokemon.abilitiesDisplayText(): String {
    return abilities.joinToString(" / ") { it.displayName }
}
