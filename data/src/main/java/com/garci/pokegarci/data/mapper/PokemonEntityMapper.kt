package com.garci.pokegarci.data.mapper

import com.garci.pokegarci.data.local.entity.PokemonEntity
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon

object PokemonEntityMapper {

    fun toDomain(entity: PokemonEntity): Pokemon {
        return Pokemon(
            id = entity.id,
            name = entity.name,
            imageUrl = entity.imageUrl,
            type1 = entity.type1,
            type2 = entity.type2,
            description = entity.description,
            hp = entity.hp,
            attack = entity.attack,
            defense = entity.defense,
            specialAttack = entity.specialAttack,
            specialDefense = entity.specialDefense,
            speed = entity.speed,
            height = entity.height,
            weight = entity.weight,
            firstAbility = Ability(
                originalName = entity.abilityOriginalName,
                displayName = entity.abilityDisplayName,
            ),
        )
    }

    fun toEntity(pokemon: Pokemon): PokemonEntity {
        return PokemonEntity(
            id = pokemon.id,
            name = pokemon.name,
            imageUrl = pokemon.imageUrl,
            type1 = pokemon.type1,
            type2 = pokemon.type2,
            description = pokemon.description,
            hp = pokemon.hp,
            attack = pokemon.attack,
            defense = pokemon.defense,
            specialAttack = pokemon.specialAttack,
            specialDefense = pokemon.specialDefense,
            speed = pokemon.speed,
            height = pokemon.height,
            weight = pokemon.weight,
            abilityOriginalName = pokemon.firstAbility.originalName,
            abilityDisplayName = pokemon.firstAbility.displayName,
        )
    }
}
