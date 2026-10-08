package se.pbt.curiositas.person;

import java.util.UUID;

/**
 * Thrown when no person exists with a requested id, so the API can answer 404 Not Found.
 */
public class PersonNotFoundException extends RuntimeException {

    /**
     * Creates the exception with a message naming the missing id.
     *
     * @param id the id that was asked for
     */
    public PersonNotFoundException(UUID id) {
        super("No person with id " + id);
    }
}
