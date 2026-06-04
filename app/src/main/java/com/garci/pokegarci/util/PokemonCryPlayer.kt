package com.garci.pokegarci.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import java.io.File

class PokemonCryPlayer(
    private val context: Context,
) {

    private var mediaPlayer: MediaPlayer? = null

    fun play(source: String) {
        stop()
        if (source.isBlank()) return

        runCatching {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build(),
                )
                when {
                    source.startsWith("http", ignoreCase = true) -> {
                        setDataSource(context, Uri.parse(source))
                    }
                    else -> {
                        setDataSource(File(source).absolutePath)
                    }
                }
                setOnPreparedListener { preparedPlayer -> preparedPlayer.start() }
                setOnCompletionListener { stop() }
                setOnErrorListener { _, _, _ ->
                    stop()
                    true
                }
                prepareAsync()
            }
        }.onFailure {
            stop()
        }
    }

    fun stop() {
        mediaPlayer?.runCatching {
            if (isPlaying) {
                stop()
            }
            reset()
            release()
        }
        mediaPlayer = null
    }
}
