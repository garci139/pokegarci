package com.garci.pokegarci

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.garci.pokegarci.util.AppFont
import com.garci.pokegarci.util.ClickSound
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PokeGarciApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ClickSound.init(this)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                activity.window?.decorView?.let { AppFont.applyTo(it) }
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}
