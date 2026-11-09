package com.crimegrid.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.crimegrid.data.*
import com.crimegrid.domain.Navigation
import com.crimegrid.domain.WalkDirection
import com.crimegrid.domain.stepDirection
import kotlin.math.roundToInt

private data class FootPoint(val room: Position, val point: PortalPoint)

/** The draggable ghost is only a preview. Committed movement follows doors in the collision graph. */
@Composable fun WalkingLayer(walk: InvestigationViewModel.Walk, scene: Scene, drag: DragController, origin: Offset,
    onFacing: (String, WalkDirection) -> Unit, onFinish: (Long) -> Unit) {
    fun foot(position: Position): FootPoint {
        val room = scene.rooms.first { it.position == position }
        return FootPoint(position, PortalPoint(room.footX, room.footY))
    }
    val points = remember(walk.sequence) {
        buildList {
            add(foot(walk.route.first()))
            walk.route.zipWithNext().forEach { (from, to) ->
                val edge = checkNotNull(Navigation.edge(scene, from, to))
                add(FootPoint(from, edge.exit)); add(FootPoint(to, edge.entry)); add(foot(to))
            }
        }
    }
    var segment by remember(walk.sequence) { mutableIntStateOf(0) }
    val fraction = remember(walk.sequence) { Animatable(0f) }
    val finish by rememberUpdatedState(onFinish)
    val facing by rememberUpdatedState(onFacing)
    val a = points[segment]
    val b = points[minOf(segment + 1, points.lastIndex)]
    val direction = stepDirection(a.room, a.point, b.room, b.point)
    LaunchedEffect(walk.sequence) {
        for (index in 0 until points.lastIndex) {
            segment = index
            val start = points[index]; val end = points[index + 1]
            facing(walk.suspect, stepDirection(start.room, start.point, end.room, end.point))
            fraction.snapTo(0f)
            fraction.animateTo(1f, tween(if (start.room != end.room) 45 else 170, easing = LinearEasing))
        }
        finish(walk.sequence)
    }
    fun root(point: FootPoint): Offset? = drag.roomBounds[point.room]?.let {
        Offset(it.left + point.point.x * it.width, it.top + point.point.y * it.height)
    }
    val start = root(a); val end = root(b)
    val heightPx = (drag.roomBounds[a.room]?.height ?: 0f) * .34f
    if (start != null && end != null && heightPx > 0) {
        val point = start + (end - start) * fraction.value - origin
        val height = with(LocalDensity.current) { heightPx.toDp() }
        CharacterSprite(walk.suspect, Modifier.offset { IntOffset((point.x - heightPx * .375f).roundToInt(), (point.y - heightPx).roundToInt()) }
            .size(height * .75f, height), direction, walking = true)
    }
}
