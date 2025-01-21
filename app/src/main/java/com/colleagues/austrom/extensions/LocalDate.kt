package com.colleagues.austrom.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun LocalDate.toDayOfWeekAndShortDateFormat(): String {
    val chipDayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).startWithUppercase()
    return "$chipDayOfWeek ${this.format(DateTimeFormatter.ofPattern("dd.MM"))}"
}

fun LocalDate.toDayOfWeekAndLongDateFormat(): String {
    val chipDayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).startWithUppercase()
    return "$chipDayOfWeek ${this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
}

fun LocalDate.serialize() : String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}