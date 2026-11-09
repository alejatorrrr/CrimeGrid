package com.crimegrid.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidBitmap
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
class CastleInspectorTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var model:InvestigationViewModel
    @Before fun start(){
        val context=RuntimeEnvironment.getApplication();context.getSharedPreferences("investigation",0).edit().clear().commit()
        model=InvestigationViewModel(context,SavedStateHandle());model.openCase("003")
        compose.setContent{CrimeGridApp(model)}
    }
    private fun capture(name:String){compose.runOnIdle{
        val activity=androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
            .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).first()
        val view=activity.window.decorView
        val image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
        view.draw(android.graphics.Canvas(image));val file=File("build/reports/screenshots/castle-$name.png")
        file.parentFile!!.mkdirs();file.outputStream().use{image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it)}
    }}
    @Test fun originalTilesComposeExactlyAndMapIsContinuous(){
        val assets=RuntimeEnvironment.getApplication().assets
        val map=CastleArtwork.manifest(assets)
        assertEquals("005",map.sourceCaseId)
        val atlas=CastleArtwork.atlas(assets,map)
        assertEquals(1536,atlas.width);assertEquals(1536,atlas.height)
        for(room in map.rooms){
            val tile=PixelAssets.bitmap(assets,CastleArtwork.directory+room.file)!!
            assertEquals(384,tile.width);assertEquals(384,tile.height)
            val expected=IntArray(384*384);val actual=IntArray(384*384)
            tile.getPixels(expected,0,384,0,0,384,384)
            atlas.asAndroidBitmap().getPixels(actual,0,384,room.sourceCrop[0],room.sourceCrop[1],384,384)
            assertArrayEquals("Original pixels must be unchanged: ${room.id}",expected,actual)
        }
        assertThrows(IllegalArgumentException::class.java){map.copy(rooms=map.rooms.dropLast(1)).validate()}
        compose.onNodeWithText("El castillo encantado").assertExists()
        compose.onNodeWithText("Biblioteca maldita").assertDoesNotExist()
        compose.onNodeWithText("A1").assertDoesNotExist()
        val a=compose.onNodeWithTag("map-cell-A1").fetchSemanticsNode().boundsInRoot
        val b=compose.onNodeWithTag("map-cell-B1").fetchSemanticsNode().boundsInRoot
        assertEquals(a.right,b.left,.1f)
        capture("mapa")
    }
    @Test fun emptyRoomLongPressOpensReadOnlyPinchAndPanInspector(){
        compose.onNodeWithTag("map-cell-A1").performTouchInput{longClick()}
        compose.onNodeWithTag("room-inspector").assertExists()
        compose.onNodeWithText("Biblioteca maldita").assertDoesNotExist()
        compose.onNodeWithText("A1").assertDoesNotExist()
        val viewer=compose.onNodeWithTag("inspector-image")
        viewer.performTouchInput{
            down(0,center-Offset(30f,0f));down(1,center+Offset(30f,0f))
            repeat(8){step->
                updatePointerTo(0,center-Offset(30f+step*12f,0f));updatePointerTo(1,center+Offset(30f+step*12f,0f));move()
            }
            up(0);up(1)
        }
        assertNotEquals("100%",viewer.fetchSemanticsNode().config[androidx.compose.ui.semantics.SemanticsProperties.StateDescription])
        viewer.performTouchInput{swipe(center,center+Offset(40f,35f))}
        capture("visor")
        compose.onNodeWithText("Cerrar visor").performClick()
        compose.onNodeWithTag("room-inspector").assertDoesNotExist()
        compose.runOnIdle{assertTrue(model.state.value.positions.isEmpty());assertNull(model.state.value.selectedSuspect)}
    }
    @Test fun dragWinsOnCharacterButFloorCanInspectWithoutSelection(){
        compose.onNodeWithText("Ada").performScrollTo().performClick()
        compose.onNodeWithTag("map-cell-A1").performScrollTo().performClick()
        compose.runOnIdle{model.clearSelection()}
        val from=compose.onNodeWithTag("map-cell-A1");val to=compose.onNodeWithTag("map-cell-B2")
        val delta=to.fetchSemanticsNode().boundsInRoot.center-from.fetchSemanticsNode().boundsInRoot.center
        from.performTouchInput{down(center);advanceEventTime(700);moveTo(center+delta,delayMillis=200);up()}
        compose.onNodeWithTag("room-inspector").assertDoesNotExist()
        compose.runOnIdle{assertEquals(Position(1,1),model.state.value.positions["ada"]);model.clearSelection()}
        compose.onNodeWithTag("map-cell-B2").performTouchInput{longClick(Offset(3f,3f))}
        compose.onNodeWithTag("room-inspector").assertExists()
        compose.runOnIdle{assertEquals(Position(1,1),model.state.value.positions["ada"])}
    }
    @Test fun selectedSuspectBlocksInspectionAndTapStillPlaces(){
        compose.onNodeWithText("Gael").performScrollTo().performClick()
        val cell=compose.onNodeWithTag("map-cell-C2").performScrollTo()
        cell.performTouchInput{longClick(Offset(3f,3f))}
        compose.onNodeWithTag("room-inspector").assertDoesNotExist()
        cell.performClick()
        compose.runOnIdle{assertEquals(Position(1,2),model.state.value.positions["gael"])}
    }
}
