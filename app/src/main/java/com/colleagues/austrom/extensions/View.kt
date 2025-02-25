package com.colleagues.austrom.extensions

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