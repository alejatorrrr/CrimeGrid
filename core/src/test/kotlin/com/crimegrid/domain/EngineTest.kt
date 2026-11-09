package com.crimegrid.domain

import com.crimegrid.data.*
import org.junit.Assert.*
import org.junit.Test

class EngineTest {
    private val case = CaseRepository { javaClass.getResource("/cases/case001.json")!!.readText() }.load()
    @Test fun officialSatisfiesEveryClue() { assertTrue(Engine.complete(case, case.solution.positions)); case.clues.forEach { assertTrue(Engine.satisfies(it, case.solution.positions)) } }
    @Test fun exactlyOneSolution() { assertEquals(listOf(case.solution), Engine.solve(case)) }
    @Test fun rejectsRowAndColumnConflicts() {
        assertFalse(Engine.valid(case, mapOf("bruno" to Position(0,0), "clara" to Position(0,1))))
        assertFalse(Engine.valid(case, mapOf("bruno" to Position(0,0), "clara" to Position(1,0))))
        val state = InvestigationState(mapOf("bruno" to Position(0,0)))
        assertEquals(state.positions, Engine.place(case, state, "clara", Position(0,1)).positions)
    }
    @Test fun movingNeverDuplicates() {
        val first = Engine.place(case, InvestigationState(), "bruno", Position(0,0))
        val moved = Engine.place(case, first, "bruno", Position(2,3))
        assertEquals(mapOf("bruno" to Position(2,3)), moved.positions)
        assertTrue(Engine.remove(moved, "bruno").positions.isEmpty())
    }
    @Test fun wrongAndIncompleteDoNotComplete() {
        assertFalse(Engine.complete(case, emptyMap()))
        assertFalse(Engine.complete(case, case.solution.positions + ("bruno" to Position(0,2)) + ("elías" to Position(3,1))))
    }
    @Test fun resetClearsAllPositions() { assertTrue(Engine.reset(InvestigationState(case.solution.positions)).positions.isEmpty()) }
    @Test fun culpritOccupiesOffice() { assertEquals("Clara", Engine.culprit(case, case.solution.positions)?.name) }
    @Test fun rejectsOutsideAndUnknown() { assertFalse(Engine.valid(case, mapOf("bruno" to Position(4,0)))); assertFalse(Engine.valid(case, mapOf("unknown" to Position(0,0)))) }
    @Test fun supportsLargerBoards() {
        for (size in 5..6) {
            val expanded = case.copy(size = size)
            assertEquals(2, Engine.solve(expanded).size) // More columns make the original clues ambiguous.
            val constrained = expanded.copy(clues = expanded.clues + Clue("Columna de Elías", ClueType.COLUMN, "elías", 2))
            assertEquals(listOf(case.solution), Engine.solve(constrained))
        }
    }
    @Test fun stateSerializationRoundTrip() {
        val state = InvestigationState(case.solution.positions, "clara", "result")
        val encoded = kotlinx.serialization.json.Json.encodeToString(InvestigationState.serializer(), state)
        assertEquals(state, kotlinx.serialization.json.Json.decodeFromString(InvestigationState.serializer(), encoded))
    }
}
