package se.pbt.curiositas.data;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

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
        super(FileReport.describe(new TreeMap<>(problemsByFile)));
        this.problemsByFile = new TreeMap<>(problemsByFile);
    }

    /** Returns the problems found, keyed by file in alphabetical order. */
    public SortedMap<String, List<String>> getProblemsByFile() {
        return problemsByFile;
    }
}
