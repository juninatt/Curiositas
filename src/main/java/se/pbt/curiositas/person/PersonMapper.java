package se.pbt.curiositas.person;

import org.springframework.data.domain.Page;
import se.pbt.curiositas.api.model.AgeRangeDto;
import se.pbt.curiositas.api.model.AlternativeNameDto;
import se.pbt.curiositas.api.model.CountryDto;
import se.pbt.curiositas.api.model.GenderDto;
import se.pbt.curiositas.api.model.HistoricalDateDto;
import se.pbt.curiositas.api.model.NameTypeDto;
import se.pbt.curiositas.api.model.PersonDto;
import se.pbt.curiositas.api.model.PersonPageDto;
import se.pbt.curiositas.date.AgeRange;
import se.pbt.curiositas.date.HistoricalDate;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;

/**
 * Converts persons to the API's response models. This is where derived values such as display
 * texts, age at death and country names are calculated, so they are never stored.
 */
final class PersonMapper {

    /** Not instantiable; all methods are static. */
    private PersonMapper() {
    }

    /**
     * Converts a person, including all derived values.
     *
     * @param person the stored person
     * @return the person as returned by the API
     */
    static PersonDto toDto(Person person) {
        return new PersonDto(
                person.getId(),
                person.getName(),
                person.getAlsoKnownAs().stream().map(PersonMapper::toDto).toList(),
                GenderDto.valueOf(person.getGender().name()),
                toOffsetDateTime(person.getCreatedAt()),
                toOffsetDateTime(person.getUpdatedAt()))
                .wikidataId(person.getWikidataId())
                .birth(toDto(person.getBirth()))
                .death(toDto(person.getDeath()))
                .floruit(toDto(person.getFloruit()))
                .ageAtDeath(ageAtDeath(person))
                .birthPlace(person.getBirthPlace())
                .deathPlace(person.getDeathPlace())
                .birthCountry(toCountry(person.getBirthCountry()));
    }

    /**
     * Converts one page of persons, keeping the paging information.
     *
     * @param page the stored persons on one page
     * @return the page as returned by the API
     */
    static PersonPageDto toDto(Page<Person> page) {
        return new PersonPageDto(
                page.getContent().stream().map(PersonMapper::toDto).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    /** Converts an alternative name. */
    private static AlternativeNameDto toDto(AlternativeName name) {
        return new AlternativeNameDto(name.name(), NameTypeDto.valueOf(name.type().name()));
    }

    /** Converts a date and adds its display text, or returns {@code null} if the date is unknown. */
    private static HistoricalDateDto toDto(HistoricalDate date) {
        if (date == null) {
            return null;
        }
        return new HistoricalDateDto(date.year(), date.uncertaintyYears(), date.displayText())
                .month(date.month())
                .day(date.day());
    }

    /** Calculates the age at death, or returns {@code null} unless both birth and death are known. */
    private static AgeRangeDto ageAtDeath(Person person) {
        if (person.getBirth() == null || person.getDeath() == null) {
            return null;
        }
        AgeRange age = AgeRange.between(person.getBirth(), person.getDeath());
        return new AgeRangeDto(age.min(), age.max());
    }

    /** Adds the English country name to an ISO code, or returns {@code null} if the country is unknown. */
    private static CountryDto toCountry(String code) {
        if (code == null) {
            return null;
        }
        return new CountryDto(code, Locale.of("", code).getDisplayCountry(Locale.ENGLISH));
    }

    /** Converts a point in time to the API's format, always in UTC. */
    private static OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
