package com.crimegrid.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crimegrid.data.InvestigationState

/** All annotations are player-authored. They never query the solver or evaluate an individual clue. */
@Composable fun InvestigationNotebook(model:InvestigationViewModel,state:InvestigationState,onClose:()->Unit) {
    BackHandler(onBack=onClose)
    Column(Modifier.fillMaxSize().testTag("notebook").padding(12.dp).detectivePanel(parchment=true)) {
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(10.dp)){
            PixelIcon("book")
            Text("Cuaderno de investigación",Modifier.weight(1f),color=DetectiveInk,fontFamily=DetectiveType,fontSize=23.sp,lineHeight=27.sp)
        }
        Text("${(state.reviewedClues+state.markedClues).size}/${model.case.clues.size} pistas revisadas",
            Modifier.padding(horizontal=18.dp),color=DetectiveInk,fontFamily=FontFamily.Monospace,fontSize=14.sp)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text(model.case.premise,color=DetectiveInk,fontSize=16.sp,lineHeight=23.sp)
            model.case.clues.forEachIndexed{index,clue->
                Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)){
                    Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                        PixelIcon(if(index in state.markedClues)"quill" else "search")
                        Text("${index+1}. ${clue.text}",color=DetectiveInk,fontSize=16.sp,lineHeight=23.sp)
                    }
                    val label=when(index){in state.markedClues->"Marcada";in state.reviewedClues->"Leída";else->"Pendiente"}
                    PixelButton("Pista ${index+1}: $label",{model.cycleNotebookClue(index)},Modifier.fillMaxWidth().testTag("clue-state-$index"))
                }
            }
            Text("Tus marcas no verifican las pistas.",color=DetectiveInk,fontSize=14.sp)
            PixelButton(if(state.hintsRevealed<model.case.hints.size)"Pedir ayuda (${state.hintsRevealed}/${model.case.hints.size})" else "Todas las ayudas vistas",
                model::requestHint,Modifier.fillMaxWidth(),state.hintsRevealed<model.case.hints.size)
            model.case.hints.take(state.hintsRevealed).forEachIndexed{index,hint->
                Text("Ayuda ${index+1}: ${hint.text}",color=DetectiveInk,fontSize=16.sp,lineHeight=23.sp)
            }
        }
        PixelButton("Cerrar cuaderno",onClose,Modifier.fillMaxWidth().padding(12.dp))
    }
}
