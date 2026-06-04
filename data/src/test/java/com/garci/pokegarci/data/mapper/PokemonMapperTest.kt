package com.garci.pokegarci.data.mapper

import com.garci.pokegarci.data.remote.dto.AbilityBasicDetails
import com.garci.pokegarci.data.remote.dto.AbilitySimpleName
import com.garci.pokegarci.data.remote.dto.CriesResponse
import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.SpeciesResponse
import com.garci.pokegarci.data.remote.dto.SpriteResponse
import com.garci.pokegarci.data.remote.dto.Stats
import com.garci.pokegarci.data.remote.dto.StatInfo
import com.garci.pokegarci.data.remote.dto.TypeInfo
import com.garci.pokegarci.data.remote.dto.TypeSlot
import com.garci.pokegarci.domain.model.Pokemon
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonMapperTest {

    @Test
    fun `mapToDomain uses legacy cry when available`() {
        val pokemon = mapPokemon(
            cries = CriesResponse(
                latest = "https://example.com/latest.ogg",
                legacy = "https://example.com/legacy.ogg",
            ),
        )

        assertEquals("https://example.com/legacy.ogg", pokemon.legacyCryUrl)
    }

    @Test
    fun `mapToDomain falls back to latest cry when legacy is null`() {
        val pokemon = mapPokemon(
            cries = CriesResponse(
                latest = "https://example.com/latest.ogg",
                legacy = null,
            ),
        )

        assertEquals("https://example.com/latest.ogg", pokemon.legacyCryUrl)
    }

    @Test
    fun `mapToDomain uses empty cry url when cries are missing`() {
        val pokemon = mapPokemon(cries = null)

        assertEquals("", pokemon.legacyCryUrl)
    }

    @Test
    fun `cryUrlFromDetails falls back to latest when gson legacy is null`() {
        val json = """
            {
              "id": 731,
              "name": "pikipek",
              "sprites": { "front_default": "https://example.com/sprite.png" },
              "types": [{ "slot": 1, "type": { "name": "normal" } }],
              "stats": [{ "base_stat": 35, "stat": { "name": "hp" } }],
              "height": 3,
              "weight": 12,
              "abilities": [],
              "cries": {
                "latest": "https://example.com/latest/731.ogg",
                "legacy": null
              }
            }
        """.trimIndent()

        val details = Gson().fromJson(json, PokemonDetailsResponse::class.java)

        assertEquals("https://example.com/latest/731.ogg", PokemonMapper.cryUrlFromDetails(details))
    }

    private fun mapPokemon(cries: CriesResponse?): Pokemon {
        return PokemonMapper.mapToDomain(
            details = PokemonDetailsResponse(
                id = 25,
                name = "pikachu",
                sprites = SpriteResponse(front_default = "https://example.com/sprite.png"),
                types = listOf(TypeSlot(1, TypeInfo("electric"))),
                stats = listOf(Stats(35, StatInfo("hp"))),
                height = 4,
                weight = 60,
                abilities = listOf(
                    AbilityBasicDetails(
                        ability = AbilitySimpleName("static"),
                        slot = 1,
                        is_hidden = false,
                    ),
                ),
                cries = cries,
            ),
            species = SpeciesResponse(flavor_text_entries = emptyList()),
            language = "es",
        )
    }
}
