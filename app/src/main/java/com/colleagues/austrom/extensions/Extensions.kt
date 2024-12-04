package com.colleagues.austrom.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
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

fun String?.parseToDouble(): Double? {
    if (this.isNullOrEmpty()) return null
    val normalizedValue = this.replace(',', '.')
    return normalizedValue.toDoubleOrNull()
}

fun String?.parseToLocalDate(): LocalDate? {
    if (this.isNullOrEmpty()) return null
    val dateFormats = listOf(
        "dd.MM.yyyy HH:mm:ss",
        "dd.MM.yyyy",
        "yyyy-MM-dd",
        "dd-MM-yyyy",
        "MM/dd/yyyy",
        "yyyy/MM/dd",
        "yyyy-MM-dd HH:mm:ss",
        "dd/MM/yyyy HH:mm:ss",
        "MM-dd-yyyy",
        "yyyyMMdd"
    )

    for (format in dateFormats) {
        try {
            val formatter = DateTimeFormatter.ofPattern(format)
            return LocalDate.parse(this, formatter)
        } catch (_: DateTimeParseException) {
        }
    }
    return null
}

fun LocalDate.toDayOfWeekAndShortDateFormat(): String {
    val chipDayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).startWithUppercase()
    return "$chipDayOfWeek ${this.format(DateTimeFormatter.ofPattern("dd.MM"))}"
}