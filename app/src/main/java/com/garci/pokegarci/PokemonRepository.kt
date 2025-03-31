package com.garci.pokegarci

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

object PokemonRepository {
    private val pokemonList = mutableListOf<Pokemon>()
    private val _isDataLoaded = MutableLiveData<Boolean>(false)
    val isDataLoaded: LiveData<Boolean> get() = _isDataLoaded

    fun setPokemonList(pokemons: List<Pokemon>) {
        _isDataLoaded.postValue(false)
        pokemonList.clear()
        pokemonList.addAll(pokemons)
        _isDataLoaded.postValue(true)
    }

    fun getPokemonList(): List<Pokemon> = pokemonList

    fun isDataLoaded(): Boolean = pokemonList.isNotEmpty()

    // Funcion para forzar el estado de Loading, se usa para cuando la lista
    // esta completa pero hay que actualizar datos (descripciones, por ahora)
    fun setDataLoadingState(isLoading: Boolean) {
        _isDataLoaded.postValue(isLoading)
    }

}
