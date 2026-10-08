package se.pbt.curiositas.person;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Stores and finds persons in the database.
 */
public interface PersonRepository extends JpaRepository<Person, UUID> {
}
