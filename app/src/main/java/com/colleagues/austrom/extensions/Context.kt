package com.colleagues.austrom.extensions

import android.content.Context
import android.graphics.Paint
import android.text.TextPaint
import android.util.TypedValue

/**
 * Extension for converting density-independent pixels to pixels
 * @property dp density-independent pixels value
 * @return Pixels value of provided dp
 */
fun Context.dpToPx(dp: Int): Float {
    return dp.toFloat() * this.resources.displayMetrics.density
}

/**
 * Extension for converting scale-independent pixels to pixels.
 * @property sp scale-independent pixels value
 * @return Pixels value of provided sp
 */
fun Context.spToPx(sp: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), this.resources.displayMetrics);
}

