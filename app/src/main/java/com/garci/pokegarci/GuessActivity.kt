package com.garci.pokegarci

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.PokemonGuessAdapter
import com.garci.pokegarci.utils.vibrate

class GuessActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper()) // Handler global

    private lateinit var recyclerView: RecyclerView
    private lateinit var guessProgressBar: ProgressBar
    private lateinit var nestedScrollView: NestedScrollView
    private lateinit var pokemonList: List<Pokemon>
    private lateinit var pokemonRandomList: List<Pokemon>
    private lateinit var adapter: PokemonGuessAdapter
    private lateinit var guessSearchView: SearchView
    private lateinit var guessPlayButton: ImageButton
    private lateinit var pokemonSolution: Pokemon
    private lateinit var currentPokemonsGuessed: TextView
    private lateinit var accumulatedScore: TextView
    private lateinit var blockView: View
    private lateinit var currentHighscore: TextView
    private lateinit var allScoreboardsLayout: ConstraintLayout

    // Huecos para las respuestas
    private lateinit var guessAnswer1: TextView
    private lateinit var guessAnswer2: TextView
    private lateinit var guessAnswer3: TextView
    private lateinit var guessAnswer4: TextView
    private lateinit var guessAnswer5: TextView

    // Pistas del pokemon
    private lateinit var solutionImage: ImageView
    private lateinit var solutionMask: ImageView
    private lateinit var solutionType1Image: ImageView
    private lateinit var solutionType2Image: ImageView
    private lateinit var solutionAbilityName: TextView
    private lateinit var solutionId: TextView
    private lateinit var solutionFirstChar: TextView

    // Signos de interrogacion
    private lateinit var questionMark1: ImageView
    private lateinit var questionMark2: ImageView
    private lateinit var questionMark3: ImageView
    private lateinit var questionMark4: ImageView
    private lateinit var questionMark5: ImageView

    // Elementos de resultados finales
    private lateinit var resultsLayout: LinearLayout
    private lateinit var resultsImage: ImageView
    private lateinit var resultsPhrase: TextView
    private lateinit var resultsTextScore: TextView

    // Vidas
    private lateinit var lifesLayout: LinearLayout
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var heart4: ImageView
    private lateinit var heart5: ImageView
    private lateinit var heart6: ImageView

    // Para llevar la cuenta de los intentos
    private var tryCount = 0
    private var solutionPokemonIndex = 0
    private var possiblePoints = 6
    private var accumulatedScoreCount = 0
    private val fadeDuration: Long = 400
    private val longerFadeDuration: Long = 800
    private val ultraLongDuration: Long = 1200
    private var pokemonTotalNumber: Int = 251

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guess)

        guessProgressBar = findViewById(R.id.guessProgressBar)
        guessProgressBar.visibility = View.VISIBLE

        val guessLayout = findViewById<ConstraintLayout>(R.id.guessLayout)
        recyclerView = findViewById(R.id.guessRecyclerView)
        nestedScrollView = findViewById(R.id.guessNestedScrollView)
        guessSearchView = findViewById(R.id.guessPokemonSearchView)
        guessPlayButton = findViewById(R.id.guessPlayButton)
        currentPokemonsGuessed = findViewById(R.id.currentScore)
        accumulatedScore = findViewById(R.id.accumulatedScore)
        blockView = findViewById(R.id.guessBlockView)
        blockView.visibility = View.VISIBLE
        allScoreboardsLayout = findViewById(R.id.guessScoreboards)

        val pikachuWinImage: ImageView = findViewById(R.id.guessPikachuWinImage)

        resultsLayout = findViewById(R.id.guessResultsLayout)
        resultsImage = findViewById(R.id.guessResultsImage)
        resultsPhrase = findViewById(R.id.guessResultsPhrase)
        resultsTextScore = findViewById(R.id.guessResultsTextScore)

        currentHighscore = findViewById(R.id.guessHighscore)
        currentHighscore.text = getHighscore().toString()

        guessAnswer1 = findViewById(R.id.guessTry1Text)
        guessAnswer2 = findViewById(R.id.guessTry2Text)
        guessAnswer3 = findViewById(R.id.guessTry3Text)
        guessAnswer4 = findViewById(R.id.guessTry4Text)
        guessAnswer5 = findViewById(R.id.guessTry5Text)

        questionMark1 = findViewById(R.id.questionMark1)
        questionMark2 = findViewById(R.id.questionMark2)
        questionMark3 = findViewById(R.id.questionMark3)
        questionMark4 = findViewById(R.id.questionMark4)
        questionMark5 = findViewById(R.id.questionMark5)

        solutionImage = findViewById(R.id.solutionPokemonImage)
        solutionMask = findViewById(R.id.solutionPokemonMask)
        solutionType1Image = findViewById(R.id.guessHint1Type1)
        solutionType2Image = findViewById(R.id.guessHint2Type2)
        solutionId = findViewById(R.id.guessHint3Id)
        solutionFirstChar = findViewById(R.id.guessHint5FirstChar)
        solutionAbilityName = findViewById(R.id.guessHint4Ability)

        lifesLayout = findViewById(R.id.lifesLayout)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        heart4 = findViewById(R.id.heart4)
        heart5 = findViewById(R.id.heart5)
        heart6 = findViewById(R.id.heart6)

        // Oculto toodo por defecto
        solutionImage.visibility = View.GONE
        solutionMask.visibility = View.GONE
        solutionType1Image.visibility = View.INVISIBLE
        solutionType2Image.visibility = View.INVISIBLE
        solutionAbilityName.visibility = View.INVISIBLE
        solutionId.visibility = View.INVISIBLE
        solutionFirstChar.visibility = View.INVISIBLE
        currentPokemonsGuessed.visibility = View.GONE
        accumulatedScore.visibility = View.GONE

        pokemonList = PokemonRepository.getPokemonList()

        PokemonRepository.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) {
                fadeAnimation(guessProgressBar,guessPlayButton,fadeDuration)
                guessPlayButton.isEnabled = true
                guessProgressBar.visibility = View.GONE
                pokemonList = PokemonRepository.getPokemonList()
                pokemonTotalNumber = pokemonList.size
                adapter.updateList(pokemonList)
            }
        }

        // Configurar el ADAPTADOR con la lista completa
        adapter = PokemonGuessAdapter(pokemonList) { selectedPokemon ->
            guessSearchView.setQuery("",false)
            // Oculto el teclado al hacer click
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(recyclerView.windowToken, 0)

            if (selectedPokemon.id != pokemonSolution.id) { //               SI FALLA
                vibrate(false)
                when (tryCount) {
                    in 0..4 -> {
                        tryCount++ // Anoto el fallo
                        possiblePoints--
                        showHint(selectedPokemon.name, tryCount)
                    }
                    5 -> { // Se acabo el juego
                        changeHeart(heart6)
                        gameOver()
                    }
                }

            } else { //                                                     SI ACIERTA
                vibrate()
                blockView.visibility = View.VISIBLE
                // Se aumenta el indice de la lista
                solutionPokemonIndex++
                currentPokemonsGuessed.text = solutionPokemonIndex.toString()
                // Se le suman los puntos correspondientes
                accumulatedScoreCount += possiblePoints
                accumulatedScore.text = accumulatedScoreCount.toString()
                // Se comprueba si es record y se guarda
                checkHighscore(accumulatedScoreCount)
                fadeAnimation(solutionMask,solutionImage, fadeDuration)
                val oldTryCount = tryCount

                handler.postDelayed({
                    resetHintsAndLifes(oldTryCount)
                    moveLeft(solutionImage){

                        if(solutionPokemonIndex==pokemonTotalNumber){ //           SI SE ACIERTA EL ULTIMO
                            fadeOut(allScoreboardsLayout, fadeDuration)
                            fadeOut(lifesLayout, fadeDuration)
                            resultsTextScore.text = getString(R.string.resultsScore, accumulatedScoreCount)
                            resultsPhrase.text = getString(R.string.winPhrase)
                            resultsImage.setImageResource(R.drawable.fortnite_victory_image)
                            fadeIn(pikachuWinImage, fadeDuration)
                            fadeIn(resultsLayout, fadeDuration)

                            handler.postDelayed({
                                fadeOut(pikachuWinImage, fadeDuration)
                                fadeOut(resultsLayout, fadeDuration)
                                fadeIn(guessPlayButton, ultraLongDuration)
                                resetHintsAndLifes(oldTryCount)
                                guessPlayButton.isEnabled = true
                            },5000)

                        }else{  //                              SINO ES EL ULTIMO, NEXT POKEMON
                            pokemonSolution = pokemonRandomList[solutionPokemonIndex]
                            loadSolutionData(pokemonSolution)
                            // Pausa antes de la derecha
                            solutionMask.postDelayed({
                                //fadeIn(lifesLayout, fadeDuration)
                                moveRight(solutionMask)
                            }, 100)
                        }
                    }
                },1000)
                possiblePoints = 6
                tryCount=0
            }

        }
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = guessLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()

        // Listener de la barra de busqueda
        guessSearchView.queryHint = getString(R.string.name)
        guessSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No hay submits
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().lowercase()
                // Es importante que el nestedScrollView se haga GONE, y no INVISIBLE, porque luego,
                // si no, le cuesta redibujar el RecyclerView de su interior
                if (query=="") {
                    nestedScrollView.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                } else {
                    val filteredList = pokemonList.filter { it.name.lowercase().startsWith(query) }
                    adapter.updateList(filteredList)
                    nestedScrollView.visibility = View.VISIBLE
                    recyclerView.visibility = View.VISIBLE
                    // Para que actualice y recalcule sus límites
                    nestedScrollView.requestLayout()
                }
                return true
            }

        })

        // Cambiar color de texto de barra de busqueda de searchView
        // (no dejaba cambiarla con XML)
        val guessEditText = guessSearchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        guessEditText.setTextColor(Color.BLACK) // Color del texto
        guessEditText.setHintTextColor(Color.GRAY) // Color del hint

        // Cambiar color de icono de borrar de searchView
        // (no dejaba cambiarla con XML)
        val guessCloseButton = guessSearchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        guessCloseButton.setColorFilter(Color.BLACK) // Color del icono
        guessCloseButton.setOnClickListener {
            // Borrar la query
            guessSearchView.setQuery("", false)
            // Ocultar el RecyclerView
            nestedScrollView.visibility = View.GONE
            // Ocultar el teclado
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(guessSearchView.windowToken, 0)
        }

        // Boton de play
        guessPlayButton.setOnClickListener {
            vibrate()
            blockView.visibility = View.GONE
            startPlay()
        }

    }

    private fun gameOver() {
        blockView.visibility = View.VISIBLE
        val oshawottImage: ImageView = findViewById(R.id.sadOshawottImage)
        // Hago que la imagen solucion sea visible durante 3 segundos y paso a resultados
        fadeAnimation(solutionMask, solutionImage, longerFadeDuration)
        handler.postDelayed({
            fadeOut(allScoreboardsLayout, fadeDuration)
            fadeOut(lifesLayout, fadeDuration)
            moveLeft(solutionImage){
                // Preparo plantilla de derrota
                resultsTextScore.text = getString(R.string.resultsScore, accumulatedScoreCount)
                resultsPhrase.text = getString(R.string.losePhrase)
                resultsImage.setImageResource(R.drawable.game_over_image)
                fadeIn(resultsLayout, longerFadeDuration)
                fadeIn(oshawottImage, longerFadeDuration)
                handler.postDelayed({
                    fadeOut(oshawottImage, longerFadeDuration)
                    fadeOut(resultsLayout, longerFadeDuration)
                    resetHintsAndLifes(tryCount)
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        fadeIn(guessPlayButton, ultraLongDuration)
                        guessPlayButton.isEnabled = true
                    },1000)
                },5000)
            }
        }, 3000)

    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

    private fun showHint(wrongPokemonName: String,tryCount: Int) {
            when(tryCount) {
                1 -> {
                    fadeAnimation(questionMark1, solutionType1Image, fadeDuration)
                    changeHeart(heart1)
                    guessAnswer1.text = wrongPokemonName
                    guessAnswer1.paintFlags = guessAnswer1.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }
                2 -> {
                    fadeAnimation(questionMark2, solutionType2Image, fadeDuration)
                    changeHeart(heart2)
                    guessAnswer2.text = wrongPokemonName
                    guessAnswer2.paintFlags = guessAnswer2.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }
                3 -> {
                    fadeAnimation(questionMark3, solutionId, fadeDuration)
                    changeHeart(heart3)
                    guessAnswer3.text = wrongPokemonName
                    guessAnswer3.paintFlags = guessAnswer3.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }
                4 -> {
                    fadeAnimation(questionMark4, solutionAbilityName, fadeDuration)
                    changeHeart(heart4)
                    guessAnswer4.text = wrongPokemonName
                    guessAnswer4.paintFlags = guessAnswer4.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }
                5 -> {
                    fadeAnimation(questionMark5, solutionFirstChar, fadeDuration)
                    changeHeart(heart5)
                    guessAnswer5.text = wrongPokemonName
                    guessAnswer5.paintFlags = guessAnswer5.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
                }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun startPlay() {
        tryCount=0
        solutionPokemonIndex = 0
        possiblePoints = 6
        // Marcadores a cero
        currentPokemonsGuessed.text = solutionPokemonIndex.toString()
        accumulatedScoreCount = 0
        accumulatedScore.text = accumulatedScoreCount.toString()
        // Aparecen los marcadores
        fadeIn(allScoreboardsLayout, fadeDuration)
        resetLifesImages()
        fadeIn(lifesLayout, fadeDuration)

        // Se mezcla la lista de nuevo
        pokemonRandomList = pokemonList.shuffled()
        pokemonSolution = pokemonRandomList[solutionPokemonIndex]

        // Lo oculto al empezar partida
        guessPlayButton.isEnabled = false
        fadeAnimation(guessPlayButton,solutionMask, fadeDuration)
        resetHintsAndLifes(tryCount)

        // Cargo datos del pokemonSolution
        loadSolutionData(pokemonSolution)

        currentPokemonsGuessed.visibility = View.VISIBLE
        accumulatedScore.visibility = View.VISIBLE

    }

    // Funcion para cargar los datos del nuevo pokemon
    // Todos los datos son invisibles por defecto, salvo la imagen
    @SuppressLint("DefaultLocale")
    private fun loadSolutionData(pokemon: Pokemon) {
        // Tipo 1
        val firstTypeIconResGuess = typeIconMap[pokemon.type1]
        if (firstTypeIconResGuess != null) {
            solutionType1Image.setImageResource(firstTypeIconResGuess)
        }
        solutionType1Image.visibility = View.INVISIBLE
        // Tipo 2
        val secondTypeIconResGuess = typeIconMap[pokemon.type2]
        if (secondTypeIconResGuess != null) {
            solutionType2Image.setImageResource(secondTypeIconResGuess)
            solutionType2Image.setBackgroundResource(R.drawable.guess_each_pokemon_bg)
            solutionType2Image.clearColorFilter()
        }else{
            solutionType2Image.setImageResource(R.drawable.x)
            solutionType2Image.background = null
            solutionType2Image.setColorFilter(Color.WHITE)
        }
        solutionType2Image.visibility = View.INVISIBLE
        // Habilidad
        solutionAbilityName.text = pokemon.firstAbility.languageName
        solutionAbilityName.visibility = View.INVISIBLE
        // ID
        solutionId.text = String.format("#%03d", pokemon.id)
        solutionId.visibility = View.INVISIBLE
        // Primera letra
        solutionFirstChar.text = pokemon.name.first().toString()
        solutionFirstChar.visibility = View.INVISIBLE
        // Imagen
        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(solutionImage)
        solutionImage.visibility = View.INVISIBLE
        // Mascara
        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(solutionMask)
        solutionMask.visibility = View.VISIBLE
        Log.d("DEBUG_MASK", "loadSolutionData, cargado pokemon ${pokemon.name}: ${solutionMask.height}x${solutionMask.width}")

    }

    // Funcion que oculta pistas, muestra ? y borra textos
    private fun resetHintsAndLifes(tryCount: Int){
        when(tryCount){
            1 -> {
                fadeAnimation(solutionType1Image, questionMark1, fadeDuration)
                changeHeart(heart1, true)
            }
            2 -> {
                fadeAnimation(solutionType1Image, questionMark1, fadeDuration)
                fadeAnimation(solutionType2Image, questionMark2, fadeDuration)
                changeHeart(heart1, true)
                changeHeart(heart2, true)
            }
            3 -> {
                fadeAnimation(solutionType1Image, questionMark1, fadeDuration)
                fadeAnimation(solutionType2Image, questionMark2, fadeDuration)
                fadeAnimation(solutionId, questionMark3, fadeDuration)
                changeHeart(heart1, true)
                changeHeart(heart2, true)
                changeHeart(heart3, true)
            }
            4 -> {
                fadeAnimation(solutionType1Image, questionMark1, fadeDuration)
                fadeAnimation(solutionType2Image, questionMark2, fadeDuration)
                fadeAnimation(solutionId, questionMark3, fadeDuration)
                fadeAnimation(solutionAbilityName, questionMark4, fadeDuration)
                changeHeart(heart1, true)
                changeHeart(heart2, true)
                changeHeart(heart3, true)
                changeHeart(heart4, true)
            }
            5 -> {
                fadeAnimation(solutionType1Image, questionMark1, fadeDuration)
                fadeAnimation(solutionType2Image, questionMark2, fadeDuration)
                fadeAnimation(solutionId, questionMark3, fadeDuration)
                fadeAnimation(solutionAbilityName, questionMark4, fadeDuration)
                fadeAnimation(solutionFirstChar, questionMark5, fadeDuration)
                changeHeart(heart1, true)
                changeHeart(heart2, true)
                changeHeart(heart3, true)
                changeHeart(heart4, true)
                changeHeart(heart5, true)
            }
        }
        // Reincio los intentos guardados de antes
        guessAnswer1.text = ""
        guessAnswer2.text = ""
        guessAnswer3.text = ""
        guessAnswer4.text = ""
        guessAnswer5.text = ""
    }

    // Funcion para hacer el efecto de fade, que lo voy a usar bastante
    private fun fadeAnimation(previous: View, next: View, duration: Long) {
        fadeOut(previous, duration)
        // Ahora next pasara de full transparente a opaco
        fadeIn(next, duration)
    }

    private fun fadeOut(view: View, duration: Long){
        view.animate().alpha(0f).setDuration(duration).withEndAction {
            view.visibility = View.INVISIBLE
        }.start()
    }

    private fun fadeIn(view: View, duration: Long){
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate().alpha(1f).setDuration(duration).start()
    }

    // Funcion para hacer la animcacion de desplazamiento
    private fun moveLeft(view: View, onAnimationEnd: () -> Unit) {
        val screenWidth = view.context.resources.displayMetrics.widthPixels.toFloat()
        val moveLeft = ObjectAnimator.ofFloat(view, "translationX", 0F, -screenWidth)
        moveLeft.duration = 500
        moveLeft.start()
        moveLeft.doOnEnd {
            view.visibility = View.GONE // Ocultar la imagen tras salir
            view.translationX = 0f // Reinicio la posicion ahora que esta fuera e invisible
            onAnimationEnd() // Ejecutar la siguiente acción
        }
    }

    // Mueve un View desde la derecha hasta su posición original
    private fun moveRight(view: View) {
            view.translationX = view.context.resources.displayMetrics.widthPixels.toFloat()
            view.visibility = View.VISIBLE
            view.alpha = 1f

            val moveRight = ObjectAnimator.ofFloat(view, "translationX", view.translationX, 0f)
            moveRight.duration = 500
            moveRight.start()
            blockView.visibility = View.GONE
    }

    // Funcion para guardar el posible record
    @SuppressLint("SetTextI18n")
    private fun checkHighscore(newScore: Int) {
        val sharedPref = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        val currentRecord = sharedPref.getInt("highscore", 0) // Obtiene el record actual
        if (newScore > currentRecord) { // Si la puntuacion es mayor, lo guarda
            sharedPref.edit().putInt("highscore", newScore).apply()
            currentHighscore.text = newScore.toString()
        }
    }

    // Funcion para obtener el record
    private fun getHighscore(): Int {
        val sharedPref = getSharedPreferences("game_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("highscore", 0) // Devuelve el record guardado o 0 si no hay
    }

    // Funcion para ocultar el teclado al tocar cualquier parte de la pantalla que no sea searchView
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view !is SearchView) {
                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
                view?.clearFocus()
            }
        }
        return super.dispatchTouchEvent(event)
    }

    private fun changeHeart(heart: ImageView, changetoFullHeart: Boolean = false) {
        fadeOut(heart,fadeDuration)
        if (!changetoFullHeart) heart.setImageResource(R.drawable.empty_heart) else heart.setImageResource(R.drawable.full_heart)
        fadeIn(heart,fadeDuration)
    }

    private fun resetLifesImages(){
        heart1.setImageResource(R.drawable.full_heart)
        heart2.setImageResource(R.drawable.full_heart)
        heart3.setImageResource(R.drawable.full_heart)
        heart4.setImageResource(R.drawable.full_heart)
        heart5.setImageResource(R.drawable.full_heart)
        heart6.setImageResource(R.drawable.full_heart)
    }
}