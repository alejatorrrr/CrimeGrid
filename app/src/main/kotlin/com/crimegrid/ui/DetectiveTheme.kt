package com.crimegrid.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.crimegrid.R

val DetectiveGold=Color(0xFFC9A565)
val DetectiveInk=Color(0xFF170F12)
val DetectivePaper=Color(0xFFDDC493)
val DetectiveLeather=Color(0xFF382026)
val DetectiveType=FontFamily(Font(R.font.pixelify_sans))


fun Modifier.detectivePanel(parchment:Boolean=false,selected:Boolean=false):Modifier =
    background(if(parchment)DetectivePaper else DetectiveLeather).drawBehind {
        val px=2.dp.toPx()
        val border=if(selected) Color(0xFFF3CE79) else DetectiveGold.copy(alpha=.65f)
        drawRect(border,style=androidx.compose.ui.graphics.drawscope.Stroke(px))
        val inner=px*2
        drawRect(if(parchment)Color(0xFF8D6642) else Color(0xFF5F3B2D),Offset(inner,inner),
            Size(size.width-inner*2,size.height-inner*2),style=androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
        // Sparse, fixed grain instead of gradients, blur or decorative bitmap allocations.
        for(i in 0..24){
            val x=((i*73+19)%257)/257f*size.width;val y=((i*47+31)%239)/239f*size.height
            drawRect(if(parchment)Color(0x10804B22) else Color(0x156F4533),Offset(x,y),Size(2.dp.toPx(),1.dp.toPx()))
        }
    }

@Composable fun PixelButton(text:String,onClick:()->Unit,modifier:Modifier=Modifier,enabled:Boolean=true) {
    val interaction=remember{MutableInteractionSource()}
    val pressed by interaction.collectIsPressedAsState()
    Box(modifier.heightIn(min=48.dp).detectivePanel(selected=pressed)
        .clickable(interactionSource=interaction,indication=null,enabled=enabled,role=Role.Button,onClick=onClick)
        .padding(horizontal=12.dp,vertical=10.dp),contentAlignment=Alignment.Center) {
        Text(text,color=if(enabled)DetectiveGold else Color(0xFF8B7770),fontFamily=DetectiveType,
            fontSize=18.sp,lineHeight=22.sp)
    }
}

@Composable fun PixelTitle(text:String,modifier:Modifier=Modifier) {
    Text(text,modifier,fontFamily=DetectiveType,color=DetectiveGold,fontSize=24.sp,lineHeight=28.sp)
}

@Composable fun PixelIcon(kind:String,modifier:Modifier=Modifier) {
    Canvas(modifier.size(24.dp)) {
        val u=size.width/16
        fun block(x:Int,y:Int,w:Int,h:Int,color:Color=DetectiveGold)=drawRect(color,Offset(x*u,y*u),Size(w*u,h*u))
        when(kind){
            "book"->{block(2,2,11,12,Color(0xFF8D573F));block(4,3,8,10,DetectivePaper);block(6,5,4,1);block(6,8,4,1);block(3,2,1,12)}
            "quill"->{for(i in 0..8)block(3+i,12-i,2,2);block(3,13,9,1)}
            else->{block(3,2,7,1);block(2,3,1,6);block(10,3,1,6);block(3,9,7,1);block(10,10,2,2);block(12,12,2,2)}
        }
    }
}
