package com.garci.pokegarci

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.garci.pokegarci.databinding.ActivityLanguageBinding
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.startGradientBackgroundAnimation
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseLocaleActivity() {

    private lateinit var binding: ActivityLanguageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.languageLayout.startGradientBackgroundAnimation()

        binding.btnSpanish.setOnClickListener {
            vibrate()
            changeLanguage("es")
        }

        binding.btnEnglish.setOnClickListener {
            vibrate()
            changeLanguage("en")
        }
    }

    private fun changeLanguage(language: String) {
        val prefs = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
        val oldLanguage = LocaleManager.getLanguage(this)
        prefs.edit().putString(AppConstants.OLD_LANGUAGE_KEY, oldLanguage).apply()
        LocaleManager.saveLanguage(this, language)

        startActivity(
            Intent(this, FirstMenuActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
        )
        finish()
    }
}
