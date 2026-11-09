package com.crimegrid.data

import com.crimegrid.domain.Engine
import kotlinx.serialization.json.Json

/** A stream adapter keeps Android resources outside the reusable model and engine. */
class CaseRepository(private val readJson: () -> String) {
    fun load(): Case = Json.decodeFromString<Case>(readJson()).also { case ->
        require(case.size in 1..26 && case.suspects.size <= case.size)
        require(case.suspects.map { it.id }.distinct().size == case.suspects.size)
        require(case.clues.all { clue -> clue.subject in case.suspects.map { it.id } &&
            (clue.type in setOf(ClueType.ROW, ClueType.COLUMN, ClueType.NOT_ROW, ClueType.NOT_COLUMN) || clue.reference in case.suspects.map { it.id }) })
        case.scene?.let { scene ->
            require(scene.rooms.size == case.size * case.size)
            require(scene.rooms.map { it.position }.distinct().size == scene.rooms.size)
            require(scene.rooms.all { it.position.row in 0 until case.size && it.position.column in 0 until case.size })
        }
        require(Engine.complete(case, case.solution.positions)) { "Invalid official solution" }
        require(Engine.solve(case, 2).size == 1) { "Case must have exactly one solution" }
        require(case.hints.all { hint -> hint.clueIndices.all { it in case.clues.indices } &&
            (hint.subject == null || case.suspects.any { it.id == hint.subject }) })
        require(Engine.culprit(case, case.solution.positions) != null)
    }
}
