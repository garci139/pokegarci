package com.garci.pokegarci.util

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.garci.pokegarci.utils.LocaleManager

abstract class BaseLocaleActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }
}
