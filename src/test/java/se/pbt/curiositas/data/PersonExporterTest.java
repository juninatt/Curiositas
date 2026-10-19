package se.pbt.curiositas.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.pbt.curiositas.date.HistoricalDate;
import se.pbt.curiositas.person.Gender;
import se.pbt.curiositas.person.Person;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that an export makes the data folder match the database exactly, since the files are
 * the source of truth and anything missing or left over there would be wrong data.
 */
class PersonExporterTest {

    private final PersonExporter exporter = new PersonExporter();

    @TempDir
    private Path directory;

    /** Writes one file per person, named after the person. */
    @Test
    void writesOneFilePerPerson() throws IOException {
        PersonExporter.Result result = exporter.export(List.of(person("Anne Bonny", 1697), person("Edward Teach", 1680)),
                directory.resolve("persons"));

        assertThat(fileNames(directory.resolve("persons"))).containsExactly("anne-bonny-1697.json", "edward-teach-1680.json");
        assertThat(Files.readString(directory.resolve("persons/anne-bonny-1697.json"))).contains("\"name\": \"Anne Bonny\"");
        assertThat(result).isEqualTo(new PersonExporter.Result(2, 0, 0));
    }

    /** Leaves files untouched when their content is already right, so nothing changes needlessly. */
    @Test
    void leavesUnchangedFilesAlone() {
        List<Person> persons = List.of(person("Anne Bonny", 1697));
        exporter.export(persons, directory);

        assertThat(exporter.export(persons, directory)).isEqualTo(new PersonExporter.Result(0, 1, 0));
    }

    /** Removes the files of persons that no longer exist, but never touches other files. */
    @Test
    void removesFilesOfDeletedPersonsOnly() throws IOException {
        exporter.export(List.of(person("Anne Bonny", 1697), person("Edward Teach", 1680)), directory);
        Files.writeString(directory.resolve("README.txt"), "notes");

        PersonExporter.Result result = exporter.export(List.of(person("Edward Teach", 1680)), directory);

        assertThat(fileNames(directory)).containsExactly("README.txt", "edward-teach-1680.json");
        assertThat(result).isEqualTo(new PersonExporter.Result(0, 1, 1));
    }

    /**
     * Numbers persons whose file names would clash by their content, so the same persons always
     * get the same files whatever order the database returns them in.
     */
    @Test
    void numbersClashingNamesByContent() throws IOException {
        Person inBristol = person("Edward Teach", 1680);
        inBristol.setBirthPlace("Bristol");
        Person inLondon = person("Edward Teach", 1680);
        inLondon.setBirthPlace("London");

        exporter.export(List.of(inLondon, inBristol), directory.resolve("first"));
        exporter.export(List.of(inBristol, inLondon), directory.resolve("second"));

        assertThat(fileNames(directory.resolve("first"))).containsExactly("edward-teach-1680-2.json", "edward-teach-1680.json");
        assertThat(Files.readString(directory.resolve("first/edward-teach-1680.json"))).contains("Bristol");
        assertThat(Files.readString(directory.resolve("second/edward-teach-1680.json"))).contains("Bristol");
    }

    /** Creates a person born in the given year. */
    private static Person person(String name, int birthYear) {
        Person person = new Person(name, Gender.UNKNOWN);
        person.setBirth(new HistoricalDate(birthYear, null, null, 0));
        return person;
    }

    /** Lists the file names in a folder in alphabetical order. */
    private static List<String> fileNames(Path folder) throws IOException {
        try (Stream<Path> files = Files.list(folder)) {
            return files.map(file -> file.getFileName().toString()).sorted().toList();
        }
    }
}
