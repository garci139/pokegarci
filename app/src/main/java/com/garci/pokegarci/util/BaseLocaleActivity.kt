package com.garci.pokegarci.util

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.garci.pokegarci.R
import com.garci.pokegarci.ui.AmbientAnimationLifecycle
import com.garci.pokegarci.ui.PokemonAmbientBackgroundLayout
import com.garci.pokegarci.utils.LocaleManager

abstract class BaseLocaleActivity : AppCompatActivity() {

    private var ambientRoot: PokemonAmbientBackgroundLayout? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

    override fun setContentView(layoutResID: Int) {
        setContentView(layoutInflater.inflate(layoutResID, null, false))
    }

    override fun setContentView(view: View?) {
        if (view == null) {
            super.setContentView(null)
            return
        }
        if (view is PokemonAmbientBackgroundLayout) {
            super.setContentView(view)
            bindAmbientRoot(view)
            return
        }
        val wrapper = layoutInflater.inflate(R.layout.layout_ambient_root, null, false) as PokemonAmbientBackgroundLayout
        val contentHost = wrapper.findViewById<FrameLayout>(R.id.ambient_content)
        contentHost.addView(
            view,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        super.setContentView(wrapper)
        bindAmbientRoot(wrapper)
    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        if (view == null) {
            super.setContentView(null)
            return
        }
        if (view is PokemonAmbientBackgroundLayout) {
            super.setContentView(view, params)
            bindAmbientRoot(view)
            return
        }
        val wrapper = layoutInflater.inflate(R.layout.layout_ambient_root, null, false) as PokemonAmbientBackgroundLayout
        val contentHost = wrapper.findViewById<FrameLayout>(R.id.ambient_content)
        contentHost.addView(
            view,
            params ?: FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        super.setContentView(wrapper)
        bindAmbientRoot(wrapper)
    }

    override fun onResume() {
        super.onResume()
        ambientRoot?.startAmbientAnimation()
        window?.decorView?.let { decor ->
            AppFont.applyTo(decor)
            AppFont.scheduleReapply(decor)
        }
    }

    private fun bindAmbientRoot(root: PokemonAmbientBackgroundLayout) {
        ambientRoot = root
        AmbientAnimationLifecycle.ambientRoot = root
        root.startAmbientAnimation()
    }
}
