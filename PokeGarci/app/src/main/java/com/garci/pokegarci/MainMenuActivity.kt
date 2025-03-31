package com.garci.pokegarci

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        val mainMenu = findViewById<ConstraintLayout>(R.id.mainMenu)
        val btnFunction1 = findViewById<CardView>(R.id.function1)
//        val btnFunction2 = findViewById<CardView>(R.id.function2)
//        val btnFunction3 = findViewById<CardView>(R.id.function3)
//        val btnFunction4 = findViewById<CardView>(R.id.function4)
        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = mainMenu.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()
        // FUNCION 1: POKEDEX
        btnFunction1.setOnClickListener {
            val toPokedex = Intent(this,PokedexActivity::class.java)
            startActivity(toPokedex)
        }
//        // FUNCION 2: EQUIPO
//        btnFunction2.setOnClickListener {
//            val toTeam = Intent(this,PokedexActivity::class.java)
//            startActivity(toTeam)
//        }
//        // FUNCION 3: NOTICIAS
//        btnFunction3.setOnClickListener {
//            val toNews = Intent(this,PokedexActivity::class.java)
//            startActivity(toNews)
//        }
//        // FUNCION 4: AJUSTES
//        btnFunction4.setOnClickListener {
//            val toSettings = Intent(this,PokedexActivity::class.java)
//            startActivity(toSettings)
//        }
    }
}