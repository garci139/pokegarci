package com.garci.pokegarci

import PokemonDialogAdapter
import android.annotation.SuppressLint
import com.garci.pokegarci.R.*
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate
import com.google.android.material.card.MaterialCardView
import java.util.Locale

class SizeActivity : AppCompatActivity() {

    private lateinit var pokemonList: List<Pokemon>
    private lateinit var firstSelectedPokemon: Pokemon
    private lateinit var secondSelectedPokemon: Pokemon
    private lateinit var progressBarSize: ProgressBar
    private lateinit var progressBarSize2: ProgressBar
    private lateinit var sizeBox: MaterialCardView

    // Variables de los botones de cambio de pokemon
    private lateinit var changePkmn1Size: MaterialCardView
    private lateinit var changePkmn2Size: MaterialCardView
    private lateinit var typeChangePkmn1Size: View
    private lateinit var typeChangePkmn2Size: View
    private lateinit var changePkmn1SizeText: TextView
    private lateinit var changePkmn2SizeText: TextView

    // Variables de stats de ambos pokemon
    private lateinit var sizeHP1: TextView
    private lateinit var sizeHP2: TextView
    private lateinit var sizeAttack1: TextView
    private lateinit var sizeAttack2: TextView
    private lateinit var sizeDefense1: TextView
    private lateinit var sizeDefense2: TextView
    private lateinit var sizeSpAttack1: TextView
    private lateinit var sizeSpAttack2: TextView
    private lateinit var sizeSpDefense1: TextView
    private lateinit var sizeSpDefense2: TextView
    private lateinit var sizeSpeed1: TextView
    private lateinit var sizeSpeed2: TextView

    // Para el trackeo de movimiento al arrastrar imagenes
    private var dX = 0f
    private var dY = 0f
    private var originalX1 = 0f
    private var originalY1 = 0f
    private var originalX2 = 0f
    private var originalY2 = 0f

