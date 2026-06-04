package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.local.PokemonCryLocalDataSource
import com.garci.pokegarci.data.remote.dto.AbilityBasicDetails
import com.garci.pokegarci.data.remote.dto.AbilitySimpleName
import com.garci.pokegarci.data.remote.dto.CriesResponse
import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.SpriteResponse
import com.garci.pokegarci.data.remote.dto.Stats
import com.garci.pokegarci.data.remote.dto.StatInfo
import com.garci.pokegarci.data.remote.dto.TypeInfo
import com.garci.pokegarci.data.remote.dto.TypeSlot
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonRemoteDataSourceCryTest {

    private val api = mockk<PokeApiService>()
    private val abilityTranslationService = mockk<AbilityTranslationService>(relaxed = true)
    private val cryLocalDataSource = mockk<PokemonCryLocalDataSource>(relaxed = true)
    private val remoteDataSource = PokemonRemoteDataSource(
        api,
        abilityTranslationService,
        cryLocalDataSource,
    )

    @Test
    fun `refreshMissingCryUrls fetches details when cry url is blank`() = runTest {
        val pokemon = samplePokemon(id = 731, legacyCryUrl = "")
        coEvery { api.getPokemonDetails(731) } returns sampleDetails()

        val result = remoteDataSource.refreshMissingCryUrls(listOf(pokemon)).single()

        assertEquals("https://example.com/latest/731.ogg", result.legacyCryUrl)
        coVerify(exactly = 1) { api.getPokemonDetails(731) }
    }

    @Test
    fun `refreshMissingCryUrls keeps existing cry url`() = runTest {
        val pokemon = samplePokemon(
            id = 731,
            legacyCryUrl = "https://example.com/existing.ogg",
            backImageUrl = "https://example.com/back.png",
        )

        val result = remoteDataSource.refreshMissingCryUrls(listOf(pokemon)).single()

        assertEquals(pokemon, result)
        coVerify(exactly = 0) { api.getPokemonDetails(any()) }
    }

    @Test
    fun `refreshMissingPokedexExtras fetches back sprite when missing`() = runTest {
        val pokemon = samplePokemon(
            id = 731,
            legacyCryUrl = "https://example.com/existing.ogg",
            backImageUrl = "",
        )
        coEvery { api.getPokemonDetails(731) } returns sampleDetails()

        val result = remoteDataSource.refreshMissingPokedexExtras(listOf(pokemon)).single()

        assertEquals("https://example.com/back/731.png", result.backImageUrl)
        assertEquals("https://example.com/existing.ogg", result.legacyCryUrl)
        coVerify(exactly = 1) { api.getPokemonDetails(731) }
    }

    private fun sampleDetails(): PokemonDetailsResponse {
        return PokemonDetailsResponse(
            id = 731,
            name = "pikipek",
            sprites = SpriteResponse(
                front_default = "https://example.com/sprite.png",
                back_default = "https://example.com/back/731.png",
            ),
            types = listOf(TypeSlot(1, TypeInfo("normal"))),
            stats = listOf(Stats(35, StatInfo("hp"))),
            height = 3,
            weight = 12,
            abilities = listOf(
                AbilityBasicDetails(
                    ability = AbilitySimpleName("keen-eye"),
                    slot = 1,
                    is_hidden = false,
                ),
            ),
            cries = CriesResponse(
                latest = "https://example.com/latest/731.ogg",
                legacy = null,
            ),
        )
    }

    private fun samplePokemon(
        id: Int,
        legacyCryUrl: String,
        backImageUrl: String = "https://example.com/back.png",
    ): Pokemon {
        return Pokemon(
            id = id,
            name = "Pikipek",
            imageUrl = "https://example.com/sprite.png",
            type1 = "normal",
            type2 = null,
            description = "Woodpecker Pokemon.",
            hp = 35,
            attack = 55,
            defense = 40,
            specialAttack = 50,
            specialDefense = 50,
            speed = 90,
            height = 3,
            weight = 12,
            abilities = listOf(Ability("keen-eye", "Keen Eye")),
            legacyCryUrl = legacyCryUrl,
            backImageUrl = backImageUrl,
        )
    }
}
