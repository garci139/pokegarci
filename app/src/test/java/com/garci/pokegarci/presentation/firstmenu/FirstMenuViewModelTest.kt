package com.garci.pokegarci.presentation.firstmenu

import android.content.Context
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.domain.repository.PokemonRepository
import com.garci.pokegarci.domain.usecase.LoadPokemonUseCase
import com.garci.pokegarci.util.AppConstants
import com.garci.pokegarci.utils.LocaleManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FirstMenuViewModelTest {

    private val loadPokemonUseCase = mockk<LoadPokemonUseCase>()
    private val repository = mockk<PokemonRepository>()
    private val context = mockk<Context>(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        mockkObject(LocaleManager)
        every { LocaleManager.getLanguage(context) } returns "es"
        every { repository.isDataLoaded } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ensurePokemonLoaded sets Ready when cache already has data`() = runTest {
        every { repository.getPokemonList() } returns listOf(mockk<Pokemon>())

        val viewModel = FirstMenuViewModel(loadPokemonUseCase, repository, context)
        viewModel.ensurePokemonLoaded(pendingLanguageChange = false)
        advanceUntilIdle()

        assertEquals(FirstMenuLoadState.Ready, viewModel.loadState.value)
    }

    @Test
    fun `ensurePokemonLoaded sets Ready after successful network load`() = runTest {
        every { repository.getPokemonList() } returns emptyList()
        coEvery { loadPokemonUseCase(AppConstants.POKEMON_LIMIT, "es") } returns Result.success(Unit)

        val viewModel = FirstMenuViewModel(loadPokemonUseCase, repository, context)
        viewModel.ensurePokemonLoaded(pendingLanguageChange = false)
        advanceUntilIdle()

        assertEquals(FirstMenuLoadState.Ready, viewModel.loadState.value)
    }

    @Test
    fun `ensurePokemonLoaded sets Error when load fails`() = runTest {
        every { repository.getPokemonList() } returns emptyList()
        coEvery { loadPokemonUseCase(AppConstants.POKEMON_LIMIT, "es") } returns Result.failure(IllegalStateException("network"))

        val viewModel = FirstMenuViewModel(loadPokemonUseCase, repository, context)
        viewModel.ensurePokemonLoaded(pendingLanguageChange = false)
        advanceUntilIdle()

        assertEquals(FirstMenuLoadState.Error, viewModel.loadState.value)
    }
}
