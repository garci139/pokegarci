package com.garci.pokegarci

import PokemonAdapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Color
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.garci.pokegarci.R.*
import com.google.android.material.card.MaterialCardView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate
import com.google.android.material.button.MaterialButton
import java.util.Locale

class PokedexActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val pokemonList = mutableListOf<Pokemon>()
    private val filteredPokemonList = mutableListOf<Pokemon>()
    private lateinit var progressBar: ProgressBar

    // Recursos para la tarjeta ampliada
    private lateinit var expandedCard: MaterialCardView
    private lateinit var expandedSubView: View
    private lateinit var expandedPokemonImage: ImageView
    private lateinit var expandedPokemonName: TextView
    private lateinit var expandedPokemonId: TextView
    private lateinit var expandedFirstTypeIcon: ImageView
    private lateinit var expandedSecondTypeIcon: ImageView
    private lateinit var closeCardButton: MaterialButton
    private lateinit var expandedPokemonDescription: TextView
    private lateinit var expandedPokemonHP: TextView
    private lateinit var expandedPokemonAttack: TextView
    private lateinit var expandedPokemonDefense: TextView
    private lateinit var expandedPokemonSpecialAttack: TextView
    private lateinit var expandedPokemonSpecialDefense: TextView
    private lateinit var expandedPokemonSpeed: TextView
    private lateinit var expandedPokemonHeight: TextView
    private lateinit var expandedPokemonWeight: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_pokedex)

        val pokedexLayout = findViewById<ConstraintLayout>(id.pokedexLayout)
        progressBar = findViewById(id.progressBar)
        recyclerView = findViewById(id.pokedexBox)
        val searchView = findViewById<SearchView>(id.searchView)
        searchView.queryHint = getString(string.queryHintSearchView)

        // Cambiar color de texto de barra de busqueda de searchView
        // (no dejaba cambiarla con XML)
        val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.setTextColor(Color.BLACK) // Color del texto
        searchEditText.setHintTextColor(Color.GRAY) // Color del hint

        // Cambiar color de icono de borrar de searchView
        // (no dejaba cambiarla con XML)
        val searchCloseButton = searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseButton.setColorFilter(Color.BLACK) // Color del icono

        // Loading... (luego se hace invisible)
        progressBar.visibility = View.VISIBLE

        // Tarjeta ampliada vinculada
        expandedCard = findViewById(id.expandedPokemonCard)
        expandedPokemonImage = findViewById(id.expandedPokemonImage)
        expandedPokemonName = findViewById(id.expandedPokemonName)
        expandedPokemonId = findViewById(id.expandedPokemonId)
        expandedFirstTypeIcon = findViewById(id.expandedFirstTypeIcon)
        expandedSecondTypeIcon = findViewById(id.expandedSecondTypeIcon)
        closeCardButton = findViewById(id.closeCardButton)
        expandedSubView = findViewById(id.expandedSubView)
        expandedPokemonDescription = findViewById(id.expandedPokemonDescription)
        expandedPokemonHP = findViewById(id.expandedPokemonHP)
        expandedPokemonAttack = findViewById(id.expandedPokemonAttack)
        expandedPokemonDefense = findViewById(id.expandedPokemonDefense)
        expandedPokemonSpecialAttack = findViewById(id.expandedPokemonSpAttack)
        expandedPokemonSpecialDefense = findViewById(id.expandedPokemonSpDefense)
        expandedPokemonSpeed = findViewById(id.expandedPokemonSpeed)
        expandedPokemonHeight = findViewById(id.expandedPokemonHeight)
        expandedPokemonWeight = findViewById(id.expandedPokemonWeight)

        // Cerrar tarjeta al hacer clic en el boton
        closeCardButton.setOnClickListener {
            vibrate()
            recyclerView.isEnabled = true
            findViewById<View>(id.disableRecyclerView).visibility = View.INVISIBLE
            expandedCard.visibility = View.GONE
        }

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = pokedexLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()

        // Listener de la barra de busqueda
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterPokemon(query)
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                filterPokemon(newText)
                return true
            }
        })

        // Para que la barra de busqueda no muestre el cursor
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Accede al EditText interno
                val searchEditText = searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                // Oculta el cursor
                searchEditText.isCursorVisible = false
            }
        }

        // Preparo el RecycleView
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = PokemonAdapter(filteredPokemonList) { pokemon ->
            vibrate()
            showExpandedCard(pokemon)
        }
        recyclerView.isNestedScrollingEnabled = false // Evita problemas de scroll en algunos dispositivos

        // CARGA DE LOS PRIMEROS POKEMON CUANDO ESTE LA LISTA
        // Hay un observador que esta atento a la carga completa en PokemonRepository
        PokemonRepository.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) { // Una vez ha cargado la lista
                progressBar.visibility = View.GONE
                updatePokemonList()
            }
        }
    }

    // Funcion para mostrar la tarjeta expandida al hacer click
    @SuppressLint("DefaultLocale")
    private fun showExpandedCard(pokemon: Pokemon) {
        // Nombre, ID y descripcion expandidas
        expandedPokemonName.text = pokemon.name
        expandedPokemonId.text = String.format("#%03d", pokemon.id)
        expandedPokemonDescription.text = pokemon.description
        expandedPokemonHeight.text = String.format(Locale.US, "%.1f m", pokemon.height / 10.0)
        expandedPokemonWeight.text = String.format(Locale.US, "%.1f kg", pokemon.weight / 10.0)

        // Stats expandidas
        expandedPokemonHP.text = String.format(Locale.US, "%d", pokemon.hp)
        expandedPokemonAttack.text = String.format(Locale.US, "%d", pokemon.attack)
        expandedPokemonDefense.text = String.format(Locale.US, "%d", pokemon.defense)
        expandedPokemonSpecialAttack.text = String.format(Locale.US, "%d", pokemon.specialAttack)
        expandedPokemonSpecialDefense.text = String.format(Locale.US, "%d", pokemon.specialDefense)
        expandedPokemonSpeed.text = String.format(Locale.US, "%d", pokemon.speed)

        // Opciones de Glide
        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Para hacer cache

        // Imagen expandida
        Glide.with(this)
            .load(pokemon.imageUrl)
            .apply(requestOptions)
            .into(expandedPokemonImage)

        // Tipos expandidos
        // Los 'ifs' son para que se muestren o no si es monotipo
        val firstTypeIconRes = typeIconMap[pokemon.type1]
        if (firstTypeIconRes != null) {
            expandedFirstTypeIcon.setImageResource(firstTypeIconRes)
            expandedFirstTypeIcon.visibility = View.VISIBLE
        } else {
            expandedFirstTypeIcon.visibility = View.GONE
        }
        val secondTypeIconRes = typeIconMap[pokemon.type2]
        if (secondTypeIconRes != null) {
            expandedSecondTypeIcon.setImageResource(secondTypeIconRes)
            expandedSecondTypeIcon.visibility = View.VISIBLE
        } else {
            expandedSecondTypeIcon.visibility = View.GONE
        }

        // Uso el mismo fondo del pokemon para el SubView de expandedCard
        val safeExpandedBackground = pokemon.backgroundDrawable.constantState?.newDrawable()?.mutate()
        if (safeExpandedBackground != null) {
            expandedSubView.background = safeExpandedBackground
        } else {
            expandedSubView.setBackgroundColor(Color.GRAY) // Fondo gris en caso de error
        }

        recyclerView.isEnabled = false

        // Al aparecer expandedCard, el teclado se oculta
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(recyclerView.windowToken, 0)

        // Tarjeta expandida visible
        findViewById<View>(id.disableRecyclerView).visibility = View.VISIBLE
        expandedCard.visibility = View.VISIBLE
    }

    // Funcion para filtrar busqueda (usa filteredPokemonList, por defecto vacia)
    @SuppressLint("NotifyDataSetChanged")
    private fun filterPokemon(query: String?) {
        filteredPokemonList.clear() //Vacio la lista por si acaso
        if (query.isNullOrEmpty()) {
            // Si el texto está vacio, mostrar todos los pokemon
            filteredPokemonList.addAll(pokemonList)
        } else {
            val lowerQuery = query.lowercase().trim()
            // Filtrar por nombre o por ID
            val filteredResults = pokemonList.filter {
                it.name.lowercase().startsWith(lowerQuery) || it.id.toString().contains(lowerQuery)
            }
            filteredPokemonList.addAll(filteredResults)
        }
        runOnUiThread {
            recyclerView.adapter?.notifyDataSetChanged()
            recyclerView.scrollToPosition(0)
        }
    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

    // Funcion para actualizar la lista en caso de cambio de idioma
    @SuppressLint("NotifyDataSetChanged")
    private fun updatePokemonList() {
        pokemonList.clear()
        pokemonList.addAll(PokemonRepository.getPokemonList())
        filteredPokemonList.clear()
        filteredPokemonList.addAll(pokemonList)
        recyclerView.adapter?.notifyDataSetChanged()
    }

}