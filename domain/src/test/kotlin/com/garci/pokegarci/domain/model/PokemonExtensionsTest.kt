package com.garci.pokegarci.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonExtensionsTest {

    @Test
    fun `abilitiesDisplayText joins localized names with slash separator`() {
        val pokemon = Pokemon(
            id = 1,
            name = "Bulbasaur",
            imageUrl = "",
            type1 = "grass",
            type2 = "poison",
            description = "Description",
            hp = 45,
            attack = 49,
            defense = 49,
            specialAttack = 65,
            specialDefense = 65,
            speed = 45,
            height = 7,
            weight = 69,
            abilities = listOf(
                Ability("overgrow", "Espesura"),
                Ability("chlorophyll", "Clorofila"),
            ),
        )

        assertEquals("Espesura / Clorofila", pokemon.abilitiesDisplayText())
    }
}
