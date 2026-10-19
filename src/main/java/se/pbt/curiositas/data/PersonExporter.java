package se.pbt.curiositas.data;

import org.springframework.stereotype.Component;
import se.pbt.curiositas.person.Person;
import se.pbt.curiositas.person.PersonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
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
        Map<String, String> files = filesByName(persons);
        try {
            Files.createDirectories(directory);
            int written = 0;
            int unchanged = 0;
            for (Map.Entry<String, String> file : files.entrySet()) {
                Path path = directory.resolve(file.getKey());
                if (Files.exists(path) && Files.readString(path, StandardCharsets.UTF_8).equals(file.getValue())) {
                    unchanged++;
                } else {
                    Files.writeString(path, file.getValue(), StandardCharsets.UTF_8);
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

    /** Gives every person a unique file name and its file content, ordered by file name. */
    private static Map<String, String> filesByName(List<Person> persons) {
        Map<String, List<String>> contentByBaseName = persons.stream()
                .map(PersonMapper::toInput)
                .collect(Collectors.groupingBy(PersonFileFormat::baseName,
                        Collectors.mapping(PersonFileFormat::toJson, Collectors.toList())));
        Map<String, String> files = new LinkedHashMap<>();
        contentByBaseName.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(group -> {
                    List<String> contents = new ArrayList<>(group.getValue());
                    contents.sort(Comparator.naturalOrder());
                    for (int i = 0; i < contents.size(); i++) {
                        String suffix = i == 0 ? "" : "-" + (i + 1);
                        files.put(group.getKey() + suffix + ".json", contents.get(i));
                    }
                });
        return files;
    }

    /** Lists the JSON files directly in the folder; other files are never touched. */
    private static Set<Path> jsonFilesIn(Path directory) throws IOException {
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
}
