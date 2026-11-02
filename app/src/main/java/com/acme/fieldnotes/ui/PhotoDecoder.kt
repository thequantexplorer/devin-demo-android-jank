package com.acme.fieldnotes.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.tracing.trace

object PhotoDecoder {

    fun decode(context: Context, path: String): Bitmap = trace("PhotoDecoder.decode") {
        context.assets.open(path).use { BitmapFactory.decodeStream(it) }
    }

    /** Average colour of the photo, used as the card background behind the image. */
    fun averageColor(bitmap: Bitmap): Color = trace("PhotoDecoder.averageColor") {
        var r = 0L
        var g = 0L
        var b = 0L
        var n = 0L
        for (y in 0 until bitmap.height step 4) {
            for (x in 0 until bitmap.width step 4) {
                val p = bitmap.getPixel(x, y)
                r += (p shr 16) and 0xFF
                g += (p shr 8) and 0xFF
                b += p and 0xFF
                n++
            }
        }
        Color(red = (r / n).toInt(), green = (g / n).toInt(), blue = (b / n).toInt())
    }
}
