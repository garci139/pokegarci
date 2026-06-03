package com.garci.pokegarci

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import com.garci.pokegarci.domain.model.Pokemon
import com.garci.pokegarci.presentation.guess.GuessViewModel
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

    private val fadeDuration: Long = 400
    private val longerFadeDuration: Long = 800
    private val ultraLongDuration: Long = 1200

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.guessBlockView.visibility = View.VISIBLE

        binding.solutionPokemonImage.visibility = View.GONE
        binding.solutionPokemonMask.visibility = View.GONE
        binding.guessHint1Type1.visibility = View.INVISIBLE
        binding.guessHint2Type2.visibility = View.INVISIBLE
        binding.guessHint4Ability.visibility = View.INVISIBLE
        binding.guessHint3Id.visibility = View.INVISIBLE
        binding.guessHint5FirstChar.visibility = View.INVISIBLE
        binding.currentScore.visibility = View.GONE
        binding.accumulatedScore.visibility = View.GONE

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
                                    fadeIn(binding.guessPlayButton, ultraLongDuration)
                                    resetHintsAndLifes(hintsUsedBeforeCorrect)
                                    binding.guessPlayButton.isEnabled = true
                                }, 5000)
                            } else {
                                viewModel.currentSolution?.let { loadSolutionData(it) }
                                binding.solutionPokemonMask.postDelayed({
                                    moveRight(binding.solutionPokemonMask)
                                }, 100)
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
                val query = newText.orEmpty().lowercase()
                if (query == "") {
                    binding.guessNestedScrollView.visibility = View.GONE
                    binding.guessRecyclerView.visibility = View.GONE
                } else {
                    val filteredList = pokemonList.filter { it.name.lowercase().startsWith(query) }
                    adapter.updateList(filteredList)
                    binding.guessNestedScrollView.visibility = View.VISIBLE
                    binding.guessRecyclerView.visibility = View.VISIBLE
                    binding.guessNestedScrollView.requestLayout()
                }
                return true
            }
        })

        val guessCloseButton = binding.guessPokemonSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        guessCloseButton.setOnClickListener {
            binding.guessPokemonSearchView.setQuery("", false)
            binding.guessNestedScrollView.visibility = View.GONE
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(binding.guessPokemonSearchView.windowToken, 0)
        }

        binding.guessPlayButton.setOnClickListener {
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
                    binding.guessNestedScrollView,
                    binding.guessPlayButton,
                    binding.guessScoreboards,
                ),
            ),
            onRetry = { viewModel.retryLoad() },
            onLoaded = {
                fadeAnimation(binding.guessProgressBar, binding.guessPlayButton, fadeDuration)
                binding.guessPlayButton.isEnabled = true
                binding.guessBlockView.visibility = View.GONE
                viewModel.refreshPokemonList()
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
                        adapter.updateList(loadedPokemon)
                    }
                }
                launch {
                    viewModel.highscore.collect { highscore ->
                        binding.guessHighscore.text = highscore.toString()
                    }
                }
            }
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
                        fadeIn(binding.guessPlayButton, ultraLongDuration)
                        binding.guessPlayButton.isEnabled = true
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
        val solution = viewModel.startGame()
        updateScoreboards()
        fadeIn(binding.guessScoreboards, fadeDuration)
        resetLifesImages()
        fadeIn(binding.lifesLayout, fadeDuration)

        binding.guessPlayButton.isEnabled = false
        fadeAnimation(binding.guessPlayButton, binding.solutionPokemonMask, fadeDuration)
        resetHintsAndLifes(0)
        loadSolutionData(solution)

        binding.currentScore.visibility = View.VISIBLE
        binding.accumulatedScore.visibility = View.VISIBLE
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

        binding.guessHint4Ability.text = pokemon.firstAbility.displayName
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
        binding.solutionPokemonMask.visibility = View.VISIBLE
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

    private fun resetLifesImages() {
        binding.heart1.setImageResource(R.drawable.full_heart)
        binding.heart2.setImageResource(R.drawable.full_heart)
        binding.heart3.setImageResource(R.drawable.full_heart)
        binding.heart4.setImageResource(R.drawable.full_heart)
        binding.heart5.setImageResource(R.drawable.full_heart)
        binding.heart6.setImageResource(R.drawable.full_heart)
    }
}
