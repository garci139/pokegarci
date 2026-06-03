package com.garci.pokegarci.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "ability_names",
    primaryKeys = ["originalName", "language"],
)
data class AbilityNameEntity(
    val originalName: String,
    val language: String,
    val displayName: String,
)
