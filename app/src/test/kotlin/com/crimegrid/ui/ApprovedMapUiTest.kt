package com.crimegrid.ui

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.SavedStateHandle
import com.crimegrid.data.Position
import java.io.File
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28], qualifiers="w393dp-h852dp")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ApprovedMapUiTest {
    @get:Rule val compose=createComposeRule()
    private lateinit var game: InvestigationViewModel
    @Before fun start() {
        val context=RuntimeEnvironment.getApplication()
        context.getSharedPreferences("investigation",0).edit().clear().commit()
        game=InvestigationViewModel(context,SavedStateHandle())
        game.openCase("004")
        compose.setContent { CrimeGridApp(game) }
    }
    @Test fun allRoomsAndFourCharactersRenderAndSelectionStillWorks() {
        compose.runOnIdle {
            game.drop("alma",Position(0,0)); game.drop("darío",Position(1,1))
            game.drop("irene",Position(2,2)); game.drop("tomás",Position(3,3))
        }
        compose.onNodeWithText("Alma").performScrollTo().performClick()
        compose.onNodeWithText("Alma").assertIsSelected()
        val board=compose.onNodeWithTag("room-board").performScrollTo()
        compose.onNodeWithContentDescription("Casilla A1, Alma").assertExists()
        compose.onNodeWithContentDescription("Casilla D4, tomás",ignoreCase=true).assertExists()
        compose.runOnIdle {
            val activity = androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(androidx.test.runner.lifecycle.Stage.RESUMED).first()
            val view=activity.window.decorView
            val image=android.graphics.Bitmap.createBitmap(view.width,view.height,android.graphics.Bitmap.Config.ARGB_8888)
            view.draw(android.graphics.Canvas(image))
            val target=File("build/reports/screenshots/caso004.png")
            target.parentFile!!.mkdirs()
            target.outputStream().use { image.compress(android.graphics.Bitmap.CompressFormat.PNG,100,it) }
        }
    }
    @Test fun clickMoveFollowsDoorsThenSettlesAtDestination() {
        compose.runOnIdle { game.drop("alma",Position(0,0)) }
        compose.onNodeWithText("Alma").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Casilla B2, vacía").performScrollTo().performClick()
        compose.waitForIdle()
        compose.onNodeWithContentDescription("Casilla B2, Alma").assertExists()
        compose.runOnIdle { org.junit.Assert.assertEquals(Position(1,1),game.state.value.positions["alma"]) }
    }
}
