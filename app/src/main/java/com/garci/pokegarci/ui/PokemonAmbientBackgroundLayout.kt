package com.garci.pokegarci.ui

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.garci.pokegarci.R

class PokemonAmbientBackgroundLayout @JvmOverloads constructor(
    context: Context,
    attrs: android.util.AttributeSet? = null,
) : FrameLayout(context, attrs) {

    private val gradientLayer: View
    private val particleOverlay: AmbientParticleOverlayView

    init {
        clipChildren = false
        clipToPadding = false

        gradientLayer = View(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.gradient_list)
        }
        particleOverlay = AmbientParticleOverlayView(context).apply {
            isClickable = false
            isFocusable = false
        }

        addView(gradientLayer, 0, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(particleOverlay, 1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    fun startAmbientAnimation() {
        (gradientLayer.background as? android.graphics.drawable.AnimationDrawable)?.apply {
            if (!isRunning) {
                setEnterFadeDuration(1_500)
                setExitFadeDuration(3_000)
                start()
            }
        }
        particleOverlay.start()
    }

    fun stopAmbientAnimation() {
        (gradientLayer.background as? android.graphics.drawable.AnimationDrawable)?.stop()
        particleOverlay.stop()
    }

}