package com.colleagues.austrom.extensions

import com.colleagues.austrom.AustromApplication
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
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

fun LocalDate.getLocalizedMonthName(): String {
    return "${this.format(DateTimeFormatter.ofPattern("MMMM", Locale(AustromApplication.appLanguageCode ?: "en"))).startWithUppercase().replace(".","")} ${this.format(DateTimeFormatter.ofPattern("yyyy"))}"
}

fun LocalDate.getLocalizedWeekName(): String {
    return "${getFirstDayOfWeek().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}-${getLastDayOfWeek().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}"
}

fun LocalDate.getWeekNumber(): Int {
    val weekFields = WeekFields.of(Locale.getDefault())
    return this.get(weekFields.weekOfWeekBasedYear())
}

fun LocalDate.toDayMonthYearFormat(): String {return this.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}
fun LocalDate.getFirstDayOfWeek(): LocalDate { return this.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))}
fun LocalDate.getLastDayOfWeek(): LocalDate { return this.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))}
fun LocalDate.getFirstDayOfMonth(): LocalDate {return this.withDayOfMonth(1)}
fun LocalDate.getLastDayOfMonth(): LocalDate {return this.with(TemporalAdjusters.lastDayOfMonth())}
fun LocalDate.getFirstDayOfYear(): LocalDate {return this.withDayOfYear(1)}
fun LocalDate.getLastDayOfYear(): LocalDate {return this.withDayOfYear(this.lengthOfYear())}
fun LocalDate.serialize() : String { return this.format(DateTimeFormatter.ISO_LOCAL_DATE) }
fun LocalDate.toInt(): Int { return this.year * 10000 + this.monthValue * 100 + this.dayOfMonth }

fun LocalDate.getListOfDaysTillDate(otherDate: LocalDate): List<LocalDate> {
    val days = mutableListOf<LocalDate>()
    var current = this
    while (current <= otherDate) {
        days.add(current)
        current = current.plusDays(1)
    }
    return days
}

fun LocalDate.getListOfMonthTillDate(otherDate: LocalDate): List<YearMonth> {
    val months = mutableListOf<YearMonth>()
    var currentYearMonth = YearMonth.from(this)
    val endYearMonth = YearMonth.from(otherDate)

    while (currentYearMonth <= endYearMonth) {
        months.add(currentYearMonth)
        currentYearMonth = currentYearMonth.plusMonths(1)
    }
    return months
}

