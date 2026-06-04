package com.garci.pokegarci.ui.pokedex

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import com.google.android.material.card.MaterialCardView

/**
 * Expanded card shell. While the card is open, [dispatchTouchEvent] must return true on
 * DOWN/MOVE even when non-clickable children (TextViews, tables) reject the stream; otherwise
 * [disableRecyclerView] below in z-order steals MOVE and swipe only works on clickable areas
 * (e.g. [expandedSubView] in the image box).
 */
class ExpandedPokemonCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val swipeHandler = ExpandedCardSwipeTouchHandler(context)
    private var interceptingSwipe = false

    var onSwipe: ((direction: Int) -> Unit)?
        get() = swipeHandler.onSwipe
        set(value) {
            swipeHandler.onSwipe = value
        }

    var swipeEnabled: () -> Boolean
        get() = swipeHandler.swipeEnabled
        set(value) {
            swipeHandler.swipeEnabled = value
        }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (!swipeHandler.swipeEnabled()) {
            return super.dispatchTouchEvent(event)
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                interceptingSwipe = false
                swipeHandler.onDown(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!interceptingSwipe && swipeHandler.shouldInterceptSwipe(event)) {
                    interceptingSwipe = true
                    parent?.requestDisallowInterceptTouchEvent(true)
                    val cancel = MotionEvent.obtain(event)
                    cancel.action = MotionEvent.ACTION_CANCEL
                    super.dispatchTouchEvent(cancel)
                    cancel.recycle()
                    return onTouchEvent(event)
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (interceptingSwipe) {
                    swipeHandler.onRelease(event)
                    interceptingSwipe = false
                    return true
                }
            }
        }

        if (interceptingSwipe) {
            if (event.actionMasked == MotionEvent.ACTION_UP ||
                event.actionMasked == MotionEvent.ACTION_CANCEL
            ) {
                swipeHandler.onRelease(event)
                interceptingSwipe = false
            }
            return true
        }

        val childHandled = super.dispatchTouchEvent(event)
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> true
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val released = swipeHandler.onRelease(event)
                childHandled || released
            }
            else -> childHandled
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (interceptingSwipe) {
            when (event.actionMasked) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    swipeHandler.onRelease(event)
                    interceptingSwipe = false
                    return true
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }
}
