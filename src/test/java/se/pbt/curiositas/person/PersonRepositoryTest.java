package se.pbt.curiositas.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import se.pbt.curiositas.TestcontainersConfiguration;
import se.pbt.curiositas.date.HistoricalDate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that persons survive a round trip through the real database schema, since a mismatch
 * between the Flyway migration and the entity would lose or corrupt historical data.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class PersonRepositoryTest {

    @Autowired
    private PersonRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    /**
     * Stores every field, including a year before Christ and dates with unknown month and day,
     * and reads back exactly the same values.
     */
    @Test
    void storesAndReadsBackAllFields() {
        Person caesar = new Person("Julius Caesar", Gender.MALE);
        caesar.setWikidataId("Q1048");
        caesar.setAlsoKnownAs(List.of(
                new AlternativeName("Gaius Julius Caesar", NameType.BIRTH_NAME),
                new AlternativeName("Gaius Iulius Caesar", NameType.SPELLING_VARIANT)));
        caesar.setBirth(new HistoricalDate(-99, 7, 12, 0));
        caesar.setDeath(new HistoricalDate(-43, 3, 15, 0));
        caesar.setFloruit(new HistoricalDate(-59, null, null, 15));
        caesar.setBirthPlace("Rome");
        caesar.setDeathPlace("Rome");
        caesar.setBirthCountry("IT");

        Person found = saveAndReload(caesar);

        assertThat(found.getId()).isNotNull();
        assertThat(found.getWikidataId()).isEqualTo("Q1048");
        assertThat(found.getName()).isEqualTo("Julius Caesar");
        assertThat(found.getAlsoKnownAs()).containsExactly(
                new AlternativeName("Gaius Julius Caesar", NameType.BIRTH_NAME),
                new AlternativeName("Gaius Iulius Caesar", NameType.SPELLING_VARIANT));
        assertThat(found.getBirth()).isEqualTo(new HistoricalDate(-99, 7, 12, 0));
        assertThat(found.getDeath()).isEqualTo(new HistoricalDate(-43, 3, 15, 0));
        assertThat(found.getFloruit()).isEqualTo(new HistoricalDate(-59, null, null, 15));
        assertThat(found.getBirthPlace()).isEqualTo("Rome");
        assertThat(found.getDeathPlace()).isEqualTo("Rome");
        assertThat(found.getBirthCountry()).isEqualTo("IT");
        assertThat(found.getGender()).isEqualTo(Gender.MALE);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    /** Keeps unknown values unknown, instead of turning them into empty or default values. */
    @Test
    void keepsUnknownValuesUnknown() {
        Person found = saveAndReload(new Person("Anne Bonny", Gender.FEMALE));

        assertThat(found.getWikidataId()).isNull();
        assertThat(found.getAlsoKnownAs()).isEmpty();
        assertThat(found.getBirth()).isNull();
        assertThat(found.getDeath()).isNull();
        assertThat(found.getFloruit()).isNull();
        assertThat(found.getBirthCountry()).isNull();
    }

    /** Increases the version on every update, which later detects concurrent changes. */
    @Test
    void increasesVersionOnUpdate() {
        Person person = repository.saveAndFlush(new Person("Edward Teach", Gender.MALE));
        long versionAfterCreate = person.getVersion();

        person.setBirth(new HistoricalDate(1680, null, null, 5));
        Person updated = repository.saveAndFlush(person);

        assertThat(updated.getVersion()).isGreaterThan(versionAfterCreate);
    }

    /**
     * Saves the person and reads it back from the database rather than from the persistence
     * context, so the test checks what was actually stored.
     */
    private Person saveAndReload(Person person) {
        Person saved = repository.saveAndFlush(person);
        entityManager.clear();
        return repository.findById(saved.getId()).orElseThrow();
    }
}
