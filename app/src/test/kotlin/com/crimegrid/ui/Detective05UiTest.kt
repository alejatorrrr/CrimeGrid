package com.crimegrid.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.lifecycle.SavedStateHandle
import com.crimegrid.data.Position
import java.io.File
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28],qualifiers="w393dp-h852dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class Detective05UiTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var model:InvestigationViewModel
    @Before fun start(){
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("investigation",0).edit().clear().commit()
        model=InvestigationViewModel(context,SavedStateHandle());model.openCase("004")
        compose.setContent{CrimeGridApp(model)}
    }
    private fun screenshot(name:String){
        compose.runOnIdle{
            val activity=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).first()
            val view=activity.window.decorView
            val image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            val file=File("build/reports/screenshots/05-$name.png");file.parentFile!!.mkdirs()
            file.outputStream().use{image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
        }
    }
    @Test fun completeMansionIsSquareContinuousAndHasNoMapText(){
        val map=compose.onNodeWithTag("room-board").fetchSemanticsNode().boundsInRoot
        assertEquals(map.width,map.height,.1f)
        for(row in 1..4)for(column in 'A'..'D'){
            val node=compose.onNodeWithTag("map-cell-$column$row").fetchSemanticsNode().boundsInRoot
            if(column<'D'){
                val next=compose.onNodeWithTag("map-cell-${column+1}$row").fetchSemanticsNode().boundsInRoot
                assertEquals(node.right,next.left,.1f)
            }
            if(row<4){
                val next=compose.onNodeWithTag("map-cell-$column${row+1}").fetchSemanticsNode().boundsInRoot
                assertEquals(node.bottom,next.top,.1f)
            }
        }
        compose.onNodeWithText("A1").assertDoesNotExist()
        compose.onNodeWithText("Biblioteca").assertDoesNotExist()
        compose.onNodeWithText("Despacho").assertDoesNotExist()
        compose.onNodeWithText("EXTERIOR").assertDoesNotExist()
        compose.runOnIdle{
            model.drop("alma",Position(0,0));model.drop("darío",Position(1,1))
            model.drop("irene",Position(2,2));model.drop("tomás",Position(3,3))
        }
        screenshot("tablero")
    }
    @Test fun tapZoomDoesNotMoveAnyoneAndSelectedTapStillPlaces(){
        val before=compose.onNodeWithTag("map-cell-A1").fetchSemanticsNode().boundsInRoot.width
        compose.onNodeWithTag("map-cell-A1").performClick()
        compose.waitForIdle()
        compose.onNodeWithTag("room-board").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Vista ampliada"))
        val after=compose.onNodeWithTag("map-cell-A1").fetchSemanticsNode().boundsInRoot.width
        assertTrue(after>before*2)
        compose.runOnIdle{assertTrue(model.state.value.positions.isEmpty());assertNull(model.state.value.selectedSuspect)}
        screenshot("zoom")
        compose.onNodeWithText("Vista general").performScrollTo().performClick()
        compose.onNodeWithText("Alma").performScrollTo().performClick()
        compose.onNodeWithTag("map-cell-B2").performScrollTo().performClick()
        compose.runOnIdle{assertEquals(Position(1,1),model.state.value.positions["alma"])}
        compose.onNodeWithTag("room-board").assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Vista general"))
    }
    @Test fun notebookCyclesReopensAndPersistsWithoutJudgingClues(){
        compose.onNodeWithText("Cuaderno").performScrollTo().performClick()
        compose.onNodeWithText("Pista 1: Pendiente").performScrollTo().performClick()
        compose.onNodeWithText("Pista 1: Leída").performClick()
        compose.onNodeWithText("Pista 1: Marcada").assertExists()
        screenshot("cuaderno")
        compose.onNodeWithText("Cerrar cuaderno").performClick()
        compose.onNodeWithText("Cuaderno").performScrollTo().performClick()
        compose.onNodeWithText("Pista 1: Marcada").performScrollTo().assertIsDisplayed()
        compose.runOnIdle{
            val restored=InvestigationViewModel(RuntimeEnvironment.getApplication(),SavedStateHandle())
            assertEquals(setOf(0),restored.state.value.markedClues)
            assertTrue(restored.state.value.positions.isEmpty())
        }
    }
    @Test fun draggingFromPanelAndInsideZoomPreservesRules(){
        val source=compose.onNodeWithText("Alma");val destination=compose.onNodeWithTag("map-cell-A1")
        source.performScrollTo();destination.performScrollTo()
        var delta=destination.fetchSemanticsNode().boundsInRoot.center-source.fetchSemanticsNode().boundsInRoot.center
        source.performTouchInput{down(center);advanceEventTime(700);moveTo(center+delta,delayMillis=200);up()}
        compose.runOnIdle{assertEquals(Position(0,0),model.state.value.positions["alma"]);model.clearSelection()}
        compose.onNodeWithTag("map-cell-A1").performClick()
        val from=compose.onNodeWithTag("map-cell-A1");val to=compose.onNodeWithTag("map-cell-B2")
        delta=to.fetchSemanticsNode().boundsInRoot.center-from.fetchSemanticsNode().boundsInRoot.center
        from.performTouchInput{down(center);advanceEventTime(700);moveTo(center+delta,delayMillis=200);up()}
        compose.runOnIdle{
            assertEquals(Position(1,1),model.state.value.positions["alma"])
            model.drop("darío",Position(0,0))
        }
        val blocked=compose.onNodeWithTag("map-cell-A2")
        val current=compose.onNodeWithTag("map-cell-B2")
        delta=blocked.fetchSemanticsNode().boundsInRoot.center-current.fetchSemanticsNode().boundsInRoot.center
        current.performTouchInput{down(center);advanceEventTime(700);moveTo(center+delta,delayMillis=200);up()}
        compose.runOnIdle{assertEquals(Position(1,1),model.state.value.positions["alma"]);assertNotNull(model.feedback.value)}
        current.performTouchInput{down(center);advanceEventTime(700);moveTo(androidx.compose.ui.geometry.Offset(-600f,center.y),delayMillis=200);up()}
        compose.runOnIdle{assertEquals(Position(1,1),model.state.value.positions["alma"])}
    }
    @Test fun resolutionUsesTheRealCulpritLocationAndClues(){
        compose.runOnIdle{
            for((id,position)in model.case.solution.positions)model.drop(id,position)
            model.check()
        }
        compose.onNodeWithText("CASO RESUELTO").assertExists()
        compose.onNodeWithText("Culpable: Tomás").assertExists()
        compose.onNodeWithText("Despacho · Palacio de Niebla").assertExists()
        compose.onNodeWithText(model.case.explanation).assertExists()
        screenshot("resolucion")
    }

    @Test @Config(sdk=[28],qualifiers="w320dp-h568dp")
    fun notebookIsScrollableOnSmallPhonesAndBlocksMapTouches(){
        compose.onNodeWithText("Cuaderno").performScrollTo().performClick()
        compose.onNodeWithTag("notebook").performTouchInput{click(androidx.compose.ui.geometry.Offset(100f,260f))}
        compose.onNodeWithText("Pista 8: Pendiente").performScrollTo().performClick()
        compose.onNodeWithText("Pista 8: Leída").assertExists()
        compose.onNodeWithText("Cerrar cuaderno").performClick()
        compose.onNodeWithTag("room-board").performScrollTo().assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription,"Vista general"))
        compose.runOnIdle{assertTrue(model.state.value.positions.isEmpty())}
    }
}
