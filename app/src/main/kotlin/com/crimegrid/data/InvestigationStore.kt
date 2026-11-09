package com.crimegrid.data

import android.content.Context
import kotlinx.serialization.json.Json

/** Local progress survives process death and cold starts; broken saves fall back safely. */
class InvestigationStore(context: Context) {
    private val preferences = context.getSharedPreferences("investigation", Context.MODE_PRIVATE)
    fun read(caseId: String = preferences.getString("activeCase", "001") ?: "001"): InvestigationState =
        decode(preferences.getString("case$caseId", null))?.copy(caseId = caseId) ?: InvestigationState(caseId = caseId)
    fun write(state: InvestigationState) {
        preferences.edit().putString("case${state.caseId}", encode(state)).putString("activeCase", state.caseId).apply()
    }
    companion object {
        fun encode(state: InvestigationState) = Json.encodeToString(InvestigationState.serializer(), state)
        fun decode(value: String?): InvestigationState? = value?.let {
            runCatching { Json.decodeFromString(InvestigationState.serializer(), it) }.getOrNull()
        }
    }
}
