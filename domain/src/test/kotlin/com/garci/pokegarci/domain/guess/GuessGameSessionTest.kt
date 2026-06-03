package com.garci.pokegarci.domain.guess

import com.garci.pokegarci.domain.model.Ability
import com.garci.pokegarci.domain.model.Pokemon
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GuessGameSessionTest {

    private val session = GuessGameSession()
    private val pikachu = samplePokemon(25, "Pikachu")
    private val bulbasaur = samplePokemon(1, "Bulbasaur")

    @Test
    fun `start shuffles pokemon and exposes first solution`() {
        session.start(listOf(pikachu, bulbasaur))

        assertEquals(2, session.totalPokemon)
        assertTrue(session.currentSolution != null)
        assertEquals(0, session.guessedCount)
        assertEquals(0, session.score)
        assertEquals(6, session.remainingLives)
    }

    @Test
    fun `wrong guess reveals hint level and reduces lives`() {
        session.start(listOf(pikachu, bulbasaur))
        val solution = session.currentSolution!!

        val outcome = session.submitGuess(samplePokemon(999, "Wrongmon"))

        assertTrue(outcome is GuessOutcome.Wrong)
        outcome as GuessOutcome.Wrong
        assertEquals(1, outcome.hintLevel)
        assertEquals("Wrongmon", outcome.wrongName)
        assertEquals(5, session.remainingLives)
    }

    @Test
    fun `correct guess awards points and advances`() {
        session.start(listOf(pikachu, bulbasaur))
        val solution = session.currentSolution!!

        val outcome = session.submitGuess(solution)

        assertTrue(outcome is GuessOutcome.Correct)
        outcome as GuessOutcome.Correct
        assertEquals(6, outcome.pointsEarned)
        assertEquals(false, outcome.completedAll)
        assertEquals(0, outcome.hintsUsedBeforeCorrect)
        assertEquals(1, session.guessedCount)
        assertEquals(6, session.score)
        assertEquals(6, session.remainingLives)
    }

    @Test
    fun `sixth wrong guess ends the game`() {
        session.start(listOf(pikachu))

        repeat(5) {
            session.submitGuess(samplePokemon(999, "Wrongmon"))
        }
        val outcome = session.submitGuess(samplePokemon(999, "Wrongmon"))

        assertEquals(GuessOutcome.Defeated, outcome)
    }

    private fun samplePokemon(id: Int, name: String): Pokemon {
        return Pokemon(
            id = id,
            name = name,
            imageUrl = "",
            type1 = "normal",
            type2 = null,
            description = "",
            hp = 1,
            attack = 1,
            defense = 1,
            specialAttack = 1,
            specialDefense = 1,
            speed = 1,
            height = 1,
            weight = 1,
            firstAbility = Ability("test", "Test"),
        )
    }
}
