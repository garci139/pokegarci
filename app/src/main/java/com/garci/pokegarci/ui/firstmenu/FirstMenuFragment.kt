package com.garci.pokegarci.ui.firstmenu

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.garci.pokegarci.R
import com.garci.pokegarci.databinding.ActivityFirstMenuBinding
import com.garci.pokegarci.presentation.firstmenu.FirstMenuLoadState
import com.garci.pokegarci.presentation.firstmenu.FirstMenuViewModel
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.util.getAppVersionName
import com.garci.pokegarci.util.playClickEmeraldSound
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class FirstMenuFragment : Fragment() {

    private val viewModel: FirstMenuViewModel by viewModels()
    private var _binding: ActivityFirstMenuBinding? = null
    private val binding get() = _binding!!
    private var loadingStatusAnimationJob: Job? = null
    private var loadingPhraseIndex = 0
    private var loadingEllipsisCount = 1

    private val loadingPhrases: Array<String> by lazy {
        resources.getStringArray(R.array.loading_status_phrases)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ActivityFirstMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appVersionText.text = getString(R.string.app_version_format, requireContext().getAppVersionName())
        setupLoadingEllipsisSlot()

        val pendingLanguageChange = requireContext()
            .getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
            .getString(AppConstants.OLD_LANGUAGE_KEY, null) != null

        binding.btnPlay.setOnClickListener {
            requireContext().vibrate()
            requireContext().playClickEmeraldSound()
            findNavController().navigate(R.id.action_firstMenu_to_mainMenu)
        }

        binding.firstMenuRetryButton.setOnClickListener {
            requireContext().vibrate()
            viewModel.retryLoad(pendingLanguageChange)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

    override fun onDestroyView() {
        stopLoadingStatusAnimation()
        super.onDestroyView()
        _binding = null
    }

    private fun startLoadingStatusAnimation() {
        stopLoadingStatusAnimation()
        if (loadingPhrases.isEmpty()) return

        loadingPhraseIndex = 0
        loadingEllipsisCount = 1
        updateLoadingStatusText()

        loadingStatusAnimationJob = viewLifecycleOwner.lifecycleScope.launch {
            launch {
                while (isActive) {
                    delay(LOADING_PHRASE_INTERVAL_MS.milliseconds)
                    loadingPhraseIndex = (loadingPhraseIndex + 1) % loadingPhrases.size
                    updateLoadingStatusText()
                }
            }
            while (isActive) {
                delay(LOADING_ELLIPSIS_INTERVAL_MS.milliseconds)
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