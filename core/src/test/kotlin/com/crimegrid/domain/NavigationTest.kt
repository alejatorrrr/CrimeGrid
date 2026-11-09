package com.crimegrid.domain

import com.crimegrid.data.*
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class NavigationTest {
    private val scene = Json.decodeFromString<Scene>(javaClass.getResource("/maps/mansion.json")!!.readText())
    @Test fun allRoomsAreReachableWithoutLeavingGraphOrCuttingWalls() {
        Navigation.validate(scene)
        assertEquals(21, scene.connections.size)
        val matrix = Navigation.matrix(scene)
        assertEquals(16,matrix.size)
        assertEquals(42,matrix.sumOf { row -> row.count { it } })
        for (a in 0..15) for (b in 0..15) assertEquals(matrix[a][b],matrix[b][a])
        for (from in scene.rooms) for (to in scene.rooms) {
            val route = Navigation.route(scene, from.position, to.position)!!
            assertEquals(from.position, route.first()); assertEquals(to.position, route.last())
            route.zipWithNext().forEach { (a,b) -> assertNotNull(Navigation.edge(scene,a,b)) }
        }
    }
    @Test fun facadeOnlyHasTheEntranceAndMapBoundsAreClosed() {
        for (column in listOf(0,2,3)) assertNull(Navigation.edge(scene, Position(2,column), Position(3,column)))
        assertNotNull(Navigation.edge(scene, Position(2,1), Position(3,1)))
        assertNull(Navigation.route(scene, Position(-1,0), Position(0,0)))
        assertNull(Navigation.route(scene, Position(0,0), Position(4,0)))
        val disconnected = scene.copy(connections = emptyList())
        assertNull(Navigation.route(disconnected, Position(0,0), Position(1,1)))
    }
    @Test fun connectionsAreSymmetricAndAnchorsMatchExactly() {
        for (edge in scene.connections) {
            val reverse = Navigation.edge(scene, edge.toPosition, edge.fromPosition)!!
            assertEquals(edge.exit, reverse.entry); assertEquals(edge.entry, reverse.exit)
            val expected = if (edge.toPosition.row > edge.fromPosition.row) WalkDirection.DOWN else WalkDirection.RIGHT
            assertEquals(expected,stepDirection(edge.fromPosition,edge.exit,edge.toPosition,edge.entry))
            val reverseExpected = if (expected == WalkDirection.DOWN) WalkDirection.UP else WalkDirection.LEFT
            assertEquals(reverseExpected,stepDirection(reverse.fromPosition,reverse.exit,reverse.toPosition,reverse.entry))
        }
        val invalid = scene.copy(connections = listOf(scene.connections.first().copy(entry=PortalPoint(.3f,.4f))))
        assertThrows(IllegalArgumentException::class.java) { Navigation.validate(invalid) }
    }
    @Test fun everyDirectionUsesTheSuppliedSheetRow() {
        assertEquals(0, walkDirection(0f, 1f).row)
        assertEquals(1, walkDirection(-1f, 0f).row)
        assertEquals(2, walkDirection(1f, 0f).row)
        assertEquals(3, walkDirection(0f,-1f).row)
    }
}
