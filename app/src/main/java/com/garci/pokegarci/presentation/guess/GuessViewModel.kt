package com.garci.pokegarci.presentation.guess

import android.content.Context
import com.garci.pokegarci.domain.guess.GuessGameSession
import com.garci.pokegarci.domain.guess.GuessOutcome
import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.GetPokemonListUseCase
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.presentation.common.PokemonDataViewModel
import com.garci.pokegarci.util.GamePreferences
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class GuessViewModel @Inject constructor(
    private val getPokemonListUseCase: GetPokemonListUseCase,
    private val gamePreferences: GamePreferences,
    repository: PokemonRepository,
    loadPokemonUseCase: LoadPokemonUseCase,
    @ApplicationContext context: Context,
) : PokemonDataViewModel(repository, loadPokemonUseCase, context) {

    private val _pokemonList = MutableStateFlow<List<Pokemon>>(emptyList())
    val pokemonList: StateFlow<List<Pokemon>> = _pokemonList.asStateFlow()

    private val _selectedGenerations = MutableStateFlow(PokemonGeneration.ALL)
    val selectedGenerations: StateFlow<Set<PokemonGeneration>> = _selectedGenerations.asStateFlow()

    val canStartGame: StateFlow<Boolean> = _selectedGenerations
        .map { it.isNotEmpty() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = true,
        )

    private val _highscore = MutableStateFlow(gamePreferences.getHighscore())
    val highscore: StateFlow<Int> = _highscore.asStateFlow()

    private val gameSession = GuessGameSession()
    private var allPokemon: List<Pokemon> = emptyList()

    val guessedCount: Int get() = gameSession.guessedCount
    val score: Int get() = gameSession.score
    val totalPokemon: Int get() = gameSession.totalPokemon
    val currentSolution: Pokemon? get() = gameSession.currentSolution

    fun refreshPokemonList() {
        allPokemon = getPokemonListUseCase()
        applyGenerationFilter()
    }

    fun setGenerationSelected(generation: PokemonGeneration, selected: Boolean) {
        _selectedGenerations.update { current ->
            val updated = current.toMutableSet()
            if (selected) {
                updated.add(generation)
            } else {
                updated.remove(generation)
            }
            updated
        }
        applyGenerationFilter()
    }

    fun startGame(): Pokemon {
        gameSession.start(_pokemonList.value)
        return requireNotNull(gameSession.currentSolution)
    }

    fun submitGuess(guess: Pokemon): GuessOutcome {
        val outcome = gameSession.submitGuess(guess)
        if (outcome is GuessOutcome.Correct) {
            _highscore.value = gamePreferences.saveHighscoreIfRecord(gameSession.score)
        }
        return outcome
    }

    private fun applyGenerationFilter() {
        _pokemonList.value = PokemonGeneration.filterPokemon(
            allPokemon,
            _selectedGenerations.value,
        )
    }
}
