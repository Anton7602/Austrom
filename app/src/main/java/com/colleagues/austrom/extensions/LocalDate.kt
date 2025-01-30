package com.colleagues.austrom.extensions

import com.colleagues.austrom.AustromApplication
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

fun LocalDate.toDayAndShortMonthNameFormat(): String {
    return "${this.format(DateTimeFormatter.ofPattern("dd"))} ${this.format(DateTimeFormatter.ofPattern("MMM", Locale(AustromApplication.appLanguageCode ?: "en"))).startWithUppercase().replace(".","")}"
}

fun LocalDate.serialize() : String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun LocalDate.toInt(): Int { return this.year * 10000 + this.monthValue * 100 + this.dayOfMonth }