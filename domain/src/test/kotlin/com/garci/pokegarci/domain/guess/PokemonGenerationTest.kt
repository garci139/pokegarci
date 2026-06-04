package com.garci.pokegarci.domain.guess

import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PokemonGenerationTest {

    @Test
    fun `generation ranges include boundary ids`() {
        assertTrue(PokemonGeneration.GEN_I.contains(1))
        assertTrue(PokemonGeneration.GEN_I.contains(151))
        assertFalse(PokemonGeneration.GEN_I.contains(152))

        assertTrue(PokemonGeneration.GEN_IX.contains(906))
        assertTrue(PokemonGeneration.GEN_IX.contains(1025))
        assertFalse(PokemonGeneration.GEN_IX.contains(905))
    }

    @Test
    fun `filterPokemon returns empty list when no generation selected`() {
        val pokemon = listOf(samplePokemon(25))

        val filtered = PokemonGeneration.filterPokemon(pokemon, emptySet())

        assertTrue(filtered.isEmpty())
    }

    @Test
    fun `filterPokemon keeps pokemon in any selected generation`() {
        val pokemon = listOf(
            samplePokemon(1),
            samplePokemon(152),
            samplePokemon(906),
        )

        val filtered = PokemonGeneration.filterPokemon(
            pokemon,
            setOf(PokemonGeneration.GEN_I, PokemonGeneration.GEN_IX),
        )

        assertEquals(listOf(1, 906), filtered.map { it.id })
    }

    private fun samplePokemon(id: Int): Pokemon {
        return Pokemon(
            id = id,
            name = "Pokemon$id",
            imageUrl = "",
            type1 = "normal",
            type2 = null,
            description = "",
            hp = 1,
            attack = 1,
            defense = 1,
            specialAttack = 1,
            specialDefense = 1,
            speed = 1,
            height = 1,
            weight = 1,
            abilities = listOf(Ability("test", "Test")),
        )
    }
}
