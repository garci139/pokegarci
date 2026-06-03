package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon

internal fun samplePokemonEntity(
    id: Int = 25,
    name: String = "Pikachu",
    description: String = "Mouse Pokemon.",
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
        abilityOriginalName = "static",
        abilityDisplayName = "Static",
    )
}

internal fun samplePokemon(
    id: Int = 25,
    name: String = "Pikachu",
    description: String = "Mouse Pokemon.",
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
        firstAbility = Ability(
            originalName = "static",
            displayName = "Static",
        ),
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
