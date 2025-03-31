package com.garci.pokegarci

import android.content.Context
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val mainMenu = findViewById<ConstraintLayout>(R.id.mainMenu)
        val btnFunction1 = findViewById<CardView>(R.id.function1)
        val btnFunction2 = findViewById<CardView>(R.id.function2)
        val btnFunction3 = findViewById<CardView>(R.id.function3)
        val btnFunction4 = findViewById<CardView>(R.id.function4)

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = mainMenu.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()

        // FUNCION 1: POKEDEX
        btnFunction1.setOnClickListener {
            vibrate()
            val toPokedex = Intent(this,PokedexActivity::class.java)
            startActivity(toPokedex)
        }

        // FUNCION 2: TAMAÑO
        btnFunction2.setOnClickListener {
            vibrate()
            val toSize = Intent(this,SizeActivity::class.java)
            startActivity(toSize)
        }

        // FUNCION 3: ADIVINAR
        btnFunction3.setOnClickListener {
            vibrate()
            val toGuess = Intent(this,GuessActivity::class.java)
            startActivity(toGuess)
        }

        // FUNCION 4: AJUSTES
        btnFunction4.setOnClickListener {
            vibrate()
            val toSettings = Intent(this,LanguageActivity::class.java)
            startActivity(toSettings)
        }
    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

}