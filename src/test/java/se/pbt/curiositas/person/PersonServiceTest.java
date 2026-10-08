package se.pbt.curiositas.person;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import se.pbt.curiositas.TestcontainersConfiguration;
import se.pbt.curiositas.date.HistoricalDate;
import se.pbt.curiositas.person.InvalidPersonException.Violation;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies replacing persons against the real database, since optimistic locking depends on the
 * version the database actually stores.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PersonService.class})
class PersonServiceTest {

    @Autowired
    private PersonService service;

    @Autowired
    private TestEntityManager entityManager;

    /** Applies the changes and moves the person to the next version. */
    @Test
    void replacesPersonAtExpectedVersion() {
        Person created = service.create(new Person("Edward Teach", Gender.MALE));

        Person replaced = service.replace(created.getId(), 0,
                person -> person.setBirth(new HistoricalDate(1680, null, null, 5)));

        assertThat(replaced.getVersion()).isEqualTo(1);
        assertThat(reload(created.getId()).getBirth()).isEqualTo(new HistoricalDate(1680, null, null, 5));
    }

    /** Refuses changes based on an outdated version and leaves the stored person as it was. */
    @Test
    void refusesReplaceAtOutdatedVersion() {
        Person created = service.create(new Person("Edward Teach", Gender.MALE));
        service.replace(created.getId(), 0, person -> person.setBirthPlace("Bristol"));

        assertThatExceptionOfType(PersonChangedException.class)
                .isThrownBy(() -> service.replace(created.getId(), 0, person -> person.setName("Edward Thatch")))
                .withMessageContaining("version 0")
                .withMessageContaining("version 1");
        assertThat(reload(created.getId()).getName()).isEqualTo("Edward Teach");
    }

    /** Leaves the person unchanged when the new data is rejected. */
    @Test
    void keepsPersonWhenChangesAreRejected() {
        Person created = service.create(new Person("Edward Teach", Gender.MALE));

        assertThatExceptionOfType(InvalidPersonException.class)
                .isThrownBy(() -> service.replace(created.getId(), 0, person -> {
                    throw new InvalidPersonException(List.of(new Violation("death", "is before birth")));
                }));
        assertThat(reload(created.getId()).getVersion()).isZero();
    }

    /** Answers that the person does not exist rather than creating it. */
    @Test
    void refusesReplaceOfUnknownPerson() {
        assertThatExceptionOfType(PersonNotFoundException.class)
                .isThrownBy(() -> service.replace(UUID.randomUUID(), 0, person -> person.setName("Nobody")));
    }

    /** Reads the person from the database rather than from the persistence context. */
    private Person reload(UUID id) {
        entityManager.clear();
        return service.get(id);
    }
}
