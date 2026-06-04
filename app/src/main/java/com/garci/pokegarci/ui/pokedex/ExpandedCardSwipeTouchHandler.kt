package com.garci.pokegarci.ui.pokedex

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

internal class ExpandedCardSwipeTouchHandler(context: Context) {

    var onSwipe: ((direction: Int) -> Unit)? = null
    var swipeEnabled: () -> Boolean = { true }

    private var downRawX = 0f
    private var downRawY = 0f
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop
    private val swipeThresholdPx get() = touchSlop * 2

    fun onDown(event: MotionEvent) {
        downRawX = event.rawX
        downRawY = event.rawY
    }

    fun shouldInterceptSwipe(event: MotionEvent): Boolean {
        if (!swipeEnabled()) return false
        val dx = event.rawX - downRawX
        val dy = event.rawY - downRawY
        return abs(dx) >= swipeThresholdPx && abs(dx) > abs(dy)
    }

    fun onRelease(event: MotionEvent): Boolean {
        if (!swipeEnabled()) return false
        if (event.actionMasked != MotionEvent.ACTION_UP &&
            event.actionMasked != MotionEvent.ACTION_CANCEL
        ) {
            return false
        }
        val dx = event.rawX - downRawX
        val dy = event.rawY - downRawY
        if (abs(dx) > abs(dy) && abs(dx) >= swipeThresholdPx) {
            onSwipe?.invoke(if (dx < 0f) 1 else -1)
            return true
        }
        return false
    }
}
