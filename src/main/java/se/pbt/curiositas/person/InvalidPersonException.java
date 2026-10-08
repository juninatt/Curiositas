package se.pbt.curiositas.person;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Thrown when a person's data is readable but describes something impossible, such as a date
 * that does not exist or a death before the birth, so the API can answer 422 with every problem
 * at once instead of one at a time.
 */
public class InvalidPersonException extends RuntimeException {

    private final List<Violation> violations;

    /**
     * Creates the exception.
     *
     * @param violations every problem found, at least one
     */
    public InvalidPersonException(List<Violation> violations) {
        super(violations.stream()
                .map(violation -> violation.field() + ": " + violation.message())
                .collect(Collectors.joining("; ")));
        this.violations = List.copyOf(violations);
    }

    /** Returns every problem found. */
    public List<Violation> getViolations() {
        return violations;
    }

    /**
     * One problem with one field.
     *
     * @param field   the field in the request, for example "birth" or "birthCountry"
     * @param message what is wrong with it
     */
    public record Violation(String field, String message) {
    }
}
