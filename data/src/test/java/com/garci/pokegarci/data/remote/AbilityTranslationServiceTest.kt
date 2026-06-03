package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.local.dao.AbilityDao
import com.garci.pokegarci.data.local.entity.AbilityNameEntity
import com.garci.pokegarci.data.remote.dto.AbilityName
import com.garci.pokegarci.data.remote.dto.AbilityResponse
import com.garci.pokegarci.data.remote.dto.LanguageDto
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AbilityTranslationServiceTest {

    private val api = mockk<PokeApiService>()
    private val abilityDao = mockk<AbilityDao>(relaxed = true)
    private lateinit var service: AbilityTranslationService

    @Before
    fun setUp() {
        service = AbilityTranslationService(api, abilityDao)
    }

    @Test
    fun `ensureAllCached fetches only missing abilities`() = runTest {
        coEvery { abilityDao.getCachedOriginalNames() } returns listOf("overgrow")
        coEvery { api.getAbilityDetails("chlorophyll") } returns abilityResponse(
            originalName = "chlorophyll",
            esName = "Clorofila",
            enName = "Chlorophyll",
        )

        service.ensureAllCached(setOf("overgrow", "chlorophyll"))

        coVerify(exactly = 1) { api.getAbilityDetails("chlorophyll") }
        coVerify(exactly = 0) { api.getAbilityDetails("overgrow") }
        coVerify(exactly = 1) {
            abilityDao.insertAll(
                listOf(
                    AbilityNameEntity("chlorophyll", "es", "Clorofila"),
                    AbilityNameEntity("chlorophyll", "en", "Chlorophyll"),
                ),
            )
        }
    }

    @Test
    fun `applyAbilityLanguage uses cached translation for requested language`() = runTest {
        coEvery { abilityDao.getDisplayName("overgrow", "es") } returns "Espesura"

        val pokemon = samplePokemon(ability = Ability("overgrow", "Overgrow"))
        val localized = service.applyAbilityLanguage(pokemon, "es")

        assertEquals("Espesura", localized.firstAbility.displayName)
        coVerify(exactly = 0) { api.getAbilityDetails(any()) }
    }

    @Test
    fun `applyAbilityLanguage fetches ability when translation is missing`() = runTest {
        coEvery { abilityDao.getDisplayName("overgrow", "es") } returnsMany listOf(null, "Espesura")
        coEvery { api.getAbilityDetails("overgrow") } returns abilityResponse(
            originalName = "overgrow",
            esName = "Espesura",
            enName = "Overgrow",
        )

        val pokemon = samplePokemon(ability = Ability("overgrow", "Overgrow"))
        val localized = service.applyAbilityLanguage(pokemon, "es")

        assertEquals("Espesura", localized.firstAbility.displayName)
        coVerify(exactly = 1) { api.getAbilityDetails("overgrow") }
    }

    private fun abilityResponse(
        originalName: String,
        esName: String,
        enName: String,
    ): AbilityResponse {
        return AbilityResponse(
            names = listOf(
                AbilityName(LanguageDto("es"), esName),
                AbilityName(LanguageDto("en"), enName),
            ),
        )
    }

    private fun samplePokemon(ability: Ability): Pokemon {
        return Pokemon(
            id = 1,
            name = "Bulbasaur",
            imageUrl = "https://example.com/bulbasaur.png",
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
            firstAbility = ability,
        )
    }
}