    // Para mantener la posicion en el RecylerView
    private var lastSelectedPosition1: Int = 0
    private var lastSelectedPosition2: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_size)

        val sizeLayout = findViewById<ConstraintLayout>(R.id.sizeLayout)
        sizeBox = findViewById(id.sizeBox)

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = sizeLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()

        // Botones de cambio
        changePkmn1Size = findViewById(id.changePkmn1Size)
        changePkmn2Size = findViewById(id.changePkmn2Size)
        changePkmn1Size.isEnabled = false
        changePkmn2Size.isEnabled = false

        // Fondos de botones (cambian con el tipo
        typeChangePkmn1Size = findViewById(id.typeChangePkmn1Size)
        typeChangePkmn2Size = findViewById(id.typeChangePkmn2Size)

        // Texto de botones
        changePkmn1SizeText = findViewById(id.changePkmn1SizeText)
        changePkmn2SizeText = findViewById(id.changePkmn2SizeText)

        // Stats
        sizeHP1 = findViewById(id.sizeHP1)
        sizeHP2 = findViewById(id.sizeHP2)
        sizeAttack1 = findViewById(id.sizeAttack1)
        sizeAttack2 = findViewById(id.sizeAttack2)
        sizeDefense1 = findViewById(id.sizeDefense1)
        sizeDefense2 = findViewById(id.sizeDefense2)
        sizeSpAttack1 = findViewById(id.sizeSpAttack1)
        sizeSpAttack2 = findViewById(id.sizeSpAttack2)
        sizeSpDefense1 = findViewById(id.sizeSpDefense1)
        sizeSpDefense2 = findViewById(id.sizeSpDefense2)
        sizeSpeed1 = findViewById(id.sizeSpeed1)
        sizeSpeed2 = findViewById(id.sizeSpeed2)

        // Habilitar arrastre en ambas imagenes
        val pokemon1Shape = findViewById<ImageView>(R.id.pokemon1Shape)
        val pokemon2Shape = findViewById<ImageView>(R.id.pokemon2Shape)
        val resetButton = findViewById<ImageView>(id.resetImagesPositionSize)

        // Guardar posiciones originales
        pokemon1Shape.post {
            originalX1 = pokemon1Shape.x
            originalY1 = pokemon1Shape.y
        }

        pokemon2Shape.post {
            originalX2 = pokemon2Shape.x
            originalY2 = pokemon2Shape.y
        }
        enableDrag(pokemon1Shape)
        enableDrag(pokemon2Shape)

        // Restaurar posiciones al hacer clic en el botón
        resetButton.setOnClickListener {
            // Restaurar la posición X e Y originales (no alineadas verticalmente, cuidao)
            pokemon1Shape.animate().x(originalX1).y(originalY1).setDuration(200).start()
            pokemon2Shape.animate().x(originalX2).y(originalY2).setDuration(200).start()

            // Pillar proporciones para depsues centrar verticalemnte
            val tallestPokemon = if (pokemon1Shape.height > pokemon2Shape.height) pokemon1Shape else pokemon2Shape
            val shortestPokemon = if (pokemon1Shape.height > pokemon2Shape.height) pokemon2Shape else pokemon1Shape

            val tallestOriginalY = if (tallestPokemon == pokemon1Shape) originalY1 else originalY2
            val tallestHeight = tallestPokemon.height
            val shortestHeight = shortestPokemon.height

            // Centrar la pequeña con respecto al tamaño de la grande
            val shortestTargetY = tallestOriginalY + (tallestHeight - shortestHeight) / 2

            shortestPokemon.animate().y(shortestTargetY).setDuration(200).start()
        }

        // Activo la barra de carga de datos
        progressBarSize = findViewById(id.progressBarSize)
        progressBarSize2 = findViewById(id.progressBarSize2)
        progressBarSize.visibility = View.VISIBLE // Mostrar el indicador de carga
        progressBarSize2.visibility = View.VISIBLE

        pokemonList = PokemonRepository.getPokemonList() // Obtener lista desde el repositorio

        // Estar atento a la carga
        PokemonRepository.isDataLoaded.observe(this) { isLoaded ->
            if (isLoaded) { // Una vez ha cargado la lista
                progressBarSize.visibility = View.GONE
                progressBarSize2.visibility = View.GONE
                changePkmn1Size.isEnabled = true
                changePkmn2Size.isEnabled = true
                changePkmn2Size.isEnabled = true
                initializeDefaultPokemon(pokemonList)
            }
        }

        // Si el usuario quiere cambiar el pokemon 1 (izq)
        changePkmn1Size.setOnClickListener {
            vibrate()
            showPokemonSelectorDialog(true) { selectedPokemon ->
                firstSelectedPokemon = selectedPokemon
                // Actualizo
                if (::secondSelectedPokemon.isInitialized) {
                    updateSizeComparison()
                }
            }
        }

        // Si el usuario quiere cambiar el pokemon 2 (der)
        changePkmn2Size.setOnClickListener {
            vibrate()
            showPokemonSelectorDialog(false) { selectedPokemon ->
                secondSelectedPokemon = selectedPokemon
                // Actualizo
                if (::firstSelectedPokemon.isInitialized) {
                    updateSizeComparison()
                }
            }
        }

    }

    // Funcion para mostrar la lista de pokemon al hacer click
    private fun showPokemonSelectorDialog(isFirstButton: Boolean, onPokemonSelected: (Pokemon) -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Para evitar esquinas cuadradas
        dialog.setContentView(layout.dialog_size_selector)

        val searchViewSize = dialog.findViewById<SearchView>(id.searchViewSize)
        val recyclerPokemon = dialog.findViewById<RecyclerView>(R.id.recyclerPokemon)

        //Cambio colores de texto de barra de busqueda (Size) porque no deja en XML
        val searchEditTextSize = searchViewSize.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditTextSize.setTextColor(Color.BLACK)
        searchEditTextSize.setHintTextColor(Color.GRAY)

        // Cambiar color de icono de borrar de searchView
        // (no dejaba cambiarla con XML)
        val searchCloseButtonSize = searchViewSize.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)
        searchCloseButtonSize.setColorFilter(Color.BLACK) // Color del icono

        // Para que la barra de busqueda no muestre el cursor
        searchViewSize.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // Accede al EditText interno
                val searchEditText = searchViewSize.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
                // Oculta el cursor
                searchEditText.isCursorVisible = false
            }
        }

        val adapter = PokemonDialogAdapter(pokemonList) { selectedPokemon ->
            // Guardar la posicion en la variable de boton correspondiente
            vibrate()
            if (isFirstButton) {
                lastSelectedPosition1 = pokemonList.indexOf(selectedPokemon)
                if (lastSelectedPosition1>2) lastSelectedPosition1+=2
            } else {
                lastSelectedPosition2 = pokemonList.indexOf(selectedPokemon)
                if (lastSelectedPosition2>2) lastSelectedPosition2+=2
            }
            onPokemonSelected(selectedPokemon)
            dialog.dismiss() // Cerrar al seleccionar
        }

        recyclerPokemon.layoutManager = LinearLayoutManager(this)
        recyclerPokemon.adapter = adapter

        // Determinar cuál índice usar para el scroll
        val scrollToPosition = if (isFirstButton) lastSelectedPosition1 else lastSelectedPosition2
        recyclerPokemon.post {
            recyclerPokemon.scrollToPosition(scrollToPosition)
        }

        searchViewSize.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false // No hay submits
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().lowercase()
                val filteredList = pokemonList.filter { it.name.lowercase().contains(query) }
                adapter.updateList(filteredList)
                return true
            }
        })

        dialog.show()
    }

    // Funcion que se ejecuta para refrescar la comparacion
    private fun updateSizeComparison() {
        // Verificar si las imagenes ya fueron inicializadas
        if (!::firstSelectedPokemon.isInitialized || !::secondSelectedPokemon.isInitialized) return

        val pokemon1Shape = findViewById<ImageView>(R.id.pokemon1Shape)
        val pokemon2Shape = findViewById<ImageView>(R.id.pokemon2Shape)

        changePkmn1SizeText.text = firstSelectedPokemon.name.uppercase()
        changePkmn2SizeText.text = secondSelectedPokemon.name.uppercase()
        typeChangePkmn1Size.background = firstSelectedPokemon.backgroundDrawable
        typeChangePkmn2Size.background = secondSelectedPokemon.backgroundDrawable

        val height1 = firstSelectedPokemon.height.toFloat() // Altura en dm, como la API
        val height2 = secondSelectedPokemon.height.toFloat()

        // Definir el tamaño base en px para mantener la referencia
        val baseHeightInPixels = 600f

        // Hallar la mayor y menor altura
        val maxHeight = maxOf(height1, height2)
        val minHeight = minOf(height1, height2)

        // Obtener la escala entre ellos
        val scaleFactor = if (height1 == height2) {
            1f
        } else {
                minHeight / maxHeight
        }

        // Asignar tamaños respetando la proporcion
        if (height1 > height2) {
            pokemon1Shape.layoutParams.height = baseHeightInPixels.toInt()
            pokemon2Shape.layoutParams.height = (baseHeightInPixels * scaleFactor).toInt()
        } else {
            pokemon1Shape.layoutParams.height = (baseHeightInPixels * scaleFactor).toInt()
            pokemon2Shape.layoutParams.height = baseHeightInPixels.toInt()
        }

        // **Centrar el Pokémon más pequeño verticalmente respecto al más grande**
        val tallestPokemon = if (height1 > height2) pokemon1Shape else pokemon2Shape
        val shortestPokemon = if (height1 > height2) pokemon2Shape else pokemon1Shape

        val tallestY = tallestPokemon.y
        val tallestHeight = tallestPokemon.height
        val shortestHeight = shortestPokemon.height

        shortestPokemon.y = tallestY + (tallestHeight - shortestHeight) / 2

        // Cargar las imagenes actualizadas
        Glide.with(this)
            .load(firstSelectedPokemon.imageUrl)
            .into(pokemon1Shape)
        pokemon1Shape.scaleX = -1f // Invierte la imagen de la izq horizontalmente (provisional)

        Glide.with(this)
            .load(secondSelectedPokemon.imageUrl)
            .into(pokemon2Shape)

        // Asigno las stats de ambos pokemon
        sizeHP1.text = String.format(Locale.US, "%d", firstSelectedPokemon.hp)
        sizeHP2.text = String.format(Locale.US, "%d", secondSelectedPokemon.hp)
        sizeAttack1.text = String.format(Locale.US, "%d", firstSelectedPokemon.attack)
        sizeAttack2.text = String.format(Locale.US, "%d", secondSelectedPokemon.attack)
        sizeDefense1.text = String.format(Locale.US, "%d", firstSelectedPokemon.defense)
        sizeDefense2.text = String.format(Locale.US, "%d", secondSelectedPokemon.defense)
        sizeSpAttack1.text = String.format(Locale.US, "%d", firstSelectedPokemon.specialAttack)
        sizeSpAttack2.text = String.format(Locale.US, "%d", secondSelectedPokemon.specialAttack)
        sizeSpDefense1.text = String.format(Locale.US, "%d", firstSelectedPokemon.specialDefense)
        sizeSpDefense2.text = String.format(Locale.US, "%d", secondSelectedPokemon.specialDefense)
        sizeSpeed1.text = String.format(Locale.US, "%d", firstSelectedPokemon.speed)
        sizeSpeed2.text = String.format(Locale.US, "%d", secondSelectedPokemon.speed)

        // Asigno los fondos (verde, rojo o blanco) en funcion de sus stats
        // La funcion setStatBackground esta definida abajo
        setStatBackground(sizeHP1, sizeHP2, firstSelectedPokemon.hp, secondSelectedPokemon.hp)
        setStatBackground(sizeAttack1, sizeAttack2, firstSelectedPokemon.attack, secondSelectedPokemon.attack)
        setStatBackground(sizeDefense1, sizeDefense2, firstSelectedPokemon.defense, secondSelectedPokemon.defense)
        setStatBackground(sizeSpAttack1, sizeSpAttack2, firstSelectedPokemon.specialAttack, secondSelectedPokemon.specialAttack)
        setStatBackground(sizeSpDefense1, sizeSpDefense2, firstSelectedPokemon.specialDefense, secondSelectedPokemon.specialDefense)
        setStatBackground(sizeSpeed1, sizeSpeed2, firstSelectedPokemon.speed, secondSelectedPokemon.speed)

        // Refrescar las vistas
        pokemon1Shape.requestLayout()
        pokemon2Shape.requestLayout()
    }

    // Funcion que busca los datos de Bulbasur para mostrarlos por defecto al acabar la carga
    private fun initializeDefaultPokemon(pokemonList: List<Pokemon>) {
        val bulbasaur = pokemonList.find { it.id == 1 }
        if (bulbasaur != null) {
            firstSelectedPokemon = bulbasaur
            secondSelectedPokemon = bulbasaur
            updateSizeComparison()
        }
    }

    // Funcion para asignar fondos verdes/rojos a valores superiores/inferiores
    private fun setStatBackground(textView1: TextView, textView2: TextView, value1: Int, value2: Int) {
        val green = ContextCompat.getColor(textView1.context, color.green_goodStat)
        val red = ContextCompat.getColor(textView1.context, color.red_badStat)
        val white = ContextCompat.getColor(textView1.context, color.white)

        when {
            value1 > value2 -> {
                textView1.setBackgroundColor(green)
                textView2.setBackgroundColor(red)
            }
            value1 < value2 -> {
                textView1.setBackgroundColor(red)
                textView2.setBackgroundColor(green)
            }
            else -> {
                textView1.setBackgroundColor(white)
                textView2.setBackgroundColor(white)
            }
        }
    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

    // Funcion para habilitar el arrastre de las imagenes dentro de los limites
    // (esta sacado de StackOverflow)
    @SuppressLint("ClickableViewAccessibility")
    private fun enableDrag(imageView: ImageView) {
        imageView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.x - event.rawX
                    dY = view.y - event.rawY
                }
                MotionEvent.ACTION_MOVE -> {
                    view.x = event.rawX + dX
                    view.y = event.rawY + dY

                }
                MotionEvent.ACTION_UP -> {
                    view.performClick() // Llamar a performClick para evitar la advertencia
                }
            }
            true // Indicar que manejamos el evento
        }
    }
}