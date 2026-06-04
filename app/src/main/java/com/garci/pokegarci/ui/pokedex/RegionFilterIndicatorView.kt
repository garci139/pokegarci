package com.garci.pokegarci.ui.pokedex

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.garci.pokegarci.R

class RegionFilterIndicatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = 0xFF000000.toInt()
        strokeWidth = resources.displayMetrics.density * 2f
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val path = Path()

    var regionColors: IntArray = intArrayOf()
        set(value) {
            field = value
            invalidate()
        }

    var isRegionSelected: Boolean = false
        set(value) {
            field = value
            invalidate()
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.RegionFilterIndicatorView) {
            isRegionSelected = getBoolean(R.styleable.RegionFilterIndicatorView_regionSelected, false)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val inset = strokePaint.strokeWidth / 2f
        val left = paddingLeft + inset
        val top = paddingTop + inset
        val right = width - paddingRight - inset
        val bottom = height - paddingBottom - inset

        if (!isRegionSelected || regionColors.isEmpty()) {
            fillPaint.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(left, top, right, bottom, fillPaint)
            canvas.drawRect(left, top, right, bottom, strokePaint)
            return
        }

        when (regionColors.size) {
            1 -> {
                fillPaint.color = regionColors[0]
                canvas.drawRect(left, top, right, bottom, fillPaint)
            }
            2 -> drawTwoColorSplit(canvas, left, top, right, bottom, regionColors[0], regionColors[1])
            3 -> drawHoennXSplit(canvas, left, top, right, bottom, regionColors[0], regionColors[1], regionColors[2])
            else -> drawTwoColorSplit(canvas, left, top, right, bottom, regionColors[0], regionColors[1])
        }
        canvas.drawRect(left, top, right, bottom, strokePaint)
    }

    private fun drawTwoColorSplit(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        firstColor: Int,
        secondColor: Int,
    ) {
        fillPaint.color = firstColor
        path.reset()
        path.moveTo(left, top)
        path.lineTo(right, top)
        path.lineTo(left, bottom)
        path.close()
        canvas.drawPath(path, fillPaint)

        fillPaint.color = secondColor
        path.reset()
        path.moveTo(right, top)
        path.lineTo(right, bottom)
        path.lineTo(left, bottom)
        path.close()
        canvas.drawPath(path, fillPaint)
    }

    /** Hoenn: X split — top red, left blue, right & bottom green. */
    private fun drawHoennXSplit(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        rubyRed: Int,
        sapphireBlue: Int,
        emeraldGreen: Int,
    ) {
        val cx = (left + right) / 2f
        val cy = (top + bottom) / 2f

        fillPaint.color = rubyRed
        path.reset()
        path.moveTo(left, top)
        path.lineTo(right, top)
        path.lineTo(cx, cy)
        path.close()
        canvas.drawPath(path, fillPaint)

        fillPaint.color = sapphireBlue
        path.reset()
        path.moveTo(left, top)
        path.lineTo(left, bottom)
        path.lineTo(cx, cy)
        path.close()
        canvas.drawPath(path, fillPaint)

        fillPaint.color = emeraldGreen
        path.reset()
        path.moveTo(right, top)
        path.lineTo(right, bottom)
        path.lineTo(cx, cy)
        path.close()
        canvas.drawPath(path, fillPaint)

        path.reset()
        path.moveTo(left, bottom)
        path.lineTo(right, bottom)
        path.lineTo(cx, cy)
        path.close()
        canvas.drawPath(path, fillPaint)
    }
}
