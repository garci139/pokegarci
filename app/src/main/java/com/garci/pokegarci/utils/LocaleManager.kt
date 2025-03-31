package com.garci.pokegarci.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleManager {
    private const val PREFS_NAME = "app_prefs"
    private const val LANGUAGE_KEY = "language"

    // Establece el idioma usado en la app
    fun setLocale(activity: Activity, lang: String) {
        saveLanguage(activity, lang)
        activity.finish()
        activity.startActivity(activity.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // Guarda el idioma en SharedPreferences
    private fun saveLanguage(context: Context, lang: String) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_KEY, lang).apply()
    }

    // Obtiene el idioma guardado en SharedPreferences
    fun getLanguage(context: Context): String {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_KEY, Locale.getDefault().language) ?: "es" // Español por defecto
    }

    // Aplica el idioma guardado en SharedPreferences
    fun applyLanguage(context: Context): Context {
        val lang = getLanguage(context)
        val locale = Locale(lang)
        Locale.setDefault(locale)

        val resources: Resources = context.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
