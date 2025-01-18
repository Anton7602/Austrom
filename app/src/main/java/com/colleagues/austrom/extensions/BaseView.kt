package com.colleagues.austrom.extensions

import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View

abstract class BaseView@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

    protected fun initPaints(textPaint: TextPaint, textSize: Float, textColor: Int) {
        textPaint.color = textColor
        textPaint.isAntiAlias = true
        textPaint.textSize = textSize
    }

    protected fun initPaints(paint: Paint, color: Int) {
        paint.color = color
        paint.isAntiAlias = true
    }

    protected fun getTextBounds(text: String, textPaint: TextPaint): Rect {
        val bounds = Rect()
        textPaint.getTextBounds(text, 0, text.length, bounds)
        return bounds
    }

    protected fun getTextWidth(text: String, textPaint: TextPaint): Int { return getTextBounds(text, textPaint).width()  }
    protected fun getTextHeight(text: String, textPaint: TextPaint): Int { return getTextBounds(text, textPaint).height() }
}