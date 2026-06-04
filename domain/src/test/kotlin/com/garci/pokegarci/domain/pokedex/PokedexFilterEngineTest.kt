package com.garci.pokegarci.domain.pokedex

import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.pokedex.PokemonTypes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PokedexFilterEngineTest {

    private val pikachu = samplePokemon(
        id = 25,
        type1 = "electric",
        type2 = null,
        hp = 35,
        speed = 90,
    )
    private val charizard = samplePokemon(
        id = 6,
        type1 = "fire",
        type2 = "flying",
        hp = 78,
        speed = 100,
    )
    private val bulbasaur = samplePokemon(
        id = 1,
        type1 = "grass",
        type2 = "poison",
        hp = 45,
        speed = 45,
    )

    @Test
    fun `region filter keeps pokemon in selected generations`() {
        val chikorita = samplePokemon(id = 152, type1 = "grass", type2 = null, hp = 45, speed = 45)
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, chikorita),
            searchQuery = "",
            regionFilter = setOf(PokemonGeneration.GEN_II),
            typeFilter = null,
            statFilter = null,
        )

        assertEquals(listOf(chikorita), result)
    }

    @Test
    fun `type filter single type matches mono and dual`() {
        val filter = PokedexTypeFilter(selectedTypes = setOf("fire"), monotypeOnly = false)
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = filter,
            statFilter = null,
        )

        assertEquals(listOf(charizard), result)
    }

    @Test
    fun `type filter monotype only`() {
        val filter = PokedexTypeFilter(selectedTypes = emptySet(), monotypeOnly = true)
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = filter,
            statFilter = null,
        )

        assertEquals(listOf(pikachu), result)
    }

    @Test
    fun `type filter monotype with selected types`() {
        val filter = PokedexTypeFilter(selectedTypes = setOf("grass", "electric"), monotypeOnly = true)
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = filter,
            statFilter = null,
        )

        assertEquals(listOf(pikachu), result)
    }

    @Test
    fun `type filter multiple types requires dual match from selection`() {
        val filter = PokedexTypeFilter(
            selectedTypes = setOf("water", "fire", "grass"),
            monotypeOnly = false,
        )
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = filter,
            statFilter = null,
        )

        assertEquals(listOf(bulbasaur), result)
    }

    @Test
    fun `type filter with all types selected includes mono and dual types`() {
        val filter = PokedexTypeFilter(
            selectedTypes = PokemonTypes.ALL,
            monotypeOnly = false,
        )
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = filter,
            statFilter = null,
        )

        assertEquals(listOf(pikachu, charizard, bulbasaur), result)
        assertEquals(false, filter.isActive())
    }

    @Test
    fun `stat filter applies min and max thresholds`() {
        val filter = PokedexStatFilter(
            speed = StatThreshold(min = 80),
            hp = StatThreshold(max = 50),
        )
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizard, bulbasaur),
            searchQuery = "",
            regionFilter = null,
            typeFilter = null,
            statFilter = filter,
        )

        assertEquals(listOf(pikachu), result)
    }

    @Test
    fun `search query combines with filters`() {
        val charizardNamed = charizard.copy(name = "Charizard")
        val result = PokedexFilterEngine.filter(
            pokemon = listOf(pikachu, charizardNamed),
            searchQuery = "char",
            regionFilter = null,
            typeFilter = null,
            statFilter = null,
        )

        assertEquals(listOf(charizardNamed), result)
    }

    private fun samplePokemon(
        id: Int,
        type1: String,
        type2: String?,
        hp: Int,
        speed: Int,
    ): Pokemon {
        return Pokemon(
            id = id,
            name = "Pokemon$id",
            imageUrl = "",
            type1 = type1,
            type2 = type2,
            description = "",
            hp = hp,
            attack = 50,
            defense = 50,
            specialAttack = 50,
            specialDefense = 50,
            speed = speed,
            height = 0,
            weight = 0,
            abilities = emptyList(),
        )
    }
}
