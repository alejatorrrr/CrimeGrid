package com.crimegrid.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.crimegrid.data.Position
import org.junit.Assert.*
import org.junit.Test

class DragControllerTest {
    @Test fun translatedCoordinatesResolveDestinationAndFinishOnlyOnce() {
        val drag = DragController()
        drag.cells[Position(1,2)] = Rect(100f, 300f, 200f, 400f)
        drag.start("bruno", Offset(50f, 50f)); drag.move(Offset(75f, 275f))
        assertEquals(Position(1,2), drag.target)
        var calls = 0
        drag.finish { id, pos -> assertEquals("bruno", id); assertEquals(Position(1,2), pos); calls++ }
        drag.finish { _, _ -> calls++ }
        assertEquals(1, calls); assertNull(drag.suspect)
    }
    @Test fun outsideDropAndCancellationNeverCommit() {
        val drag = DragController()
        drag.cells[Position(0,0)] = Rect(0f,0f,100f,100f)
        drag.start("bruno", Offset(200f,200f))
        drag.finish { _, _ -> fail("Outside drop must not move a suspect") }
        drag.start("bruno", Offset(50f,50f)); drag.cancel()
        drag.finish { _, _ -> fail("Cancelled gesture must not commit") }
    }

    @Test fun spriteFilesUseAsciiNames() {
        assertEquals("damian", spriteFileName("damián"))
        assertEquals("elias", spriteFileName("elías"))
        assertEquals("oscar", spriteFileName("óscar"))
    }
}
