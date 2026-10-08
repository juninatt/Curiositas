package se.pbt.curiositas.person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Reads and changes persons. Keeps transactions and business rules out of the web layer.
 */
@Service
@Transactional(readOnly = true)
public class PersonService {

    /** Orders by name, with the id as tie-breaker so pages stay stable when names are equal. */
    private static final Sort BY_NAME = Sort.by("name").and(Sort.by("id"));

    private final PersonRepository repository;

    /**
     * Creates the service.
     *
     * @param repository where persons are stored
     */
    public PersonService(PersonRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns one page of persons, ordered by name.
     *
     * @param page the page number, starting at 0
     * @param size the number of persons per page
     * @return the persons on that page
     */
    public Page<Person> list(int page, int size) {
        return repository.findAll(PageRequest.of(page, size, BY_NAME));
    }

    /**
     * Returns the person with the given id.
     *
     * @param id the id of the person
     * @return the person
     * @throws PersonNotFoundException if no person has that id
     */
    public Person get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
    }

    /**
     * Saves a new person. The person is written immediately, so the returned person has its id,
     * version and timestamps.
     *
     * @param person the new person, already validated
     * @return the saved person
     */
    @Transactional
    public Person create(Person person) {
        return repository.saveAndFlush(person);
    }

    /**
     * Replaces a person's data, but only if nobody has changed the person since the client read
     * it. The version is checked here, and again by the database when writing, which also catches
     * a change made between those two moments.
     *
     * @param id              the id of the person
     * @param expectedVersion the version the client read before making its change
     * @param changes         applies the new data to the person; may reject it
     * @return the updated person with its new version
     * @throws PersonNotFoundException if no person has that id
     * @throws PersonChangedException  if the person is no longer at the expected version
     */
    @Transactional
    public Person replace(UUID id, long expectedVersion, Consumer<Person> changes) {
        Person person = get(id);
        if (person.getVersion() != expectedVersion) {
            throw new PersonChangedException(expectedVersion, person.getVersion());
        }
        changes.accept(person);
        return repository.saveAndFlush(person);
    }

    /**
     * Deletes a person permanently.
     *
     * @param id the id of the person
     * @throws PersonNotFoundException if no person has that id
     */
    @Transactional
    public void delete(UUID id) {
        if (!repository.existsById(id)) {
            throw new PersonNotFoundException(id);
        }
        repository.deleteById(id);
    }
}
