package se.pbt.curiositas.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import se.pbt.curiositas.person.PersonRepository;
import se.pbt.curiositas.person.PersonsChangedEvent;

/**
 * Keeps the data files in step with every change made through the API, so the files, which are
 * the source of truth, are always ready to be committed.
 *
 * <p>The files are written just before the database change is committed. If writing fails, the
 * exception rolls the database change back, so the files and the database never disagree.
 * Switched on with {@code curiositas.data.sync=true}, which only the local {@code dev} profile
 * does; tests, data commands and deployments never write to the data folder.
 */
@Component
@ConditionalOnBooleanProperty("curiositas.data.sync")
public class PersonFileSync {

    private static final Logger log = LoggerFactory.getLogger(PersonFileSync.class);

    private final PersonRepository repository;
    private final PersonExporter exporter;
    private final DataProperties properties;

    /**
     * Creates the sync.
     *
     * @param repository reads all persons, including the change about to be committed
     * @param exporter   writes the files
     * @param properties where the files live
     */
    public PersonFileSync(PersonRepository repository, PersonExporter exporter, DataProperties properties) {
        this.repository = repository;
        this.exporter = exporter;
        this.properties = properties;
    }

    /**
     * Rewrites the files that the change affects. A full export is used rather than writing one
     * file, since a file name can depend on other persons (two John Smiths) and a renamed person
     * moves to a new file; files that are already right are left untouched.
     *
     * @param event the change, announced inside the transaction that made it
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void writeFiles(PersonsChangedEvent event) {
        PersonExporter.Result result = exporter.export(repository.findAll(), properties.personsDirectory());
        if (result.written() > 0 || result.removed() > 0) {
            log.info("Updated {}: {} written, {} removed", properties.personsDirectory(), result.written(), result.removed());
        }
    }
}
