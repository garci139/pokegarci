package com.garci.pokegarci.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.garci.pokegarci.R
import com.garci.pokegarci.util.AppFont
import kotlin.math.max

/**
 * Barra de carga con el aspecto de la barra de PS de los combates Pokémon (relleno ascendente).
 */
class PokemonHpLoadBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    var progress: Int = 0
        set(value) {
            val clamped = value.coerceIn(0, 100)
            if (field != clamped) {
                field = clamped
                progressLabel = context.getString(R.string.loadingProgressPercent, clamped)
                requestLayout()
                invalidate()
            }
        }

    private var progressLabel: String = context.getString(R.string.loadingProgressPercent, 0)

    private val density = resources.displayMetrics.density

    private val hpLabel = context.getString(R.string.hpLabel)
    private val barHeightPx = BAR_HEIGHT_DP * density
    private val barOuterStrokePx = BAR_OUTER_STROKE_DP * density
    private val barInnerInsetPx = BAR_INNER_INSET_DP * density
    private val sectionGapPx = SECTION_GAP_DP * density

    private val outerFrameRect = RectF()
    private val innerFrameRect = RectF()
    private val trackRect = RectF()
    private val fillRect = RectF()

    private val outerFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.hp_bar_frame_outer)
        strokeWidth = barOuterStrokePx
    }

    private val innerFramePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.hp_bar_frame_inner)
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.hp_bar_track)
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = ContextCompat.getColor(context, R.color.hp_bar_fill)
    }

    private val hpLabelPaint = outlinedTextPaint(
        textSizeSp = HP_LABEL_TEXT_SP,
        fillColor = R.color.hp_bar_label,
        strokeColor = R.color.hp_bar_text_outline,
    )

    private val valuePaint = outlinedTextPaint(
        textSizeSp = HP_VALUE_TEXT_SP,
        fillColor = R.color.white,
        strokeColor = R.color.hp_bar_text_outline,
    )

    private var hpLabelWidth = 0f
    private var valueWidth = 0f
    private var textBaseline = 0f

    init {
        measureTextWidths()
    }

    private fun outlinedTextPaint(
        textSizeSp: Float,
        fillColor: Int,
        strokeColor: Int,
    ): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = AppFont.get(context)
        textSize = textSizeSp * resources.displayMetrics.scaledDensity
        color = ContextCompat.getColor(context, fillColor)
        style = Paint.Style.FILL
        setShadowLayer(2f * density, 0f, 1f * density, ContextCompat.getColor(context, strokeColor))
    }

    private fun measureTextWidths() {
        hpLabelWidth = hpLabelPaint.measureText(hpLabel)
        valueWidth = valuePaint.measureText(progressLabel)
        val sampleMax = valuePaint.measureText("100%")
        valueWidth = max(valueWidth, sampleMax)

        val fontMetrics = valuePaint.fontMetrics
        textBaseline = (fontMetrics.ascent + fontMetrics.descent) / 2f
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureTextWidths()
        val minBarWidth = (MIN_BAR_WIDTH_DP * density).toInt()
        val textHeight = valuePaint.textSize
        val rowHeight = max(barHeightPx, textHeight)
        val desiredWidth = (
            paddingLeft +
                hpLabelWidth +
                sectionGapPx +
                minBarWidth +
                sectionGapPx +
                valueWidth +
                paddingRight
            ).toInt()
        val desiredHeight = (paddingTop + rowHeight + paddingBottom).toInt()

        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val contentLeft = paddingLeft.toFloat()
        val contentRight = (width - paddingRight).toFloat()
        val contentTop = paddingTop.toFloat()
        val contentBottom = (height - paddingBottom).toFloat()
        val rowHeight = contentBottom - contentTop
        val centerY = (contentTop + contentBottom) / 2f

        val barTop = centerY - barHeightPx / 2f
        val barBottom = centerY + barHeightPx / 2f
        val cornerRadius = barHeightPx / 2.8f

        val valueLeft = contentRight - valueWidth
        val barRight = valueLeft - sectionGapPx
        val barLeft = contentLeft + hpLabelWidth + sectionGapPx
        val barWidth = max(0f, barRight - barLeft)

        drawOutlinedText(
            canvas = canvas,
            text = hpLabel,
            x = contentLeft,
            centerY = centerY,
            paint = hpLabelPaint,
        )

        drawOutlinedText(
            canvas = canvas,
            text = progressLabel,
            x = valueLeft,
            centerY = centerY,
            paint = valuePaint,
        )

        if (barWidth <= 0f) return

        outerFrameRect.set(barLeft, barTop, barRight, barBottom)
        val outerInset = barOuterStrokePx / 2f
        innerFrameRect.set(
            outerFrameRect.left + outerInset,
            outerFrameRect.top + outerInset,
            outerFrameRect.right - outerInset,
            outerFrameRect.bottom - outerInset,
        )
        trackRect.set(
            innerFrameRect.left + barInnerInsetPx,
            innerFrameRect.top + barInnerInsetPx,
            innerFrameRect.right - barInnerInsetPx,
            innerFrameRect.bottom - barInnerInsetPx,
        )

        val innerCorner = max(0f, cornerRadius - barOuterStrokePx - barInnerInsetPx)
        canvas.drawRoundRect(outerFrameRect, cornerRadius, cornerRadius, outerFramePaint)
        canvas.drawRoundRect(innerFrameRect, cornerRadius - outerInset, cornerRadius - outerInset, innerFramePaint)
        canvas.drawRoundRect(trackRect, innerCorner, innerCorner, trackPaint)

        if (progress > 0) {
            val fillRight = trackRect.left + trackRect.width() * (progress / 100f)
            fillRect.set(trackRect.left, trackRect.top, fillRight, trackRect.bottom)
            canvas.drawRoundRect(fillRect, innerCorner, innerCorner, fillPaint)
        }
    }

    private fun drawOutlinedText(
        canvas: Canvas,
        text: String,
        x: Float,
        centerY: Float,
        paint: Paint,
    ) {
        val y = centerY - textBaseline
        canvas.drawText(text, x, y, paint)
    }

    companion object {
        private const val BAR_HEIGHT_DP = 12f
        private const val BAR_OUTER_STROKE_DP = 2.5f
        private const val BAR_INNER_INSET_DP = 2f
        private const val SECTION_GAP_DP = 8f
        private const val MIN_BAR_WIDTH_DP = 168f
        private const val HP_LABEL_TEXT_SP = 13f
        private const val HP_VALUE_TEXT_SP = 15f
    }
}
