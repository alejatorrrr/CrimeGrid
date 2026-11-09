package com.crimegrid.ui

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.crimegrid.data.*
import com.crimegrid.domain.Engine
import com.crimegrid.domain.Navigation
import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Feedback is ephemeral: repeated invalid taps trigger a fresh animation without saving an error. */
data class MoveFeedback(val position: Position, val sequence: Long)

class InvestigationViewModel(context: Context, private val savedState: SavedStateHandle) : ViewModel() {
    private val mansion = Json.decodeFromString<Scene>(checkNotNull(javaClass.getResourceAsStream("/maps/mansion.json")).bufferedReader().use { it.readText() }).also(Navigation::validate)
    val cases = listOf("001", "002", "003", "004").map { id ->
        CaseRepository {
            checkNotNull(javaClass.getResourceAsStream("/cases/case$id.json")).bufferedReader().use { it.readText() }
        }.load().let { if (it.id == "004") it.copy(scene = mansion) else it }
    }
    private val store = InvestigationStore(context.applicationContext)
    private fun sanitize(restored: InvestigationState): InvestigationState {
        val case = cases.find { it.id == restored.caseId } ?: return InvestigationState()
        return restored.takeIf {
            Engine.valid(case, it.positions) && it.screen in setOf("menu", "investigation", "result") &&
                (it.screen != "result" || Engine.complete(case, it.positions)) &&
                it.hintsRevealed in 0..case.hints.size && it.reviewedClues.all { index -> index in case.clues.indices } &&
                (it.selectedSuspect == null || case.suspects.any { suspect -> suspect.id == it.selectedSuspect })
        } ?: InvestigationState(caseId = case.id)
    }
    private val mutableState = MutableStateFlow(sanitize(InvestigationStore.decode(savedState["progress"]) ?: store.read()))
    val state = mutableState.asStateFlow()
    val case: Case get() = cases.first { it.id == state.value.caseId }
    private var feedbackSequence = 0L
    private val mutableFeedback = MutableStateFlow<MoveFeedback?>(null)
    val feedback = mutableFeedback.asStateFlow()

    fun progress(id: String): InvestigationState = if (id == state.value.caseId) state.value else sanitize(store.read(id))
    private fun update(next: InvestigationState) {
        mutableState.value = next
        savedState["progress"] = InvestigationStore.encode(next)
        store.write(next)
    }
    fun openCase(id: String) {
        if (cases.none { it.id == id }) return
        mutableFeedback.value = null
        mutableWalk.value = null
        val next = progress(id)
        val selectedCase = cases.first { it.id == id }
        update(next.copy(screen = if (Engine.complete(selectedCase, next.positions)) "result" else "investigation", message = ""))
    }
    fun play() = openCase(state.value.caseId)
    fun menu() { mutableFeedback.value = null; mutableWalk.value = null; update(state.value.copy(screen = "menu", message = "")) }
    fun select(id: String) {
        if (case.suspects.none { it.id == id }) return
        mutableFeedback.value = null
        update(state.value.copy(selectedSuspect = id, message = ""))
    }
    data class Walk(val suspect: String, val route: List<Position>, val sequence: Long)
    private val mutableWalk = MutableStateFlow<Walk?>(null)
    val walk = mutableWalk.asStateFlow()
    fun finishWalk(sequence: Long) { if (mutableWalk.value?.sequence == sequence) mutableWalk.value = null }
    fun drop(suspect: String, position: Position) {
        if (case.suspects.none { it.id == suspect }) return
        select(suspect)
        place(position)
    }
    fun place(position: Position) {
        val current = state.value
        val selected = current.selectedSuspect
        val scene = case.scene
        val origin = current.positions[selected]
        val route = if (origin != null && scene?.enforceConnections == true) Navigation.route(scene, origin, position) else null
        val next = if (selected == null) current.copy(message = "Toca el nombre de un sospechoso y después una casilla.")
            else if (scene?.enforceConnections == true && origin != null && route == null)
                current.copy(message = "Movimiento rechazado: no hay un camino de puertas válido.")
            else Engine.place(case, current, selected, position)
        update(next)
        if (next.message.isEmpty() && origin != null && origin != position && route != null)
            mutableWalk.value = Walk(selected!!, route, ++feedbackSequence)
        mutableFeedback.value = if (next.message.isNotEmpty()) MoveFeedback(position, ++feedbackSequence) else null
    }
    fun remove() { mutableFeedback.value = null; mutableWalk.value = null; state.value.selectedSuspect?.let { update(Engine.remove(state.value, it)) } }
    fun reset() {
        mutableFeedback.value = null
        mutableWalk.value = null
        val current = state.value
        update(Engine.reset(current).copy(screen = "investigation", solved = current.solved || Engine.complete(case, current.positions)))
    }
    fun toggleClue(index: Int) {
        if (index !in case.clues.indices) return
        val read = state.value.reviewedClues
        update(state.value.copy(reviewedClues = if (index in read) read - index else read + index))
    }
    fun requestHint() {
        if (state.value.hintsRevealed < case.hints.size)
            update(state.value.copy(hintsRevealed = state.value.hintsRevealed + 1))
    }
    fun check() {
        val current = state.value
        update(if (Engine.complete(case, current.positions)) current.copy(screen = "result", message = "", solved = true)
        else current.copy(message = if (current.positions.size < case.suspects.size) "Faltan sospechosos por colocar." else "La reconstrucción no encaja con todas las pistas. Revisa tus deducciones."))
    }
}
