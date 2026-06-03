package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.remote.dto.AbilityResponse
import com.garci.pokegarci.data.remote.dto.PokemonDetailsResponse
import com.garci.pokegarci.data.remote.dto.PokemonListResponse
import com.garci.pokegarci.data.remote.dto.SpeciesResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonListResponse

    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(@Path("name") name: String): PokemonDetailsResponse

    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): SpeciesResponse

    @GET("ability/{name}")
    suspend fun getAbilityDetails(@Path("name") name: String): AbilityResponse
}
