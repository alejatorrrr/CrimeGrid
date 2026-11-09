package com.crimegrid.ui

import androidx.compose.foundation.gestures.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.crimegrid.data.Position

/** One recognizer owns the long press: a character hit drags; free floor inspects only without selection. */
@Composable fun Modifier.roomGestures(suspect:String?,selected:String?,footX:Float,footY:Float,drag:DragController,
    onTap:()->Unit,onInspect:()->Unit,onDrop:(String,Position)->Unit):Modifier {
    var origin by remember{mutableStateOf(Offset.Zero)}
    val tap by rememberUpdatedState(onTap);val inspect by rememberUpdatedState(onInspect);val drop by rememberUpdatedState(onDrop)
    return onGloballyPositioned{origin=it.positionInRoot()}.pointerInput(suspect,selected,drag){
        awaitEachGesture {
            val down=awaitFirstDown(requireUnconsumed=false)
            val press=awaitLongPressOrCancellation(down.id)
            if(press!=null){
                val height=size.height*.34f;val width=height*.75f
                val padding=minOf(size.height*.14f,viewConfiguration.touchSlop*4)
                val actor=Rect(size.width*footX-width/2-padding,size.height*footY-height-padding,
                    size.width*footX+width/2+padding,size.height*footY+padding)
                if(suspect!=null && actor.contains(down.position)){
                    drag.start(suspect,origin+press.position)
                    try {
                        val success=drag(press.id){change->val delta=change.positionChange();change.consume();drag.move(delta)}
                        if(success){
                            currentEvent.changes.forEach{if(it.changedToUpIgnoreConsumed())it.consume()}
                            drag.finish(drop)
                        }else drag.cancel()
                    } finally {drag.cancel()}
                }else{
                    if(selected==null)inspect()
                    if(currentEvent.changes.any{it.pressed})waitForUpOrCancellation()?.consume()
                    else currentEvent.changes.forEach{if(it.changedToUpIgnoreConsumed())it.consume()}
                }
            }else{
                val up=currentEvent.changes.firstOrNull{it.id==down.id}
                if(up!=null && up.changedToUpIgnoreConsumed() && !up.isConsumed &&
                    (up.position-down.position).getDistance()<=viewConfiguration.touchSlop){up.consume();tap()}
            }
        }
    }
}
