package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.remote.dto.AbilityResponse
import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.SpeciesResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeApiService {
    @GET("pokemon/{id}")
    suspend fun getPokemonDetails(@Path("id") id: Int): PokemonDetailsResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): SpeciesResponse

    @GET("ability/{name}")
    suspend fun getAbilityDetails(@Path("name") name: String): AbilityResponse
}
