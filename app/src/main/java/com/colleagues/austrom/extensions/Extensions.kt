package com.colleagues.austrom.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

fun Double.toMoneyFormat(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ' '
    }
    val decimalFormat = DecimalFormat("#,##0.00", symbols)
    return decimalFormat.format(this)
}

fun String?.startWithUppercase(): String {
    return this?.replaceFirstChar { it.uppercase() } ?: ""
}

fun LocalDate.toDayOfWeekAndShortDateFormat(): String {
    val chipDayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).startWithUppercase()
    return "$chipDayOfWeek ${this.format(DateTimeFormatter.ofPattern("dd.MM"))}"
}