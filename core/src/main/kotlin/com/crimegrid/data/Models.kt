package com.crimegrid.data

import kotlinx.serialization.Serializable

/** Coordinates are zero-based internally; labels use column letter + one-based row. */
@Serializable data class Position(val row: Int, val column: Int) {
    val label: String get() = "${('A'.code + column).toChar()}${row + 1}"
}
@Serializable data class Suspect(val id: String, val name: String)
@Serializable enum class ClueType { ROW, COLUMN, ROW_OFFSET, COLUMN_OFFSET, ROW_BEFORE, COLUMN_BEFORE, ROW_DISTANCE, COLUMN_DISTANCE, NOT_ROW, NOT_COLUMN }
@Serializable data class Clue(val text: String, val type: ClueType, val subject: String, val value: Int, val reference: String? = null)
@Serializable data class Solution(val positions: Map<String, Position>)
@Serializable data class Hint(
    val text: String, val clueIndices: List<Int> = emptyList(),
    val subject: String? = null, val row: Int? = null, val column: Int? = null
)
@Serializable data class Case(
    val id: String, val title: String, val location: String, val premise: String,
    val size: Int, val suspects: List<Suspect>, val clues: List<Clue>,
    val office: Position, val solution: Solution, val explanation: String,
    val difficulty: String = "Iniciación", val hints: List<Hint> = emptyList(),
    val scene: Scene? = null
)
@Serializable data class InvestigationState(
    val positions: Map<String, Position> = emptyMap(),
    val selectedSuspect: String? = null, val screen: String = "menu", val message: String = "",
    val caseId: String = "001", val hintsRevealed: Int = 0,
    val reviewedClues: Set<Int> = emptySet(), val solved: Boolean = false
)

/** Visual locations remain separate from logical coordinates and do not alter clues. */
@Serializable data class Room(val position: Position, val name: String, val asset: String, val exterior: Boolean = false, val footX: Float = .5f, val footY: Float = .79f)
@Serializable data class PortalPoint(val x: Float, val y: Float)
@Serializable data class Connection(val fromPosition: Position, val toPosition: Position, val exit: PortalPoint, val entry: PortalPoint)
@Serializable data class Scene(val rooms: List<Room>, val exteriorAsset: String? = null,
    val connections: List<Connection> = emptyList(), val enforceConnections: Boolean = false)
