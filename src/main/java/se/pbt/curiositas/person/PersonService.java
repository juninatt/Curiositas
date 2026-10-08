package se.pbt.curiositas.person;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
}
