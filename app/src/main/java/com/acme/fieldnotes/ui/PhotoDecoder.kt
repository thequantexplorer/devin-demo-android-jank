package com.acme.fieldnotes.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.tracing.trace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object PhotoDecoder {

    class Photo(val bitmap: ImageBitmap, val averageColor: Color)

    private val cache = LruCache<String, Photo>(32)
    private val decoding = Mutex()

    fun cached(path: String): Photo? = cache.get(path)

    /** Decodes [path] off the main thread, scaled down to about [reqWidth] pixels, and caches it. */
    suspend fun load(context: Context, path: String, reqWidth: Int): Photo =
        cache.get(path) ?: withContext(Dispatchers.IO) {
            decoding.withLock {
                cache.get(path) ?: run {
                    val bitmap = decode(context, path, reqWidth)
                    Photo(bitmap.asImageBitmap(), averageColor(bitmap)).also { cache.put(path, it) }
                }
            }
        }

    fun decode(context: Context, path: String, reqWidth: Int): Bitmap = trace("PhotoDecoder.decode") {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sampleSize = 1
        while (reqWidth > 0 && bounds.outWidth / (sampleSize * 2) >= reqWidth) sampleSize *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }!!
    }

    /** Average colour of the photo, used as the card background behind the image. */
    fun averageColor(bitmap: Bitmap): Color = trace("PhotoDecoder.averageColor") {
        val w = bitmap.width
        val h = bitmap.height
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        var r = 0L
        var g = 0L
        var b = 0L
        var n = 0L
        for (y in 0 until h step 4) {
            for (x in 0 until w step 4) {
                val p = pixels[y * w + x]
                r += (p shr 16) and 0xFF
                g += (p shr 8) and 0xFF
                b += p and 0xFF
                n++
            }
        }
        Color(red = (r / n).toInt(), green = (g / n).toInt(), blue = (b / n).toInt())
    }
}
