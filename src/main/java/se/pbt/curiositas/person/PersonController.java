package se.pbt.curiositas.person;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import se.pbt.curiositas.api.PersonsApi;
import se.pbt.curiositas.api.model.PersonDto;
import se.pbt.curiositas.api.model.PersonInputDto;
import se.pbt.curiositas.api.model.PersonPageDto;

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

    /** Not implemented yet; answers 501 so clients get an honest response until it is. */
    @Override
    public ResponseEntity<PersonDto> createPerson(PersonInputDto personInputDto) {
        throw notImplemented("Creating persons");
    }

    /** Not implemented yet; answers 501 so clients get an honest response until it is. */
    @Override
    public ResponseEntity<PersonDto> replacePerson(UUID id, PersonInputDto personInputDto, String ifMatch) {
        throw notImplemented("Replacing persons");
    }

    /** Not implemented yet; answers 501 so clients get an honest response until it is. */
    @Override
    public ResponseEntity<Void> deletePerson(UUID id) {
        throw notImplemented("Deleting persons");
    }

    /** Creates the error for an operation that is described in the contract but not built yet. */
    private static ResponseStatusException notImplemented(String operation) {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, operation + " is not implemented yet");
    }
}
