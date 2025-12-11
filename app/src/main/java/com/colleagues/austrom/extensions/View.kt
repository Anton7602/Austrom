package com.colleagues.austrom.extensions

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.View

fun View.setOnSafeClickListener(debounceTime: Long = 1000L, onClick: (View) -> Unit) {
    var lastClickTime = 0L

    this.setOnClickListener {
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            onClick(it)
        }
    }
}

fun View.setOnDoubleClickListener(doubleClickThresholdMs: Long = 300L, onDoubleClick: (View) -> Unit) {
    var lastClickTime = 0L

    this.setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < doubleClickThresholdMs) {
            onDoubleClick(view)
            lastClickTime = 0L
        } else {
            lastClickTime = currentTime
        }
    }
}

fun View.setCombinedClickListeners(doubleClickThresholdMs: Long = 200L,  onSingleClick: ((View) -> Unit)? = null,  onDoubleClick: ((View) -> Unit)? = null) {
    if (onSingleClick == null && onDoubleClick == null) return

    val handler = Handler(Looper.getMainLooper())
    var lastClickTime = 0L
    var pendingSingleClick: Runnable? = null

    this.setOnClickListener { view ->
        val currentTime = SystemClock.elapsedRealtime()
        if (currentTime - lastClickTime < doubleClickThresholdMs) {
            handler.removeCallbacks(pendingSingleClick!!) // Cancel the pending single click
            pendingSingleClick = null
            onDoubleClick?.invoke(view)
            lastClickTime = 0L
        } else {
            val singleClickAction = Runnable {
                onSingleClick?.invoke(view)
                pendingSingleClick = null
            }

            pendingSingleClick = singleClickAction
            handler.postDelayed(singleClickAction, doubleClickThresholdMs)
            lastClickTime = currentTime
        }
    }
}