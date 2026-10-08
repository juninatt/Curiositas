package se.pbt.curiositas.person;

import java.util.UUID;

/**
 * Thrown when a person would duplicate one already in the database, so the API can answer 409
 * and point to the existing person instead of storing the same person twice.
 */
public class DuplicatePersonException extends RuntimeException {

    /**
     * Creates the exception with a message naming the existing person and why it matches.
     *
     * @param existingId the id of the person already in the database
     * @param reason     what the two persons have in common
     */
    public DuplicatePersonException(UUID existingId, String reason) {
        super("The person " + existingId + " already has " + reason);
    }
}
