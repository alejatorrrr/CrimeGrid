package com.crimegrid.domain

import com.crimegrid.data.*
import kotlin.math.abs

/** Collision graph is authored separately from pixels; decorations are never collision masks. */
object Navigation {
    fun validate(scene: Scene) {
        val cells = scene.rooms.map { it.position }.toSet()
        require(cells.size == scene.rooms.size)
        scene.connections.forEach { edge ->
            require(edge.fromPosition in cells && edge.toPosition in cells)
            val dr = edge.toPosition.row - edge.fromPosition.row
            val dc = edge.toPosition.column - edge.fromPosition.column
            require(abs(dr) + abs(dc) == 1) { "Only orthogonal neighbors may share a door" }
            require(edge.exit.x in 0f..1f && edge.exit.y in 0f..1f && edge.entry.x in 0f..1f && edge.entry.y in 0f..1f)
            require(edge.exit.x + edge.fromPosition.column == edge.entry.x + edge.toPosition.column &&
                edge.exit.y + edge.fromPosition.row == edge.entry.y + edge.toPosition.row) { "Door endpoints must align in world space" }
        }
    }
    fun edge(scene: Scene, from: Position, to: Position): Connection? = scene.connections.firstNotNullOfOrNull {
        when {
            it.fromPosition == from && it.toPosition == to -> it
            it.toPosition == from && it.fromPosition == to -> Connection(from, to, it.entry, it.exit)
            else -> null
        }
    }
    /** Matrix indices follow row-major room coordinates, independent from artwork pixels. */
    fun matrix(scene: Scene): List<List<Boolean>> {
        val cells = scene.rooms.map { it.position }.sortedWith(compareBy({ it.row }, { it.column }))
        return cells.map { from -> cells.map { to -> edge(scene, from, to) != null } }
    }
    fun route(scene: Scene, from: Position, to: Position): List<Position>? {
        val cells = scene.rooms.map { it.position }.toSet()
        if (from !in cells || to !in cells) return null
        val queue = ArrayDeque<List<Position>>()
        queue.add(listOf(from))
        val visited = mutableSetOf(from)
        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            if (path.last() == to) return path
            scene.connections.forEach { edge ->
                val next = when (path.last()) { edge.fromPosition -> edge.toPosition; edge.toPosition -> edge.fromPosition; else -> null }
                if (next != null && visited.add(next)) queue.add(path + next)
            }
        }
        return null
    }
}

enum class WalkDirection(val row: Int) { DOWN(0), LEFT(1), RIGHT(2), UP(3) }
fun walkDirection(dx: Float, dy: Float): WalkDirection = if (abs(dx) > abs(dy)) {
    if (dx < 0) WalkDirection.LEFT else WalkDirection.RIGHT
} else if (dy < 0) WalkDirection.UP else WalkDirection.DOWN

/** Shared boundary anchors have equal world coordinates, but crossing keeps the doorway direction. */
fun stepDirection(from: Position, fromPoint: PortalPoint, to: Position, toPoint: PortalPoint): WalkDirection =
    if (from != to) walkDirection((to.column-from.column).toFloat(),(to.row-from.row).toFloat())
    else walkDirection(toPoint.x-fromPoint.x,toPoint.y-fromPoint.y)
