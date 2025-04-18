package com.colleagues.austrom
import com.colleagues.austrom.extensions.parseToLocalDate
import com.colleagues.austrom.extensions.toMoneyFormat
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


    private fun testTurnToMoneyFormat(input: Double, expected: String) { assertEquals(expected, input.toMoneyFormat()) }
    @Test fun testPositiveWholeNumber() { testTurnToMoneyFormat(100.0, "100")  }
    @Test fun testPositiveDecimalNumber() { testTurnToMoneyFormat(1234.56, "1 234.56") }
    @Test fun testPositiveDecimalNumberWithFewerDigits() { testTurnToMoneyFormat(12.3, "12.30") }
    @Test fun testPositiveDecimalNumberWithMoreDigits() { testTurnToMoneyFormat(98765.4321, "98 765.43") }
    @Test fun testZero() { testTurnToMoneyFormat(0.0, "0") }
    @Test fun testNegativeWholeNumber() { testTurnToMoneyFormat(-50.0, "-50") }
    @Test fun testNegativeDecimalNumber() { testTurnToMoneyFormat(-678.90, "-678.90") }
    @Test fun testNegativeDecimalNumberWithFewerDigits() { testTurnToMoneyFormat(-1.2, "-1.20") }
    @Test fun testNegativeDecimalNumberWithMoreDigits() { testTurnToMoneyFormat(-3456.789, "-3 456.79") }
    @Test fun testLargePositiveNumber() { testTurnToMoneyFormat(1000000.0, "1 000 000") }
    @Test fun testSmallPositiveDecimal() { testTurnToMoneyFormat(0.01, "0.01")  }
    @Test fun testSmallNegativeDecimal() { testTurnToMoneyFormat(-0.05, "-0.05")  }
}