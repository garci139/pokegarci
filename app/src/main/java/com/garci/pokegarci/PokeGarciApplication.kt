package com.garci.pokegarci

import android.app.Application
import com.garci.pokegarci.ui.AmbientAnimationLifecycle
import com.garci.pokegarci.util.AppFont
import com.garci.pokegarci.util.ClickSound
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PokeGarciApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AmbientAnimationLifecycle.init()
        ClickSound.init(this)
        AppFont.install(this)
    }
}
