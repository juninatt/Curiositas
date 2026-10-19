package se.pbt.curiositas.data;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import se.pbt.curiositas.person.PersonService;

import java.io.PrintStream;

/**
 * Runs one data command, such as {@code export}, and then stops the application. Started through
 * {@code data.cmd} or {@code data.sh}, which set {@code curiositas.data.command}; the application
 * then runs without a web server, so commands work even when the server could not start.
 */
@Component
@ConditionalOnProperty("curiositas.data.command")
public class DataCommandRunner implements ApplicationRunner {

    private final DataProperties properties;
    private final PersonService persons;
    private final PersonExporter exporter;
    private final ConfigurableApplicationContext context;
    private final String command;
    private final PrintStream out;

    /**
     * Creates the runner.
     *
     * @param properties where the data files live
     * @param persons    reads the persons to export
     * @param exporter   writes the files
     * @param context    the application, stopped when the command is done
     * @param command    the command to run, from {@code curiositas.data.command}
     */
    public DataCommandRunner(DataProperties properties, PersonService persons, PersonExporter exporter,
                             ConfigurableApplicationContext context,
                             @Value("${curiositas.data.command}") String command) {
        this.properties = properties;
        this.persons = persons;
        this.exporter = exporter;
        this.context = context;
        this.command = command.strip();
        this.out = System.out;
    }

    /**
     * Runs the command, prints its outcome in plain words and exits with 0 on success and 1 on
     * failure, so scripts can tell the difference.
     */
    @Override
    public void run(ApplicationArguments arguments) {
        int exitCode;
        try {
            exitCode = switch (command) {
                case "export" -> export();
                default -> {
                    out.println("Unknown data command '" + command + "'. Available commands: export");
                    yield 1;
                }
            };
        } catch (RuntimeException failure) {
            out.println("The data command '" + command + "' failed: " + failure.getMessage());
            exitCode = 1;
        }
        int code = exitCode;
        System.exit(SpringApplication.exit(context, () -> code));
    }

    /** Writes every person in the database to the data folder. */
    private int export() {
        PersonExporter.Result result = exporter.export(persons.listAll(), properties.personsDirectory());
        out.printf("Exported persons to %s: %d written, %d unchanged, %d removed.%n",
                properties.personsDirectory(), result.written(), result.unchanged(), result.removed());
        return 0;
    }
}
