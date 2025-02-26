package com.colleagues.austrom.extensions

import android.graphics.Canvas
import android.text.StaticLayout
import androidx.core.graphics.withTranslation

/**
 * Extension for drawing with translation
 */
fun StaticLayout.draw(canvas: Canvas, x: Float, y: Float) {
    canvas.withTranslation(x, y) {
        draw(this)
    }
}