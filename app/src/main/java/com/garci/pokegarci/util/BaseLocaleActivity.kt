package com.garci.pokegarci.util

import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import com.garci.pokegarci.utils.LocaleManager

abstract class BaseLocaleActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }
}
