package com.garci.pokegarci.data.mapper

import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.SpeciesResponse
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon

object PokemonMapper {

    private const val MAX_ABILITIES = 3

    fun mapToDomain(
        details: PokemonDetailsResponse,
        species: SpeciesResponse,
        language: String,
    ): Pokemon {
        val type1 = details.types.getOrNull(0)?.type?.name ?: "unknown"
        val type2 = details.types.getOrNull(1)?.type?.name
        val statsMap = details.stats.associate { stat -> stat.stat.name to stat.base_stat }

        val description = species.flavor_text_entries
            .firstOrNull { it.language.name == language }
            ?.flavor_text
            ?.replace("\n", " ")
            ?.replace("\u000c", " ")
            ?: "Description unavailable"

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
            abilities = mapAbilities(details),
        )
    }

    fun updateLocalizedContent(
        pokemon: Pokemon,
        species: SpeciesResponse,
        language: String,
    ): Pokemon {
        val description = species.flavor_text_entries
            .firstOrNull { it.language.name == language }
            ?.flavor_text
            ?.replace("\n", " ")
            ?.replace("\u000c", " ")
            ?: pokemon.description

        return pokemon.copy(description = description)
    }

    private fun mapAbilities(details: PokemonDetailsResponse): List<Ability> {
        return details.abilities
            .sortedBy { it.slot }
            .take(MAX_ABILITIES)
            .map { abilitySlot ->
                val originalName = abilitySlot.ability.name
                Ability(
                    originalName = originalName,
                    displayName = AbilityNameFormatter.format(originalName),
                )
            }
            .ifEmpty {
                listOf(
                    Ability(
                        originalName = "unknown",
                        displayName = AbilityNameFormatter.format("unknown"),
                    ),
                )
            }
    }
}
