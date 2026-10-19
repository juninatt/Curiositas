package se.pbt.curiositas.data;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Thrown when the data files cannot be imported, listing every problem in every file at once, so
 * they can all be fixed in one go instead of one import at a time.
 */
public class InvalidDataFilesException extends RuntimeException {

    private final SortedMap<String, List<String>> problemsByFile;

    /**
     * Creates the exception.
     *
     * @param problemsByFile the problems found, keyed by file, at least one
     */
    public InvalidDataFilesException(Map<String, List<String>> problemsByFile) {
        super(describe(new TreeMap<>(problemsByFile)));
        this.problemsByFile = new TreeMap<>(problemsByFile);
    }

    /** Returns the problems found, keyed by file in alphabetical order. */
    public SortedMap<String, List<String>> getProblemsByFile() {
        return problemsByFile;
    }

    /** Lists each file with its problems indented below it. */
    private static String describe(SortedMap<String, List<String>> problemsByFile) {
        return problemsByFile.entrySet().stream()
                .map(file -> "  " + file.getKey() + "\n" + file.getValue().stream()
                        .map(problem -> "      " + problem)
                        .collect(Collectors.joining("\n")))
                .collect(Collectors.joining("\n"));
    }
}
