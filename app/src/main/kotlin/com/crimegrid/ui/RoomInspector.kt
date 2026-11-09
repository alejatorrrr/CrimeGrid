package com.crimegrid.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/** Read-only art viewer. Neither gesture state nor image identity is written into the investigation. */
@Composable fun RoomInspector(asset:String,onClose:()->Unit) {
    val assets=LocalContext.current.assets
    val image=remember(asset){checkNotNull(PixelAssets.bitmap(assets,asset)).asImageBitmap()}
    var zoom by remember(asset){mutableFloatStateOf(1f)}
    var pan by remember(asset){mutableStateOf(Offset.Zero)}
    BackHandler(onBack=onClose)
    Surface(Modifier.fillMaxSize().testTag("room-inspector").pointerInput(Unit){detectTapGestures{}},color=DetectiveInk){
        Column(Modifier.fillMaxSize().padding(12.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
            PixelTitle("Inspección")
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds().testTag("inspector-image")
                .semantics { contentDescription="Ilustración ampliada";stateDescription="${(zoom*100).roundToInt()}%" }) {
                val density=androidx.compose.ui.platform.LocalDensity.current
                val width=with(density){maxWidth.toPx()};val height=with(density){maxHeight.toPx()}
                val base=minOf(width,height)
                fun limited(value:Offset,scale:Float)=Offset(value.x.coerceIn(-maxOf(0f,(base*scale-width)/2),maxOf(0f,(base*scale-width)/2)),
                    value.y.coerceIn(-maxOf(0f,(base*scale-height)/2),maxOf(0f,(base*scale-height)/2)))
                Canvas(Modifier.fillMaxSize().pointerInput(asset,width,height){
                    detectTransformGestures { centroid,delta,factor,_->
                        val next=(zoom*factor).coerceIn(1f,6f)
                        val center=Offset(width/2,height/2)
                        val relative=centroid-center
                        pan=limited(relative-(relative-pan)*(next/zoom)+delta,next)
                        zoom=next
                    }
                }){
                    val extent=(base*zoom).roundToInt()
                    drawImage(image,dstOffset=IntOffset(((size.width-extent)/2+pan.x).roundToInt(),((size.height-extent)/2+pan.y).roundToInt()),
                        dstSize=IntSize(extent,extent),filterQuality=FilterQuality.None)
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                PixelButton("Alejar",{zoom=(zoom/1.5f).coerceAtLeast(1f);pan=Offset.Zero},Modifier.weight(1f))
                PixelButton("Acercar",{zoom=(zoom*1.5f).coerceAtMost(6f)},Modifier.weight(1f))
            }
            PixelButton("Cerrar visor",onClose,Modifier.fillMaxWidth())
        }
    }
}
