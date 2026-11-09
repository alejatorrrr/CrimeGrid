package com.crimegrid.domain

import com.crimegrid.data.*
import org.junit.Assert.*
import org.junit.Test

class CampaignTest {
    private fun load(id: String) = CaseRepository { javaClass.getResource("/cases/case$id.json")!!.readText() }.load()
    @Test fun everyCaseHasExactlyTheOfficialSolutionAndAnOfficeCulprit() {
        for (id in listOf("001", "002", "003", "004")) {
            val case = load(id)
            assertEquals(listOf(case.solution), Engine.solve(case))
            assertNotNull(Engine.culprit(case, case.solution.positions))
        }
    }
    @Test fun hintsAreProvenByTheirCitedCluesWithoutPlayerAssumptions() {
        for (id in listOf("001", "002", "003", "004")) {
            val case = load(id)
            assertEquals(3, case.hints.size)
            for (hint in case.hints) {
                val subset = case.copy(clues = hint.clueIndices.map { case.clues[it] })
                val possibilities = Engine.solve(subset, 1000)
                assertTrue(possibilities.isNotEmpty())
                for (solution in possibilities) {
                    hint.subject?.let { subject ->
                        hint.row?.let { assertEquals(it, solution.positions[subject]!!.row) }
                        hint.column?.let { assertEquals(it, solution.positions[subject]!!.column) }
                    }
                }
            }
        }
    }
    @Test fun advancedCasesDoNotGiveExactRowsOrColumns() {
        for (id in listOf("002", "003", "004")) {
            assertTrue(load(id).clues.none { it.type in setOf(ClueType.ROW, ClueType.COLUMN) })
        }
    }
    @Test fun newRulesHandleBothDirectionsAndPartialBoards() {
        val positions = mapOf("a" to Position(0,3), "b" to Position(2,1))
        val correct = listOf(
            Clue("", ClueType.ROW_BEFORE, "a", 0, "b"),
            Clue("", ClueType.COLUMN_BEFORE, "b", 0, "a"),
            Clue("", ClueType.COLUMN_OFFSET, "a", 2, "b"),
            Clue("", ClueType.ROW_DISTANCE, "a", 2, "b"),
            Clue("", ClueType.COLUMN_DISTANCE, "a", 2, "b"),
            Clue("", ClueType.NOT_ROW, "a", 1), Clue("", ClueType.NOT_COLUMN, "b", 0)
        )
        correct.forEach { assertTrue(Engine.satisfies(it, positions)); assertTrue(Engine.satisfies(it, emptyMap())) }
        assertFalse(Engine.satisfies(correct[0].copy(subject="b", reference="a"), positions))
        assertFalse(Engine.satisfies(correct[2].copy(value=-2), positions))
        assertFalse(Engine.satisfies(correct[3].copy(value=1), positions))
        assertFalse(Engine.satisfies(correct[5].copy(value=0), positions))
    }

    @Test fun mansionHasTwelveRoomsFourOutdoorZonesAndTheRightOffice() {
        val case = load("001")
        val rooms = case.scene!!.rooms
        assertEquals(16, rooms.map { it.position }.toSet().size)
        assertEquals(12, rooms.count { !it.exterior })
        assertEquals(4, rooms.count { it.exterior && it.position.row == 3 })
        assertEquals("Despacho", rooms.single { it.position == case.office }.name)
    }
}
