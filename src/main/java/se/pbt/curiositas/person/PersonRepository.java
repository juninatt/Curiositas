package se.pbt.curiositas.person;

import jakarta.persistence.QueryHint;
import org.hibernate.jpa.HibernateHints;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stores and finds persons in the database.
 *
 * <p>The duplicate lookups do not flush pending changes first. They run while a replacement is
 * being checked, and that unchecked replacement must not reach the database before the check.
 */
public interface PersonRepository extends JpaRepository<Person, UUID> {

    /**
     * Finds the person with the given Wikidata id.
     *
     * @param wikidataId the Wikidata id, for example "Q7259"
     * @return the person, if one has that id
     */
    @QueryHints(@QueryHint(name = HibernateHints.HINT_FLUSH_MODE, value = "COMMIT"))
    Optional<Person> findByWikidataId(String wikidataId);

    /**
     * Finds persons with exactly the same birth, death and birth country, where unknown matches
     * unknown. These are the candidates for a duplicate when no Wikidata id is known; their
     * names are compared afterwards, since names must be compared without accents.
     *
     * @param birthYear             the birth year, or {@code null} if birth is unknown
     * @param birthMonth            the birth month, or {@code null}
     * @param birthDay              the birth day, or {@code null}
     * @param birthUncertaintyYears the birth uncertainty, or {@code null} if birth is unknown
     * @param deathYear             the death year, or {@code null} if death is unknown
     * @param deathMonth            the death month, or {@code null}
     * @param deathDay              the death day, or {@code null}
     * @param deathUncertaintyYears the death uncertainty, or {@code null} if death is unknown
     * @param birthCountry          the birth country code, or {@code null}
     * @return the persons with the same life dates and birth country
     */
    @QueryHints(@QueryHint(name = HibernateHints.HINT_FLUSH_MODE, value = "COMMIT"))
    @Query("""
            select p from Person p
            where p.birth.year is not distinct from :birthYear
              and p.birth.month is not distinct from :birthMonth
              and p.birth.day is not distinct from :birthDay
              and p.birth.uncertaintyYears is not distinct from :birthUncertaintyYears
              and p.death.year is not distinct from :deathYear
              and p.death.month is not distinct from :deathMonth
              and p.death.day is not distinct from :deathDay
              and p.death.uncertaintyYears is not distinct from :deathUncertaintyYears
              and p.birthCountry is not distinct from :birthCountry
            """)
    List<Person> findWithSameLifeDatesAndBirthCountry(
            @Param("birthYear") Integer birthYear,
            @Param("birthMonth") Integer birthMonth,
            @Param("birthDay") Integer birthDay,
            @Param("birthUncertaintyYears") Integer birthUncertaintyYears,
            @Param("deathYear") Integer deathYear,
            @Param("deathMonth") Integer deathMonth,
            @Param("deathDay") Integer deathDay,
            @Param("deathUncertaintyYears") Integer deathUncertaintyYears,
            @Param("birthCountry") String birthCountry);
}
