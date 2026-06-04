package com.garci.pokegarci.ui

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner

object AmbientAnimationLifecycle {

    var ambientRoot: PokemonAmbientBackgroundLayout? = null

    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                ambientRoot?.stopAmbientAnimation()
            }

            override fun onStart(owner: LifecycleOwner) {
                ambientRoot?.startAmbientAnimation()
            }
        })
    }
}
