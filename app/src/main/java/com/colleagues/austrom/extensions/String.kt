package com.colleagues.austrom.extensions

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

fun String?.startWithUppercase(): String {
    return this?.replaceFirstChar { it.uppercase() } ?: ""
}

fun String?.parseToDouble(): Double? {
    if (this.isNullOrEmpty()) return null
    val normalizedValue = this.replace(',', '.')
    return normalizedValue.toDoubleOrNull()
}

fun String?.parseToLocalDate(isDayBeforeMonth: Boolean = true): LocalDate? {
    if (this.isNullOrEmpty()) return null
    var normalizedValue = this
    if (normalizedValue.contains('-')) normalizedValue = normalizedValue.replace('-','.')
    if (normalizedValue.contains('/')) normalizedValue = normalizedValue.replace('/','.')
    if (normalizedValue.contains('\\')) normalizedValue = normalizedValue.replace('\\','.')
    if (normalizedValue.contains(':')) normalizedValue = normalizedValue.substring(0, normalizedValue.lastIndexOf(' '))

    return try {
        val numbers = normalizedValue.split('.').toMutableList()
        var year =-1
        var month = -1
        var day=-1
        if (numbers.count()==3) {
            for (i in 0..2) {
                if (numbers[i].length==4) {
                    year=numbers[i].toInt()
                    numbers.removeAt(i)
                    break
                }
            }
            if (numbers.count()==3) {
                year = numbers[2].toInt()
                if (year<100) year += 2000
                numbers.removeAt(2)
            }
            if (numbers.count()==2) {
                if ((isDayBeforeMonth || numbers[0].toInt()>12) && numbers[1].toInt()<12) {
                    day = numbers[0].toInt()
                    month = numbers[1].toInt()
                } else {
                    day = numbers[1].toInt()
                    month = numbers[0].toInt()
                }
            } else {
                return null
            }
        } else if (numbers.count()==1) {
            var number1 = numbers[0].substring(0,2).toInt()
            var number2 = numbers[0].substring(2,4).toInt()
            var number3 = numbers[0].substring(4).toInt()
            if (number3<100) number3+=2000
            year = number3
            if (number1>12 || isDayBeforeMonth) {
                day = number1
                month = number2
            } else {
                day = number2
                month = number1
            }
        } else {
            return null
        }
        LocalDate.of(year,month,day)
    } catch (ex: Exception) {
        null
    }
}

fun String?.substringBeforeLastPipe(): String? {
    return this?.substringBeforeLast('|')
}

fun String?.intAfterLastPipe(): Int? {
    return this?.substringAfterLast('|')?.toIntOrNull()
}