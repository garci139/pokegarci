package com.garci.pokegarci.util

import android.app.Activity
import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.widget.SearchView

object GuessKeyboardDismiss {

    fun handle(activity: Activity, event: MotionEvent?) {
        if (event?.action != MotionEvent.ACTION_DOWN) return
        val view = activity.currentFocus ?: return
        if (view is SearchView || view is EditText) return
        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }
}