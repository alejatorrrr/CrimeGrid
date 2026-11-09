package com.crimegrid.ui

import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.draw.clipToBounds
import com.crimegrid.domain.WalkDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimegrid.data.*
import com.crimegrid.domain.Engine

private val Ink = Color(0xFF0B1118)
private val Paper = Color(0xFFE4E8E9)
private val Amber = Color(0xFFE4B86B)
private val Teal = Color(0xFF88D8CB)

@Composable fun CrimeGridApp(model: InvestigationViewModel) {
    val state by model.state.collectAsStateWithLifecycle()
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Amber, onPrimary = Ink, secondary = Teal, background = Ink,
        surface = Color(0xFF16212C), onSurface = Paper, onBackground = Paper
    )) {
        BackHandler(state.screen != "menu") { model.menu() }
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Box(Modifier.safeDrawingPadding().fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                when (state.screen) {
                    "menu" -> Menu(model, state)
                    "result" -> Result(model, state)
                    else -> Investigation(model, state)
                }
            }
        }
    }
}

@Composable private fun Eyebrow(text: String) {
    Text(text, color = Teal, style = MaterialTheme.typography.labelMedium, fontFamily = FontFamily.Monospace)
}

@Composable private fun Menu(model: InvestigationViewModel, state: InvestigationState) {
    Column(Modifier.widthIn(max = 520.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Eyebrow("ARCHIVO DE INVESTIGACIÓN")
        Text("CrimeGrid", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
        Text("Lee entre líneas. Reconstruye la verdad.", color = Amber)
        Text("Toca un nombre y después una casilla. No puede haber dos personas en la misma fila o columna.")
        Button(onClick = model::play, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) { Text("Jugar") }
        Text("SELECCIÓN DE CASOS", color = Teal, fontFamily = FontFamily.Monospace)
        model.cases.forEach { case ->
            val progress = model.progress(case.id)
            val status = when {
                progress.solved || Engine.complete(case, progress.positions) -> "Resuelto"
                progress.positions.isNotEmpty() || progress.hintsRevealed > 0 || progress.reviewedClues.isNotEmpty() -> "En curso"
                else -> "Pendiente"
            }
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Eyebrow("${case.id} · ${case.difficulty}")
                    Text(case.title, style = MaterialTheme.typography.titleLarge)
                    Text(case.location, style = MaterialTheme.typography.bodySmall)
                    Text("$status · ${progress.positions.size}/${case.suspects.size} colocados · ${progress.hintsRevealed} ${if (progress.hintsRevealed == 1) "ayuda" else "ayudas"}", color = Amber)
                    Button(onClick = { model.openCase(case.id) }, modifier = Modifier.fillMaxWidth()) { Text("Abrir caso ${case.id}") }
                }
            }
        }
        Text("El caso 001 es de iniciación. Para un reto mayor, empieza por el 002 o el 003.", style = MaterialTheme.typography.bodySmall, color = Teal)
    }
}

