package com.garci.pokegarci.data.remote

import com.garci.pokegarci.data.local.dao.AbilityDao
import com.garci.pokegarci.data.local.entity.AbilityNameEntity
import com.garci.pokegarci.data.mapper.AbilityNameFormatter
import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbilityTranslationService @Inject constructor(
    private val api: PokeApiService,
    private val abilityDao: AbilityDao,
) {

    suspend fun ensureAllCached(originalNames: Set<String>) = coroutineScope {
        if (originalNames.isEmpty()) return@coroutineScope

        val normalizedNames = originalNames.map(::normalizeOriginalName).toSet()
        val cached = abilityDao.getCachedOriginalNames().toSet()
        val missing = normalizedNames - cached

        missing.map { originalName ->
            async { fetchAndCache(originalName) }
        }.awaitAll()
    }

    suspend fun applyAbilityLanguage(pokemon: Pokemon, language: String): Pokemon {
        val localizedAbilities = pokemon.abilities.map { ability ->
            ability.copy(displayName = resolveDisplayName(ability.originalName, language))
        }
        return pokemon.copy(abilities = localizedAbilities)
    }

    private suspend fun resolveDisplayName(originalName: String, language: String): String {
        val normalizedName = normalizeOriginalName(originalName)
        abilityDao.getDisplayName(normalizedName, language)?.let { return it }

        fetchAndCache(normalizedName)
        return abilityDao.getDisplayName(normalizedName, language)
            ?: AbilityNameFormatter.format(originalName)
    }

    private suspend fun fetchAndCache(originalName: String) {
        val response = api.getAbilityDetails(originalName)
        abilityDao.insertAll(
            response.names.map { nameEntry ->
                AbilityNameEntity(
                    originalName = originalName,
                    language = nameEntry.language.name,
                    displayName = nameEntry.name,
                )
            },
        )
    }

    private fun normalizeOriginalName(originalName: String): String {
        return originalName.lowercase()
    }
}
