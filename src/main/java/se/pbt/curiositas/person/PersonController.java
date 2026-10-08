package se.pbt.curiositas.person;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.pbt.curiositas.api.PersonsApi;
import se.pbt.curiositas.api.model.PersonDto;
import se.pbt.curiositas.api.model.PersonInputDto;
import se.pbt.curiositas.api.model.PersonPageDto;

import java.net.URI;
import java.util.UUID;

/**
 * Serves the person endpoints described in the OpenAPI contract. Paths, parameters and models
 * come from the generated {@link PersonsApi}, so this class cannot drift from the contract.
 */
@RestController
public class PersonController implements PersonsApi {

    private final PersonService service;

    /**
     * Creates the controller.
     *
     * @param service reads and changes persons
     */
    public PersonController(PersonService service) {
        this.service = service;
    }

    /** Returns one page of persons, ordered by name. */
    @Override
    public ResponseEntity<PersonPageDto> listPersons(Integer page, Integer size) {
        return ResponseEntity.ok(PersonMapper.toDto(service.list(page, size)));
    }

    /** Returns one person, with its version as ETag so a later replacement can detect conflicts. */
    @Override
    public ResponseEntity<PersonDto> getPerson(UUID id) {
        Person person = service.get(id);
        return ResponseEntity.ok()
                .eTag(String.valueOf(person.getVersion()))
                .body(PersonMapper.toDto(person));
    }

    /**
     * Creates a person and answers 201 with its URL in {@code Location} and its version as ETag,
     * so the client can read or replace it without searching for it.
     */
    @Override
    public ResponseEntity<PersonDto> createPerson(PersonInputDto personInputDto) {
        Person person = service.create(PersonMapper.toNewPerson(personInputDto));
        URI location = ServletUriComponentsBuilder.fromCurrentRequestUri()
                .path("/{id}")
                .buildAndExpand(person.getId())
                .toUri();
        return ResponseEntity.created(location)
                .eTag(String.valueOf(person.getVersion()))
                .body(PersonMapper.toDto(person));
    }

    /** Not implemented yet; answers 501 so clients get an honest response until it is. */
    @Override
    public ResponseEntity<PersonDto> replacePerson(UUID id, PersonInputDto personInputDto, String ifMatch) {
        throw notImplemented("Replacing persons");
    }

    /** Deletes a person and answers 204, since there is nothing left to return. */
    @Override
    public ResponseEntity<Void> deletePerson(UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    /** Creates the error for an operation that is described in the contract but not built yet. */
    private static ResponseStatusException notImplemented(String operation) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, operation + " is not implemented yet");
    }
}
