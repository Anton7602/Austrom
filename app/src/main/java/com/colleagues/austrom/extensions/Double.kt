package com.colleagues.austrom.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

fun Double.toMoneyFormat(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ' '
    }
    val decimalFormat = DecimalFormat("#,##0.00", symbols)
    return decimalFormat.format(this)
}