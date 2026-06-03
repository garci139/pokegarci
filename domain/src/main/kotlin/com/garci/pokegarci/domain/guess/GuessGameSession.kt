package com.garci.pokegarci.domain.guess

import com.garci.pokegarci.domain.model.Pokemon

sealed interface GuessOutcome {
    data class Wrong(val hintLevel: Int, val wrongName: String) : GuessOutcome
    data class Correct(
        val pointsEarned: Int,
        val completedAll: Boolean,
        val hintsUsedBeforeCorrect: Int,
    ) : GuessOutcome
    data object Defeated : GuessOutcome
}

class GuessGameSession {

    private var tryCount = 0
    private var solutionIndex = 0
    private var possiblePoints = MAX_LIVES
    private var accumulatedScore = 0
    private var randomList: List<Pokemon> = emptyList()

    val guessedCount: Int get() = solutionIndex
    val score: Int get() = accumulatedScore
    val totalPokemon: Int get() = randomList.size
    val remainingLives: Int get() = possiblePoints
    val currentSolution: Pokemon?
        get() = randomList.getOrNull(solutionIndex)

    fun start(allPokemon: List<Pokemon>) {
        tryCount = 0
        solutionIndex = 0
        possiblePoints = MAX_LIVES
        accumulatedScore = 0
        randomList = allPokemon.shuffled()
    }

    fun submitGuess(guess: Pokemon): GuessOutcome {
        val solution = currentSolution ?: return GuessOutcome.Defeated

        if (guess.id != solution.id) {
            if (tryCount < MAX_LIVES - 1) {
                tryCount++
                possiblePoints--
                return GuessOutcome.Wrong(tryCount, guess.name)
            }
            return GuessOutcome.Defeated
        }

        val pointsEarned = possiblePoints
        val hintsUsedBeforeCorrect = tryCount
        accumulatedScore += pointsEarned
        solutionIndex++
        val completedAll = solutionIndex >= randomList.size
        tryCount = 0
        possiblePoints = MAX_LIVES
        return GuessOutcome.Correct(
            pointsEarned = pointsEarned,
            completedAll = completedAll,
            hintsUsedBeforeCorrect = hintsUsedBeforeCorrect,
        )
    }

    private companion object {
        const val MAX_LIVES = 6
    }
}
