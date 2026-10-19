package se.pbt.curiositas.data;

import se.pbt.curiositas.person.Person;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Compares the database with the data files and describes every difference precisely enough to
 * act on without searching: which file, which field, and the value on each side.
 */
final class PersonDataChecker {

    /** Not instantiable; all methods are static. */
    private PersonDataChecker() {
    }

    /**
     * Compares the persons with the files an export of them would give.
     *
     * @param persons   every person in the database
     * @param directory the folder with one JSON file per person; a missing folder counts as empty
     * @return every difference, keyed by file; empty when they match
     * @throws UncheckedIOException if a file cannot be read
     */
    static DataDifferences compare(List<Person> persons, Path directory) {
        Map<String, PersonExporter.PersonFile> expected = PersonExporter.filesFor(persons);
        Map<String, String> actual = readFiles(directory);

        Map<String, List<String>> differences = new TreeMap<>();
        TreeSet<String> names = new TreeSet<>(expected.keySet());
        names.addAll(actual.keySet());
        for (String name : names) {
            PersonExporter.PersonFile inDatabase = expected.get(name);
            String inFile = actual.get(name);
            List<String> found = new ArrayList<>();
            if (inFile == null) {
                Person person = inDatabase.person();
                found.add("only in the database: " + person.getName() + " (" + person.getId() + ")");
            } else if (inDatabase == null) {
                found.add("only in the files");
            } else if (!inFile.equals(inDatabase.content())) {
                found.addAll(fieldDifferences(inFile, inDatabase.content()));
            }
            if (!found.isEmpty()) {
                differences.put(directory.resolve(name).toString(), found);
            }
        }
        return new DataDifferences(differences);
    }

    /**
     * Lists the fields whose values differ, aligned so both values are easy to compare. A file
     * that is not valid JSON is reported with line and column instead.
     */
    private static List<String> fieldDifferences(String inFile, String inDatabase) {
        JsonNode file;
        try {
            file = PersonFileFormat.toTree(inFile);
        } catch (StreamReadException malformed) {
            return List.of("cannot be read, line " + malformed.getLocation().getLineNr() + ", column "
                    + malformed.getLocation().getColumnNr() + ": " + malformed.getOriginalMessage());
        }
        List<String[]> rows = new ArrayList<>();
        compareNodes("", file, PersonFileFormat.toTree(inDatabase), rows);
        if (rows.isEmpty()) {
            return List.of("same data, but not formatted the way an export writes it");
        }
        int width = rows.stream().mapToInt(row -> row[0].length()).max().orElse(0);
        return rows.stream()
                .map(row -> String.format("%-" + width + "s   file: %s   database: %s", row[0], row[1], row[2]))
                .toList();
    }

    /** Walks both JSON trees in step and records each path where they hold different values. */
    private static void compareNodes(String path, JsonNode file, JsonNode database, List<String[]> rows) {
        if (file != null && database != null && file.isObject() && database.isObject()) {
            TreeSet<String> fields = new TreeSet<>(file.propertyNames());
            fields.addAll(database.propertyNames());
            for (String field : fields) {
                compareNodes(path.isEmpty() ? field : path + "." + field, file.get(field), database.get(field), rows);
            }
        } else if (file != null && database != null && file.isArray() && database.isArray()
                && file.size() == database.size()) {
            for (int i = 0; i < file.size(); i++) {
                compareNodes(path + "[" + i + "]", file.get(i), database.get(i), rows);
            }
        } else if (file == null || !file.equals(database)) {
            rows.add(new String[]{path, render(file), render(database)});
        }
    }

    /** Shows a value as JSON, or "(none)" when the field is missing on that side. */
    private static String render(JsonNode node) {
        return node == null ? "(none)" : node.toString();
    }

    /** Reads every JSON file in the folder, keyed by file name; a missing folder has no files. */
    private static Map<String, String> readFiles(Path directory) {
        Map<String, String> files = new TreeMap<>();
        if (!Files.isDirectory(directory)) {
            return files;
        }
        try {
            for (Path path : PersonExporter.jsonFilesIn(directory)) {
                files.put(path.getFileName().toString(), Files.readString(path, StandardCharsets.UTF_8));
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not read the data folder " + directory, exception);
        }
        return files;
    }
}
