package com.garci.pokegarci.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.garci.pokegarci.domain.model.Ability

internal object AbilityJsonCodec {
    private val gson = Gson()
    private val listType = object : TypeToken<List<Ability>>() {}.type

    fun encode(abilities: List<Ability>): String {
        return gson.toJson(abilities)
    }

    fun decode(json: String): List<Ability> {
        if (json.isBlank()) return emptyList()
        return gson.fromJson(json, listType) ?: emptyList()
    }
}