@Composable private fun Investigation(model: InvestigationViewModel, state: InvestigationState) {
    val drag = remember(model.case.id) { DragController() }
    val walk by model.walk.collectAsStateWithLifecycle()
    val facing = remember(model.case.id) { mutableStateMapOf<String, WalkDirection>() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var viewportHeight by remember { mutableIntStateOf(0) }
    val scroll = rememberScrollState()
    val edge = with(LocalDensity.current) { 64.dp.toPx() }
    val step = with(LocalDensity.current) { 10.dp.toPx() }
    LaunchedEffect(drag.suspect) {
        if (drag.suspect != null) while (true) {
            val localY = drag.point.y - origin.y
            when {
                localY < edge -> scroll.scrollBy(-step)
                localY > viewportHeight - edge -> scroll.scrollBy(step)
            }
            delay(16)
        }
    }
    Box(Modifier.widthIn(max = 520.dp).fillMaxSize().clipToBounds().onGloballyPositioned { origin = it.positionInRoot(); viewportHeight = it.size.height }) {
    Column(Modifier.fillMaxSize().verticalScroll(scroll, enabled = drag.suspect == null).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Eyebrow("EXPEDIENTE ${model.case.id}"); Text(model.case.title, style = MaterialTheme.typography.titleLarge) }
            TextButton(onClick = model::menu) { Text("Menú") }
        }
        Text("${model.case.location} · Despacho ${model.case.office.label}", color = Teal, style = MaterialTheme.typography.bodySmall)
        Text(model.case.premise, style = MaterialTheme.typography.bodyMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Eyebrow("SOSPECHOSOS · ${state.positions.size}/${model.case.suspects.size}")
            Text("Selecciona y coloca", style = MaterialTheme.typography.labelSmall)
        }
        model.case.suspects.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { suspect ->
                    val selected = state.selectedSuspect == suspect.id
                    Surface(onClick = { model.select(suspect.id) }, modifier = Modifier.weight(1f).dragCharacter(suspect.id, drag, model::drop),
                        shape = RoundedCornerShape(10.dp), color = if (selected) Color(0xFF334039) else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) Amber else Color(0xFF354452))) {
                        Column(Modifier.semantics { this.selected = selected }.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CharacterSprite(suspect.id, Modifier.size(24.dp, 36.dp))
                                Text(suspect.name, fontWeight = FontWeight.Bold)
                            }
                            Text(state.positions[suspect.id]?.let { "Colocado · ${it.label}" } ?: "Pendiente", color = if (suspect.id in state.positions) Teal else Paper,
                                style = MaterialTheme.typography.bodySmall)
                            if (selected) Text("Seleccionado", color = Amber, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        Text(state.selectedSuspect?.let { id -> "Ahora toca una casilla para colocar a ${model.case.suspects.first { it.id == id }.name}." }
            ?: "1. Toca un nombre. 2. Toca una casilla.", color = Amber, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        val feedback by model.feedback.collectAsStateWithLifecycle()
        RoomBoard(model.case, state, feedback, drag, model::place, model::drop, walk, facing)
        Text("Mantén pulsado un personaje y arrástralo a otra habitación. Suelta fuera para cancelar.", style = MaterialTheme.typography.bodySmall, color = Teal)
        OutlinedButton(onClick = model::remove, enabled = state.selectedSuspect in state.positions, modifier = Modifier.fillMaxWidth()) { Text("Retirar seleccionado") }
        if (state.message.isNotEmpty()) Text(state.message, color = Amber, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
        Eyebrow("PISTAS · ${state.reviewedClues.size}/${model.case.clues.size} LEÍDAS")
        LazyColumn(Modifier.fillMaxWidth().height(190.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)), contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(model.case.clues) { index, clue ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("%02d".format(index + 1), color = Amber, fontFamily = FontFamily.Monospace)
                    Column(Modifier.weight(1f)) {
                        Text(clue.text, style = MaterialTheme.typography.bodyMedium)
                        Text(if (index in state.reviewedClues) "Marcada como leída" else "Pendiente de revisar", color = Teal, style = MaterialTheme.typography.labelSmall)
                    }
                    Checkbox(checked = index in state.reviewedClues, onCheckedChange = { model.toggleClue(index) },
                        modifier = Modifier.semantics { contentDescription = "Marcar pista ${index + 1} como leída" })
                }
            }
        }
        Text("Las marcas de lectura son tuyas; no indican si una pista se cumple.", style = MaterialTheme.typography.bodySmall, color = Teal)
        OutlinedButton(onClick = model::requestHint, enabled = state.hintsRevealed < model.case.hints.size, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.hintsRevealed < model.case.hints.size) "Pedir ayuda (${state.hintsRevealed}/${model.case.hints.size})" else "Todas las ayudas vistas")
        }
        model.case.hints.take(state.hintsRevealed).forEachIndexed { index, hint ->
            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF26332E)) {
                Text("Ayuda ${index + 1}: ${hint.text}", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
        Text("Una persona por fila y columna. Las pistas solo se comprueban al enviar.", style = MaterialTheme.typography.bodySmall, color = Teal)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = model::reset, modifier = Modifier.weight(1f)) { Text("Reiniciar") }
            Button(onClick = model::check, modifier = Modifier.weight(1f)) { Text("Comprobar") }
        }
        Spacer(Modifier.height(8.dp))
    }
    walk?.let { movement -> model.case.scene?.let { scene ->
        WalkingLayer(movement, scene, drag, origin, { id, direction -> facing[id] = direction }, model::finishWalk)
    } }
    drag.suspect?.let { id ->
        Box(Modifier.offset { IntOffset((drag.point.x - origin.x).roundToInt() - 24.dp.roundToPx(), (drag.point.y - origin.y).roundToInt() - 66.dp.roundToPx()) }.size(48.dp, 64.dp)) {
            CharacterSprite(id, Modifier.fillMaxSize(), drag.direction, walking = true)
        }
    }
    }
}

@Composable private fun Result(model: InvestigationViewModel, state: InvestigationState) {
    Column(Modifier.widthIn(max = 520.dp).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Eyebrow("EXPEDIENTE ${model.case.id} / RESUELTO")
        Text("La verdad,\ncasilla a casilla.", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text("Culpable: ${Engine.culprit(model.case, state.positions)?.name.orEmpty()}", color = Amber, style = MaterialTheme.typography.headlineSmall)
        Text(model.case.explanation, style = MaterialTheme.typography.bodyLarge)
        Text("Ayudas utilizadas: ${state.hintsRevealed}/${model.case.hints.size}", color = Teal)
        HorizontalDivider()
        model.case.suspects.forEach { suspect ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(suspect.name); Text(state.positions[suspect.id]?.label.orEmpty(), color = Teal, fontFamily = FontFamily.Monospace)
            }
        }
        Button(onClick = model::menu, modifier = Modifier.fillMaxWidth()) { Text("Volver al menú") }
        OutlinedButton(onClick = model::reset, modifier = Modifier.fillMaxWidth()) { Text("Jugar de nuevo") }
        model.cases.getOrNull(model.cases.indexOf(model.case) + 1)?.let { next ->
            Button(onClick = { model.openCase(next.id) }, modifier = Modifier.fillMaxWidth()) { Text("Siguiente caso · ${next.id}") }
        }
    }
}
