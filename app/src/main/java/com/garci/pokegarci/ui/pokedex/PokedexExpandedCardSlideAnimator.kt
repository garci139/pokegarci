package com.garci.pokegarci.ui.pokedex

import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.view.doOnLayout

object PokedexExpandedCardSlideAnimator {

    private const val SLIDE_DURATION_MS = 160L

    fun slide(
        card: View,
        direction: Int,
        onSwapContent: () -> Unit,
        onComplete: () -> Unit,
    ) {
        if (direction == 0) {
            onSwapContent()
            onComplete()
            return
        }

        fun runSlide() {
            val width = card.width
            if (width == 0) {
                onSwapContent()
                onComplete()
                return
            }
            val outDelta = if (direction > 0) -width.toFloat() else width.toFloat()
            val inStart = -outDelta
            card.animate().cancel()
            card.animate()
                .translationX(outDelta)
                .setDuration(SLIDE_DURATION_MS)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    onSwapContent()
                    card.translationX = inStart
                    card.animate()
                        .translationX(0f)
                        .setDuration(SLIDE_DURATION_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .withEndAction(onComplete)
                }
        }

        if (card.width > 0) {
            runSlide()
        } else {
            card.doOnLayout { runSlide() }
        }
    }
}
