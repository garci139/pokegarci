package com.garci.pokegarci.data.remote.dto

data class PokemonDetailsResponse(
    val id: Int,
    val name: String,
    val sprites: SpriteResponse,
    val types: List<TypeSlot>,
    val stats: List<Stats>,
    val height: Int,
    val weight: Int,
    val abilities: List<AbilityBasicDetails>,
)

data class AbilityBasicDetails(
    val ability: AbilitySimpleName,
)

data class AbilitySimpleName(
    val name: String,
)

data class SpriteResponse(
    val front_default: String?,
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo,
)

data class TypeInfo(
    val name: String,
)

data class SpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>,
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: LanguageDto,
)

data class Stats(
    val base_stat: Int,
    val stat: StatInfo,
)

data class StatInfo(
    val name: String,
)

data class LanguageDto(
    val name: String,
)

data class AbilityResponse(
    val names: List<AbilityName>,
)

data class AbilityName(
    val language: LanguageDto,
    val name: String,
)
