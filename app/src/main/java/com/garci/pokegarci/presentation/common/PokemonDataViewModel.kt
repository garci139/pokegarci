package com.garci.pokegarci.presentation.common

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.utils.LocaleManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class PokemonDataViewModel(
    repository: PokemonRepository,
    private val loadPokemonUseCase: LoadPokemonUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val dataUiState: StateFlow<PokemonDataUiState> = combine(
        repository.isDataLoaded,
        repository.loadFailed,
    ) { isLoaded, loadFailed ->
        when {
            isLoaded -> PokemonDataUiState.Loaded
            loadFailed -> PokemonDataUiState.Error
            else -> PokemonDataUiState.Loading
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PokemonDataUiState.Loading,
    )

    fun retryLoad() {
        viewModelScope.launch {
            loadPokemonUseCase(AppConstants.POKEMON_LIMIT, LocaleManager.getLanguage(context))
        }
    }
}
