package com.crimegrid.ui

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.util.LruCache

/** One shared, byte-bounded CPU cache; every tile, panel and moving sprite reuses decoded pixels. */
object PixelAssets {
    private val cache = object : LruCache<String, Bitmap>(16 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }
    private val absent = mutableSetOf<String>()
    @Synchronized fun bitmap(assets: AssetManager, path: String): Bitmap? {
        cache.get(path)?.let { return it }
        if (path in absent) return null
        val decoded = runCatching { assets.open(path).use { BitmapFactory.decodeStream(it) } }.getOrNull()
        if (decoded == null) absent += path else cache.put(path, decoded)
        return decoded
    }
    private val feet = mutableMapOf<String, Array<Array<Pair<Float,Float>>>>()
    @Synchronized fun spriteFeet(key: String, source: Bitmap): Array<Array<Pair<Float,Float>>> = feet.getOrPut(key) {
        Array(4) { row -> Array(4) { frame ->
            var minX=48; var maxX=0; var bottom=0
            for (y in 0 until 64) for (x in 0 until 48) if ((source.getPixel(frame*48+x,row*64+y) ushr 24)>32) {
                minX=minOf(minX,x); maxX=maxOf(maxX,x); bottom=maxOf(bottom,y+1)
            }
            Pair((minX+maxX+1)/2f,bottom.toFloat())
        } }
    }
}
