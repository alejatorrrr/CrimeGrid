package com.crimegrid.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], qualifiers = "w393dp-h852dp")
class CrimeGridUiTest {
    @get:Rule val compose = createComposeRule()
    @Before fun start() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("investigation", 0).edit().clear().commit()
        val model = InvestigationViewModel(context, SavedStateHandle())
        compose.setContent { CrimeGridApp(model) }
    }

    @Test fun playerCanSolveCaseUsingVisibleControls() {
        compose.onNodeWithText("Jugar").performClick()
        val placements = listOf("Bruno" to "B1", "Clara" to "D2", "Damián" to "A3", "Elías" to "C4")
        for ((name, label) in placements) {
            compose.onNodeWithText(name).performScrollTo().performClick()
            val description = "Casilla $label${if (label == "D2") ", despacho" else ""}, vacía"
            compose.onNodeWithContentDescription(description).performScrollTo().performClick()
        }
        compose.onNodeWithText("Comprobar").performScrollTo().performClick()
        compose.onNodeWithText("Culpable: Clara").assertIsDisplayed()
        compose.onNodeWithText("Jugar de nuevo").performScrollTo().performClick()
        compose.onNodeWithText("SOSPECHOSOS · 0/4").performScrollTo().assertIsDisplayed()
    }

    @Test fun playerCanMoveAndRemoveSuspect() {
        compose.onNodeWithText("Jugar").performClick()
        compose.onNodeWithText("Bruno").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla B1, vacía").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla C2, vacía").performScrollTo().performClick()
        compose.onNodeWithText("Colocado · C2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retirar seleccionado").performScrollTo().performClick()
        compose.onNodeWithText("SOSPECHOSOS · 0/4").performScrollTo().assertIsDisplayed()
    }

    @Test fun playerCanChooseCaseAndRequestOptionalHelp() {
        compose.onNodeWithText("Abrir caso 003").performScrollTo().performClick()
        compose.onNodeWithText("La función sin aplausos").assertExists()
        compose.onNodeWithText("Pedir ayuda (0/3)").performScrollTo().performClick()
        compose.onNodeWithText("Pedir ayuda (1/3)").assertExists()
        compose.onNodeWithText("Menú").performScrollTo().performClick()
        compose.onNodeWithText("En curso · 0/4 colocados · 1 ayuda").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Abrir caso 002").performScrollTo().performClick()
        compose.onNodeWithText("Pedir ayuda (0/3)").performScrollTo().assertIsDisplayed()
    }
    @Test fun rejectedCellExposesAccessibleFeedbackAndLeavesBoardUntouched() {
        compose.onNodeWithText("Jugar").performClick()
        compose.onNodeWithText("Bruno").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla B1, vacía").performScrollTo().performClick()
        compose.onNodeWithText("Clara").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla D1, vacía").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla D1, vacía").assert(SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Movimiento rechazado"))
        compose.onNodeWithText("SOSPECHOSOS · 1/4").performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Casilla D2, despacho, vacía").performScrollTo().performClick()
        compose.onNodeWithText("SOSPECHOSOS · 2/4").performScrollTo().assertIsDisplayed()
    }

    private fun dragBetween(from: String, to: String) {
        val source = compose.onNodeWithContentDescription(from)
        val destination = compose.onNodeWithContentDescription(to)
        source.performScrollTo(); destination.performScrollTo()
        val delta = destination.fetchSemanticsNode().boundsInRoot.center - source.fetchSemanticsNode().boundsInRoot.center
        source.performTouchInput {
            down(center); advanceEventTime(700)
            moveTo(center + delta, delayMillis = 200); up()
        }
    }
    @Test fun longPressDragMovesCharacterAndRejectsConflictingRoom() {
        compose.onNodeWithText("Jugar").performClick()
        compose.onNodeWithText("Bruno").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla B1, vacía").performScrollTo().performClick()
        dragBetween("Casilla B1, Bruno", "Casilla C2, vacía")
        compose.onNodeWithText("Colocado · C2").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Clara").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla D1, vacía").performScrollTo().performClick()
        dragBetween("Casilla C2, Bruno", "Casilla D3, vacía")
        compose.onNodeWithContentDescription("Casilla C2, Bruno").assertExists()
        compose.onNodeWithContentDescription("Casilla D3, vacía").assert(SemanticsMatcher.expectValue(
            androidx.compose.ui.semantics.SemanticsProperties.StateDescription, "Movimiento rechazado"))
    }

    @Test fun pendingCharacterCanBeDraggedFromNamePanel() {
        compose.onNodeWithText("Jugar").performClick()
        val source = compose.onNodeWithText("Bruno")
        val destination = compose.onNodeWithContentDescription("Casilla B1, vacía")
        source.performScrollTo(); destination.performScrollTo()
        val delta = destination.fetchSemanticsNode().boundsInRoot.center - source.fetchSemanticsNode().boundsInRoot.center
        source.performTouchInput {
            down(center); advanceEventTime(700); moveTo(center + delta, delayMillis = 200); up()
        }
        compose.onNodeWithContentDescription("Casilla B1, Bruno").assertExists()
    }
}
