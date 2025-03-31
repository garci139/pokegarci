package com.garci.pokegarci

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate

class LanguageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)

        val languageLayout = findViewById<ConstraintLayout>(R.id.languageLayout)

        // Vincula botones
        val btnSpanish = findViewById<CardView>(R.id.btnSpanish)
        val btnEnglish = findViewById<CardView>(R.id.btnEnglish)

        btnSpanish.setOnClickListener {
            vibrate()
            changeLanguage("es")
            restartApp()
        }

        btnEnglish.setOnClickListener {
            vibrate()
            changeLanguage("en")
            restartApp()
        }

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = languageLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()
    }

    // Reiniciar la app para cambiar idioma
    private fun restartApp() {
        val intent = Intent(this, FirstMenuActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // Funcion para actualizar las descripciones al cambiar el idioma
    // Esto se hace porque, al cambiar el idioma, las descripciones no se actualizaban, ya que
    // los datos ya se habian cargado al principio con el idioma inicial
    private fun changeLanguage(language: String) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val oldLanguage = LocaleManager.getLanguage(this)
        prefs.edit().putString("OLD_LANGUAGE", oldLanguage).apply()  // Guardar idioma actual en SharedPreferences
        Log.d("Idioma", "Antiguo idioma guardado en SharedPreferences: $oldLanguage")
        LocaleManager.setLocale(this, language)  // Aplicar idioma nuevo con LocaleManager
        Log.d("Idioma", "Nuevo idioma aplicado con LocaleManager: $language")
        restartApp()
    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }
}
