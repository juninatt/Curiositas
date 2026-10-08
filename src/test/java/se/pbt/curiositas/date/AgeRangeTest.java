package se.pbt.curiositas.date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

/**
 * Verifies the age range calculation, since ages are shown to users and used in statistics and
 * must cover every reading of uncertain dates without inventing precision.
 */
class AgeRangeTest {

    /**
     * Calculates the youngest and oldest possible age for dates at every precision, including
     * years before Christ and overlapping uncertain dates.
     */
    @ParameterizedTest(name = "{0}: {1}-{2}-{3} ± {4} to {5}-{6}-{7} ± {8} -> {9}–{10}")
    @CsvSource(nullValues = "null", value = {
            "exact dates after the birthday,          1680, 3,    15,   0, 1718, 11,   22,   0, 38, 38",
            "exact dates the day before the birthday, 1680, 3,    15,   0, 1718, 3,    14,   0, 37, 37",
            "exact dates on the birthday,             1680, 3,    15,   0, 1718, 3,    15,   0, 38, 38",
            "same month but unknown days,             1680, 3,    null, 0, 1718, 3,    null, 0, 37, 38",
            "years only,                              1680, null, null, 0, 1718, null, null, 0, 37, 38",
            "uncertain birth year,                    1680, null, null, 5, 1718, null, null, 0, 32, 43",
            "Julius Caesar (100 BC to 44 BC),         -99,  7,    12,   0, -43,  3,    15,   0, 55, 55",
            "born and died the same year,             1718, null, null, 0, 1718, null, null, 0, 0,  0",
            "overlapping uncertain dates,             1700, null, null, 5, 1702, null, null, 0, 0,  7",
    })
    void calculatesYoungestAndOldestPossibleAge(String description,
                                                int birthYear, Integer birthMonth, Integer birthDay, int birthUncertainty,
                                                int deathYear, Integer deathMonth, Integer deathDay, int deathUncertainty,
                                                int expectedMin, int expectedMax) {
        HistoricalDate birth = new HistoricalDate(birthYear, birthMonth, birthDay, birthUncertainty);
        HistoricalDate death = new HistoricalDate(deathYear, deathMonth, deathDay, deathUncertainty);

        assertThat(AgeRange.between(birth, death)).isEqualTo(new AgeRange(expectedMin, expectedMax));
    }

    /** Rejects a death that lies before the birth in every reading of the dates. */
    @Test
    void rejectsDeathBeforeBirth() {
        HistoricalDate birth = new HistoricalDate(1718, null, null, 0);
        HistoricalDate death = new HistoricalDate(1700, null, null, 0);

        assertThatIllegalArgumentException()
                .isThrownBy(() -> AgeRange.between(birth, death))
                .withMessageContaining("cannot be before birth");
    }

    /** Rejects ranges that cannot describe a real age. */
    @ParameterizedTest
    @CsvSource({"-1, 5", "10, 9"})
    void rejectsInvalidRange(int min, int max) {
        assertThatIllegalArgumentException().isThrownBy(() -> new AgeRange(min, max));
    }
}
