package com.garci.pokegarci.util

import android.app.Activity
import android.app.Application
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.garci.pokegarci.R

object AppFont {

    private var typeface: Typeface? = null

    fun install(application: Application) {
        application.registerActivityLifecycleCallbacks(AppFontActivityCallbacks())
    }

    fun get(context: Context): Typeface {
        typeface?.let { return it }
        val appContext = context.applicationContext
        val loaded = ResourcesCompat.getFont(appContext, R.font.pokemon_emerald)
        return (loaded ?: Typeface.DEFAULT).also { typeface = it }
    }

    fun applyTo(root: View) {
        applyToInternal(root)
        root.post { applyToInternal(root) }
    }

    fun scheduleReapply(root: View) {
        val observer = root.viewTreeObserver
        if (!observer.isAlive) return
        observer.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (root.viewTreeObserver.isAlive)
                    root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                applyToInternal(root)
            }
        })
    }

    private fun applyToInternal(root: View) {
        when (root) {
            is TextView -> root.typeface = get(root.context)
            is ViewGroup -> {
                for (i in 0 until root.childCount) {
                    applyToInternal(root.getChildAt(i))
                }
            }
        }
    }

    private class AppFontActivityCallbacks : Application.ActivityLifecycleCallbacks {
        private val fragmentCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                fragment: Fragment,
                view: View,
                savedInstanceState: Bundle?
            ) {
                applyTo(view)
                scheduleReapply(view)
            }
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity is FragmentActivity)
                activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                    fragmentCallbacks,
                    true
                )
        }

        override fun onActivityResumed(activity: Activity) {
            activity.window?.decorView?.let { decor ->
                applyTo(decor)
                scheduleReapply(decor)
            }
        }

        override fun onActivityStarted(activity: Activity) = Unit
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) = Unit
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
