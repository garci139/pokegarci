package com.garci.pokegarci.util

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.garci.pokegarci.R

object ClickSound {

    private var mediaPlayer: MediaPlayer? = null

    private fun audioAttributes(): AudioAttributes {
        return AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    }

    fun init(context: Context) {
        if (mediaPlayer != null) return
        preparePlayer(context.applicationContext)
    }

    private fun preparePlayer(context: Context) {
        release()
        val player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(audioAttributes())
                val asset = context.resources.openRawResourceFd(R.raw.click_emerald)
                try {
                    setDataSource(asset.fileDescriptor, asset.startOffset, asset.length)
                } finally {
                    asset.close()
                }
                prepare()
                setVolume(1f, 1f)
            }
        }.getOrNull() ?: MediaPlayer.create(context, R.raw.click_emerald)?.apply {
            setAudioAttributes(audioAttributes())
            setVolume(1f, 1f)
        }

        mediaPlayer = player
    }

    fun isReady(): Boolean = mediaPlayer != null

    fun playPrepared(): Boolean {
        val player = mediaPlayer ?: return false
        return runCatching {
            if (player.isPlaying)
                player.pause()
            player.seekTo(0)
            player.setVolume(1f, 1f)
            player.start()
            true
        }.getOrElse {
            mediaPlayer = null
            false
        }
    }

    fun playOneShot(context: Context) {
        runCatching {
            MediaPlayer.create(context, R.raw.click_emerald)?.apply {
                setAudioAttributes(audioAttributes())
                setVolume(1f, 1f)
                setOnCompletionListener { player -> player.release() }
                start()
            }
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

fun Context.playClickEmeraldSound() {
    if (!ClickSound.isReady())
        ClickSound.init(applicationContext)
    if (!ClickSound.playPrepared())
        ClickSound.playOneShot(this)
}
