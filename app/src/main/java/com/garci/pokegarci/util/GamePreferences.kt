package com.garci.pokegarci.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GamePreferences @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getHighscore(): Int = prefs.getInt(HIGHSCORE_KEY, 0)

    fun saveHighscoreIfRecord(newScore: Int): Int {
        val currentRecord = getHighscore()
        if (newScore > currentRecord) {
            prefs.edit().putInt(HIGHSCORE_KEY, newScore).apply()
            return newScore
        }
        return currentRecord
    }

    private companion object {
        const val PREFS_NAME = "game_prefs"
        const val HIGHSCORE_KEY = "highscore"
    }
}
