package com.colleagues.austrom
import com.colleagues.austrom.extensions.parseToLocalDate
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate

class ExtensionsTest {
    private fun testParseToLocalDate(input: String?, expected: LocalDate?, isDayBeforeMonth: Boolean = true) { assertEquals(expected, input.parseToLocalDate(isDayBeforeMonth)) }
    @Test fun testFormat_dash_MM_dd_yy_HH_mm() { testParseToLocalDate("02-25-25 22:12", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_MM_dd_yy_H_mm() { testParseToLocalDate("02-25-25 2:28", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_M_dd_yy_H_mm() { testParseToLocalDate("1/13/2025 2:28", LocalDate.of(2025, 1, 13)) }
    @Test fun testFormat_dd_MM_yyyy_HH_mm_ss() { testParseToLocalDate("25.02.2025 14:30:00", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dot_dd_MM_yyyy_HH_mm() { testParseToLocalDate("25.02.2025 14:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dot_dd_MM_yyyy_H_mm() { testParseToLocalDate("25.02.2025 4:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dot_dd_MM_yyyy() { testParseToLocalDate("25.02.2025", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dot_yyyy_dd_MM() { testParseToLocalDate("2025.25.02", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_yyyy_MM_dd() { testParseToLocalDate("2025-02-25", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_dd_MM_yyyy() { testParseToLocalDate("25-02-2025", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_MM_dd_yyyy() { testParseToLocalDate("02/25/2025", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_yyyy_MM_dd() { testParseToLocalDate("2025/02/25", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_yyyy_MM_dd_HH_mm_ss() { testParseToLocalDate("2025-02-25 14:30:00", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_yyyy_MM_dd_HH_mm() {testParseToLocalDate("2025-02-25 14:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_yyyy_MM_dd_H_mm() { testParseToLocalDate("2025-02-25 4:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_dd_MM_yyyy_HH_mm_ss() { testParseToLocalDate("25/02/2025 14:30:00", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_dd_MM_yyyy_HH_mm() { testParseToLocalDate("25/02/2025 14:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_slash_dd_MM_yyyy_H_mm() { testParseToLocalDate("25/02/2025 4:30", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_dash_MM_dd_yyyy() { testParseToLocalDate("02-25-2025", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_yyyyMMdd() { testParseToLocalDate("25022025", LocalDate.of(2025, 2, 25)) }
    @Test fun testFormat_InvalidDate() { testParseToLocalDate("invalid-date", null) }
    @Test fun testParseToLocalDate_NullString() {  testParseToLocalDate(null, null) }
    @Test fun testParseToLocalDate_DayBeforeMonth() { testParseToLocalDate("02-25-2025", LocalDate.of(2025, 2, 25), false) }
}