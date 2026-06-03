package com.garci.pokegarci.data.di

import android.content.Context
import com.garci.pokegarci.data.remote.PokeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://pokeapi.co/api/v2/"
    private const val CACHE_SIZE_BYTES = 10L * 1024 * 1024

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDirectory = File(context.cacheDir, "http_cache")
        val cache = Cache(cacheDirectory, CACHE_SIZE_BYTES)
        return OkHttpClient.Builder()
            .cache(cache)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun providePokeApiService(retrofit: Retrofit): PokeApiService {
        return retrofit.create(PokeApiService::class.java)
    }
}
