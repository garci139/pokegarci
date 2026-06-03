package com.garci.pokegarci

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.garci.pokegarci.databinding.ActivityFirstMenuBinding
import com.garci.pokegarci.presentation.firstmenu.FirstMenuLoadState
import com.garci.pokegarci.presentation.firstmenu.FirstMenuViewModel
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.startGradientBackgroundAnimation
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirstMenuActivity : BaseLocaleActivity() {

    private val viewModel: FirstMenuViewModel by viewModels()
    private lateinit var binding: ActivityFirstMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.firstMenu.startGradientBackgroundAnimation()

        val pendingLanguageChange = getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(AppConstants.OLD_LANGUAGE_KEY, null) != null

        binding.btnPlay.setOnClickListener {
            vibrate()
            startActivity(
                Intent(this, MainMenuActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
            )
        }

        binding.firstMenuRetryButton.setOnClickListener {
            vibrate()
            viewModel.retryLoad(pendingLanguageChange)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loadState.collect { state ->
                    when (state) {
                        FirstMenuLoadState.Idle -> {
                            binding.firstMenuProgressBar.visibility = View.GONE
                            binding.firstMenuStatusText.visibility = View.GONE
                            binding.firstMenuRetryButton.visibility = View.GONE
                            binding.pressToPlay.visibility = View.VISIBLE
                        }
                        FirstMenuLoadState.Loading -> {
                            binding.firstMenuProgressBar.visibility = View.VISIBLE
                            binding.firstMenuStatusText.visibility = View.VISIBLE
                            binding.firstMenuStatusText.text = getString(R.string.loadingPokemon)
                            binding.firstMenuRetryButton.visibility = View.GONE
                            binding.pressToPlay.visibility = View.GONE
                        }
                        FirstMenuLoadState.Ready -> {
                            binding.firstMenuProgressBar.visibility = View.GONE
                            binding.firstMenuStatusText.visibility = View.GONE
                            binding.firstMenuRetryButton.visibility = View.GONE
                            binding.pressToPlay.visibility = View.VISIBLE
                        }
                        FirstMenuLoadState.Error -> {
                            binding.firstMenuProgressBar.visibility = View.GONE
                            binding.firstMenuStatusText.visibility = View.VISIBLE
                            binding.firstMenuStatusText.text = getString(R.string.loadError)
                            binding.firstMenuRetryButton.visibility = View.VISIBLE
                            binding.pressToPlay.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.ensurePokemonLoaded(pendingLanguageChange)
    }
}
