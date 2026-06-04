package com.garci.pokegarci

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FirstMenuActivity : BaseLocaleActivity() {

    private val viewModel: FirstMenuViewModel by viewModels()
    private lateinit var binding: ActivityFirstMenuBinding
    private var loadingStatusAnimationJob: Job? = null
    private var loadingPhraseIndex = 0
    private var loadingEllipsisCount = 1

    private val loadingPhrases: Array<String> by lazy {
        resources.getStringArray(R.array.loading_status_phrases)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirstMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.firstMenu.startGradientBackgroundAnimation()
        setupLoadingEllipsisSlot()

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
                launch {
                    viewModel.loadState.collect { state ->
                        when (state) {
                            FirstMenuLoadState.Idle -> {
                                stopLoadingStatusAnimation()
                                hideLoadingProgress()
                                hideLoadingStatus()
                                binding.firstMenuRetryButton.visibility = View.GONE
                                binding.pressToPlay.visibility = View.GONE
                                updateScreenInteraction(state)
                            }
                            FirstMenuLoadState.Loading -> {
                                showLoadingStatus()
                                binding.firstMenuProgressBar.visibility = View.VISIBLE
                                binding.firstMenuRetryButton.visibility = View.GONE
                                binding.pressToPlay.visibility = View.GONE
                                binding.firstMenuProgressBar.progress = viewModel.loadProgress.value
                                startLoadingStatusAnimation()
                                updateScreenInteraction(state)
                            }
                            FirstMenuLoadState.Ready -> {
                                stopLoadingStatusAnimation()
                                hideLoadingProgress()
                                hideLoadingStatus()
                                binding.firstMenuRetryButton.visibility = View.GONE
                                binding.pressToPlay.visibility = View.VISIBLE
                                updateScreenInteraction(state)
                            }
                            FirstMenuLoadState.Error -> {
                                stopLoadingStatusAnimation()
                                hideLoadingProgress()
                                showLoadError()
                                binding.firstMenuRetryButton.visibility = View.VISIBLE
                                binding.pressToPlay.visibility = View.GONE
                                updateScreenInteraction(state)
                            }
                        }
                    }
                }
                launch {
                    viewModel.loadProgress.collect { progress ->
                        if (viewModel.loadState.value != FirstMenuLoadState.Loading) return@collect
                        binding.firstMenuProgressBar.progress = progress
                    }
                }
            }
        }

        viewModel.ensurePokemonLoaded(pendingLanguageChange)
    }

    private fun startLoadingStatusAnimation() {
        stopLoadingStatusAnimation()
        if (loadingPhrases.isEmpty()) return

        loadingPhraseIndex = 0
        loadingEllipsisCount = 1
        updateLoadingStatusText()

        loadingStatusAnimationJob = lifecycleScope.launch {
            launch {
                while (isActive) {
                    delay(LOADING_PHRASE_INTERVAL_MS)
                    loadingPhraseIndex = (loadingPhraseIndex + 1) % loadingPhrases.size
                    updateLoadingStatusText()
                }
            }
            while (isActive) {
                delay(LOADING_ELLIPSIS_INTERVAL_MS)
                loadingEllipsisCount = if (loadingEllipsisCount >= 3) 1 else loadingEllipsisCount + 1
                updateLoadingStatusText()
            }
        }
    }

    private fun setupLoadingEllipsisSlot() {
        val ellipsisView = binding.firstMenuStatusEllipsis
        val fullDotsWidth = ellipsisView.paint.measureText(ELLIPSIS_FULL).toInt()
        ellipsisView.minWidth = fullDotsWidth
        ellipsisView.gravity = Gravity.START
    }

    private fun updateLoadingStatusText() {
        val phrase = loadingPhrases.getOrElse(loadingPhraseIndex) { return }
        binding.firstMenuStatusPhrase.text = phrase
        binding.firstMenuStatusEllipsis.text = ".".repeat(loadingEllipsisCount)
    }

    private fun showLoadingStatus() {
        binding.firstMenuLoadingStatusRow.visibility = View.VISIBLE
        binding.firstMenuErrorText.visibility = View.GONE
    }

    private fun hideLoadingStatus() {
        binding.firstMenuLoadingStatusRow.visibility = View.GONE
        binding.firstMenuErrorText.visibility = View.GONE
    }

    private fun showLoadError() {
        binding.firstMenuLoadingStatusRow.visibility = View.GONE
        binding.firstMenuErrorText.visibility = View.VISIBLE
        binding.firstMenuErrorText.text = getString(R.string.loadError)
    }

    private fun stopLoadingStatusAnimation() {
        loadingStatusAnimationJob?.cancel()
        loadingStatusAnimationJob = null
    }

    private fun hideLoadingProgress() {
        binding.firstMenuProgressBar.visibility = View.GONE
    }

    private fun updateScreenInteraction(state: FirstMenuLoadState) {
        val canEnterApp = state == FirstMenuLoadState.Ready
        binding.btnPlay.isEnabled = canEnterApp
        binding.btnPlay.isClickable = canEnterApp
        binding.firstMenuLoadingBlocker.visibility =
            if (state == FirstMenuLoadState.Loading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val LOADING_PHRASE_INTERVAL_MS = 5_000L
        private const val LOADING_ELLIPSIS_INTERVAL_MS = 500L
        private const val ELLIPSIS_FULL = "..."
    }
}
