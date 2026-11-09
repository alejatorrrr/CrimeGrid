package com.crimegrid.ui

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.crimegrid.data.*
import com.crimegrid.domain.WalkDirection
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable private data class AtlasPatch(val asset:String,val x:Int,val y:Int)

private object MansionAtlas {
    var image: androidx.compose.ui.graphics.ImageBitmap? = null
    @Synchronized fun load(assets: android.content.res.AssetManager, scene: Scene): androidx.compose.ui.graphics.ImageBitmap {
        image?.let { return it }
        val atlas = Bitmap.createBitmap(1024,1024,Bitmap.Config.ARGB_8888).also { bitmap ->
            val canvas=AndroidCanvas(bitmap)
            scene.rooms.forEach { room ->
                canvas.drawBitmap(checkNotNull(PixelAssets.bitmap(assets,room.asset)),room.position.column*256f,room.position.row*256f,null)
            }
            val patches=assets.open("pixelart/mansion/patches/manifest.json").bufferedReader().use { Json.decodeFromString<List<AtlasPatch>>(it.readText()) }
            patches.forEach { patch ->
                canvas.drawBitmap(checkNotNull(PixelAssets.bitmap(assets,patch.asset)),patch.x.toFloat(),patch.y.toFloat(),null)
            }
        }
        check(atlas.width==atlas.height)
        return atlas.asImageBitmap().also { image=it }
    }
}

/** A single nearest-neighbor atlas prevents subpixel seams. Hit targets use the identical camera. */
@Composable fun MansionMap(case: Case, state: InvestigationState, feedback: MoveFeedback?, drag: DragController,
    walk: InvestigationViewModel.Walk?, facing: Map<String,WalkDirection>, focus: Int?,
    onRoomTap: (Position)->Unit, onDrop:(String,Position)->Unit, onInspect:(String)->Unit={}) {
    val scene=checkNotNull(case.scene)
    val assets=LocalContext.current.assets
    val atlas=remember(case.id) { if(case.id=="003") CastleArtwork.atlas(assets,CastleArtwork.manifest(assets)) else MansionAtlas.load(assets,scene) }
    val scale by animateFloatAsState(if(focus==null) 1f else 2.4f,tween(320),label="room-zoom")
    val targetX=if(focus==null) 0f else (.5f-2.4f*((focus%4+.5f)/4)).coerceIn(1f-2.4f,0f)
    val targetY=if(focus==null) 0f else (.5f-2.4f*((focus/4+.5f)/4)).coerceIn(1f-2.4f,0f)
    val shiftX by animateFloatAsState(targetX,tween(320),label="camera-x")
    val shiftY by animateFloatAsState(targetY,tween(320),label="camera-y")
    DisposableEffect(case.id) { onDispose { drag.cancel();drag.cells.clear();drag.roomBounds.clear();drag.viewport=null } }
    BoxWithConstraints(Modifier.fillMaxWidth().aspectRatio(1f).clipToBounds().onGloballyPositioned{drag.viewport=it.boundsInRoot()}.testTag("room-board")
        .semantics { stateDescription=if(focus==null) "Vista general" else "Vista ampliada" }) {
        val density=LocalDensity.current
        val width=with(density){maxWidth.toPx()}
        val extent=width*scale
        val left=width*shiftX
        val top=width*shiftY
        Canvas(Modifier.fillMaxSize()) {
            drawImage(atlas,dstOffset=IntOffset(left.roundToInt(),top.roundToInt()),
                dstSize=IntSize(extent.roundToInt(),extent.roundToInt()),filterQuality=FilterQuality.None)
        }
        scene.rooms.forEach { room ->
            val pos=room.position
            val x=(left+pos.column*extent/4).roundToInt()
            val y=(top+pos.row*extent/4).roundToInt()
            val right=(left+(pos.column+1)*extent/4).roundToInt()
            val bottom=(top+(pos.row+1)*extent/4).roundToInt()
            val w=with(density){(right-x).toDp()};val h=with(density){(bottom-y).toDp()}
            val suspect=case.suspects.find { state.positions[it.id]==pos }
            var pulse by remember { mutableStateOf(false) }
            LaunchedEffect(feedback?.sequence) {
                pulse=false
                if(feedback?.position==pos) repeat(2){pulse=true;delay(180);pulse=false;delay(180)}
            }
            Box(Modifier.offset{IntOffset(x,y)}.size(w,h)
                .onGloballyPositioned {
                    drag.cells[pos]=it.boundsInRoot()
                    drag.roomBounds[pos]=Rect(it.positionInRoot(),Size(it.size.width.toFloat(),it.size.height.toFloat()))
                }
                .testTag("map-cell-${pos.label}")
                .roomGestures(suspect?.id,state.selectedSuspect,room.footX,room.footY,drag,{onRoomTap(pos)},{onInspect(room.asset)},onDrop)
                .semantics {
                    contentDescription="Casilla ${pos.label}${if(pos==case.office) ", ${room.name.lowercase()}" else ""}, ${suspect?.name?:"vacía"}"
                    if(feedback?.position==pos) stateDescription="Movimiento rechazado"
                    onClick{onRoomTap(pos);true}
                    if(state.selectedSuspect==null) onLongClick(label="Inspeccionar"){onInspect(room.asset);true}
                }) {
                if(suspect!=null && state.selectedSuspect==suspect.id) Canvas(Modifier.fillMaxSize()) {
                    val px=(size.width/48).coerceAtLeast(1f)
                    val cx=size.width*room.footX;val cy=size.height*room.footY
                    // A small marker at the character's feet, never a border around a room.
                    drawRect(Color(0xFFD8B56A),Offset(cx-6*px,cy),Size(12*px,px))
                    drawRect(Color(0xFFD8B56A),Offset(cx-7*px,cy-px),Size(px,px))
                    drawRect(Color(0xFFD8B56A),Offset(cx+6*px,cy-px),Size(px,px))
                }
                if(suspect!=null && drag.suspect!=suspect.id && walk?.suspect!=suspect.id) {
                    val spriteH=h*.34f
                    CharacterSprite(suspect.id,Modifier.offset(w*room.footX-spriteH*.375f,h*room.footY-spriteH)
                        .size(spriteH*.75f,spriteH),facing[suspect.id]?:WalkDirection.DOWN)
                }
                if(pulse) Canvas(Modifier.fillMaxSize()){drawRect(Color(0x99C22C3C))}
            }
        }
    }
}
