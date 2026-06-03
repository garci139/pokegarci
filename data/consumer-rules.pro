# Retrofit + Gson: modelos JSON de PokeAPI
-keep class com.garci.pokegarci.data.remote.dto.** { *; }
-keep interface com.garci.pokegarci.data.remote.PokeApiService { *; }

-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.paging.**

# Hilt en el módulo data
-keep class com.garci.pokegarci.data.di.** { *; }
-keep class com.garci.pokegarci.data.repository.PokemonRepositoryImpl { *; }
-keep class com.garci.pokegarci.data.local.** { *; }
-keep class com.garci.pokegarci.data.remote.** { *; }

# OkHttp / Retrofit (silenciar warnings habituales)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
