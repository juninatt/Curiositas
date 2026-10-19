package se.pbt.curiositas.person;

/**
 * Published whenever persons are created, replaced or deleted, so other parts of the application,
 * such as the data files, can follow the change without the person code knowing about them.
 */
public record PersonsChangedEvent() {
}
