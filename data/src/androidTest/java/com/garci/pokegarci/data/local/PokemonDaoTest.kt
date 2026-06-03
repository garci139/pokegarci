package com.garci.pokegarci.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.garci.pokegarci.data.local.dao.PokemonDao
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PokemonDaoTest {

    private lateinit var database: PokeGarciDatabase
    private lateinit var dao: PokemonDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, PokeGarciDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.pokemonDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceAll_persistsPokemonAndMetadata() = runTest {
        val pokemon = listOf(samplePokemonEntity(id = 1, name = "Bulbasaur"), samplePokemonEntity(id = 25))
        val metadata = sampleMetadata(language = "es", pokemonCount = 2)

        dao.replaceAll(pokemon, metadata)

        assertEquals(2, dao.getCount())
        assertEquals("es", dao.getMetadata()?.language)
        assertEquals(2, dao.getMetadata()?.pokemonCount)
        assertEquals(listOf(1, 25), dao.getAllOrderedById().map { it.id })
    }

    @Test
    fun replaceAll_replacesPreviousCache() = runTest {
        dao.replaceAll(
            listOf(samplePokemonEntity(id = 1)),
            sampleMetadata(language = "es", pokemonCount = 1),
        )
        dao.replaceAll(
            listOf(samplePokemonEntity(id = 25, name = "Pikachu")),
            sampleMetadata(language = "en", pokemonCount = 1),
        )

        assertEquals(1, dao.getCount())
        assertEquals("en", dao.getMetadata()?.language)
        assertEquals("Pikachu", dao.getAllOrderedById().single().name)
    }

    @Test
    fun getMetadata_returnsNullWhenCacheIsEmpty() = runTest {
        assertNull(dao.getMetadata())
        assertEquals(0, dao.getCount())
    }
}
