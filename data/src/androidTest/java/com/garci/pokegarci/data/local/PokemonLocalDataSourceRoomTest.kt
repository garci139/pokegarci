package com.garci.pokegarci.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonLocalDataSourceRoomTest {

    private lateinit var database: PokeGarciDatabase
    private lateinit var localDataSource: PokemonLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokeGarciDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        localDataSource = PokemonLocalDataSource(database.pokemonDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun getCachedPokemon_returnsNullWhenDatabaseIsEmpty() = runTest {
        assertNull(localDataSource.getCachedPokemon(minCount = 1, language = "es"))
    }

    @Test
    fun getCachedPokemon_returnsDataWhenLanguageAndCountMatch() = runTest {
        val pokemon = listOf(samplePokemon(id = 1, name = "Bulbasaur"))
        localDataSource.saveAll(pokemon, language = "es")

        assertEquals(pokemon, localDataSource.getCachedPokemon(minCount = 1, language = "es"))
    }

    @Test
    fun getCachedPokemon_returnsNullWhenLanguageDiffers() = runTest {
        localDataSource.saveAll(listOf(samplePokemon()), language = "es")

        assertNull(localDataSource.getCachedPokemon(minCount = 1, language = "en"))
    }

    @Test
    fun getCachedPokemon_returnsNullWhenStoredCountIsBelowMinimum() = runTest {
        localDataSource.saveAll(listOf(samplePokemon()), language = "es")

        assertNull(localDataSource.getCachedPokemon(minCount = 251, language = "es"))
    }

    @Test
    fun getCachedPokemonIgnoringLanguage_returnsDataRegardlessOfLanguage() = runTest {
        val pokemon = listOf(samplePokemon(), samplePokemon(id = 4, name = "Charmander"))
        localDataSource.saveAll(pokemon, language = "es")

        assertEquals(pokemon, localDataSource.getCachedPokemonIgnoringLanguage(minCount = 2))
    }

    @Test
    fun saveAll_replacesExistingCache() = runTest {
        localDataSource.saveAll(listOf(samplePokemon(id = 1, name = "Bulbasaur")), language = "es")
        val updated = listOf(samplePokemon(id = 25, name = "Pikachu"))
        localDataSource.saveAll(updated, language = "en")

        assertEquals(updated, localDataSource.getCachedPokemon(minCount = 1, language = "en"))
        assertNull(localDataSource.getCachedPokemon(minCount = 1, language = "es"))
    }
}
