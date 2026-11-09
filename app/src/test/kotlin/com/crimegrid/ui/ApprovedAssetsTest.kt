package com.crimegrid.ui

import android.graphics.BitmapFactory
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk=[28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ApprovedAssetsTest {
    private val context get() = RuntimeEnvironment.getApplication()
    @Test fun allSixteenOriginalRoomsDecodeAtTheirCoordinates() {
        val game = InvestigationViewModel(context,SavedStateHandle())
        val scene = game.cases.first { it.id=="004" }.scene!!
        assertEquals(16,scene.rooms.size)
        val expected = listOf("Biblioteca","Despacho","Dormitorio","Baño","Sala de estar","Vestíbulo","Galería","Invernadero","Estrategia","Cocina","Bodega","Música","Tenis","Entrada","Jardín","Cabaña")
        val sorted = scene.rooms.sortedBy { it.position.row * 4 + it.position.column }
        assertEquals(expected,sorted.map { it.name })
        sorted.forEach { room ->
            val image = context.assets.open(room.asset).use { BitmapFactory.decodeStream(it) }
            assertNotNull(image); assertEquals(256,image.width); assertEquals(256,image.height)
            assertEquals(room.position.row==3,room.exterior)
        }
        assertEquals("Despacho",sorted.single { it.position == game.cases.first { it.id=="004" }.office }.name)
        assertEquals("Invernadero",sorted.single { it.position.label=="D2" }.name)
    }
    @Test fun fourSheetsHaveRealTransparencyAndSixteenNonemptyFrames() {
        for (name in listOf("alma","dario","irene","tomas")) {
            val sheet = context.assets.open("pixelart/characters/${name}_walk_48x64.png").use { BitmapFactory.decodeStream(it) }
            assertEquals(192,sheet.width); assertEquals(256,sheet.height);assertTrue(sheet.hasAlpha())
            for (row in 0..3) for (frame in 0..3) {
                var transparent=0; var opaque=0
                for (y in 0 until 64) for (x in 0 until 48) {
                    val alpha=sheet.getPixel(frame*48+x,row*64+y) ushr 24
                    if (alpha==0) transparent++
                    if (alpha>128) opaque++
                }
                assertTrue(transparent>50);assertTrue(opaque>50)
            }
        }
    }
    @Test fun movementKeepsDestinationRulesAndFindsADoorRoute() {
        context.getSharedPreferences("investigation",0).edit().clear().commit()
        val game = InvestigationViewModel(context,SavedStateHandle())
        game.openCase("004");game.drop("alma",com.crimegrid.data.Position(0,0))
        game.drop("alma",com.crimegrid.data.Position(3,1))
        val walk=game.walk.value!!
        assertEquals("alma",walk.suspect)
        assertTrue(walk.route.zipWithNext().all { (a,b) -> com.crimegrid.domain.Navigation.edge(game.case.scene!!,a,b)!=null })
        game.finishWalk(walk.sequence)
        game.drop("darío",com.crimegrid.data.Position(3,2))
        assertFalse("darío" in game.state.value.positions)
        assertNotNull(game.feedback.value)
        for (case in game.cases) assertTrue(com.crimegrid.domain.Engine.complete(case,case.solution.positions))
    }

    @Test fun decodedPngsAndSpriteAnchorsAreReused(){
        val a=PixelAssets.bitmap(context.assets,"pixelart/characters/alma_walk_48x64.png")!!
        val b=PixelAssets.bitmap(context.assets,"pixelart/characters/alma_walk_48x64.png")!!
        assertSame(a,b)
        assertSame(PixelAssets.spriteFeet("alma",a),PixelAssets.spriteFeet("alma",b))
    }
}
