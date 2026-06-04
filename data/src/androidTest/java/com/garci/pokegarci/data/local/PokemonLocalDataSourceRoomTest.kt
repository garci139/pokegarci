package com.garci.pokegarci.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
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
        assertNull(localDataSource.getCachedPokemon(language = "es"))
    }

    @Test
    fun getCachedPokemon_returnsDataWhenLanguageMatches() = runTest {
        val pokemon = listOf(samplePokemon(id = 1, name = "Bulbasaur"))
        localDataSource.saveAll(pokemon, language = "es")

        assertEquals(pokemon, localDataSource.getCachedPokemon(language = "es"))
    }

    @Test
    fun getCachedPokemon_returnsNullWhenLanguageDiffers() = runTest {
        localDataSource.saveAll(listOf(samplePokemon()), language = "es")

        assertNull(localDataSource.getCachedPokemon(language = "en"))
    }

    @Test
    fun getCachedPokemonIgnoringLanguage_returnsDataRegardlessOfLanguage() = runTest {
        val pokemon = listOf(samplePokemon(), samplePokemon(id = 4, name = "Charmander"))
        localDataSource.saveAll(pokemon, language = "es")

        assertEquals(pokemon, localDataSource.getCachedPokemonIgnoringLanguage())
    }

    @Test
    fun saveAll_replacesExistingCache() = runTest {
        localDataSource.saveAll(listOf(samplePokemon(id = 1, name = "Bulbasaur")), language = "es")
        val updated = listOf(samplePokemon(id = 25, name = "Pikachu"))
        localDataSource.saveAll(updated, language = "en")

        assertEquals(updated, localDataSource.getCachedPokemon(language = "en"))
        assertNull(localDataSource.getCachedPokemon(language = "es"))
    }

    private fun samplePokemon(id: Int = 25, name: String = "Pikachu"): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            imageUrl = "https://example.com/$name.png",
            type1 = "electric",
            type2 = null,
            description = "Description",
            hp = 35,
            attack = 55,
            defense = 40,
            specialAttack = 50,
            specialDefense = 50,
            speed = 90,
            height = 4,
            weight = 60,
            abilities = listOf(Ability("static", "Static")),
        )
    }
}
