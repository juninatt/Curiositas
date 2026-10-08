package se.pbt.curiositas.person;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies name normalisation, since it decides which persons count as duplicates.
 */
class PersonNamesTest {

    /** Ignores case, accents and extra spaces, which do not make a different person. */
    @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
    @CsvSource(delimiter = '|', value = {
            "Edward Teach            | edward teach",
            "EDWARD TEACH            | edward teach",
            "'  Edward   Teach  '    | edward teach",
            "Édouard Teach           | edouard teach",
            "Gaius Iūlius Caesar     | gaius iulius caesar",
            "Hernán Cortés           | hernan cortes",
    })
    void ignoresCaseAccentsAndSpacing(String name, String expected) {
        assertThat(PersonNames.normalize(name)).isEqualTo(expected);
    }
}
