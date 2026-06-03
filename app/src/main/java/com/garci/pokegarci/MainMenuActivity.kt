package com.garci.pokegarci

import android.content.Intent
import android.os.Bundle
import com.garci.pokegarci.databinding.ActivityMainMenuBinding
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.startGradientBackgroundAnimation
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainMenuActivity : BaseLocaleActivity() {

    private lateinit var binding: ActivityMainMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainMenu.startGradientBackgroundAnimation()

        binding.function1.setOnClickListener {
            vibrate()
            startActivity(Intent(this, PokedexActivity::class.java))
        }

        binding.function2.setOnClickListener {
            vibrate()
            startActivity(Intent(this, SizeActivity::class.java))
        }

        binding.function3.setOnClickListener {
            vibrate()
            startActivity(Intent(this, GuessActivity::class.java))
        }

        binding.function4.setOnClickListener {
            vibrate()
            startActivity(Intent(this, LanguageActivity::class.java))
        }
    }
}
