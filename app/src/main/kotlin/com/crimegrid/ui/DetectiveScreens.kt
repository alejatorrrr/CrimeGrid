package com.crimegrid.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.crimegrid.data.*
import com.crimegrid.domain.*
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable fun DetectiveMenu(model:InvestigationViewModel,state:InvestigationState) {
    var help by rememberSaveable { mutableStateOf(false) }
    BackHandler(help){help=false}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Text("CRIMEGRID",fontFamily=DetectiveType,fontSize=42.sp,color=DetectiveGold)
        Text("ARCHIVOS DE LA MANSIÓN",fontFamily=FontFamily.Monospace,color=DetectivePaper,fontSize=13.sp)
        PixelButton("Jugar",model::play,Modifier.fillMaxWidth())
        PixelButton(if(help)"Cerrar ayuda" else "Cómo investigar",{help=!help},Modifier.fillMaxWidth())
        if(help) Column(Modifier.detectivePanel(parchment=true).padding(16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            Text("Toca un nombre y después una habitación para colocarlo. Mantén pulsado un personaje para arrastrarlo. Una persona por fila y columna.",color=DetectiveInk)
            Text("Sin selección, toca una habitación para ampliarla. Usa Dejar selección para inspeccionar. El cuaderno guarda tus marcas personales; no verifica las pistas.",color=DetectiveInk)
        }
        model.cases.forEach{case->
            val progress=model.progress(case.id)
            val status=when {progress.solved||Engine.complete(case,progress.positions)->"Resuelto";progress.positions.isNotEmpty()||progress.hintsRevealed>0||progress.reviewedClues.isNotEmpty()->"En curso";else->"Pendiente"}
            Column(Modifier.fillMaxWidth().detectivePanel().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("EXPEDIENTE ${case.id} · ${case.difficulty}",color=DetectiveGold,fontFamily=FontFamily.Monospace,fontSize=12.sp)
                PixelTitle(case.title)
                Text(case.location,color=DetectivePaper)
                Text("$status · ${progress.positions.size}/${case.suspects.size} colocados · ${progress.hintsRevealed} ${if(progress.hintsRevealed==1)"ayuda" else "ayudas"}",color=DetectivePaper,fontSize=13.sp)
                PixelButton("Abrir caso ${case.id}",{model.openCase(case.id)},Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable fun DetectiveInvestigation(model:InvestigationViewModel,state:InvestigationState) {
    var inspected by rememberSaveable{mutableStateOf<String?>(null)}
    var notebook by rememberSaveable { mutableStateOf(false) }
    var focus by rememberSaveable { mutableStateOf<Int?>(null) }
    val drag=remember { DragController() }
    val walk by model.walk.collectAsStateWithLifecycle()
    val feedback by model.feedback.collectAsStateWithLifecycle()
    val facing=remember { mutableStateMapOf<String,WalkDirection>() }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableIntStateOf(0) }
    val scroll=rememberScrollState()
    val edge=with(LocalDensity.current){64.dp.toPx()}
    val speed=with(LocalDensity.current){10.dp.toPx()}
    BackHandler { when {inspected!=null->inspected=null;notebook->notebook=false;focus!=null->focus=null;else->model.menu()} }
    LaunchedEffect(drag.suspect){
        if(drag.suspect!=null) while(true){
            when {drag.point.y-origin.y<edge->scroll.scrollBy(-speed);drag.point.y-origin.y>viewport-edge->scroll.scrollBy(speed)}
            delay(16)
        }
    }
    Box(Modifier.fillMaxSize().clipToBounds().onGloballyPositioned{origin=it.positionInRoot();viewport=it.size.height}){
        Column(Modifier.fillMaxSize().verticalScroll(scroll,enabled=drag.suspect==null).semantics{if(notebook || inspected!=null)invisibleToUser()},verticalArrangement=Arrangement.spacedBy(10.dp)){
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Column(Modifier.weight(1f)){
                    Text("EXPEDIENTE ${model.case.id}",color=DetectiveGold,fontFamily=FontFamily.Monospace,fontSize=11.sp)
                    PixelTitle(model.case.title)
                }
                PixelButton("Menú",model::menu)
            }
            MansionMap(model.case,state,feedback,drag,walk,facing,focus,{position->
                if(state.selectedSuspect!=null) model.place(position) else focus=position.row*4+position.column
            },{id,pos->facing[id]=drag.direction;model.drop(id,pos)},{asset->inspected=asset})
            if(focus!=null) PixelButton("Vista general",{focus=null},Modifier.fillMaxWidth().padding(horizontal=12.dp))
            Column(Modifier.padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){
                Text("SOSPECHOSOS · ${state.positions.size}/${model.case.suspects.size}",color=DetectiveGold,fontFamily=FontFamily.Monospace,fontSize=12.sp)
                model.case.suspects.chunked(2).forEach{pair->
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        pair.forEach{suspect->
                            val selected=state.selectedSuspect==suspect.id
                            Row(Modifier.weight(1f).heightIn(min=72.dp).detectivePanel(selected=selected)
                                .dragCharacter(suspect.id,drag,model::drop)
                                .clickable(interactionSource=remember{MutableInteractionSource()},indication=null){model.select(suspect.id)}
                                .semantics{this.selected=selected;role=Role.Button}
                                .padding(12.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(8.dp)){
                                CharacterSprite(suspect.id,Modifier.size(24.dp,36.dp))
                                Column(Modifier.weight(1f)){
                                    Text(suspect.name,color=DetectivePaper,fontFamily=DetectiveType,fontSize=20.sp)
                                    Text(if(suspect.id in state.positions)"Colocado" else "Pendiente",color=DetectiveGold,fontSize=12.sp)
                                }
                            }
                        }
                    }
                }
                if(state.message.isNotEmpty()) Text(state.message,color=androidx.compose.ui.graphics.Color(0xFFFFA59D),modifier=Modifier.semantics{liveRegion=LiveRegionMode.Polite})
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    PixelButton("Cuaderno",{notebook=true},Modifier.weight(1f))
                    PixelButton("Dejar selección",model::clearSelection,Modifier.weight(1f),state.selectedSuspect!=null)
                }
                PixelButton("Retirar seleccionado",model::remove,Modifier.fillMaxWidth(),state.selectedSuspect in state.positions)
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                    PixelButton("Reiniciar",{model.reset();focus=null},Modifier.weight(1f))
                    PixelButton("Comprobar",model::check,Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        walk?.let{movement->drag.viewport?.let{viewportBounds->
            val width=with(LocalDensity.current){viewportBounds.width.toDp()}
            val height=with(LocalDensity.current){viewportBounds.height.toDp()}
            Box(Modifier.offset{IntOffset((viewportBounds.left-origin.x).roundToInt(),(viewportBounds.top-origin.y).roundToInt())}.size(width,height).clipToBounds()){
                WalkingLayer(movement,checkNotNull(model.case.scene),drag,viewportBounds.topLeft,{id,direction->facing[id]=direction},model::finishWalk)
            }
        }}
        drag.suspect?.let{id->
            CharacterSprite(id,Modifier.offset{IntOffset((drag.point.x-origin.x).roundToInt()-24.dp.roundToPx(),(drag.point.y-origin.y).roundToInt()-66.dp.roundToPx())}
                .size(48.dp,64.dp),drag.direction,walking=true)
        }
        inspected?.let{asset->RoomInspector(asset){inspected=null}}
        if(notebook) androidx.compose.material3.Surface(Modifier.fillMaxSize().pointerInput(Unit){detectTapGestures{}},color=DetectiveInk){InvestigationNotebook(model,state){notebook=false}}
    }
}

@Composable fun DetectiveResult(model:InvestigationViewModel,state:InvestigationState) {
    val steps=remember(state.positions){Resolution.steps(model.case,state.positions)}
    val culprit=Engine.culprit(model.case,state.positions)
    val room=model.case.scene?.rooms?.firstOrNull{it.position==model.case.office}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(18.dp)){
        Column(Modifier.fillMaxWidth().detectivePanel(selected=true).padding(18.dp),horizontalAlignment=Alignment.CenterHorizontally){
            Text("CASO RESUELTO",color=DetectiveGold,fontFamily=DetectiveType,fontSize=34.sp)
            Text("EXPEDIENTE ${model.case.id}",color=DetectivePaper,fontFamily=FontFamily.Monospace)
        }
        PixelTitle("Culpable: ${culprit?.name.orEmpty()}")
        Text("${room?.name?:model.case.office.label} · ${model.case.location}",color=DetectivePaper)
        Column(Modifier.detectivePanel(parchment=true).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){
            Text(model.case.explanation,color=DetectiveInk,fontSize=16.sp,lineHeight=23.sp)
            steps.forEachIndexed{index,step->
                Text("${index+1}. ${step.clue}",color=DetectiveInk,fontSize=16.sp,lineHeight=23.sp)
                Text("${step.remaining} reconstrucciones compatibles."+(if(step.facts.isEmpty())"" else "\n"+step.facts.joinToString("\n")),color=DetectiveInk,fontFamily=FontFamily.Monospace,fontSize=13.sp,lineHeight=19.sp)
            }
        }
        PixelButton("Volver al menú",model::menu,Modifier.fillMaxWidth())
        PixelButton("Jugar de nuevo",model::reset,Modifier.fillMaxWidth())
    }
}
