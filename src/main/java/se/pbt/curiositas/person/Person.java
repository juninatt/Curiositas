package se.pbt.curiositas.person;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Version;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import se.pbt.curiositas.date.HistoricalDate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A historical person. Only facts are stored; derived values such as age or display texts are
 * calculated when needed, so they can never contradict the stored data. Unknown values are
 * {@code null} rather than guessed.
 *
 * <p>This is a mutable class rather than a record because JPA needs to create empty instances
 * and track changes to them.
 */
@Entity
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Increases on every update; used to detect concurrent changes and exposed as the ETag. */
    @Version
    private long version;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    private String wikidataId;

    private String name;

    // Always needed with the person; loaded in batches so a page of persons costs one extra query.
    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 100)
    @CollectionTable(name = "person_alternative_name", joinColumns = @JoinColumn(name = "person_id"))
    @OrderColumn(name = "position")
    private List<AlternativeName> alsoKnownAs = new ArrayList<>();

    @Embedded
    @AttributeOverride(name = "year", column = @Column(name = "birth_year"))
    @AttributeOverride(name = "month", column = @Column(name = "birth_month"))
    @AttributeOverride(name = "day", column = @Column(name = "birth_day"))
    @AttributeOverride(name = "uncertaintyYears", column = @Column(name = "birth_uncertainty_years"))
    private HistoricalDate birth;

    @Embedded
    @AttributeOverride(name = "year", column = @Column(name = "death_year"))
    @AttributeOverride(name = "month", column = @Column(name = "death_month"))
    @AttributeOverride(name = "day", column = @Column(name = "death_day"))
    @AttributeOverride(name = "uncertaintyYears", column = @Column(name = "death_uncertainty_years"))
    private HistoricalDate death;

    @Embedded
    @AttributeOverride(name = "year", column = @Column(name = "floruit_year"))
    @AttributeOverride(name = "month", column = @Column(name = "floruit_month"))
    @AttributeOverride(name = "day", column = @Column(name = "floruit_day"))
    @AttributeOverride(name = "uncertaintyYears", column = @Column(name = "floruit_uncertainty_years"))
    private HistoricalDate floruit;

    private String birthPlace;

    private String deathPlace;

    private String birthCountry;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    /** Used by JPA when loading a person from the database. */
    protected Person() {
    }

    /**
     * Creates a person with the fields every person must have.
     *
     * @param name   the name the person is most commonly known by
     * @param gender the person's gender
     */
    public Person(String name, Gender gender) {
        this.name = name;
        this.gender = gender;
    }

    /** Returns the id, assigned by the database layer when the person is first saved. */
    public UUID getId() {
        return id;
    }

    /** Returns the version, which increases on every update. */
    public long getVersion() {
        return version;
    }

    /** Returns when the person was first saved. */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /** Returns when the person was last changed. */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /** Returns the person's item id in Wikidata, or {@code null} if none is known. */
    public String getWikidataId() {
        return wikidataId;
    }

    /** Sets the person's item id in Wikidata, for example "Q7259". */
    public void setWikidataId(String wikidataId) {
        this.wikidataId = wikidataId;
    }

    /** Returns the name the person is most commonly known by. */
    public String getName() {
        return name;
    }

    /** Sets the name the person is most commonly known by. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns the person's other names, in the order they were given. */
    public List<AlternativeName> getAlsoKnownAs() {
        return List.copyOf(alsoKnownAs);
    }

    /** Replaces the person's other names, keeping the list instance JPA tracks. */
    public void setAlsoKnownAs(List<AlternativeName> alsoKnownAs) {
        this.alsoKnownAs.clear();
        this.alsoKnownAs.addAll(alsoKnownAs);
    }

    /** Returns when the person was born, or {@code null} if unknown. */
    public HistoricalDate getBirth() {
        return birth;
    }

    /** Sets when the person was born. */
    public void setBirth(HistoricalDate birth) {
        this.birth = birth;
    }

    /** Returns when the person died, or {@code null} if unknown or still alive. */
    public HistoricalDate getDeath() {
        return death;
    }

    /** Sets when the person died. */
    public void setDeath(HistoricalDate death) {
        this.death = death;
    }

    /** Returns when the person was active, or {@code null} if unknown. */
    public HistoricalDate getFloruit() {
        return floruit;
    }

    /** Sets when the person was active, used when birth and death are unknown. */
    public void setFloruit(HistoricalDate floruit) {
        this.floruit = floruit;
    }

    /** Returns where the person was born, as free text, or {@code null} if unknown. */
    public String getBirthPlace() {
        return birthPlace;
    }

    /** Sets where the person was born, as free text until places are modelled. */
    public void setBirthPlace(String birthPlace) {
        this.birthPlace = birthPlace;
    }

    /** Returns where the person died, as free text, or {@code null} if unknown. */
    public String getDeathPlace() {
        return deathPlace;
    }

    /** Sets where the person died, as free text until places are modelled. */
    public void setDeathPlace(String deathPlace) {
        this.deathPlace = deathPlace;
    }

    /** Returns the modern country of birth as an ISO 3166-1 alpha-2 code, or {@code null}. */
    public String getBirthCountry() {
        return birthCountry;
    }

    /** Sets the modern country of birth as an ISO 3166-1 alpha-2 code, for grouping and maps. */
    public void setBirthCountry(String birthCountry) {
        this.birthCountry = birthCountry;
    }

    /** Returns the person's gender. */
    public Gender getGender() {
        return gender;
    }

    /** Sets the person's gender. */
    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
