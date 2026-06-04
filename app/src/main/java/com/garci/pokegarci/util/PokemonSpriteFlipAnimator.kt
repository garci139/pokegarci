package com.garci.pokegarci.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

object PokemonSpriteFlipAnimator {

    private const val HALF_FLIP_DURATION_MS = 150L
    private val requestOptions = RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)

    fun toggle(
        imageView: ImageView,
        showingBack: Boolean,
        frontUrl: String,
        backUrl: String,
        onShowingBackChanged: (Boolean) -> Unit,
    ) {
        if (backUrl.isBlank()) return

        val nextShowingBack = !showingBack
        val targetUrl = if (nextShowingBack) backUrl else frontUrl
        val density = imageView.resources.displayMetrics.density
        imageView.cameraDistance = 8_000f * density

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
                    .withEndAction { onShowingBackChanged(nextShowingBack) }
                    .start()
            }
            .start()
    }

    fun reset(imageView: ImageView) {
        imageView.animate().cancel()
        imageView.rotationY = 0f
    }
}
