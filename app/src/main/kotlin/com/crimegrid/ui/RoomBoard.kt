package com.crimegrid.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import com.crimegrid.domain.WalkDirection
import com.crimegrid.domain.Navigation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.crimegrid.data.*
import com.crimegrid.domain.Engine
import kotlinx.coroutines.delay

@Composable fun RoomBoard(case: Case, state: InvestigationState, feedback: MoveFeedback?, drag: DragController,
    place: (Position) -> Unit, drop: (String, Position) -> Unit,
    walk: InvestigationViewModel.Walk? = null, facing: Map<String, WalkDirection> = emptyMap()) {
    DisposableEffect(case.id) { onDispose { drag.cancel(); drag.cells.clear(); drag.roomBounds.clear() } }
    Column(Modifier.fillMaxWidth().testTag("room-board"), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row {
            Spacer(Modifier.width(18.dp))
            repeat(case.size) { Text("${('A'.code + it).toChar()}", Modifier.weight(1f), textAlign = TextAlign.Center, color = Color(0xFFE4B86B)) }
        }
        repeat(case.size) { row ->
            if (case.scene != null && row == case.size - 1) Text("EXTERIOR", color = Color(0xFF88D8CB), style = MaterialTheme.typography.labelSmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${row + 1}", Modifier.width(18.dp), color = Color(0xFFE4B86B))
                Box(Modifier.weight(1f)) {
                    val exteriorLoaded = if (row == case.size - 1) pixelImage(case.scene?.exteriorAsset,
                        Modifier.fillMaxWidth().aspectRatio(case.size.toFloat())) else false
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(case.size) { column ->
                            val pos = Position(row, column)
                            val room = case.scene?.rooms?.find { it.position == pos }
                            val suspect = case.suspects.find { state.positions[it.id] == pos }
                            val office = pos == case.office
                            var pulse by remember(case.id, pos) { mutableStateOf(false) }
                            LaunchedEffect(feedback?.sequence) {
                                pulse = false
                                if (feedback?.position == pos) {
                                    repeat(2) {
                                        pulse = true
                                        delay(180)
                                        pulse = false
                                        delay(180)
                                    }
                                }
                            }
                            val hover = drag.suspect != null && drag.target == pos
                            val allowed = drag.suspect?.let { id ->
                                Engine.valid(case, state.positions + (id to pos)) &&
                                    (case.scene?.enforceConnections != true || state.positions[id] == null || Navigation.route(case.scene!!, state.positions.getValue(id), pos) != null)
                            } ?: true
                            val border = when { pulse || hover && !allowed -> Color(0xFFFF7181); hover -> Color(0xFF88D8CB); office || suspect != null && state.selectedSuspect == suspect.id -> Color(0xFFE4B86B); else -> Color(0xFF3C4D58) }
                            Surface(onClick = { place(pos) }, color = Color.Transparent,
                                border = BorderStroke(if (hover || pulse) 3.dp else 1.dp, border),
                                modifier = Modifier.weight(1f).aspectRatio(1f)
                                    .onGloballyPositioned {
                                        drag.cells[pos] = it.boundsInRoot()
                                        drag.roomBounds[pos] = Rect(it.positionInRoot(), Size(it.size.width.toFloat(), it.size.height.toFloat()))
                                    }
                                    .dragCharacter(suspect?.id, drag, drop)
                                    .semantics {
                                        contentDescription = "Casilla ${pos.label}${if (office) ", despacho" else ""}, ${suspect?.name ?: "vacía"}"
                                        if (feedback?.position == pos) stateDescription = "Movimiento rechazado"
                                    }) {
                                Box(Modifier.fillMaxSize()) {
                                    val image = pixelImage(room?.asset, Modifier.matchParentSize())
                                    if (case.scene?.enforceConnections == true) check(image) { "Missing approved room artwork: ${room?.asset}" }
                                    if (!image && !(room?.exterior == true && exteriorLoaded)) {
                                        Box(Modifier.matchParentSize().background(if (room?.exterior == true) Color(0xFF253D32) else Color(0xFF24303C)))
                                    }
                                    if (suspect != null && drag.suspect != suspect.id && walk?.suspect != suspect.id) {
                                        BoxWithConstraints(Modifier.matchParentSize()) {
                                            val h = maxHeight * .34f
                                            val x = maxWidth * (room?.footX ?: .5f) - h * .375f
                                            val y = maxHeight * (room?.footY ?: .79f) - h
                                            CharacterSprite(suspect.id, Modifier.offset(x, y).size(h * .75f, h), facing[suspect.id] ?: WalkDirection.DOWN)
                                        }
                                    }
                                    if (pulse) Box(Modifier.matchParentSize().background(Color(0x99CA243F)))
                                    Text(pos.label, Modifier.align(Alignment.TopStart).background(Color(0xB30B1118)).padding(2.dp),
                                        color = Color.White, style = MaterialTheme.typography.labelSmall)
                                    Text(if (office) "Despacho" else room?.name ?: suspect?.name.orEmpty(),
                                        Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xC00B1118)).padding(2.dp),
                                        color = Color.White, textAlign = TextAlign.Center, maxLines = 2, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
