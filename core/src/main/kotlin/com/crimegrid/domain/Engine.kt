package com.crimegrid.domain

import com.crimegrid.data.*

/** Pure rules: no Android dependencies, no mutation, no answer leakage in move feedback. */
object Engine {
    fun valid(case: Case, positions: Map<String, Position>): Boolean =
        positions.keys.all { id -> case.suspects.any { it.id == id } } &&
        positions.values.all { it.row in 0 until case.size && it.column in 0 until case.size } &&
        positions.values.map { it.row }.distinct().size == positions.size &&
        positions.values.map { it.column }.distinct().size == positions.size

    /** Unknown operands are compatible during backtracking; complete() checks all are present. */
    fun satisfies(clue: Clue, positions: Map<String, Position>): Boolean {
        val subject = positions[clue.subject] ?: return true
        return when (clue.type) {
            ClueType.ROW -> subject.row == clue.value
            ClueType.COLUMN -> subject.column == clue.value
            ClueType.NOT_ROW -> subject.row != clue.value
            ClueType.NOT_COLUMN -> subject.column != clue.value
            ClueType.COLUMN_OFFSET -> positions[clue.reference]?.let { subject.column == it.column + clue.value } ?: true
            ClueType.ROW_BEFORE -> positions[clue.reference]?.let { subject.row < it.row } ?: true
            ClueType.COLUMN_BEFORE -> positions[clue.reference]?.let { subject.column < it.column } ?: true
            ClueType.ROW_DISTANCE -> positions[clue.reference]?.let { kotlin.math.abs(subject.row - it.row) == clue.value } ?: true
            ClueType.COLUMN_DISTANCE -> positions[clue.reference]?.let { kotlin.math.abs(subject.column - it.column) == clue.value } ?: true
            ClueType.ROW_OFFSET -> positions[clue.reference]?.let { subject.row == it.row + clue.value } ?: true
        }
    }
    fun complete(case: Case, positions: Map<String, Position>): Boolean =
        positions.size == case.suspects.size && valid(case, positions) && case.clues.all { satisfies(it, positions) }

    fun place(case: Case, state: InvestigationState, suspect: String, position: Position): InvestigationState {
        val candidate = state.positions + (suspect to position)
        return if (valid(case, candidate)) state.copy(positions = candidate, message = "")
        else state.copy(message = "Movimiento rechazado: la fila o columna está ocupada.")
    }
    fun remove(state: InvestigationState, suspect: String) = state.copy(positions = state.positions - suspect, message = "")
    fun reset(state: InvestigationState) = state.copy(positions = emptyMap(), message = "", selectedSuspect = null, hintsRevealed = 0, reviewedClues = emptySet())
    fun culprit(case: Case, positions: Map<String, Position>): Suspect? =
        case.suspects.singleOrNull { positions[it.id] == case.office }

    /** Stops at limit; use 2 to distinguish zero, unique, and ambiguous cases cheaply. */
    fun solve(case: Case, limit: Int = 2): List<Solution> {
        require(limit > 0)
        val answers = mutableListOf<Solution>()
        fun search(positions: Map<String, Position>) {
            if (answers.size >= limit) return
            if (positions.size == case.suspects.size) { answers += Solution(positions); return }
            val id = case.suspects[positions.size].id
            for (row in 0 until case.size) for (column in 0 until case.size) {
                val next = positions + (id to Position(row, column))
                if (valid(case, next) && case.clues.all { satisfies(it, next) }) search(next)
            }
        }
        search(emptyMap())
        return answers
    }
}
