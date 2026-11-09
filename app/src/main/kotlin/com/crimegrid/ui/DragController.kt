package com.crimegrid.ui

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import com.crimegrid.data.Position
import com.crimegrid.domain.WalkDirection
import com.crimegrid.domain.walkDirection

/** Root-space hit testing is independent of source, screen density and scrolling. */
class DragController {
    val roomBounds = mutableStateMapOf<Position, Rect>()
    var direction by mutableStateOf(WalkDirection.DOWN)
        private set
    val cells = mutableStateMapOf<Position, Rect>()
    var suspect by mutableStateOf<String?>(null)
        private set
    var point by mutableStateOf(Offset.Zero)
        private set
    val target: Position? get() = cells.entries.firstOrNull { it.value.contains(point) }?.key
    fun start(id: String, point: Offset) { suspect = id; this.point = point }
    fun move(delta: Offset) { if (delta.getDistance() > 0f) direction = walkDirection(delta.x, delta.y); point += delta }
    fun finish(onDrop: (String, Position) -> Unit) {
        val id = suspect
        val destination = target
        cancel()
        if (id != null && destination != null) onDrop(id, destination)
    }
    fun cancel() { suspect = null }
}

@Composable fun Modifier.dragCharacter(id: String?, drag: DragController, onDrop: (String, Position) -> Unit): Modifier {
    var bounds by remember { mutableStateOf(Rect.Zero) }
    val latestDrop by rememberUpdatedState(onDrop)
    return onGloballyPositioned { bounds = it.boundsInRoot() }.pointerInput(id, drag) {
        if (id != null) detectDragGesturesAfterLongPress(
            onDragStart = { drag.start(id, bounds.topLeft + it) },
            onDrag = { change, amount -> change.consume(); drag.move(amount) },
            onDragEnd = { drag.finish(latestDrop) }, onDragCancel = drag::cancel
        )
    }
}
