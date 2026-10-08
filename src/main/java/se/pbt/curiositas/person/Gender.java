package se.pbt.curiositas.person;

/**
 * The gender of a person, as a fixed list so that persons can be counted and grouped by it.
 * {@code UNKNOWN} is used when the sources do not say, instead of guessing.
 */
public enum Gender {
    FEMALE,
    MALE,
    OTHER,
    UNKNOWN
}
