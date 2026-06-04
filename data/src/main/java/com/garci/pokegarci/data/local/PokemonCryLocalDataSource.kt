package com.garci.pokegarci.data.local

import android.content.Context
import com.garci.pokegarci.domain.model.Pokemon
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class PokemonCryLocalDataSource @Inject constructor(
    @ApplicationContext context: Context,
    @Named("cries") private val criesHttpClient: OkHttpClient,
) {

    private val criesDirectory = File(context.filesDir, CRIES_DIR_NAME).apply { mkdirs() }
    private val downloadSemaphore = Semaphore(MAX_PARALLEL_DOWNLOADS)

    fun hasLocalCry(id: Int): Boolean = isValidOggFile(localCryFile(id))

    fun localCryPath(id: Int): String? = localCryFile(id)
        .takeIf(::isValidOggFile)
        ?.absolutePath

    suspend fun ensureCriesCached(
        pokemon: List<Pokemon>,
        onItemCompleted: ((completed: Int, total: Int) -> Unit)? = null,
    ): List<Pokemon> = coroutineScope {
        val total = pokemon.size.coerceAtLeast(1)
        val completed = java.util.concurrent.atomic.AtomicInteger(0)
        pokemon.map { entry ->
            async(Dispatchers.IO) {
                downloadSemaphore.withPermit {
                    ensureCryCached(entry)
                }.also {
                    onItemCompleted?.invoke(completed.incrementAndGet(), total)
                }
            }
        }.awaitAll()
    }

    private fun ensureCryCached(pokemon: Pokemon): Pokemon {
        val localFile = localCryFile(pokemon.id)
        if (isValidOggFile(localFile)) {
            return pokemon.copy(legacyCryUrl = localFile.absolutePath)
        }
        if (localFile.exists()) {
            localFile.delete()
        }

        val remoteUrl = pokemon.legacyCryUrl
        if (!remoteUrl.startsWith("http", ignoreCase = true)) {
            return pokemon
        }

        val localPath = downloadCry(pokemon.id, remoteUrl) ?: return pokemon
        return pokemon.copy(legacyCryUrl = localPath)
    }

    private fun downloadCry(id: Int, url: String): String? {
        return runCatching {
            val request = Request.Builder()
                .url(url)
                .cacheControl(CacheControl.FORCE_NETWORK)
                .build()
            criesHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null
                val target = localCryFile(id)
                val temp = File(criesDirectory, "$id.ogg.tmp")
                body.byteStream().use { input ->
                    temp.outputStream().use { output -> input.copyTo(output) }
                }
                if (!isValidOggFile(temp)) {
                    temp.delete()
                    return null
                }
                if (target.exists()) {
                    target.delete()
                }
                if (!temp.renameTo(target)) {
                    temp.copyTo(target, overwrite = true)
                    temp.delete()
                }
                target.absolutePath
            }
        }.getOrNull()
    }

    private fun localCryFile(id: Int): File = File(criesDirectory, "$id.ogg")

    companion object {
        private const val CRIES_DIR_NAME = "pokemon_cries"
        private const val MAX_PARALLEL_DOWNLOADS = 8
        private val OGG_HEADER = byteArrayOf(0x4F, 0x67, 0x67, 0x53)

        internal fun isValidOggFile(file: File): Boolean {
            if (!file.isFile || file.length() < OGG_HEADER.size) return false
            return file.inputStream().use { input ->
                val header = ByteArray(OGG_HEADER.size)
                input.read(header) == OGG_HEADER.size && header.contentEquals(OGG_HEADER)
            }
        }
    }
}
