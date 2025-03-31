import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokeApiService {
    // Obtener lista de pokemon con limite
    @GET("pokemon")
    suspend fun getPokemonList(@Query("limit") limit: Int): PokemonListResponse

    // Obtener los detalles de un pokemon concreto
    @GET("pokemon/{name}")
    suspend fun getPokemonDetails(@Path("name") name: String): PokemonDetailsResponse

    // Obtener info de la especie (para la descripcion)
    @GET("pokemon-species/{id}")
    suspend fun getPokemonSpecies(@Path("id") id: Int): SpeciesResponse

    // Obtener info de la habilidad
    @GET("ability/{name}")
    suspend fun getAbilityDetails(@Path("name") name: String): AbilityResponse
}

// Modelo de datos para la lista de pokemon
data class PokemonListResponse(
    val results: List<PokemonInfo>
)

data class PokemonInfo(
    val name: String,
    val url: String
)

// Modelo de datos para los detalles del Pokémon
data class PokemonDetailsResponse(
    val id: Int,
    val name: String,
    val sprites: SpriteResponse,
    val types: List<TypeSlot>,
    val stats: List<Stats>,
    val height: Int,
    val weight: Int,
    val abilities: List<AbilityBasicDetails>
)

data class AbilityBasicDetails(
    val ability: AbilitySimpleName
)

data class AbilitySimpleName(
    val name: String
)

data class SpriteResponse(
    val front_default: String  // URL de la imagen
)

data class TypeSlot(
    val slot: Int,
    val type: TypeInfo
)

data class TypeInfo(
    val name: String
)

// Modelo de datos para la descripcion del pokemon
data class SpeciesResponse(
    val flavor_text_entries: List<FlavorTextEntry>
)

data class FlavorTextEntry(
    val flavor_text: String,
    val language: Language
)

data class Stats(
    val base_stat: Int,
    val stat: StatInfo
)

data class StatInfo(
    val name: String
)

data class Language(
    val name: String
)

data class AbilityResponse(
    val names: List<AbilityName>
)

data class AbilityName(
    val language: Language,
    val name: String

)


