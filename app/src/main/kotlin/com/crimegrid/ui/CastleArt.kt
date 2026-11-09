package com.crimegrid.ui

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.crimegrid.data.*
import kotlin.math.roundToInt

object CastleArtwork {
    const val directory="pixelart/castle003/"
    fun manifest(assets:android.content.res.AssetManager)=ArtMapRepository{assets.open(directory+"manifest.json").bufferedReader().use{it.readText()}}.load()
    private var cached:androidx.compose.ui.graphics.ImageBitmap?=null
    @Synchronized fun atlas(assets:android.content.res.AssetManager,map:ArtMap):androidx.compose.ui.graphics.ImageBitmap {
        cached?.let{return it}
        val bitmap=Bitmap.createBitmap(1536,1536,Bitmap.Config.ARGB_8888)
        val canvas=AndroidCanvas(bitmap)
        map.rooms.forEach{room->canvas.drawBitmap(checkNotNull(PixelAssets.bitmap(assets,directory+room.file)),room.sourceCrop[0].toFloat(),room.sourceCrop[1].toFloat(),null)}
        return bitmap.asImageBitmap().also{cached=it}
    }
}
