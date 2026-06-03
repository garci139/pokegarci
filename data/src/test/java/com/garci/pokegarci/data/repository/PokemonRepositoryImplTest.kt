package com.garci.pokegarci.data.repository

import com.garci.pokegarci.data.local.PokemonLocalDataSource
import com.garci.pokegarci.data.remote.PokemonRemoteDataSource
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PokemonRepositoryImplTest {

    private val remoteDataSource = mockk<PokemonRemoteDataSource>()
    private val localDataSource = mockk<PokemonLocalDataSource>(relaxed = true)
    private lateinit var repository: PokemonRepositoryImpl

    @Before
    fun setUp() {
        repository = PokemonRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Test
    fun `loadPokemon uses cache when language matches`() = runTest {
        val cached = listOf(samplePokemon())
        coEvery { localDataSource.getCachedPokemon(251, "es") } returns cached

        val result = repository.loadPokemon(251, "es")

        assertTrue(result.isSuccess)
        assertEquals(cached, repository.getPokemonList())
        assertTrue(repository.isDataLoaded.value)
        coVerify(exactly = 0) { remoteDataSource.fetchAllPokemon(any(), any()) }
    }

    @Test
    fun `loadPokemon refreshes localization when cache language differs`() = runTest {
        val cached = listOf(samplePokemon())
        val localized = listOf(samplePokemon(description = "Localized"))
        coEvery { localDataSource.getCachedPokemon(251, "en") } returns null
        coEvery { localDataSource.getCachedPokemonIgnoringLanguage(251) } returns cached
        coEvery { remoteDataSource.refreshLocalizedContent(cached, "en") } returns localized
        coEvery { localDataSource.saveAll(localized, "en") } returns Unit

        val result = repository.loadPokemon(251, "en")

        assertTrue(result.isSuccess)
        assertEquals(localized, repository.getPokemonList())
        coVerify(exactly = 0) { remoteDataSource.fetchAllPokemon(any(), any()) }
        coVerify(exactly = 1) { localDataSource.saveAll(localized, "en") }
    }

    @Test
    fun `loadPokemon fetches from network when cache is empty`() = runTest {
        val fetched = listOf(samplePokemon())
        coEvery { localDataSource.getCachedPokemon(251, "es") } returns null
        coEvery { localDataSource.getCachedPokemonIgnoringLanguage(251) } returns null
        coEvery { remoteDataSource.fetchAllPokemon(251, "es") } returns fetched
        coEvery { localDataSource.saveAll(fetched, "es") } returns Unit

        val result = repository.loadPokemon(251, "es")

        assertTrue(result.isSuccess)
        assertEquals(fetched, repository.getPokemonList())
        assertFalse(repository.loadFailed.value)
        coVerify(exactly = 1) { remoteDataSource.fetchAllPokemon(251, "es") }
    }

    @Test
    fun `loadPokemon sets loadFailed when network fails`() = runTest {
        coEvery { localDataSource.getCachedPokemon(251, "es") } returns null
        coEvery { localDataSource.getCachedPokemonIgnoringLanguage(251) } returns null
        coEvery { remoteDataSource.fetchAllPokemon(251, "es") } throws IllegalStateException("offline")

        val result = repository.loadPokemon(251, "es")

        assertTrue(result.isFailure)
        assertTrue(repository.loadFailed.value)
        assertFalse(repository.isDataLoaded.value)
    }

    private fun samplePokemon(description: String = "Description"): Pokemon {
        return Pokemon(
            id = 25,
            name = "Pikachu",
            imageUrl = "https://example.com/pikachu.png",
            type1 = "electric",
            type2 = null,
            description = description,
            hp = 35,
            attack = 55,
            defense = 40,
            specialAttack = 50,
            specialDefense = 50,
            speed = 90,
            height = 4,
            weight = 60,
            firstAbility = Ability("static", "Static"),
        )
    }
}
