package se.pbt.curiositas.date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Verifies which historical dates are accepted, since every later calculation (display text,
 * age, statistics) relies on dates being valid.
 */
class HistoricalDateTest {

    /** Accepts dates at every level of precision, from a bare year to an exact day. */
    @ParameterizedTest(name = "{0}-{1}-{2} ± {3}")
    @CsvSource(nullValues = "null", value = {
            "1718, 11,   22,   0",
            "1718, 11,   null, 0",
            "1718, null, null, 0",
            "1680, null, null, 5",
    })
    void acceptsDatesAtEveryPrecision(int year, Integer month, Integer day, int uncertaintyYears) {
        assertThatNoException()
                .isThrownBy(() -> new HistoricalDate(year, month, day, uncertaintyYears));
    }

    /** Accepts year 0 and negative years, since years are astronomical and reach back before Christ. */
    @ParameterizedTest
    @ValueSource(ints = {0, -43, -2500})
    void acceptsYearsBeforeChrist(int year) {
        assertThatNoException().isThrownBy(() -> new HistoricalDate(year, 3, 15, 0));
    }

    /** Rejects months outside the calendar. */
    @ParameterizedTest
    @ValueSource(ints = {0, 13, -1})
    void rejectsMonthOutOfRange(int month) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalDate(1718, month, null, 0))
                .withMessageContaining("Month");
    }

    /** Rejects a day without a month, since a day alone does not identify a point in time. */
    @Test
    void rejectsDayWithoutMonth() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalDate(1718, null, 22, 0))
                .withMessageContaining("requires a month");
    }

    /** Rejects days that do not exist in the given month. */
    @ParameterizedTest(name = "{0}-{1}-{2}")
    @CsvSource({
            "1718,  4, 31",
            "1718,  1, 32",
            "1718,  1,  0",
            "1718,  2, 29",
            "1718, 11, -1",
    })
    void rejectsDayOutsideMonth(int year, int month, int day) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalDate(year, month, day, 0))
                .withMessageContaining("Day");
    }

    /**
     * Accepts 29 February in every year divisible by four, including years such as 1500 that
     * are leap years only in the Julian calendar used by many older sources.
     */
    @ParameterizedTest
    @ValueSource(ints = {2024, 2000, 1500, 1700, 0, -44})
    void acceptsLeapDayInEveryYearDivisibleByFour(int year) {
        assertThatNoException().isThrownBy(() -> new HistoricalDate(year, 2, 29, 0));
    }

    /** Rejects 29 February in years that are not leap years in any calendar. */
    @ParameterizedTest
    @ValueSource(ints = {2023, 1718, -43})
    void rejectsLeapDayInOtherYears(int year) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalDate(year, 2, 29, 0));
    }

    /** Rejects a negative uncertainty, since uncertainty is a distance in years. */
    @Test
    void rejectsNegativeUncertainty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new HistoricalDate(1680, null, null, -5))
                .withMessageContaining("Uncertainty");
    }
}
