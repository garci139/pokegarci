package com.garci.pokegarci.data.mapper

import com.garci.pokegarci.data.remote.dto.AbilityResponse
import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.SpeciesResponse
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon

object PokemonMapper {

    fun mapToDomain(
        details: PokemonDetailsResponse,
        species: SpeciesResponse,
        abilityResponse: AbilityResponse?,
        language: String,
    ): Pokemon {
        val firstAbilityName = details.abilities.firstOrNull()?.ability?.name ?: "unknown"
        val type1 = details.types.getOrNull(0)?.type?.name ?: "unknown"
        val type2 = details.types.getOrNull(1)?.type?.name
        val statsMap = details.stats.associate { stat -> stat.stat.name to stat.base_stat }

        val description = species.flavor_text_entries
            .firstOrNull { it.language.name == language }
            ?.flavor_text
            ?.replace("\n", " ")
            ?.replace("\u000c", " ")
            ?: "Description unavailable"

        val abilityDisplayName = abilityResponse?.names
            ?.firstOrNull { it.language.name == language }
            ?.name
            ?: firstAbilityName.replaceFirstChar { it.uppercase() }

        return Pokemon(
            id = details.id,
            name = details.name.replaceFirstChar { it.uppercase() },
            imageUrl = details.sprites.front_default.orEmpty(),
            type1 = type1,
            type2 = type2,
            description = description,
            hp = statsMap["hp"] ?: 0,
            attack = statsMap["attack"] ?: 0,
            defense = statsMap["defense"] ?: 0,
            specialAttack = statsMap["special-attack"] ?: 0,
            specialDefense = statsMap["special-defense"] ?: 0,
            speed = statsMap["speed"] ?: 0,
            height = details.height,
            weight = details.weight,
            firstAbility = Ability(
                originalName = firstAbilityName,
                displayName = abilityDisplayName,
            ),
        )
    }

    fun updateLocalizedContent(
        pokemon: Pokemon,
        species: SpeciesResponse,
        abilityResponse: AbilityResponse,
        language: String,
    ): Pokemon {
        val description = species.flavor_text_entries
            .firstOrNull { it.language.name == language }
            ?.flavor_text
            ?.replace("\n", " ")
            ?.replace("\u000c", " ")
            ?: pokemon.description

        val abilityDisplayName = abilityResponse.names
            .firstOrNull { it.language.name == language }
            ?.name
            ?: pokemon.firstAbility.displayName

        return pokemon.copy(
            description = description,
            firstAbility = pokemon.firstAbility.copy(displayName = abilityDisplayName),
        )
    }
}
