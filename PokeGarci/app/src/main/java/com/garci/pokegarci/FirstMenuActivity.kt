package com.garci.pokegarci

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout

class FirstMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_menu)

        // Al iniciar la primera pantalla
        val btnPlay = findViewById<Button>(R.id.btnPlay)

        // Inicio de fondo animado
        val firstMenu = findViewById<ConstraintLayout>(R.id.firstMenu)
        val gradientAnimationFirst:AnimationDrawable= firstMenu.background as AnimationDrawable
        gradientAnimationFirst.setEnterFadeDuration(1500)
        gradientAnimationFirst.setExitFadeDuration(3000)
        gradientAnimationFirst.start()

        // Acción de tocar pantalla
        btnPlay.setOnClickListener {
            val toMainMenu = Intent(this, MainMenuActivity::class.java)
            toMainMenu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(toMainMenu)
//            Detener esta actividad y su fondo
//            gradientAnimationFirst.stop()
//            finish()
        }
    }
}