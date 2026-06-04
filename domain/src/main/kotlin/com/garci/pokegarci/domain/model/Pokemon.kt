package com.garci.pokegarci.domain.model

data class Ability(
    val originalName: String,
    val displayName: String
)

data class Pokemon(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val type1: String,
    val type2: String?,
    val description: String,
    val hp: Int,
    val attack: Int,
    val defense: Int,
    val specialAttack: Int,
    val specialDefense: Int,
    val speed: Int,
    val height: Int,
    val weight: Int,
    val abilities: List<Ability>,
    val legacyCryUrl: String = "",
    val backImageUrl: String = "",
    val frontShinyImageUrl: String = "",
    val backShinyImageUrl: String = ""
)
