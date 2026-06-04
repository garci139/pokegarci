package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity
import com.garci.pokegarci.data.mapper.AbilityJsonCodec
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon

internal fun samplePokemonEntity(
    id: Int = 25,
    name: String = "Pikachu",
    description: String = "Mouse Pokemon.",
    abilities: List<Ability> = listOf(Ability("static", "Static")),
): PokemonEntity {
    return PokemonEntity(
        id = id,
        name = name,
        imageUrl = "https://example.com/$name.png",
        type1 = "electric",
        type2 = null,
        description = description,
        hp = 35,
        attack = 55,
        defense = 40,
        specialAttack = 50,
        specialDefense = 50,
        speed = 90,
        height = 4,
        weight = 60,
        abilitiesJson = AbilityJsonCodec.encode(abilities),
        legacyCryUrl = "https://example.com/cries/$id.ogg",
    )
}

internal fun samplePokemon(
    id: Int = 25,
    name: String = "Pikachu",
    description: String = "Mouse Pokemon.",
    abilities: List<Ability> = listOf(Ability("static", "Static")),
): Pokemon {
    return Pokemon(
        id = id,
        name = name,
        imageUrl = "https://example.com/$name.png",
        type1 = "electric",
        type2 = null,
        description = description,
        hp = 35,
        attack = 55,
        defense = 40,
        specialAttack = 50,
        specialDefense = 50,
        speed = 90,
        height = 4,
        weight = 60,
        abilities = abilities,
    )
}

internal fun sampleMetadata(
    language: String = "es",
    pokemonCount: Int = 1,
): CacheMetadataEntity {
    return CacheMetadataEntity(
        language = language,
        pokemonCount = pokemonCount,
    )
}
