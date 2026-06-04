package com.garci.pokegarci.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import androidx.core.content.ContextCompat
import com.garci.pokegarci.R
import kotlin.math.sin
import kotlin.random.Random

class AmbientParticleOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private enum class Kind {
        POKEBALL,
        LEAF
    }

    private data class Particle(
        var x: Float,
        var y: Float,
        val speedX: Float,
        val speedY: Float,
        var rotation: Float,
        val rotationSpeed: Float,
        val size: Float,
        val alpha: Int,
        val kind: Kind,
        val swayPhase: Float,
        val swayAmplitude: Float
    )

    private val choreographer: Choreographer = Choreographer.getInstance()
    private val frameCallback: Choreographer.FrameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isRunning) return
            advanceParticles()
            invalidate()
            choreographer.postFrameCallback(this)
        }
    }

    private val leafPath = Path()
    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(48, 56, 142, 60)
    }

    private val pokeballDrawable: Drawable? =
        ContextCompat.getDrawable(context, R.drawable.pokeball)?.mutate()

    private val particles = mutableListOf<Particle>()
    private val random = Random(System.currentTimeMillis())
    private var isRunning = false
    private var lastFrameNanos = 0L

    init {
        isClickable = false
        isFocusable = false
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun start() {
        if (isRunning) return
        if (width == 0 || height == 0) {
            post { start() }
            return
        }
        if (particles.isEmpty()) {
            spawnInitialParticles()
        }
        isRunning = true
        lastFrameNanos = 0L
        choreographer.postFrameCallback(frameCallback)
    }

    fun stop() {
        isRunning = false
        choreographer.removeFrameCallback(frameCallback)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        particles.forEach { particle ->
            when (particle.kind) {
                Kind.POKEBALL -> drawPokeball(canvas, particle)
                Kind.LEAF -> drawLeaf(canvas, particle)
            }
        }
    }

    private fun advanceParticles() {
        if (width == 0 || height == 0) return

        val frameNanos = System.nanoTime()
        val deltaSeconds = if (lastFrameNanos == 0L) {
            0f
        } else {
            ((frameNanos - lastFrameNanos).coerceAtMost(50_000_000L)) / 1_000_000_000f
        }
        lastFrameNanos = frameNanos
        if (deltaSeconds <= 0f) return

        for (index in particles.indices) {
            val particle = particles[index]
            particle.rotation += particle.rotationSpeed * deltaSeconds

            when (particle.kind) {
                Kind.POKEBALL -> {
                    particle.x += particle.speedX * deltaSeconds
                    particle.y += particle.speedY * deltaSeconds
                }
                Kind.LEAF -> {
                    particle.y += particle.speedY * deltaSeconds
                    particle.x += (particle.speedX + sin(particle.y * 0.02f + particle.swayPhase) * particle.swayAmplitude) * deltaSeconds
                }
            }
        }

        val countBeforeRemoval = particles.size
        particles.removeIf { isOutOfBounds(it) }
        val removedCount = countBeforeRemoval - particles.size
        repeat(removedCount) {
            particles += createParticle(randomSpawnY = true)
        }

        while (particles.size < TARGET_PARTICLE_COUNT) {
            particles += createParticle(randomSpawnY = true)
        }
    }

    private fun isOutOfBounds(particle: Particle): Boolean {
        val margin = particle.size
        return particle.x < -margin ||
            particle.x > width + margin ||
            particle.y < -margin ||
            particle.y > height + margin
    }

    private fun spawnInitialParticles() {
        particles.clear()
        repeat(TARGET_PARTICLE_COUNT) {
            particles += createParticle(randomSpawnY = true)
        }
    }

    private fun createParticle(randomSpawnY: Boolean): Particle {
        val kind = if (random.nextFloat() < 0.42f) Kind.LEAF else Kind.POKEBALL
        val size = when (kind) {
            Kind.POKEBALL -> random.nextFloat() * 28f + 36f
            Kind.LEAF -> random.nextFloat() * 10f + 14f
        }
        val x = random.nextFloat() * width.coerceAtLeast(1)
        val y = if (randomSpawnY) {
            random.nextFloat() * height.coerceAtLeast(1)
        } else {
            -size
        }

        return when (kind) {
            Kind.POKEBALL -> Particle(
                x = x,
                y = y,
                speedX = (random.nextFloat() - 0.5f) * 18f,
                speedY = random.nextFloat() * 10f + 6f,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 25f,
                size = size,
                alpha = random.nextInt(26, 52),
                kind = kind,
                swayPhase = 0f,
                swayAmplitude = 0f
            )
            Kind.LEAF -> Particle(
                x = x,
                y = if (randomSpawnY) y else -size,
                speedX = (random.nextFloat() - 0.5f) * 8f,
                speedY = random.nextFloat() * 28f + 22f,
                rotation = random.nextFloat() * 360f,
                rotationSpeed = (random.nextFloat() - 0.5f) * 70f,
                size = size,
                alpha = random.nextInt(34, 72),
                kind = kind,
                swayPhase = random.nextFloat() * 6.28f,
                swayAmplitude = random.nextFloat() * 16f + 10f
            )
        }
    }

    private fun drawPokeball(canvas: Canvas, particle: Particle) {
        val drawable = pokeballDrawable ?: return
        val half = (particle.size / 2f).toInt()
        drawable.alpha = particle.alpha
        canvas.save()
        canvas.translate(particle.x, particle.y)
        canvas.rotate(particle.rotation)
        drawable.setBounds(-half, -half, half, half)
        drawable.draw(canvas)
        canvas.restore()
    }

    private fun drawLeaf(canvas: Canvas, particle: Particle) {
        val halfW = particle.size * 0.45f
        val halfH = particle.size * 0.9f
        leafPaint.alpha = particle.alpha
        leafPath.reset()
        leafPath.moveTo(0f, -halfH)
        leafPath.quadTo(halfW, -halfH * 0.2f, 0f, halfH)
        leafPath.quadTo(-halfW, -halfH * 0.2f, 0f, -halfH)
        leafPath.close()

        canvas.save()
        canvas.translate(particle.x, particle.y)
        canvas.rotate(particle.rotation)
        canvas.drawPath(leafPath, leafPaint)
        canvas.restore()
    }

    companion object {
        private const val TARGET_PARTICLE_COUNT = 30
    }
}