package com.garci.pokegarci

import PokemonAdapter
import android.annotation.SuppressLint
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import okhttp3.*
import okhttp3.Call
import org.json.JSONObject
import java.io.IOException
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.garci.pokegarci.R.*
import com.google.android.material.card.MaterialCardView

class PokedexActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private val pokemonList = mutableListOf<Pokemon>()

    // Recursos para la tarjeta ampliada
    private lateinit var expandedCard: MaterialCardView
    private lateinit var expandedSubView: View
    private lateinit var expandedPokemonImage: ImageView
    private lateinit var expandedPokemonName: TextView
    private lateinit var expandedPokemonId: TextView
    private lateinit var expandedFirstTypeIcon: ImageView
    private lateinit var expandedSecondTypeIcon: ImageView
    private lateinit var closeCardButton: Button

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_pokedex)

        val pokedexLayout = findViewById<ConstraintLayout>(id.pokedexLayout)

        // Tarjeta ampliada vinculada
        expandedCard = findViewById(id.expandedPokemonCard)
        expandedPokemonImage = findViewById(id.expandedPokemonImage)
        expandedPokemonName = findViewById(id.expandedPokemonName)
        expandedPokemonId = findViewById(id.expandedPokemonId)
        expandedFirstTypeIcon = findViewById(id.expandedFirstTypeIcon)
        expandedSecondTypeIcon = findViewById(id.expandedSecondTypeIcon)
        closeCardButton = findViewById(id.closeCardButton)
        expandedSubView = findViewById(id.expandedSubView)

        // Cerrar tarjeta al hacer clic en el boton
        closeCardButton.setOnClickListener {
            recyclerView.isEnabled = true
            findViewById<View>(R.id.disableRecyclerView).visibility = View.INVISIBLE
            expandedCard.visibility = View.GONE
        }

        // Inicio del fondo animado
        val gradientAnimation: AnimationDrawable = pokedexLayout.background as AnimationDrawable
        gradientAnimation.setEnterFadeDuration(1500)
        gradientAnimation.setExitFadeDuration(3000)
        gradientAnimation.start()

        recyclerView = findViewById(id.pokedexBox)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = PokemonAdapter(pokemonList) { pokemon ->
            showExpandedCard(pokemon)
        }
        recyclerView.isVerticalScrollBarEnabled = true

        // CARGA DE LOS PRIMEROS POKEMON
        fetchPokemon("pokemon?limit=100")
    }

    // Funcion para obtener los pokemon
    private fun fetchPokemon(pokemonShown:String) {
        val url = "https://pokeapi.co/api/v2/$pokemonShown"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    val results = jsonObject.getJSONArray("results")

                    for (i in 0 until results.length()) {
                        val item = results.getJSONObject(i)
                        val name = item.getString("name")
                        val detailsUrl = item.getString("url") // URL con los detalles del Pokémon

                        // Hacer una peticion a la URL con detalles del Pokemon
                        val detailsRequest = Request.Builder().url(detailsUrl).build()
                        client.newCall(detailsRequest).enqueue(object : Callback {

                            override fun onFailure(call: Call, e: IOException) {
                                e.printStackTrace()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.body?.string()?.let { detailsJson ->
                                    val detailsObject = JSONObject(detailsJson)
                                    val typesArray = detailsObject.getJSONArray("types")

                                    val type1 = typesArray.getJSONObject(0)
                                        .getJSONObject("type").getString("name")

                                    val type2 = if (typesArray.length() > 1) {
                                        typesArray.getJSONObject(1)
                                            .getJSONObject("type").getString("name")
                                    } else {
                                        null
                                    }

                                    val imageUrl = detailsObject.getJSONObject("sprites")
                                        .getString("front_default")

                                    val backgroundDrawable = getBackgroundDrawable(type1, type2)

                                    runOnUiThread {
                                        if (!isFinishing) {
                                            val pokemonId = detailsObject.getInt("id")  // Obtener numero de pokedex
                                            pokemonList.add(Pokemon(pokemonId, name.capitalize(), imageUrl, backgroundDrawable, type1, type2))
                                            pokemonList.sortBy { it.id }
                                            Log.d("PkmnAdded", "Added: ${name.capitalize()} with ID $pokemonId")
                                            recyclerView.adapter?.notifyDataSetChanged()
                                        }
                                    }
                                }
                            }
                        })
                    }
                }
            }
        })
    }

    // Funcion para obtener el fondo segun el tipo
    private fun getBackgroundDrawable(type1: String, type2: String?): Drawable {
        val typeColors = mapOf(
            "grass" to Color.GREEN,
            "water" to Color.BLUE,
            "electric" to Color.YELLOW,
            "psychic" to Color.MAGENTA,
            "ice" to Color.CYAN,
            "flying" to Color.WHITE,
            "fire" to Color.parseColor("#FF5100"), // Naranja oscuro
            "ground" to Color.parseColor("#8B4513"), // Marron tierra
            "normal" to Color.parseColor("#D0D0D0"), // Gris suave
            "fighting" to Color.parseColor("#8B0000"), // Rojo oscuro
            "poison" to Color.parseColor("#800080"), // Purpura
            "bug" to Color.parseColor("#A8B820"), // Verde amarillento
            "rock" to Color.parseColor("#B8A038"), // Marron claro
            "ghost" to Color.parseColor("#705898"), // Purpura oscuro
            "dragon" to Color.parseColor("#7038F8"), // Azul oscurito
            "dark" to Color.parseColor("#705848"), // Marron oscuro
            "steel" to Color.parseColor("#B8B8D0"), // Gris acero
            "fairy" to Color.parseColor("#EE99AC")  // Rosa
        )

        val color1 = typeColors[type1] ?: Color.GRAY
        val color2 = type2?.let { typeColors[it] } ?: color1

        return if (type2 == null) {
            ColorDrawable(color1)
        } else {
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(color1, color2))
        }

    }

    // Funcion para mostrar la tarjeta expandida al hacer click
    private fun showExpandedCard(pokemon: Pokemon) {
        expandedPokemonName.text = pokemon.name
        expandedPokemonId.text = String.format("#%03d", pokemon.id)

        // Imagen expandida
        Glide.with(this)
            .load(pokemon.imageUrl)
            .into(expandedPokemonImage)

        // Tipos expandidos
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

        // Tarjeta expandida visible
        val safeExpandedBackground = pokemon.backgroundDrawable.constantState?.newDrawable()?.mutate()
        if (safeExpandedBackground != null) {
            expandedSubView.background = safeExpandedBackground
        } else {
            expandedSubView.setBackgroundColor(Color.GRAY) // Fondo gris en caso de error
        }

        // Busqueda de descripciones
        fetchPokemonDescription(pokemon.id)

        recyclerView.isEnabled = false
        findViewById<View>(R.id.disableRecyclerView).visibility = View.VISIBLE
        expandedCard.visibility = View.VISIBLE
    }

    // Funcion para obtener las descripciones (incluye selector de idioma)
    private fun fetchPokemonDescription(pokemonId: Int) {
        val url = "https://pokeapi.co/api/v2/pokemon-species/$pokemonId/"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    val jsonObject = JSONObject(it)
                    val flavorTextArray = jsonObject.getJSONArray("flavor_text_entries")

                    var description = "Descripción no disponible"

                    for (i in 0 until flavorTextArray.length()) {
                        val entry = flavorTextArray.getJSONObject(i)
                        val language = entry.getJSONObject("language").getString("name")

                        if (language == "es") { // FILTRO DE IDIOMA
                            description = entry.getString("flavor_text")
                                .replace("\n", " ")
                                .replace("\u000c", " ")
                            break
                        }
                    }
                    runOnUiThread {
                        findViewById<TextView>(R.id.expandedPokemonDescription).setText(description)
                    }
                }
            }
        })
    }

}