package com.garci.pokegarci.util

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.garci.pokegarci.presentation.common.PokemonDataUiState
import com.garci.pokegarci.utils.vibrate
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object DataLoadingUi {

    data class Views(
        val progressBar: ProgressBar,
        val errorText: TextView,
        val retryButton: MaterialButton,
        val contentViews: List<View> = emptyList(),
        val extraProgressBars: List<ProgressBar> = emptyList(),
    )

    fun bind(
        lifecycleOwner: LifecycleOwner,
        dataUiState: StateFlow<PokemonDataUiState>,
        views: Views,
        onRetry: () -> Unit,
        onLoaded: () -> Unit = {},
    ) {
        views.retryButton.setOnClickListener {
            (lifecycleOwner as? android.content.Context)?.vibrate()
            onRetry()
        }

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                dataUiState.collect { state ->
                    when (state) {
                        PokemonDataUiState.Loading -> {
                            views.progressBar.visibility = View.VISIBLE
                            views.extraProgressBars.forEach { it.visibility = View.VISIBLE }
                            views.errorText.visibility = View.GONE
                            views.retryButton.visibility = View.GONE
                            views.contentViews.forEach { it.visibility = View.INVISIBLE }
                        }
                        PokemonDataUiState.Loaded -> {
                            views.progressBar.visibility = View.GONE
                            views.extraProgressBars.forEach { it.visibility = View.GONE }
                            views.errorText.visibility = View.GONE
                            views.retryButton.visibility = View.GONE
                            views.contentViews.forEach { it.visibility = View.VISIBLE }
                            onLoaded()
                        }
                        PokemonDataUiState.Error -> {
                            views.progressBar.visibility = View.GONE
                            views.extraProgressBars.forEach { it.visibility = View.GONE }
                            views.errorText.visibility = View.VISIBLE
                            views.retryButton.visibility = View.VISIBLE
                            views.contentViews.forEach { it.visibility = View.INVISIBLE }
                        }
                    }
                }
            }
        }
    }
}
