package com.garci.pokegarci.data.mapper

import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonEntityMapperTest {

    @Test
    fun `maps domain pokemon to entity and back without data loss`() {
        val pokemon = Pokemon(
            id = 25,
            name = "Pikachu",
            imageUrl = "https://example.com/pikachu.png",
            type1 = "electric",
            type2 = null,
            description = "Mouse Pokemon.",
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
                displayName = "Elec. Estática",
            ),
        )

        val roundTrip = PokemonEntityMapper.toDomain(PokemonEntityMapper.toEntity(pokemon))

        assertEquals(pokemon, roundTrip)
    }
}
