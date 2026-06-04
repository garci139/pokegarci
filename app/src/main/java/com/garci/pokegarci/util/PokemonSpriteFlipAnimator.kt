package com.garci.pokegarci.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object PokemonSpriteFlipAnimator {

    private const val HALF_FLIP_DURATION_MS = 150L
    private const val FLIP_IN_PROGRESS_KEY = 0x71A00001
    private val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    fun toggle(
        imageView: ImageView,
        showingBack: Boolean,
        frontUrl: String,
        backUrl: String,
        onComplete: (showingBack: Boolean) -> Unit,
    ): Boolean {
        if (backUrl.isBlank() || isFlipInProgress(imageView)) return false
        val nextShowingBack = !showingBack
        val targetUrl = if (nextShowingBack) backUrl else frontUrl
        val density = imageView.resources.displayMetrics.density
        imageView.cameraDistance = 8_000f * density

        setFlipInProgress(imageView, true)
        imageView.animate().cancel()
        imageView.animate()
            .rotationY(90f)
            .setDuration(HALF_FLIP_DURATION_MS)
            .withEndAction {
                Glide.with(imageView)
                    .load(targetUrl)
                    .apply(requestOptions)
                    .into(imageView)
                imageView.rotationY = -90f
                imageView.animate()
                    .rotationY(0f)
                    .setDuration(HALF_FLIP_DURATION_MS)
                    .withEndAction {
                        setFlipInProgress(imageView, false)
                        onComplete(nextShowingBack)
                    }
                    .start()
            }
            .start()
        return true
    }

    fun reset(imageView: ImageView) {
        imageView.animate().cancel()
        imageView.rotationY = 0f
        setFlipInProgress(imageView, false)
    }

    fun isFlipInProgress(imageView: ImageView): Boolean {
        return imageView.getTag(FLIP_IN_PROGRESS_KEY) == true
    }

    private fun setFlipInProgress(imageView: ImageView, inProgress: Boolean) {
        imageView.setTag(FLIP_IN_PROGRESS_KEY, if (inProgress) true else null)
    }
}
