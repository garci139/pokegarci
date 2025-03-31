package com.garci.pokegarci

import RetrofitInstance
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import com.garci.pokegarci.utils.LocaleManager
import com.garci.pokegarci.utils.vibrate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirstMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_menu)

        // Al iniciar la primera pantalla
        val btnPlay = findViewById<Button>(R.id.btnPlay)

        // Inicio de fondo animado
        val firstMenu = findViewById<ConstraintLayout>(R.id.firstMenu)
        val gradientAnimationFirst:AnimationDrawable= firstMenu.background as AnimationDrawable
        gradientAnimationFirst.setEnterFadeDuration(1500)
        gradientAnimationFirst.setExitFadeDuration(3000)
        gradientAnimationFirst.start()

        // Acción de tocar pantalla
        btnPlay.setOnClickListener {
            vibrate()
            val toMainMenu = Intent(this, MainMenuActivity::class.java)
            toMainMenu.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(toMainMenu)
        }

        // Comienza la carga de pokemon (si no se habia hecho antes)
        if (!PokemonRepository.isDataLoaded()) {
            fetchPokemonData()
        }

        // Obtener el idioma almacenado en SharedPreferences (si lo paso por intent no se cambia)
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val oldLanguage = prefs.getString("OLD_LANGUAGE", null)
        val currentLanguage = LocaleManager.getLanguage(this)

        // LOG para depuracion
        Log.d("Idioma", "OLD_LANGUAGE en SharedPreferences: $oldLanguage")
        Log.d("Idioma", "Idioma actual de la app: $currentLanguage")

        // Verificar si se ha cambiado el idioma para cambiar las descripciones y habilidades
        if (oldLanguage != null && oldLanguage != currentLanguage) {
            Log.d("Cambio de idioma", "Old language: $oldLanguage, New language: $currentLanguage. Changing descriptions.")
            fetchPokemonDescriptionsAndAbilities(currentLanguage)
            prefs.edit().remove("OLD_LANGUAGE").apply()
        }
    }

    // Funcion para aplicar idioma al inicio
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.applyLanguage(newBase))
    }

    // Funcion para obtener datos pokemon en lista de PokemonRepository
    private fun fetchPokemonData() {
        lifecycleScope.launch {
            try {
                val pokemonResponse = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.getPokemonList(251)  // Peticion a la API
                }

                val fetchedPokemons = withContext(Dispatchers.IO) {
                    pokemonResponse.results.map { pokemonInfo ->
                        async(Dispatchers.IO) {
                            try {
                                val details = RetrofitInstance.api.getPokemonDetails(pokemonInfo.name)
                                Log.d("API", "Detalles obtenidos para: ${details.name}")
                                val species = RetrofitInstance.api.getPokemonSpecies(details.id)
                                Log.d("API", "Especie obtenida para: ${details.name}")

                                val firstAbilityName = details.abilities.firstOrNull()?.ability?.name?: "Desconocido"
                                Log.d("DEBUG_FETCH", "Nombre habilidad obtenida para ${details.name}: $firstAbilityName")

                                // abilityResponse es un pack de nombres en varios idiomas
                                val abilityResponse = try {
                                    RetrofitInstance.api.getAbilityDetails(firstAbilityName.lowercase())
                                } catch (e: Exception) {
                                    Log.e("ERROR", "No se pudo obtener la habilidad para: $firstAbilityName")
                                    null
                                }

                                val type1 = details.types.getOrNull(0)?.type?.name ?: "unknown"
                                val type2 = details.types.getOrNull(1)?.type?.name
                                val imageUrl = details.sprites.front_default
                                val backgroundDrawable = getBackgroundDrawable(type1, type2)

                                // Obtener el idioma actual guardado en la app
                                val currentLanguage = LocaleManager.getLanguage(this@FirstMenuActivity)

                                // Extraer la descripcion (SELECTOR DE IDIOMA)
                                val description = species.flavor_text_entries.firstOrNull { it.language.name == currentLanguage }
                                    ?.flavor_text?.replace("\n", " ")?.replace("\u000c", " ") ?: "Error: null description"

                                // Cambiar el idioma de la habilidad (SELECTOR DE IDIOMA)
                                val abilityTranslatedName = abilityResponse?.names?.firstOrNull { it.language.name == currentLanguage }?.name
                                    ?: "Error: null ability name"
                                Log.d("API", "Habilidad obtenida para ${details.name}: $abilityTranslatedName")

                                // Extraer stats base del pokemon
                                val statsMap = details.stats.associate { stat ->
                                    stat.stat.name to stat.base_stat
                                }
                                val hp = statsMap["hp"] ?: 0
                                val attack = statsMap["attack"] ?: 0
                                val defense = statsMap["defense"] ?: 0
                                val specialAttack = statsMap["special-attack"] ?: 0
                                val specialDefense = statsMap["special-defense"] ?: 0
                                val speed = statsMap["speed"] ?: 0

                                Pokemon(
                                    id = details.id,
                                    name = details.name.capitalize(),
                                    imageUrl = imageUrl,
                                    backgroundDrawable = backgroundDrawable,
                                    type1 = type1,
                                    type2 = type2,
                                    description = description,
                                    hp = hp,
                                    attack = attack,
                                    defense = defense,
                                    specialAttack = specialAttack,
                                    specialDefense = specialDefense,
                                    speed = speed,
                                    height = details.height,
                                    weight = details.weight,
                                    firstAbility = Ability(firstAbilityName, abilityTranslatedName) // Nombre original y multiidioma
                                )
                            } catch (e: Exception) {
                                Log.e("ERROR", "Fallo al obtener datos de ${pokemonInfo.name}: ${e.message}")
                                null
                            }
                        }
                    }.awaitAll().filterNotNull() // Espero a que cargue
                }
                PokemonRepository.setPokemonList(fetchedPokemons)
                Log.d("API", "Carga completa: ${fetchedPokemons.size} Pokémon cargados.")

            } catch (e: Exception) {
                Log.e("ERROR", "Error general en la carga de datos: ${e.message}")
            }
        }
    }

    // Para los fondos segun el pokemon, se usa en fetchPokemonData
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

        return GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(color1, color2)).apply {
            shape = GradientDrawable.RECTANGLE
        }

    }

    private fun fetchPokemonDescriptionsAndAbilities(newLanguage: String) {
        lifecycleScope.launch {
            try {
                PokemonRepository.setDataLoadingState(false)
                val updatedPokemons = withContext(Dispatchers.IO) {
                    PokemonRepository.getPokemonList().map { pokemon ->
                        async(Dispatchers.IO) {
                            Log.d("DEBUG_FETCH", "Obteniendo especie: ${pokemon.name}")
                            val species = RetrofitInstance.api.getPokemonSpecies(pokemon.id)

                            Log.d("DEBUG_FETCH", "Obteniendo habilidad: ${pokemon.firstAbility.originalName}")
                            val firstAbility = RetrofitInstance.api.getAbilityDetails(pokemon.firstAbility.originalName.lowercase())

                            // Obtener la nueva descripcion en el idioma seleccionado
                            val newDescription = species.flavor_text_entries.firstOrNull { it.language.name == newLanguage }
                                ?.flavor_text?.replace("\n", " ")?.replace("\u000c", " ") ?: "Error: null description"

                            val newAbilityName = firstAbility.names.firstOrNull { it.language.name == newLanguage }?.name?: "Error: null ability"

                            Log.d("DEBUG_FETCH", "Descripción de ${pokemon.name}: $newDescription")
                            Log.d("DEBUG_FETCH", "Habilidad en $newLanguage: $newAbilityName")

                            // Actualizar la descripcion en el repositorio
                            pokemon.description = newDescription
                            pokemon.firstAbility.languageName = newAbilityName
                            pokemon
                        }
                    }.awaitAll()
                }
                PokemonRepository.setPokemonList(updatedPokemons)

            } catch (e: Exception) {
                Log.e("DEBUG_FETCH", "Error en fetchPokemonDescriptionsAndAbilities", e)
                e.printStackTrace()
            }
        }
    }

}