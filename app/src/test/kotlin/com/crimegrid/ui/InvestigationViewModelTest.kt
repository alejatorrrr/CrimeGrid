package com.crimegrid.ui

import androidx.lifecycle.SavedStateHandle
import com.crimegrid.data.Position
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class InvestigationViewModelTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Before fun clearProgress() { context.getSharedPreferences("investigation", 0).edit().clear().commit() }
    private fun model(handle: SavedStateHandle = SavedStateHandle()) = InvestigationViewModel(context, handle)

    @Test fun savedStateRestoresSelectionBoardAndScreen() {
        val handle = SavedStateHandle()
        val first = model(handle)
        first.play(); first.select("bruno"); first.place(Position(0, 1))
        val recreated = model(SavedStateHandle(mapOf("progress" to handle.get<String>("progress"))))
        assertEquals(first.state.value, recreated.state.value)
    }
    @Test fun coldStartRestoresLocalProgress() {
        val first = model()
        first.play(); first.select("clara"); first.place(Position(1, 3))
        assertEquals(first.state.value, model().state.value)
    }
    @Test fun onlyCorrectReconstructionOpensResultAndResetPersists() {
        val game = model()
        game.play(); game.check()
        assertEquals("investigation", game.state.value.screen)
        for ((id, position) in game.case.solution.positions) { game.select(id); game.place(position) }
        game.check()
        assertEquals("result", game.state.value.screen)
        assertEquals("result", model().state.value.screen)
        game.reset()
        assertTrue(model().state.value.positions.isEmpty())
        assertEquals("investigation", game.state.value.screen)
    }
    @Test fun corruptedSaveFallsBackToEmptyBoard() {
        val game = model(SavedStateHandle(mapOf("progress" to "broken json")))
        assertTrue(game.state.value.positions.isEmpty())
    }

    @Test fun casesKeepIndependentBoardsHintsAndReviewedClues() {
        val game = model()
        game.openCase("002"); game.select("inés"); game.place(Position(0, 0))
        game.requestHint(); game.toggleClue(0)
        game.openCase("003"); game.select("ada"); game.place(Position(1, 2))
        assertEquals(0, game.state.value.hintsRevealed)
        game.openCase("002")
        assertEquals(mapOf("inés" to Position(0, 0)), game.state.value.positions)
        assertEquals(1, game.state.value.hintsRevealed)
        assertEquals(setOf(0), game.state.value.reviewedClues)
        assertEquals("002", model().state.value.caseId)
        game.reset()
        assertTrue(game.state.value.positions.isEmpty())
        assertEquals(0, game.state.value.hintsRevealed)
        assertEquals(mapOf("ada" to Position(1, 2)), game.progress("003").positions)
    }
    @Test fun repeatedInvalidTapsGenerateFeedbackAndNeverMoveCharacters() {
        val game = model()
        game.play(); game.select("bruno"); game.place(Position(0, 1))
        assertNull(game.feedback.value)
        game.select("clara"); game.place(Position(0, 3))
        val first = game.feedback.value!!
        assertEquals(Position(0, 3), first.position)
        game.place(Position(0, 3))
        assertTrue(game.feedback.value!!.sequence > first.sequence)
        assertEquals(mapOf("bruno" to Position(0, 1)), game.state.value.positions)
        game.place(Position(1, 3))
        assertNull(game.feedback.value)
        assertEquals(2, game.state.value.positions.size)
    }
    @Test fun hintsStopAtLastLevelAndSolvedBadgeSurvivesReplay() {
        val game = model()
        game.openCase("004")
        repeat(5) { game.requestHint() }
        assertEquals(3, game.state.value.hintsRevealed)
        for ((id, pos) in game.case.solution.positions) { game.select(id); game.place(pos) }
        game.check(); game.reset(); game.menu()
        assertTrue(game.progress("004").solved)
        assertTrue(model().progress("004").solved)
    }
    @Test fun oldVersionSaveIsMigratedWithoutLosingPlacements() {
        context.getSharedPreferences("investigation", 0).edit().putString("case001",
            """{"positions":{"bruno":{"row":0,"column":1}},"selectedSuspect":"bruno","screen":"investigation","message":""}""").commit()
        val game = model()
        assertEquals(Position(0, 1), game.state.value.positions["bruno"])
        game.openCase("002"); game.openCase("001")
        assertEquals(Position(0, 1), game.state.value.positions["bruno"])
    }

    @Test fun notebookCyclesThreeStatesAndPersistsIndependently(){
        val game=model();game.openCase("004")
        game.cycleNotebookClue(0)
        assertEquals(setOf(0),game.state.value.reviewedClues)
        assertTrue(game.state.value.markedClues.isEmpty())
        game.cycleNotebookClue(0)
        assertEquals(setOf(0),game.state.value.markedClues)
        assertEquals(setOf(0),model().state.value.markedClues)
        game.openCase("002")
        assertTrue(game.state.value.markedClues.isEmpty())
        game.openCase("004")
        assertEquals(setOf(0),game.state.value.markedClues)
        game.cycleNotebookClue(0)
        assertTrue(game.state.value.reviewedClues.isEmpty())
        assertTrue(game.state.value.markedClues.isEmpty())
    }
    @Test fun notebookAnnotationsNeverChangeBoardOrVictoryAndResetClearsThem(){
        val game=model();game.openCase("004")
        for((id,pos) in game.case.solution.positions)game.drop(id,pos)
        val positions=game.state.value.positions
        repeat(2){game.cycleNotebookClue(1)}
        assertEquals(positions,game.state.value.positions)
        game.check();assertEquals("result",game.state.value.screen)
        game.reset();assertTrue(game.state.value.markedClues.isEmpty())
    }
    @Test fun explicitDeselectionNeverMovesSuspects(){
        val game=model();game.openCase("004");game.drop("alma",com.crimegrid.data.Position(0,0))
        val positions=game.state.value.positions
        game.clearSelection();assertNull(game.state.value.selectedSuspect)
        assertEquals(positions,game.state.value.positions)
    }
}
