package se.pbt.curiositas.person;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

/**
 * Another name a person was known by, such as "Blackbeard" for Edward Teach.
 *
 * @param name the alternative name
 * @param type what kind of name it is
 */
@Embeddable
public record AlternativeName(
        @Column(name = "name") String name,
        @Column(name = "type") @Enumerated(EnumType.STRING) NameType type) {
}
