package com.crimegrid.ui

import android.graphics.BitmapFactory
import java.text.Normalizer
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.crimegrid.domain.WalkDirection
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

/** Optional local PNGs. Missing artwork uses explicit placeholders, never a network request. */
@Composable fun pixelImage(path: String?, modifier: Modifier, content: String? = null, scale: ContentScale = ContentScale.Fit): Boolean {
    val assets = LocalContext.current.assets
    val bitmap = remember(path) {
        path?.let { PixelAssets.bitmap(assets,it)?.asImageBitmap() }
    }
    if (bitmap != null) Image(bitmap, content, modifier, contentScale = scale, filterQuality = FilterQuality.None)
    return bitmap != null
}

@Composable private fun PlaceholderSprite(id: String, modifier: Modifier) {
    if (!pixelImage("pixelart/characters/${spriteFileName(id)}.png", modifier, scale = ContentScale.Fit)) {
        // Temporary original pixel silhouette until the final transparent character PNG is supplied.
        Canvas(modifier) {
            val unit = minOf(size.width / 12f, size.height / 18f)
            val left = (size.width - unit * 12) / 2
            fun block(x: Int, y: Int, w: Int, h: Int, color: Color) = drawRect(color,
                Offset(left + x * unit, y * unit), Size(w * unit, h * unit))
            val colors = listOf(Color(0xFF70C9B5), Color(0xFFE5AF67), Color(0xFFAF9BDF), Color(0xFF77AEDD))
            val coat = colors[(id.hashCode() and Int.MAX_VALUE) % colors.size]
            block(4,1,4,2,Color(0xFF302D3D)); block(3,3,6,4,Color(0xFFE3B58F))
            block(2,7,8,7,coat); block(1,8,2,5,coat); block(9,8,2,5,coat)
            block(3,14,2,4,Color(0xFF263143)); block(7,14,2,4,Color(0xFF263143))
        }
    }
}

/** ASCII filenames keep the art handoff independent from accents in narrative names. */
fun spriteFileName(id: String): String = Normalizer.normalize(id, Normalizer.Form.NFD)
    .filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }

/** Four supplied sheets are drawn by frame, never squeezed into a single still image. */
@Composable fun CharacterSprite(id: String, modifier: Modifier, direction: WalkDirection = WalkDirection.DOWN, walking: Boolean = false) {
    val key = when(val normalized=spriteFileName(id)) {
        "ada"->"alma";"gael"->"dario";"lara"->"irene";"victor"->"tomas";else->normalized
    }
    if (key !in setOf("alma", "dario", "irene", "tomas")) {
        PlaceholderSprite(id, modifier)
        return
    }
    val assets = LocalContext.current.assets
    val source = remember(key) {
        checkNotNull(PixelAssets.bitmap(assets,"pixelart/characters/${key}_walk_48x64.png")).also {
            check(it.width == 192 && it.height == 256 && it.hasAlpha())
        }
    }
    val sheet = remember(source) { source.asImageBitmap() }
    val feet = remember(source) { PixelAssets.spriteFeet(key,source) }

    var frame by remember(key) { mutableIntStateOf(0) }
    LaunchedEffect(walking) {
        frame = 0
        if (walking) while (true) { delay(120); frame = (frame + 1) % 4 }
    }
    Canvas(modifier) {
        val scale = minOf(size.width / 48f, size.height / 64f)
        val foot = feet[direction.row][frame]
        drawImage(sheet, srcOffset = IntOffset(frame * 48, direction.row * 64), srcSize = IntSize(48,64),
            dstOffset = IntOffset((size.width / 2 - foot.first * scale).roundToInt(), (size.height - foot.second * scale).roundToInt()),
            dstSize = IntSize((48 * scale).roundToInt().coerceAtLeast(1), (64 * scale).roundToInt().coerceAtLeast(1)),
            filterQuality = FilterQuality.None)
    }
}
