package se.pbt.curiositas.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import se.pbt.curiositas.api.model.AlternativeNameDto;
import se.pbt.curiositas.api.model.GenderDto;
import se.pbt.curiositas.api.model.HistoricalDateInputDto;
import se.pbt.curiositas.api.model.NameTypeDto;
import se.pbt.curiositas.api.model.PersonInputDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the exact format of the data files, since every byte that changes without a real
 * change in the data would show up as noise in git.
 */
class PersonFileFormatTest {

    /**
     * Writes fields in a fixed order with two-space indentation and LF line endings, ends with a
     * newline, and leaves out an uncertainty of 0.
     */
    @Test
    void writesFixedReadableFormat() {
        PersonInputDto teach = new PersonInputDto("Edward Teach", GenderDto.MALE)
                .deathPlace("Ocracoke Island")
                .death(new HistoricalDateInputDto(1718).day(22).month(11).uncertaintyYears(null))
                .birth(new HistoricalDateInputDto(1680).uncertaintyYears(5))
                .alsoKnownAs(List.of(new AlternativeNameDto("Blackbeard", NameTypeDto.NICKNAME)))
                .wikidataId("Q213518");

        assertThat(PersonFileFormat.toJson(teach)).isEqualTo("""
                {
                  "name": "Edward Teach",
                  "wikidataId": "Q213518",
                  "alsoKnownAs": [
                    {
                      "name": "Blackbeard",
                      "type": "NICKNAME"
                    }
                  ],
                  "birth": {
                    "year": 1680,
                    "uncertaintyYears": 5
                  },
                  "death": {
                    "year": 1718,
                    "month": 11,
                    "day": 22
                  },
                  "deathPlace": "Ocracoke Island",
                  "gender": "MALE"
                }
                """);
    }

    /** Leaves out everything that is unknown, so a file shows only what the sources say. */
    @Test
    void leavesUnknownValuesOut() {
        assertThat(PersonFileFormat.toJson(new PersonInputDto("Anne Bonny", GenderDto.FEMALE).alsoKnownAs(null)))
                .isEqualTo("""
                        {
                          "name": "Anne Bonny",
                          "gender": "FEMALE"
                        }
                        """);
    }

    /**
     * Names files after the person, adding the Wikidata id or else the birth year, so that
     * persons with the same name get different files.
     */
    @ParameterizedTest(name = "{0} -> {3}")
    @CsvSource(nullValues = "null", value = {
            "Edward Teach,    Q213518, 1680, edward-teach-q213518",
            "Anne Bonny,      null,    1697, anne-bonny-1697",
            "Julius Caesar,   null,    -99,  julius-caesar-100bc",
            "Romulus,         null,    null, romulus",
            "Hernán Cortés,   null,    1485, hernan-cortes-1485",
            "'  Sun   Tzu  ', null,    null, sun-tzu",
            "孫武,             null,    null, person",
    })
    void namesFilesAfterThePerson(String name, String wikidataId, Integer birthYear, String expected) {
        PersonInputDto person = new PersonInputDto(name, GenderDto.MALE)
                .wikidataId(wikidataId)
                .birth(birthYear == null ? null : new HistoricalDateInputDto(birthYear));

        assertThat(PersonFileFormat.baseName(person)).isEqualTo(expected);
    }
}
