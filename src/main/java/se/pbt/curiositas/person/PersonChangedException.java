package se.pbt.curiositas.person;

/**
 * Thrown when a client tries to replace a person based on an outdated version, so the API can
 * answer 412 instead of silently overwriting someone else's changes.
 */
public class PersonChangedException extends RuntimeException {

    /**
     * Creates the exception with a message naming both versions, so the client knows to read again.
     *
     * @param expectedVersion the version the client based its change on
     * @param currentVersion  the version that is stored now
     */
    public PersonChangedException(long expectedVersion, long currentVersion) {
        super("The person has been changed since version " + expectedVersion
                + " was read; it is now at version " + currentVersion);
    }
}
