package com.garci.pokegarci.presentation.firstmenu

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.utils.LocaleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirstMenuViewModel @Inject constructor(
    private val loadPokemonUseCase: LoadPokemonUseCase,
    private val repository: PokemonRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    val isDataLoaded: StateFlow<Boolean> = repository.isDataLoaded.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = false,
    )

    private val _loadState = MutableStateFlow<FirstMenuLoadState>(FirstMenuLoadState.Idle)
    val loadState: StateFlow<FirstMenuLoadState> = _loadState.asStateFlow()

    fun ensurePokemonLoaded(pendingLanguageChange: Boolean = false) {
        if (repository.getPokemonList().isNotEmpty()) {
            _loadState.value = FirstMenuLoadState.Ready
            clearPendingLanguageChange(pendingLanguageChange)
            return
        }

        viewModelScope.launch {
            _loadState.value = FirstMenuLoadState.Loading
            val result = loadPokemonUseCase(
                AppConstants.POKEMON_LIMIT,
                LocaleManager.getLanguage(context),
            )
            if (result.isSuccess) {
                _loadState.value = FirstMenuLoadState.Ready
                clearPendingLanguageChange(pendingLanguageChange)
            } else {
                _loadState.value = FirstMenuLoadState.Error
            }
        }
    }

    fun retryLoad(pendingLanguageChange: Boolean = false) {
        ensurePokemonLoaded(pendingLanguageChange)
    }

    private fun clearPendingLanguageChange(pendingLanguageChange: Boolean) {
        if (!pendingLanguageChange) return
        context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(AppConstants.OLD_LANGUAGE_KEY)
            .apply()
    }
}
