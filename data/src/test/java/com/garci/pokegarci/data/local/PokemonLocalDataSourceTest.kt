package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.local.entity.PokemonEntity
import com.garci.pokegarci.data.mapper.PokemonEntityMapper
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class PokemonLocalDataSourceTest {

    private val pokemonDao = mockk<PokemonDao>(relaxed = true)
    private lateinit var localDataSource: PokemonLocalDataSource

    @Before
    fun setUp() {
        localDataSource = PokemonLocalDataSource(pokemonDao)
    }

    @Test
    fun `getCachedPokemon returns null when metadata is missing`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns null

        assertNull(localDataSource.getCachedPokemon(minCount = 1, language = "es"))
    }

    @Test
    fun `getCachedPokemon returns null when language does not match`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns CacheMetadataEntity(language = "es", pokemonCount = 251)

        assertNull(localDataSource.getCachedPokemon(minCount = 251, language = "en"))
    }

    @Test
    fun `getCachedPokemon returns null when stored count is below minimum`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns CacheMetadataEntity(language = "es", pokemonCount = 10)

        assertNull(localDataSource.getCachedPokemon(minCount = 251, language = "es"))
    }

    @Test
    fun `getCachedPokemon maps entities when cache is valid`() = runTest {
        val entity = sampleEntity()
        coEvery { pokemonDao.getMetadata() } returns CacheMetadataEntity(language = "es", pokemonCount = 1)
        coEvery { pokemonDao.getAllOrderedById() } returns listOf(entity)

        val result = localDataSource.getCachedPokemon(minCount = 1, language = "es")

        assertEquals(listOf(PokemonEntityMapper.toDomain(entity)), result)
    }

    @Test
    fun `getCachedPokemonIgnoringLanguage returns null when count is insufficient`() = runTest {
        coEvery { pokemonDao.getCount() } returns 10

        assertNull(localDataSource.getCachedPokemonIgnoringLanguage(minCount = 251))
    }

    @Test
    fun `saveAll replaces cache through dao transaction`() = runTest {
        val pokemon = listOf(sampleDomainPokemon())

        localDataSource.saveAll(pokemon, language = "en")

        coVerify {
            pokemonDao.replaceAll(
                pokemon = listOf(PokemonEntityMapper.toEntity(pokemon.single())),
                metadata = CacheMetadataEntity(language = "en", pokemonCount = 1),
            )
        }
    }

    private fun sampleEntity(): PokemonEntity {
        return PokemonEntity(
            id = 25,
            name = "Pikachu",
            imageUrl = "https://example.com/pikachu.png",
            type1 = "electric",
            type2 = null,
            description = "Mouse Pokemon.",
            hp = 35,
            attack = 55,
            defense = 40,
            specialAttack = 50,
            specialDefense = 50,
            speed = 90,
            height = 4,
            weight = 60,
            abilityOriginalName = "static",
            abilityDisplayName = "Static",
        )
    }

    private fun sampleDomainPokemon(): Pokemon {
        return PokemonEntityMapper.toDomain(sampleEntity())
    }
}
