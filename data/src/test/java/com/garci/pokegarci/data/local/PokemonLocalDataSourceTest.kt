package com.garci.pokegarci.data.local

import com.garci.pokegarci.data.local.dao.PokemonDao
import com.garci.pokegarci.data.local.entity.CacheMetadataEntity
import com.garci.pokegarci.data.mapper.AbilityJsonCodec
import com.garci.pokegarci.data.local.entity.PokemonEntity
import com.garci.pokegarci.data.mapper.PokemonEntityMapper
import com.garci.pokegarci.data.remote.PokeApiConstants
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

        assertNull(localDataSource.getCachedPokemon(language = "es"))
    }

    @Test
    fun `getCachedPokemon returns null when language does not match`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns validMetadata(language = "es")

        assertNull(localDataSource.getCachedPokemon(language = "en"))
    }

    @Test
    fun `getCachedPokemon returns null when cache uses outdated catalog`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns CacheMetadataEntity(
            language = "es",
            pokemonCount = 251,
            isFullCatalog = false,
            catalogMaxId = 0,
        )

        assertNull(localDataSource.getCachedPokemon(language = "es"))
    }

    @Test
    fun `getCachedPokemon maps entities when cache is valid`() = runTest {
        val entity = sampleEntity()
        coEvery { pokemonDao.getMetadata() } returns validMetadata(language = "es", pokemonCount = 1)
        coEvery { pokemonDao.getAllOrderedById() } returns listOf(entity)

        val result = localDataSource.getCachedPokemon(language = "es")

        assertEquals(listOf(PokemonEntityMapper.toDomain(entity)), result)
    }

    @Test
    fun `getCachedPokemonIgnoringLanguage returns null when cache uses outdated catalog`() = runTest {
        coEvery { pokemonDao.getMetadata() } returns CacheMetadataEntity(
            language = "es",
            pokemonCount = 1302,
            isFullCatalog = true,
            catalogMaxId = 0,
        )

        assertNull(localDataSource.getCachedPokemonIgnoringLanguage())
    }

    @Test
    fun `saveAll replaces cache through dao transaction`() = runTest {
        val pokemon = listOf(sampleDomainPokemon())

        localDataSource.saveAll(pokemon, language = "en")

        coVerify {
            pokemonDao.replaceAll(
                pokemon = listOf(PokemonEntityMapper.toEntity(pokemon.single())),
                metadata = CacheMetadataEntity(
                    language = "en",
                    pokemonCount = 1,
                    isFullCatalog = true,
                    catalogMaxId = PokeApiConstants.POKEMON_CATALOG_MAX_ID,
                ),
            )
        }
    }

    private fun validMetadata(
        language: String,
        pokemonCount: Int = PokeApiConstants.POKEMON_CATALOG_MAX_ID,
    ): CacheMetadataEntity {
        return CacheMetadataEntity(
            language = language,
            pokemonCount = pokemonCount,
            isFullCatalog = true,
            catalogMaxId = PokeApiConstants.POKEMON_CATALOG_MAX_ID,
        )
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
            abilitiesJson = AbilityJsonCodec.encode(
                listOf(Ability("static", "Static")),
            ),
        )
    }

    private fun sampleDomainPokemon(): Pokemon {
        return PokemonEntityMapper.toDomain(sampleEntity())
    }
}
