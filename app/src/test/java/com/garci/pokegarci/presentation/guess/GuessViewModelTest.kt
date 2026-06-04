package com.garci.pokegarci.presentation.guess

import android.content.Context
import com.garci.pokegarci.domain.guess.GuessOutcome
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.GetPokemonListUseCase
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.util.GamePreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GuessViewModelTest {

    private val getPokemonListUseCase = mockk<GetPokemonListUseCase>()
    private val gamePreferences = mockk<GamePreferences>()
    private val repository = mockk<PokemonRepository>()
    private val loadPokemonUseCase = mockk<LoadPokemonUseCase>()
    private val context = mockk<Context>(relaxed = true)

    private val pikachu = samplePokemon(25, "Pikachu")
    private val bulbasaur = samplePokemon(1, "Bulbasaur")

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { repository.isDataLoaded } returns MutableStateFlow(true)
        every { repository.loadFailed } returns MutableStateFlow(false)
        every { gamePreferences.getHighscore() } returns 10
        every { gamePreferences.saveHighscoreIfRecord(any()) } answers { firstArg() }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startGame uses refreshed pokemon list`() {
        every { getPokemonListUseCase() } returns listOf(pikachu, bulbasaur)

        val viewModel = createViewModel()
        viewModel.refreshPokemonList()
        val solution = viewModel.startGame()

        assertTrue(solution.id == pikachu.id || solution.id == bulbasaur.id)
        assertEquals(2, viewModel.totalPokemon)
    }

    @Test
    fun `submitGuess correct updates score and highscore`() {
        every { getPokemonListUseCase() } returns listOf(pikachu)

        val viewModel = createViewModel()
        viewModel.refreshPokemonList()
        val solution = viewModel.startGame()

        val outcome = viewModel.submitGuess(solution)

        assertTrue(outcome is GuessOutcome.Correct)
        assertEquals(6, viewModel.score)
        assertEquals(1, viewModel.guessedCount)
        verify { gamePreferences.saveHighscoreIfRecord(6) }
    }

    @Test
    fun `submitGuess wrong reduces remaining lives`() {
        every { getPokemonListUseCase() } returns listOf(pikachu)

        val viewModel = createViewModel()
        viewModel.refreshPokemonList()
        viewModel.startGame()

        val outcome = viewModel.submitGuess(bulbasaur)

        assertTrue(outcome is GuessOutcome.Wrong)
        outcome as GuessOutcome.Wrong
        assertEquals(1, outcome.hintLevel)
        assertEquals("Bulbasaur", outcome.wrongName)
    }

    private fun createViewModel(): GuessViewModel {
        return GuessViewModel(
            getPokemonListUseCase = getPokemonListUseCase,
            gamePreferences = gamePreferences,
            repository = repository,
            loadPokemonUseCase = loadPokemonUseCase,
            context = context,
        )
    }

    private fun samplePokemon(id: Int, name: String): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            imageUrl = "",
            type1 = "normal",
            type2 = null,
            description = "",
            hp = 1,
            attack = 1,
            defense = 1,
            specialAttack = 1,
            specialDefense = 1,
            speed = 1,
            height = 1,
            weight = 1,
            abilities = listOf(Ability("test", "Test")),
        )
    }
}
