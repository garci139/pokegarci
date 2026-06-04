package com.garci.pokegarci.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class PokemonEntity(
    @PrimaryKey val id: Int,
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
    val abilitiesJson: String,
    val legacyCryUrl: String,
    val backImageUrl: String,
)
