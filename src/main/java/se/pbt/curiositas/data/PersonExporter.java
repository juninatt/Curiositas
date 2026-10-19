package se.pbt.curiositas.data;

import org.springframework.stereotype.Component;
import se.pbt.curiositas.api.model.PersonInputDto;
import se.pbt.curiositas.person.Person;
import se.pbt.curiositas.person.PersonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes persons to the data folder, one file per person, so the folder matches the given
 * persons exactly.
 */
@Component
public class PersonExporter {

    /**
     * Writes one file per person and removes the files of persons that are no longer given. Files
     * whose content is already right are left untouched.
     *
     * <p>When two persons would get the same file name, they are numbered in the order of their
     * content, never of their database ids. Ids change when the database is rebuilt, and the files
     * must not.
     *
     * @param persons   every person to export
     * @param directory the folder for person files; created if missing
     * @return how many files were written, left as they were and removed
     * @throws UncheckedIOException if a file cannot be read, written or removed
     */
    public Result export(List<Person> persons, Path directory) {
        Map<String, PersonFile> files = filesFor(persons);
        try {
            Files.createDirectories(directory);
            int written = 0;
            int unchanged = 0;
            for (Map.Entry<String, PersonFile> file : files.entrySet()) {
                Path path = directory.resolve(file.getKey());
                String content = file.getValue().content();
                if (Files.exists(path) && Files.readString(path, StandardCharsets.UTF_8).equals(content)) {
                    unchanged++;
                } else {
                    Files.writeString(path, content, StandardCharsets.UTF_8);
                    written++;
                }
            }
            int removed = 0;
            for (Path stale : jsonFilesIn(directory)) {
                if (!files.containsKey(stale.getFileName().toString())) {
                    Files.delete(stale);
                    removed++;
                }
            }
            return new Result(written, unchanged, removed);
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not export persons to " + directory, exception);
        }
    }

    /**
     * Gives every person a unique file name and its file content, ordered by file name. This is
     * what the data folder should hold for these persons, used both to write it and to check it.
     *
     * @param persons every person
     * @return each person with its file content, keyed by file name
     */
    static Map<String, PersonFile> filesFor(List<Person> persons) {
        Map<String, List<PersonFile>> byBaseName = new HashMap<>();
        for (Person person : persons) {
            PersonInputDto input = PersonMapper.toInput(person);
            byBaseName.computeIfAbsent(PersonFileFormat.baseName(input), name -> new ArrayList<>())
                    .add(new PersonFile(person, PersonFileFormat.toJson(input)));
        }
        Map<String, PersonFile> files = new LinkedHashMap<>();
        byBaseName.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(group -> {
                    List<PersonFile> clashing = new ArrayList<>(group.getValue());
                    clashing.sort(Comparator.comparing(PersonFile::content));
                    for (int i = 0; i < clashing.size(); i++) {
                        String suffix = i == 0 ? "" : "-" + (i + 1);
                        files.put(group.getKey() + suffix + ".json", clashing.get(i));
                    }
                });
        return files;
    }

    /** Lists the JSON files directly in the folder; other files are never touched. */
    static Set<Path> jsonFilesIn(Path directory) throws IOException {
        try (Stream<Path> paths = Files.list(directory)) {
            return paths.filter(path -> path.getFileName().toString().endsWith(".json"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toCollection(HashSet::new));
        }
    }

    /**
     * What an export changed.
     *
     * @param written   files created or updated
     * @param unchanged files that already had the right content
     * @param removed   files of persons that no longer exist
     */
    public record Result(int written, int unchanged, int removed) {
    }

    /**
     * A person and the content of its file.
     *
     * @param person  the stored person
     * @param content the file content an export writes for it
     */
    record PersonFile(Person person, String content) {
    }
}
