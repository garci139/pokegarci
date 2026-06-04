package com.garci.pokegarci.data.local

import android.content.Context
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PokemonCryLocalDataSourceTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `ensureCriesCached downloads remote cry and stores local path`() = runTest {
        val server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setBody(sampleOggPayload()))

        val context = mockk<Context>()
        every { context.filesDir } returns tempFolder.root

        val dataSource = PokemonCryLocalDataSource(context, OkHttpClient())
        val pokemon = samplePokemon(
            legacyCryUrl = server.url("/25.ogg").toString(),
        )

        val result = dataSource.ensureCriesCached(listOf(pokemon)).single()

        assertTrue(result.legacyCryUrl.endsWith("${File.separator}25.ogg"))
        assertTrue(File(result.legacyCryUrl).exists())
        server.shutdown()
    }

    @Test
    fun `ensureCriesCached reuses existing local file`() = runTest {
        val context = mockk<Context>()
        every { context.filesDir } returns tempFolder.root

        val criesDir = File(tempFolder.root, "pokemon_cries").apply { mkdirs() }
        val existing = File(criesDir, "25.ogg").apply { writeBytes(sampleOggPayload()) }

        val dataSource = PokemonCryLocalDataSource(context, OkHttpClient())
        val pokemon = samplePokemon(legacyCryUrl = "https://example.com/25.ogg")

        val result = dataSource.ensureCriesCached(listOf(pokemon)).single()

        assertEquals(existing.absolutePath, result.legacyCryUrl)
    }

    @Test
    fun `ensureCriesCached ignores invalid local file and downloads again`() = runTest {
        val server = MockWebServer()
        server.start()
        server.enqueue(MockResponse().setBody(sampleOggPayload()))

        val context = mockk<Context>()
        every { context.filesDir } returns tempFolder.root

        val criesDir = File(tempFolder.root, "pokemon_cries").apply { mkdirs() }
        File(criesDir, "25.ogg").writeBytes(byteArrayOf(0, 0, 0))

        val dataSource = PokemonCryLocalDataSource(context, OkHttpClient())
        val pokemon = samplePokemon(legacyCryUrl = server.url("/25.ogg").toString())

        val result = dataSource.ensureCriesCached(listOf(pokemon)).single()

        assertTrue(PokemonCryLocalDataSource.isValidOggFile(File(result.legacyCryUrl)))
        server.shutdown()
    }

    private fun sampleOggPayload(): ByteArray {
        return byteArrayOf(0x4F, 0x67, 0x67, 0x53, 1, 2, 3, 4)
    }

    private fun samplePokemon(legacyCryUrl: String): Pokemon {
        return Pokemon(
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
            abilities = listOf(Ability("static", "Static")),
            legacyCryUrl = legacyCryUrl,
        )
    }
}
