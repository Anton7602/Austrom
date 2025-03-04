package com.colleagues.austrom.extensions

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

//TODO("TESTING REQUIRED!!")
fun Double.toMoneyFormat(): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply {
        decimalSeparator = '.'
        groupingSeparator = ' '
    }
    val decimalFormat = DecimalFormat("#,##0.00", symbols)
    return decimalFormat.format(this)
}

fun Double.roundToAFirstDigit(): Double {
    val scale = 10.0.pow(floor(log10(abs(this))))
    return Math.round(this / scale) * scale
}