package se.pbt.curiositas.data;

import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

/**
 * Formats findings about data files the same way everywhere: each file on its own line, with its
 * findings indented below it.
 */
final class FileReport {

    /** Not instantiable; all methods are static. */
    private FileReport() {
    }

    /**
     * Lists each file with its findings indented below it.
     *
     * @param findingsByFile the findings, keyed by file
     * @return the report, one line per file and per finding
     */
    static String describe(SortedMap<String, List<String>> findingsByFile) {
        return findingsByFile.entrySet().stream()
                .map(file -> "  " + file.getKey() + "\n" + file.getValue().stream()
                        .map(finding -> "      " + finding)
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
    }
}
