package se.pbt.curiositas.person;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import se.pbt.curiositas.api.model.AgeRangeDto;
import se.pbt.curiositas.api.model.AlternativeNameDto;
import se.pbt.curiositas.api.model.CountryDto;
import se.pbt.curiositas.api.model.GenderDto;
import se.pbt.curiositas.api.model.HistoricalDateDto;
import se.pbt.curiositas.api.model.HistoricalDateInputDto;
import se.pbt.curiositas.api.model.NameTypeDto;
import se.pbt.curiositas.api.model.PersonDto;
import se.pbt.curiositas.api.model.PersonInputDto;
import se.pbt.curiositas.api.model.PersonPageDto;
import se.pbt.curiositas.date.HistoricalDate;

import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.InstanceOfAssertFactories.LIST;
import static se.pbt.curiositas.person.StoredPersons.STORED_AT;
import static se.pbt.curiositas.person.StoredPersons.stored;

/**
 * Verifies the conversion to API models, since that is where derived values are calculated and
 * where unknown values must stay unknown instead of becoming misleading defaults.
 */
class PersonMapperTest {

    private static final UUID ID = UUID.fromString("7f1c8a52-0d1e-4f4b-9a7e-3c2b1d0e9f8a");

    /** Copies every stored field and adds the derived display texts, age and country name. */
    @Test
    void mapsStoredFieldsAndDerivedValues() {
        Person teach = new Person("Edward Teach", Gender.MALE);
        teach.setAlsoKnownAs(List.of(new AlternativeName("Blackbeard", NameType.NICKNAME)));
        teach.setBirth(new HistoricalDate(1680, null, null, 5));
        teach.setDeath(new HistoricalDate(1718, 11, 22, 0));
        teach.setBirthPlace("Bristol");
        teach.setDeathPlace("Ocracoke Island");
        teach.setBirthCountry("GB");

        PersonDto dto = PersonMapper.toDto(stored(teach, ID, 3));

        assertThat(dto.getId()).isEqualTo(ID);
        assertThat(dto.getName()).isEqualTo("Edward Teach");
        assertThat(dto.getAlsoKnownAs()).containsExactly(new AlternativeNameDto("Blackbeard", NameTypeDto.NICKNAME));
        assertThat(dto.getBirth()).isEqualTo(new HistoricalDateDto(1680, 5, "1680 ± 5"));
        assertThat(dto.getDeath()).isEqualTo(new HistoricalDateDto(1718, 0, "22 Nov 1718").month(11).day(22));
        assertThat(dto.getAgeAtDeath()).isEqualTo(new AgeRangeDto(32, 43));
        assertThat(dto.getBirthPlace()).isEqualTo("Bristol");
        assertThat(dto.getDeathPlace()).isEqualTo("Ocracoke Island");
        assertThat(dto.getBirthCountry()).isEqualTo(new CountryDto("GB", "United Kingdom"));
        assertThat(dto.getGender()).isEqualTo(GenderDto.MALE);
        assertThat(dto.getCreatedAt()).isEqualTo(STORED_AT.atOffset(ZoneOffset.UTC));
        assertThat(dto.getUpdatedAt()).isEqualTo(STORED_AT.atOffset(ZoneOffset.UTC));
    }

    /** Leaves unknown values out, including the age, which cannot be calculated without both dates. */
    @Test
    void leavesUnknownValuesOut() {
        Person bonny = new Person("Anne Bonny", Gender.FEMALE);
        bonny.setBirth(new HistoricalDate(1697, null, null, 3));

        PersonDto dto = PersonMapper.toDto(stored(bonny, ID, 0));

        assertThat(dto.getWikidataId()).isNull();
        assertThat(dto.getDeath()).isNull();
        assertThat(dto.getFloruit()).isNull();
        assertThat(dto.getAgeAtDeath()).isNull();
        assertThat(dto.getBirthCountry()).isNull();
        assertThat(dto.getAlsoKnownAs()).isEmpty();
    }

    /** Creates a person with every field from a request, treating a missing uncertainty as 0. */
    @Test
    void createsPersonFromRequest() {
        PersonInputDto input = new PersonInputDto("Edward Teach", GenderDto.MALE)
                .wikidataId("Q213518")
                .alsoKnownAs(List.of(new AlternativeNameDto("Blackbeard", NameTypeDto.NICKNAME)))
                .birth(new HistoricalDateInputDto(1680).uncertaintyYears(5))
                .death(new HistoricalDateInputDto(1718).month(11).day(22).uncertaintyYears(null))
                .birthPlace("Bristol")
                .birthCountry("GB");

        Person person = PersonMapper.toNewPerson(input);

        assertThat(person.getName()).isEqualTo("Edward Teach");
        assertThat(person.getGender()).isEqualTo(Gender.MALE);
        assertThat(person.getWikidataId()).isEqualTo("Q213518");
        assertThat(person.getAlsoKnownAs()).containsExactly(new AlternativeName("Blackbeard", NameType.NICKNAME));
        assertThat(person.getBirth()).isEqualTo(new HistoricalDate(1680, null, null, 5));
        assertThat(person.getDeath()).isEqualTo(new HistoricalDate(1718, 11, 22, 0));
        assertThat(person.getFloruit()).isNull();
        assertThat(person.getBirthPlace()).isEqualTo("Bristol");
        assertThat(person.getBirthCountry()).isEqualTo("GB");
    }

    /** Reports every impossible value at once, so the client does not have to fix them one by one. */
    @Test
    void reportsEveryImpossibleValue() {
        PersonInputDto input = new PersonInputDto("Edward Teach", GenderDto.MALE)
                .birth(new HistoricalDateInputDto(1718).month(2).day(29))
                .floruit(new HistoricalDateInputDto(1716).day(3))
                .birthCountry("SU");

        assertThatExceptionOfType(InvalidPersonException.class)
                .isThrownBy(() -> PersonMapper.toNewPerson(input))
                .extracting(InvalidPersonException::getViolations, LIST)
                .extracting("field")
                .containsExactly("birth", "floruit", "birthCountry");
    }

    /** Rejects a death that can only have happened before the birth, and leaves the person untouched. */
    @Test
    void rejectsDeathBeforeBirthWithoutChangingPerson() {
        Person existing = new Person("Edward Teach", Gender.MALE);
        PersonInputDto input = new PersonInputDto("Changed Name", GenderDto.MALE)
                .birth(new HistoricalDateInputDto(1718))
                .death(new HistoricalDateInputDto(1680));

        assertThatExceptionOfType(InvalidPersonException.class)
                .isThrownBy(() -> PersonMapper.copyInput(input, existing))
                .extracting(InvalidPersonException::getViolations, LIST)
                .extracting("field")
                .containsExactly("death");
        assertThat(existing.getName()).isEqualTo("Edward Teach");
        assertThat(existing.getBirth()).isNull();
    }

    /** Keeps the paging information so clients can navigate between pages. */
    @Test
    void mapsPagingInformation() {
        Person teach = stored(new Person("Edward Teach", Gender.MALE), ID, 0);
        var page = new PageImpl<>(List.of(teach), PageRequest.of(2, 1), 5);

        PersonPageDto dto = PersonMapper.toDto(page);

        assertThat(dto.getContent()).extracting(PersonDto::getName).containsExactly("Edward Teach");
        assertThat(dto.getPage()).isEqualTo(2);
        assertThat(dto.getSize()).isEqualTo(1);
        assertThat(dto.getTotalElements()).isEqualTo(5);
        assertThat(dto.getTotalPages()).isEqualTo(5);
    }
}
