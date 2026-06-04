package com.garci.pokegarci

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.core.animation.doOnEnd
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.garci.pokegarci.databinding.ActivityGuessBinding
import com.garci.pokegarci.domain.guess.GuessOutcome
import com.garci.pokegarci.domain.guess.PokemonGeneration
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.databinding.ItemGuessGenFilterBinding
import com.garci.pokegarci.presentation.guess.GuessViewModel
import com.garci.pokegarci.domain.model.abilitiesDisplayText
import com.garci.pokegarci.ui.adapter.PokemonGuessAdapter
import com.garci.pokegarci.util.BaseLocaleActivity
import com.garci.pokegarci.util.DataLoadingUi
import com.garci.pokegarci.util.SearchViewUtils
import com.garci.pokegarci.util.startGradientBackgroundAnimation
import com.garci.pokegarci.util.typeIconMap
import com.garci.pokegarci.utils.vibrate
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GuessActivity : BaseLocaleActivity() {

    private val viewModel: GuessViewModel by viewModels()
    private lateinit var binding: ActivityGuessBinding

    private val handler = Handler(Looper.getMainLooper())

    private lateinit var pokemonList: List<Pokemon>
    private lateinit var adapter: PokemonGuessAdapter

    private val genFilterSlots: List<GenFilterSlot> by lazy {
        with(binding.guessGenFilterInclude) {
            listOf(
                GenFilterSlot(PokemonGeneration.GEN_I, genFilter1),
                GenFilterSlot(PokemonGeneration.GEN_II, genFilter2),
                GenFilterSlot(PokemonGeneration.GEN_III, genFilter3),
                GenFilterSlot(PokemonGeneration.GEN_IV, genFilter4),
                GenFilterSlot(PokemonGeneration.GEN_V, genFilter5),
                GenFilterSlot(PokemonGeneration.GEN_VI, genFilter6),
                GenFilterSlot(PokemonGeneration.GEN_VII, genFilter7),
                GenFilterSlot(PokemonGeneration.GEN_VIII, genFilter8),
                GenFilterSlot(PokemonGeneration.GEN_IX, genFilter9),
            )
        }
    }

    private var syncingGenFilters = false
    private var gameInProgress = false

    private val fadeDuration: Long = 400
    private val longerFadeDuration: Long = 800
    private val ultraLongDuration: Long = 1200

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.solutionPokemonImage.visibility = View.GONE
        binding.solutionPokemonMask.visibility = View.GONE
        binding.guessHint1Type1.visibility = View.INVISIBLE
        binding.guessHint2Type2.visibility = View.INVISIBLE
        binding.guessHint4Ability.visibility = View.INVISIBLE
        binding.guessHint3Id.visibility = View.INVISIBLE
        binding.guessHint5FirstChar.visibility = View.INVISIBLE
        binding.currentScore.visibility = View.GONE
        binding.accumulatedScore.visibility = View.GONE
        applyLobbyState()

        pokemonList = emptyList()

        adapter = PokemonGuessAdapter(pokemonList) { selectedPokemon ->
            binding.guessPokemonSearchView.setQuery("", false)
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.guessRecyclerView.windowToken, 0)

            when (val outcome = viewModel.submitGuess(selectedPokemon)) {
                is GuessOutcome.Wrong -> {
                    vibrate(false)
                    showHint(outcome.wrongName, outcome.hintLevel)
                }
                is GuessOutcome.Defeated -> {
                    vibrate(false)
                    changeHeart(binding.heart6)
                    gameOver()
                }
                is GuessOutcome.Correct -> {
                    vibrate()
                    binding.guessBlockView.visibility = View.VISIBLE
                    updateScoreboards()
                    fadeAnimation(binding.solutionPokemonMask, binding.solutionPokemonImage, fadeDuration)
                    val hintsUsedBeforeCorrect = outcome.hintsUsedBeforeCorrect

                    handler.postDelayed({
                        resetHintsAndLifes(hintsUsedBeforeCorrect)
                        moveLeft(binding.solutionPokemonImage) {
                            if (outcome.completedAll) {
                                fadeOut(binding.guessScoreboards, fadeDuration)
                                fadeOut(binding.lifesLayout, fadeDuration)
                                binding.guessResultsTextScore.text = getString(R.string.resultsScore, viewModel.score)
                                binding.guessResultsPhrase.text = getString(R.string.winPhrase)
                                binding.guessResultsImage.setImageResource(R.drawable.fortnite_victory_image)
                                fadeIn(binding.guessPikachuWinImage, fadeDuration)
                                fadeIn(binding.guessResultsLayout, fadeDuration)

                                handler.postDelayed({
                                    fadeOut(binding.guessPikachuWinImage, fadeDuration)
                                    fadeOut(binding.guessResultsLayout, fadeDuration)
                                    resetHintsAndLifes(hintsUsedBeforeCorrect)
                                    applyLobbyState()
                                    fadeIn(binding.guessPlayButton, ultraLongDuration)
                                    updatePlayButtonState()
                                }, 5000)
                            } else {
                                viewModel.currentSolution?.let { loadSolutionData(it) }
                                enterSolutionPokemonFromRight()
                            }
                        }
                    }, 1000)
                }
            }
        }
        binding.guessRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.guessRecyclerView.adapter = adapter

        binding.guessLayout.startGradientBackgroundAnimation()

        binding.guessPokemonSearchView.queryHint = getString(R.string.name)
        SearchViewUtils.applyDefaultStyle(binding.guessPokemonSearchView)
        binding.guessPokemonSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!gameInProgress) return false
                updateSearchResults()
                return true
            }
        })

        val guessCloseButton = binding.guessPokemonSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        guessCloseButton.setOnClickListener {
            if (!gameInProgress) return@setOnClickListener
            binding.guessPokemonSearchView.setQuery("", false)
            hideSearchResults()
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.guessPokemonSearchView.windowToken, 0)
        }

        setupGenFilters()

        binding.guessPlayButton.setOnClickListener {
            if (!viewModel.canStartGame.value) return@setOnClickListener
            vibrate()
            binding.guessBlockView.visibility = View.GONE
            startPlay()
        }

        DataLoadingUi.bind(
            lifecycleOwner = this,
            dataUiState = viewModel.dataUiState,
            views = DataLoadingUi.Views(
                progressBar = binding.guessProgressBar,
                errorText = binding.dataErrorText,
                retryButton = binding.dataRetryButton,
                contentViews = listOf(
                    binding.guessPokemonSearchView,
                    binding.guessGenFilterInclude.root,
                    binding.guessPlayButton,
                ),
            ),
            onRetry = { viewModel.retryLoad() },
            onLoaded = {
                applyLobbyState()
                fadeAnimation(binding.guessProgressBar, binding.guessPlayButton, fadeDuration)
                viewModel.refreshPokemonList()
                updatePlayButtonState()
            },
        )

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.pokemonList.collect { loadedPokemon ->
                        pokemonList = loadedPokemon
                        updateGenFilterTotal(loadedPokemon.size)
                        updateSearchResults()
                    }
                }
                launch {
                    viewModel.highscore.collect { highscore ->
                        binding.guessHighscore.text = highscore.toString()
                    }
                }
                launch {
                    viewModel.selectedGenerations.collect { selected ->
                        syncGenFilterButtons(selected)
                    }
                }
                launch {
                    viewModel.canStartGame.collect {
                        updatePlayButtonState()
                    }
                }
            }
        }
    }

    private fun setupGenFilters() {
        genFilterSlots.forEach { slot ->
            slot.binding.genFilterLabel.text =
                getString(R.string.guess_gen_label, slot.generation.romanNumeral)
            slot.binding.root.setOnClickListener {
                if (syncingGenFilters) return@setOnClickListener
                val isCurrentlySelected = slot.generation in viewModel.selectedGenerations.value
                viewModel.setGenerationSelected(slot.generation, !isCurrentlySelected)
            }
        }
        syncGenFilterButtons(viewModel.selectedGenerations.value)
    }

    private fun syncGenFilterButtons(selected: Set<PokemonGeneration>) {
        syncingGenFilters = true
        genFilterSlots.forEach { slot ->
            slot.binding.genFilterIndicator.visibility =
                if (slot.generation in selected) View.VISIBLE else View.INVISIBLE
        }
        syncingGenFilters = false
    }

    private fun updateGenFilterTotal(count: Int) {
        binding.guessGenFilterInclude.guessGenFilterTotal.text =
            getString(R.string.guess_gen_filter_total, count)
    }

    private fun updateSearchResults() {
        if (!gameInProgress) {
            hideSearchResults()
            return
        }
        val query = binding.guessPokemonSearchView.query?.toString().orEmpty().trim()
        if (query.isEmpty()) {
            adapter.updateList(pokemonList)
            hideSearchResults()
            return
        }
        val filteredList = pokemonList.filter { it.name.lowercase().startsWith(query.lowercase()) }
        adapter.updateList(filteredList)
        if (filteredList.isEmpty()) {
            hideSearchResults()
            return
        }
        binding.guessNestedScrollView.visibility = View.VISIBLE
        binding.guessRecyclerView.visibility = View.VISIBLE
        binding.guessNestedScrollView.requestLayout()
    }

    private fun hideSearchResults() {
        binding.guessNestedScrollView.visibility = View.GONE
        binding.guessRecyclerView.visibility = View.GONE
    }

    private fun updatePlayButtonState() {
        val enabled = viewModel.canStartGame.value
        binding.guessPlayButton.isEnabled = enabled
        if (enabled) {
            binding.guessPlayButton.clearColorFilter()
            binding.guessPlayButton.imageAlpha = 255
        } else {
            binding.guessPlayButton.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY)
            binding.guessPlayButton.imageAlpha = 160
        }
    }

    private fun updateScoreboards() {
        binding.currentScore.text = viewModel.guessedCount.toString()
        binding.accumulatedScore.text = viewModel.score.toString()
    }

    private fun gameOver() {
        binding.guessBlockView.visibility = View.VISIBLE
        fadeAnimation(binding.solutionPokemonMask, binding.solutionPokemonImage, longerFadeDuration)
        handler.postDelayed({
            fadeOut(binding.guessScoreboards, fadeDuration)
            fadeOut(binding.lifesLayout, fadeDuration)
            moveLeft(binding.solutionPokemonImage) {
                binding.guessResultsTextScore.text = getString(R.string.resultsScore, viewModel.score)
                binding.guessResultsPhrase.text = getString(R.string.losePhrase)
                binding.guessResultsImage.setImageResource(R.drawable.game_over_image)
                fadeIn(binding.guessResultsLayout, longerFadeDuration)
                fadeIn(binding.sadOshawottImage, longerFadeDuration)
                handler.postDelayed({
                    fadeOut(binding.sadOshawottImage, longerFadeDuration)
                    fadeOut(binding.guessResultsLayout, longerFadeDuration)
                    resetHintsAndLifes(5)
                    Handler(Looper.getMainLooper()).postDelayed({
                        applyLobbyState()
                        fadeIn(binding.guessPlayButton, ultraLongDuration)
                        updatePlayButtonState()
                    }, 1000)
                }, 5000)
            }
        }, 3000)
    }

    private fun showHint(wrongPokemonName: String, tryCount: Int) {
        when (tryCount) {
            1 -> {
                fadeAnimation(binding.questionMark1, binding.guessHint1Type1, fadeDuration)
                changeHeart(binding.heart1)
                binding.guessTry1Text.text = wrongPokemonName
                binding.guessTry1Text.paintFlags = binding.guessTry1Text.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
            2 -> {
                fadeAnimation(binding.questionMark2, binding.guessHint2Type2, fadeDuration)
                changeHeart(binding.heart2)
                binding.guessTry2Text.text = wrongPokemonName
                binding.guessTry2Text.paintFlags = binding.guessTry2Text.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
            3 -> {
                fadeAnimation(binding.questionMark3, binding.guessHint3Id, fadeDuration)
                changeHeart(binding.heart3)
                binding.guessTry3Text.text = wrongPokemonName
                binding.guessTry3Text.paintFlags = binding.guessTry3Text.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
            4 -> {
                fadeAnimation(binding.questionMark4, binding.guessHint4Ability, fadeDuration)
                changeHeart(binding.heart4)
                binding.guessTry4Text.text = wrongPokemonName
                binding.guessTry4Text.paintFlags = binding.guessTry4Text.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
            5 -> {
                fadeAnimation(binding.questionMark5, binding.guessHint5FirstChar, fadeDuration)
                changeHeart(binding.heart5)
                binding.guessTry5Text.text = wrongPokemonName
                binding.guessTry5Text.paintFlags = binding.guessTry5Text.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startPlay() {
        gameInProgress = true
        val solution = viewModel.startGame()
        updateScoreboards()
        setSearchInteractionEnabled(true)
        fadeIn(binding.guessScoreboards, fadeDuration)
        fadeIn(binding.hintsBox, fadeDuration)
        resetLifesImages()
        fadeIn(binding.lifesLayout, fadeDuration)

        binding.guessPlayButton.isEnabled = false
        resetHintsAndLifes(0)
        loadSolutionData(solution)
        fadeOut(binding.guessPlayButton, fadeDuration)
        fadeOut(binding.guessGenFilterInclude.root, fadeDuration)
        enterSolutionPokemonFromRight()

        binding.currentScore.visibility = View.VISIBLE
        binding.accumulatedScore.visibility = View.VISIBLE
    }

    private fun applyLobbyState() {
        gameInProgress = false
        binding.guessBlockView.visibility = View.GONE
        hideSearchResults()
        binding.guessPokemonSearchView.setQuery("", false)
        setSearchInteractionEnabled(false)
        binding.guessPokemonSearchView.clearFocus()
        binding.hintsBox.visibility = View.GONE
        binding.guessScoreboards.visibility = View.INVISIBLE
        binding.lifesLayout.visibility = View.INVISIBLE
        binding.solutionPokemonImage.visibility = View.GONE
        binding.solutionPokemonMask.visibility = View.GONE
        binding.currentScore.visibility = View.GONE
        binding.accumulatedScore.visibility = View.GONE
        binding.guessGenFilterInclude.root.visibility = View.VISIBLE
        binding.guessGenFilterInclude.root.alpha = 1f
    }

    @SuppressLint("DefaultLocale")
    private fun loadSolutionData(pokemon: Pokemon) {
        val firstTypeIconResGuess = typeIconMap[pokemon.type1]
        if (firstTypeIconResGuess != null) {
            binding.guessHint1Type1.setImageResource(firstTypeIconResGuess)
        }
        binding.guessHint1Type1.visibility = View.INVISIBLE

        val secondTypeIconResGuess = typeIconMap[pokemon.type2]
        if (secondTypeIconResGuess != null) {
            binding.guessHint2Type2.setImageResource(secondTypeIconResGuess)
            binding.guessHint2Type2.setBackgroundResource(R.drawable.guess_each_pokemon_bg)
            binding.guessHint2Type2.clearColorFilter()
        } else {
            binding.guessHint2Type2.setImageResource(R.drawable.x)
            binding.guessHint2Type2.background = null
            binding.guessHint2Type2.setColorFilter(Color.WHITE)
        }
        binding.guessHint2Type2.visibility = View.INVISIBLE

        binding.guessHint4Ability.text = pokemon.abilitiesDisplayText()
        binding.guessHint4Ability.visibility = View.INVISIBLE
        binding.guessHint3Id.text = String.format("#%03d", pokemon.id)
        binding.guessHint3Id.visibility = View.INVISIBLE
        binding.guessHint5FirstChar.text = pokemon.name.first().toString()
        binding.guessHint5FirstChar.visibility = View.INVISIBLE

        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(binding.solutionPokemonImage)
        binding.solutionPokemonImage.visibility = View.INVISIBLE

        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(binding.solutionPokemonMask)
        binding.solutionPokemonMask.visibility = View.GONE
    }

    private fun enterSolutionPokemonFromRight() {
        binding.solutionPokemonMask.postDelayed({
            moveRight(binding.solutionPokemonMask)
        }, 100)
    }

    private fun resetHintsAndLifes(tryCount: Int) {
        when (tryCount) {
            1 -> {
                fadeAnimation(binding.guessHint1Type1, binding.questionMark1, fadeDuration)
                changeHeart(binding.heart1, true)
            }
            2 -> {
                fadeAnimation(binding.guessHint1Type1, binding.questionMark1, fadeDuration)
                fadeAnimation(binding.guessHint2Type2, binding.questionMark2, fadeDuration)
                changeHeart(binding.heart1, true)
                changeHeart(binding.heart2, true)
            }
            3 -> {
                fadeAnimation(binding.guessHint1Type1, binding.questionMark1, fadeDuration)
                fadeAnimation(binding.guessHint2Type2, binding.questionMark2, fadeDuration)
                fadeAnimation(binding.guessHint3Id, binding.questionMark3, fadeDuration)
                changeHeart(binding.heart1, true)
                changeHeart(binding.heart2, true)
                changeHeart(binding.heart3, true)
            }
            4 -> {
                fadeAnimation(binding.guessHint1Type1, binding.questionMark1, fadeDuration)
                fadeAnimation(binding.guessHint2Type2, binding.questionMark2, fadeDuration)
                fadeAnimation(binding.guessHint3Id, binding.questionMark3, fadeDuration)
                fadeAnimation(binding.guessHint4Ability, binding.questionMark4, fadeDuration)
                changeHeart(binding.heart1, true)
                changeHeart(binding.heart2, true)
                changeHeart(binding.heart3, true)
                changeHeart(binding.heart4, true)
            }
            5 -> {
                fadeAnimation(binding.guessHint1Type1, binding.questionMark1, fadeDuration)
                fadeAnimation(binding.guessHint2Type2, binding.questionMark2, fadeDuration)
                fadeAnimation(binding.guessHint3Id, binding.questionMark3, fadeDuration)
                fadeAnimation(binding.guessHint4Ability, binding.questionMark4, fadeDuration)
                fadeAnimation(binding.guessHint5FirstChar, binding.questionMark5, fadeDuration)
                changeHeart(binding.heart1, true)
                changeHeart(binding.heart2, true)
                changeHeart(binding.heart3, true)
                changeHeart(binding.heart4, true)
                changeHeart(binding.heart5, true)
            }
        }
        binding.guessTry1Text.text = ""
        binding.guessTry2Text.text = ""
        binding.guessTry3Text.text = ""
        binding.guessTry4Text.text = ""
        binding.guessTry5Text.text = ""
    }

    private fun fadeAnimation(previous: View, next: View, duration: Long) {
        fadeOut(previous, duration)
        fadeIn(next, duration)
    }

    private fun fadeOut(view: View, duration: Long) {
        view.animate().alpha(0f).setDuration(duration).withEndAction {
            view.visibility = View.INVISIBLE
        }.start()
    }

    private fun fadeIn(view: View, duration: Long) {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(duration).start()
    }

    private fun moveLeft(view: View, onAnimationEnd: () -> Unit) {
        val screenWidth = view.context.resources.displayMetrics.widthPixels.toFloat()
        val moveLeft = ObjectAnimator.ofFloat(view, "translationX", 0F, -screenWidth)
        moveLeft.duration = 500
        moveLeft.start()
        moveLeft.doOnEnd {
            view.visibility = View.GONE
            view.translationX = 0f
            onAnimationEnd()
        }
    }

    private fun moveRight(view: View) {
        view.translationX = view.context.resources.displayMetrics.widthPixels.toFloat()
        view.visibility = View.VISIBLE
        view.alpha = 1f

        val moveRight = ObjectAnimator.ofFloat(view, "translationX", view.translationX, 0f)
        moveRight.duration = 500
        moveRight.start()
        binding.guessBlockView.visibility = View.GONE
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view !is SearchView && view !is EditText) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                view?.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun changeHeart(heart: ImageView, changeToFullHeart: Boolean = false) {
        fadeOut(heart, fadeDuration)
        if (!changeToFullHeart) {
            heart.setImageResource(R.drawable.empty_heart)
        } else {
            heart.setImageResource(R.drawable.full_heart)
        }
        fadeIn(heart, fadeDuration)
    }

    private fun setSearchInteractionEnabled(enabled: Boolean) {
        binding.guessPokemonSearchView.isEnabled = enabled
        binding.guessPokemonSearchView.isFocusable = enabled
        binding.guessPokemonSearchView.isFocusableInTouchMode = enabled
        binding.guessPokemonSearchView.isClickable = enabled
        val searchEditText =
            binding.guessPokemonSearchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText?.isEnabled = enabled
        searchEditText?.isFocusable = enabled
        searchEditText?.isFocusableInTouchMode = enabled
        searchEditText?.isClickable = enabled
        binding.guessSearchBlockOverlay.visibility = if (enabled) View.GONE else View.VISIBLE
    }

    private fun resetLifesImages() {
        binding.heart1.setImageResource(R.drawable.full_heart)
        binding.heart2.setImageResource(R.drawable.full_heart)
        binding.heart3.setImageResource(R.drawable.full_heart)
        binding.heart4.setImageResource(R.drawable.full_heart)
        binding.heart5.setImageResource(R.drawable.full_heart)
        binding.heart6.setImageResource(R.drawable.full_heart)
    }

    private data class GenFilterSlot(
        val generation: PokemonGeneration,
        val binding: ItemGuessGenFilterBinding,
    )
}
